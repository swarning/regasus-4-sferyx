package de.regasus.hotel;

import java.io.File;
import java.util.List;

import com.lambdalogic.messeinfo.hotel.data.RoomDefinitionVO;
import com.lambdalogic.util.TypeHelper;

import de.regasus.common.PhotoModel;
import de.regasus.core.PropertyModel;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.model.Activator;

public class RoomDefinitionPhotoModel extends PhotoModel {

	private static RoomDefinitionPhotoModel singleton;

	public static final String WIDTH_PROPERTY_KEY = "roomDefinition.photo.width";
	public static final String HEIGHT_PROPERTY_KEY = "roomDefinition.photo.height";


	private RoomDefinitionPhotoModel() {
		super(RoomDefinitionVO.TABLE_NAME);
	}


	public static RoomDefinitionPhotoModel getInstance() {
		if (singleton == null) {
			singleton = new RoomDefinitionPhotoModel();
			singleton.initModels();
		}
		return singleton;
	}


	@Override
	public Integer getWidth() {
		Integer width = null;

		try {
			String value = PropertyModel.getInstance().getPropertyValue(WIDTH_PROPERTY_KEY);
			width = TypeHelper.toInteger(value);
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}

		return width;
	}


	@Override
	public Integer getHeight() {
		Integer height = null;

		try {
			String value = PropertyModel.getInstance().getPropertyValue(HEIGHT_PROPERTY_KEY);
			height = TypeHelper.toInteger(value);
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}

		return height;
	}


	public void uploadPhotos(RoomDefinitionVO roomDefinition, List<File> files, int position)
	throws Exception {
		RoomDefinitionPhotoUploader photoUploader = new RoomDefinitionPhotoUploader(roomDefinition);

		uploadPhotos(photoUploader, files, position);
	}

}
