package de.regasus.hotel.view.tree;

import static com.lambdalogic.util.StringHelper.isNotEmpty;
import static de.regasus.LookupService.*;

import java.lang.invoke.MethodHandles;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lambdalogic.messeinfo.hotel.data.HotelVO;
import com.lambdalogic.messeinfo.hotel.data.RoomDefinitionVO;
import com.lambdalogic.util.FileHelper;
import com.lambdalogic.util.exception.ErrorMessageException;
import com.lambdalogic.util.image.ImageUtil;

import de.regasus.I18N;
import de.regasus.common.Photo;
import de.regasus.common.PhotoSettings;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.model.Activator;
import de.regasus.hotel.HotelPhotoModel;
import de.regasus.hotel.RoomDefinitionPhotoModel;

public class ResizePhotosJob extends Job {

	private static final Logger log = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );

	private static final PhotoSettings PHOTO_SETTINGS = new PhotoSettings();


	private Integer hotelPhotoWidth;
	private Integer hotelPhotoHeight;
	private Integer roomDefinitionPhotoWidth;
	private Integer roomDefinitionPhotoHeight;

	private int checkCount = 0;
	private int changeCount = 0;


	public ResizePhotosJob() {
		super(I18N.ResizePhotos);
		initPropertyValues();
	}


	private void initPropertyValues() {
		hotelPhotoWidth = HotelPhotoModel.getInstance().getWidth();
		hotelPhotoHeight = HotelPhotoModel.getInstance().getHeight();
		roomDefinitionPhotoWidth = RoomDefinitionPhotoModel.getInstance().getWidth();
		roomDefinitionPhotoHeight = RoomDefinitionPhotoModel.getInstance().getHeight();
	}


	@Override
	protected IStatus run(IProgressMonitor monitor) {
		try {
			log.info("Load Hotel Countries");
			List<String> countryPKs = getHotelMgr().getHotelCountryPKs();
			log.info( String.valueOf(countryPKs) );

			String city = null;
			int countryIdx = 0;
			for (String countryPK : countryPKs) {
				log.info("\n\nProcessing country " + countryPK + " (" + (++countryIdx) + " of " + countryPKs.size() + ")");

				log.info("Load hotels for country " + countryPK);
				List<HotelVO> hotelVOs = getHotelMgr().getHotelVOs(countryPK, city);
				log.info("Found " + hotelVOs.size() + " hotels of country " + countryPK);

				int hotelIdx = 0;
				for (HotelVO hotelVO : hotelVOs) {
					log.info("\nProcessing " + hotelVO + " (" + (++hotelIdx) + " of " + hotelVOs.size() + ")");
					Long hotelId = hotelVO.getId();

					log.info("Load Photos of " + hotelVO);
					List<Photo> hotelPhotoList = readHotelPhotos(hotelId);
					log.info("Found " + hotelPhotoList.size() + " photos for " + hotelVO);

					int photoIdx = 0;
					for (Photo photo : hotelPhotoList) {
						log.info("Processing " + photo + " (" + (++photoIdx) + " of " + hotelPhotoList.size() + ")");

						String internalPath = "/hotel/" + hotelVO.getId() + "/photo/";
						processPhoto(photo, internalPath, hotelPhotoWidth, hotelPhotoHeight);
					}

					List<RoomDefinitionVO> roomDefinitionVOs = getRoomDefinitionMgr().getRoomDefinitionVOsByHotelPK(hotelId);
					for (RoomDefinitionVO roomDefinitionVO : roomDefinitionVOs) {
						Long roomDefinitionId = roomDefinitionVO.getId();

						log.info("Load Photos of " + roomDefinitionVO);
						List<Photo> roomDefPhotoList = readRoomDefPhotos(roomDefinitionId);
						log.info("Found " + roomDefPhotoList.size() + " photos for " + roomDefinitionVO);

						for (Photo photo : roomDefPhotoList) {
							log.info("Processing " + photo + " (" + (++photoIdx) + " of " + roomDefPhotoList.size() + ")");

							String internalPath = "/roomDefinition/" + roomDefinitionVO.getId() + "/photo/";
							processPhoto(photo, internalPath, roomDefinitionPhotoWidth, roomDefinitionPhotoHeight);
						}
					}
				}
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			return Status.CANCEL_STATUS;
		}

		log.info("Finished resizing hotel photo job.");
		log.info("Checked photos: " + checkCount);
		log.info("Changed photos: " + changeCount);

		return Status.OK_STATUS;
	}


	private void processPhoto(Photo photo, String internalPath, int width, int height) throws Exception {
		checkCount++;

		byte[] content = null;

		// try to load original photo
		String externalPath = photo.getExternalPath();
		if ( isNotEmpty(externalPath) ) {
			java.io.File file = new java.io.File(externalPath);
			if ( file.exists() ) {
				// read file content
				log.info("Read content from " + externalPath);

				content = FileHelper.readFile(file);
			}
			else if (photo.getWidth().intValue() != width || photo.getHeight().intValue() != height) {
				// read content from DB
				log.info("Read content from DB, because its dimensions are wrong");

				Long fileId = photo.getFileId();
				content = getFileMgr().read(fileId).getContent();
			}
			else {
				log.info("Do not read content from DB, because its dimensions are correct");
			}
		}


		if (content != null) {
			changeCount++;

    		// resize content
    		ImageUtil imageUtil = new ImageUtil(content);

			int sourceWidth = imageUtil.getWidth();
			int sourceHeight = imageUtil.getHeight();
			if (sourceWidth != width || sourceHeight != height) {
				log.info("Change image from " + sourceWidth + "x" + sourceHeight + " to " + width + "x" + height);

				imageUtil.scaleTo(width, height);
				log.info("Image dimensions after scaleTo: " + imageUtil.getWidth() + "x" + imageUtil.getHeight());

        		content = imageUtil.toImageBytes( photo.getFormat().getImageFormat() );
			}
			else {
				log.info("Dimension of image are correct");
			}

    		// build new Photo
    		Photo newPhoto = new Photo();
    		newPhoto.setEventId( photo.getEventId() );
    		newPhoto.setRefId( photo.getRefId() );
    		newPhoto.setRefTable( photo.getRefTable() );
    		newPhoto.setFileId(Long.MAX_VALUE); // dummy value to pass validation
    		newPhoto.setPosition( photo.getPosition() );
    		newPhoto.setFormat( photo.getFormat() );
    		newPhoto.setExternalPath( photo.getExternalPath() );
    		newPhoto.setWidth( imageUtil.getWidth() );
    		newPhoto.setHeight( imageUtil.getHeight() );
    		newPhoto.setContent(content);
    		newPhoto.setSize(content.length);

    		newPhoto.calculateThumbnail();

    		newPhoto.setInternalPath(internalPath);


    		// delete old Photo
    		log.info("Delete old Photo");
    		HotelPhotoModel.getInstance().deletePhotos( Collections.singletonList(photo) );

    		// upload new Photo
    		log.info("Upload new Photo");
    		getPhotoMgr().create(newPhoto, PHOTO_SETTINGS);
		}
	}


	private List<Photo> readHotelPhotos(Long hotelId) throws ErrorMessageException {
		List<Photo> photoList = getHotelMgr().readPhotos(hotelId, PHOTO_SETTINGS);
		boolean reorderd = verifyOrder(photoList);
		if (reorderd) {
			photoList = getHotelMgr().readPhotos(hotelId, PHOTO_SETTINGS);
		}
		return photoList;
	}


	private List<Photo> readRoomDefPhotos(Long roomDefinitionId) throws ErrorMessageException {
		List<Photo> photoList = getRoomDefinitionMgr().readPhotos(roomDefinitionId, PHOTO_SETTINGS);
		boolean reorderd = verifyOrder(photoList);
		if (reorderd) {
			photoList = getRoomDefinitionMgr().readPhotos(roomDefinitionId, PHOTO_SETTINGS);
		}
		return photoList;
	}


	private boolean verifyOrder(List<Photo> photoList) throws ErrorMessageException {
		int pos = -1;
		for (Photo photo : photoList) {
			if (photo.getPosition() > pos) {
				pos = photo.getPosition();
			}
			else {
				// reorder
				log.info("Wrong order: Update order of " + photoList.size() + " photos");
				List<Long> idList = Photo.getPKs(photoList);
				getPhotoMgr().updateOrder(idList);
				return true;
			}
		}

		return false;
	}

}
