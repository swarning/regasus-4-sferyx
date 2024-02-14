package de.regasus.hotel;

import java.io.File;
import java.util.List;

import com.lambdalogic.messeinfo.hotel.Hotel;
import com.lambdalogic.util.TypeHelper;

import de.regasus.common.PhotoModel;
import de.regasus.core.PropertyModel;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.model.Activator;

public class HotelPhotoModel extends PhotoModel {

	private static HotelPhotoModel singleton;

	public static final String WIDTH_PROPERTY_KEY = "hotel.photo.width";
	public static final String HEIGHT_PROPERTY_KEY = "hotel.photo.height";


	private HotelPhotoModel() {
		super(Hotel.TABLE_NAME);
	}


	public static HotelPhotoModel getInstance() {
		if (singleton == null) {
			singleton = new HotelPhotoModel();
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


	public void uploadPhotos(Hotel hotel, List<File> files, int position)
	throws Exception {
		HotelPhotoUploader photoUploader = new HotelPhotoUploader(hotel);

		uploadPhotos(photoUploader, files, position);
	}

}
