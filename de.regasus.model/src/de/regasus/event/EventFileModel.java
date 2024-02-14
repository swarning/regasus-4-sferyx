package de.regasus.event;

import static de.regasus.LookupService.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.lambdalogic.util.EqualsHelper;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.model.CacheModelOperation;

import de.regasus.common.File;
import de.regasus.common.FileModel;
import de.regasus.common.FileSummary;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.model.Activator;

/**
 * This model manages the {@link File}s of an Event, which are templates of badges and notes.
 * Though it offers methods to download and upload the content of these {@link File}s, the content itself is not
 * kept in the model's cache!
 */
public class EventFileModel extends FileModel {

	private static EventFileModel singleton;


	private EventModel eventModel;


	private EventFileModel() {
		eventModel = EventModel.getInstance();
		eventModel.addListener(eventModelListener);
	}


	public static EventFileModel getInstance() {
		if (singleton == null) {
			singleton = new EventFileModel();
		}
		return singleton;
	}


	private CacheModelListener<Long> eventModelListener = new CacheModelListener<Long>() {
		@Override
		public void dataChange(CacheModelEvent<Long> event) throws Exception {
			if (!serverModel.isLoggedIn()) {
				return;
			}

			try {
				if (event.getOperation() == CacheModelOperation.DELETE) {
					Collection<Long> deletedPKs = new ArrayList<>( event.getKeyList().size() );

					for (Long eventId : event.getKeyList()) {
						for (File file : getLoadedAndCachedEntities()) {
							if ( file.getRefId().equals(eventId) ) {
								deletedPKs.add( file.getId() );
							}
						}

						/* Remove the foreign key whose entity has been deleted from the model before firing the
						 * corresponding CacheModelEvent. The entities shall exist in the model when firing the
						 * CacheModelEvent, but not the structural information about the foreign keys. If a listener gets
						 * the CacheModelEvent and consequently requests the list of all entities of the foreign key, it
						 * shall get an empty list.
						 */
						removeForeignKeyData(eventId);
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
	};


	private File getFile(Long eventId, String language, EventFile eventFile) throws Exception {
		File file = null;

		List<File> fileList = getEntityListByForeignKey(eventId);

		for (File f : fileList) {
			if (f.getFileType().equals(eventFile.getFileType())
				&&
				EqualsHelper.isEqual(f.getLanguage(), language)
			) {
				file = getExtendedEntity( f.getId() );
				break;
			}
		}

		return file;
	}


	public File getBadgeTemplateFile(Long eventId, String language) throws Exception {
		return getFile(eventId, language, EventFile.BADGE);
	}


	public File getNoteTemplateFile(Long eventId, String language) throws Exception {
		return getFile(eventId, language, EventFile.NOTE);
	}


	private List<File> getFiles(Long eventId, EventFile eventFile) throws Exception {
		String fileType = eventFile.getFileType();

		return getEntityListByForeignKey(eventId)
			.stream()
			.filter(file -> file.getFileType().equals(fileType))
			.collect( Collectors.toList() );
	}


	public List<File> getBadgeTemplateFiles(Long eventId) throws Exception {
		return getFiles(eventId, EventFile.BADGE);
	}


	public List<File> getNoteTemplateFiles(Long eventId) throws Exception {
		return getFiles(eventId, EventFile.NOTE);
	}


	public File uploadBadgeTemplate(Long eventId, byte[] content, String language, String externalPath)
	throws Exception {
		FileSummary fileSummary = getEventMgr().uploadBadgeTemplate(eventId, content, language, externalPath);

		File file = fileSummary.toFile();

		handleUpdate(file);

		return file;
	}


	public File uploadNoteTemplate(Long eventId, byte[] content, String language, String externalPath)
	throws Exception {
		FileSummary fileSummary = getEventMgr().uploadNoteTemplate(eventId, content, language, externalPath);

		File file = fileSummary.toFile();

		handleUpdate(file);

		return file;
	}


//	public void deleteBadgeTemplate(Long eventId, String language)
//	throws Exception {
//		/* Actually it is not efficient to load the File before it gets deleted.
//		 * However, clients can only delete Files that have already been loaded.
//		 */
//		File file = getBadgeTemplateFile(eventId, language);
//		if (file != null) {
//			deleteBadgeTemplate(file);
//    	}
//	}


	public void deleteBadgeTemplate(File file)
	throws Exception {
		Objects.requireNonNull(file);

		getFileMgr().delete( file.getId() );
		handleDelete(file);
	}


//	public void deleteNoteTemplate(Long eventId, String language, boolean force)
//	throws Exception {
//		/* Actually it is not efficient to load the File before it gets deleted.
//		 * However, clients can only delete Files that have already been loaded.
//		 */
//		File file = getBadgeTemplateFile(eventId, language);
//		if (file != null) {
//			deleteNoteTemplate(file, force);
//    	}
//	}


	public void deleteNoteTemplate(File file, boolean force)
	throws Exception {
		Objects.requireNonNull(file);

		getEmailTemplateMgr().deleteNoteTemplate(file.getId(), force);

		getFileMgr().delete( file.getId() );
		handleDelete(file);
	}

}
