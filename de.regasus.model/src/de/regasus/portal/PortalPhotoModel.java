package de.regasus.portal;

import java.io.File;
import java.util.List;

import de.regasus.common.Photo;
import de.regasus.common.PhotoModel;

public class PortalPhotoModel extends PhotoModel {

	private static PortalPhotoModel singleton;


	private PortalPhotoModel() {
		super(Portal.TABLE_NAME);
	}


	public static PortalPhotoModel getInstance() {
		if (singleton == null) {
			singleton = new PortalPhotoModel();
			singleton.initModels();
		}
		return singleton;
	}


	public List<Photo> getPhotosByPortalId(Long portalId) throws Exception {
		return super.getPhotosByRefId(portalId);
	}


	public void uploadPhotos(Portal portal, List<File> files, int position)
	throws Exception {
		PortalPhotoUploader photoUploader = new PortalPhotoUploader(portal);

		uploadPhotos(photoUploader, files, position);
	}

}
