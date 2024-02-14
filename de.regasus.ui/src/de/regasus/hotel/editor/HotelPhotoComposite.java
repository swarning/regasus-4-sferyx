package de.regasus.hotel.editor;

import java.io.File;
import java.util.List;

import org.eclipse.swt.widgets.Composite;

import com.lambdalogic.messeinfo.hotel.Hotel;

import de.regasus.common.Photo;
import de.regasus.common.photo.PhotoComposite;
import de.regasus.hotel.HotelPhotoModel;

/**
 * {@link PhotoComposite} for {@link Photo}s that belong a {@link Hotel}.
 */
public class HotelPhotoComposite extends PhotoComposite {

	private final Hotel hotel;


	public HotelPhotoComposite(Composite parent, Hotel hotel) {
		super(parent, HotelPhotoModel.getInstance(), hotel.getID());

		this.hotel = hotel;
	}


	@Override
	protected void uploadPhotos(List<File> selectedFiles, int targetPosition) throws Exception {
		HotelPhotoModel.getInstance().uploadPhotos(hotel, selectedFiles, targetPosition);
	}

}
