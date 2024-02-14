package de.regasus.common;

import static de.regasus.LookupService.getPhotoMgr;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;

import com.lambdalogic.messeinfo.hotel.Hotel;
import com.lambdalogic.messeinfo.participant.data.EventVO;
import com.lambdalogic.util.exception.MultiException;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.model.CacheModelOperation;
import com.lambdalogic.util.model.CoModelEvent;
import com.lambdalogic.util.rcp.BusyCursorHelper;
import com.lambdalogic.util.rcp.UtilI18N;

import de.regasus.common.photo.PhotoUploadResult;
import de.regasus.common.photo.PhotoUploader;
import de.regasus.core.ServerModel;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.model.MICacheModel;
import de.regasus.event.EventModel;
import de.regasus.model.Activator;
import de.regasus.portal.Portal;


/**
 * This model manages the {@link Photo}s which can referenced by any other entity, e.g. {@link Portal} or {@link Hotel}.
 * Though it offers methods to download and upload the content of these {@link Photo}s, the content itself is not
 * kept in the model's cache!
 */
public abstract class PhotoModel extends MICacheModel<Long, Photo> {

	private static final PhotoSettings SETTINGS_WITHOUT_CONTENT = new PhotoSettings();

	private static final PhotoSettings SETTINGS_WITH_CONTENT = new PhotoSettings();
	static {
		SETTINGS_WITH_CONTENT.withContent = true;
	}

	/**
	 * Size of the cache for entities.
	 */
	private static final int ENTITY_CACHE_SIZE = 10;

	/**
	 * Size of the cache for foreign key data.
	 */
	private static final int FOREIGN_KEY_CACHE_SIZE = 5;


	private EventModel eventModel;

	private String refTable;


	protected PhotoModel(String refTable) {
		super(ENTITY_CACHE_SIZE, FOREIGN_KEY_CACHE_SIZE);

		this.refTable = refTable;
	}


	/**
	 * Initialize references to other Models.
	 * Models are initialized outside the constructor to avoid OutOfMemoryErrors when two Models
	 * reference each other.
	 * This happens because the variable is set after the constructor is finished.
	 * If the constructor calls getInstance() of another Model that calls getInstance() of this Model,
	 * the variable instance is still null. So this Model would be created again and so on.
	 * To avoid this, the constructor has to finish before calling getInstance() of another Model.
	 * The initialization of references to other Models is done in getInstance() right after
	 * the constructor has finished.
	 */
	protected void initModels() {
		eventModel = EventModel.getInstance();

		eventModel.addListener(new CacheModelListener<Long>() {
			@Override
			public void dataChange(CacheModelEvent<Long> event) {
				if (!serverModel.isLoggedIn()) {
					return;
				}

				try {
					if (event.getOperation() == CacheModelOperation.DELETE) {
						Collection<Long> deletedPKs = new ArrayList<>(event.getKeyList().size());

						for (Long eventPK : event.getKeyList()) {
							for (Photo photo : getLoadedAndCachedEntities()) {
								if (photo.getEventId().equals(eventPK)) {
									deletedPKs.add(photo.getId());
								}
							}

							/* Remove the foreign key whose entity has been deleted from the model before firing the
							 * corresponding CacheModelEvent. The entities shall exist in the model when firing the
							 * CacheModelEvent, but not the structural information about the foreign keys. If a listener gets
							 * the CacheModelEvent and consequently requests the list of all entities of the foreign key, it
							 * shall get an empty list.
							 */
							removeForeignKeyData(eventPK);
						}

						if (!deletedPKs.isEmpty()) {
							fireDelete(deletedPKs);
							removeEntities(deletedPKs);
						}
					}
				}
				catch (Exception e) {
					RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
				}
			}
		});


		eventModel.addOpenCloseListener(new CacheModelListener<Long>() {
			@Override
			public void dataChange(CacheModelEvent<Long> event) {
				if ( !serverModel.isLoggedIn() ) {
					return;
				}

				try {
					Long eventPK = event.getFirstKey();
					EventVO eventVO = eventModel.getEventVO(eventPK);

					if (eventVO.isClosed()) {
						// determine affected refIds
						Set<Long> refIds = new HashSet<>();
						for (Photo photo : getLoadedAndCachedEntities()) {
							if (photo.getEventId().equals(eventPK)) {
								refIds.add( photo.getRefId() );
							}
						}

						// refresh Photos of affected Portals
						for (Long refId : refIds) {
							refreshForeignKey(refId);
						}
					}
				}
				catch (Exception e) {
					RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
				}
			}
		});
	}


	public Integer getWidth() {
		return null;
	}


	public Integer getHeight() {
		return null;
	}


	@Override
	protected Long getKey(Photo photo) {
		return photo.getId();
	}


	@Override
	protected boolean isExtended(Photo photo) {
		return photo.getContent() != null;
	}


	@Override
	protected void copyExtendedValues(Photo fromEntity, Photo toEntity) {
		toEntity.setContent( fromEntity.getContent() );
	}


	@Override
	protected Photo getEntityFromServer(Long id) throws Exception {
		return getPhotoMgr().read(id);
	}


	public Photo getPhotoMetadata(Long id) throws Exception {
		return getEntity(id);
	}


	public byte[] getPhotoContent(Long id) throws Exception {
		/* Get content directly from server without caching.
		 * Therefore getExtendedEntityFromServer(Long id) is not used!
		 */
		Photo photo = getPhotoMgr().read(id, SETTINGS_WITH_CONTENT);
		byte[] content = photo.getContent();

		photo.removeContent();
		put(photo);

		return content;
	}


	@Override
	protected boolean isForeignKeySupported() {
		return true;
	}


	@Override
	protected Long getForeignKey(Photo photo) {
		Long fk = null;
		if (photo != null) {
			fk = photo.getRefId();
		}
		return fk;
	}


	@Override
	protected List<Photo> getEntitiesByForeignKeyFromServer(Object foreignKey) throws Exception {
		// cast foreignKey
		Long refId = (Long) foreignKey;

		// load data from server
		List<Photo> photos = getPhotoMgr().readByRefId(refTable, refId, null /*settings*/);
		return photos;
	}


	public List<Photo> getPhotosByRefId(Long refId) throws Exception {
		return getEntityListByForeignKey(refId);
	}


	public static URL buildWebServiceUrl(Photo photo) throws Exception {
		String webServiceUrl = ServerModel.getInstance().getWebServiceUrl();
		return photo.buildContentUrl(webServiceUrl);
	}


	@Override
	protected Photo createEntityOnServer(Photo photo) throws Exception {
		photo.validate();

		// save content
		byte[] content = photo.getContent();

		photo = getPhotoMgr().create(photo, SETTINGS_WITHOUT_CONTENT);

		// restore content
		photo.setContent(content);

		return photo;
	}


	protected void uploadPhotos(PhotoUploader photoUploader, List<File> files, int position)
	throws Exception {
		try {
			BusyCursorHelper.busyCursorWhile(new IRunnableWithProgress() {

				@Override
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					monitor.beginTask(UtilI18N.Working, files.size());

					photoUploader.setWidth( getWidth() );
					photoUploader.setHeight( getHeight() );

					photoUploader.setJobObserver(e -> monitor.worked(1));


					// Thread to observe the monitor if the user cancelled.
					Thread monitorObserverThread = new Thread() {
						@Override
						public void run() {
							while (true) {
								if ( isInterrupted() ) {
									return;
								}

								if ( monitor.isCanceled() ) {
									System.out.println("\n********\nPhoto upload cancelled by user\n********\n");
									photoUploader.interrupt();
									return;
								}

								try {
									Thread.sleep(100);
								}
								catch (InterruptedException e) {
									// ignore
								}
							}
						}
					};
					monitorObserverThread.start();

					try {
    					/* Prepare and upload Photos in multiple threads.
    					 * Use uploadPhotoFilesWithoutThreads(...) to do the same in one thread
    					 */
    					PhotoUploadResult result = photoUploader.uploadPhotoFiles(files, position);

    					monitorObserverThread.interrupt();

    					/* It is not enough to add the new photos, because inserting them in a certain place will
    					 * change the position of existing photos. Therefore we have to refresh all Photos of
    					 * the refId.
    					 */
//    					List<Photo> photoList = result.getPhotoList();
//    					handleNewPhotos(photoList);
    					refreshForeignKey( photoUploader.getRefId() );

    					if ( ! result.getThrowableList().isEmpty() ) {
    						MultiException multiException = new MultiException();
    						multiException.addAll( result.getThrowableList() );
    						RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), multiException);
    					}
					}
					catch (Exception e) {
						RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
					}

					monitor.done();
			}});
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	private void handleNewPhotos(List<Photo> photoList) {
		try {
			List<Long> photoIds = Photo.getPKs(photoList);

			put(photoList);
			fireDataChange(CacheModelOperation.CREATE, photoIds);
			fireDataChange(CoModelEvent.createInstance(this, photoList));
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	public void updateOrder(List<Long> orderedPhotoIds) throws Exception {
		Objects.requireNonNull(orderedPhotoIds);

		if ( ! orderedPhotoIds.isEmpty()) {
			Photo firstPhoto = getPhotoMetadata( orderedPhotoIds.get(0) );
			Long refId = firstPhoto.getRefId();

			getPhotoMgr().updateOrder(orderedPhotoIds);

			List<Photo> freshPhotos = getPhotoMgr().readByRefId(refTable, refId, SETTINGS_WITHOUT_CONTENT);

			// copy existing content from old photos to fresh photos
			for (Photo freshPhoto : freshPhotos) {
				Photo oldPhoto = getEntityIfAvailable( freshPhoto.getId() );
				if ( isExtended(oldPhoto) ) {
					copyExtendedValues(oldPhoto, freshPhoto);
				}
			}

			put(freshPhotos);

			fireRefreshForForeignKey(refId);
		}
	}


	@Override
	protected void deleteEntityOnServer(Photo photo) throws Exception {
		getPhotoMgr().delete( photo.getId() );
	}


	@Override
	protected void deleteEntitiesOnServer(Collection<Photo> photos) throws Exception {
		getPhotoMgr().delete( Photo.getPKs(photos) );
	}


	public void deletePhotos(List<Photo> photos) throws Exception {
		super.delete(photos);
	}

}
