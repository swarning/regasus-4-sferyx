package de.regasus.portal.portal.editor;

import java.io.File;
import java.util.List;

import org.eclipse.swt.widgets.Composite;

import de.regasus.common.Photo;
import de.regasus.common.photo.PhotoComposite;
import de.regasus.portal.Portal;
import de.regasus.portal.PortalPhotoModel;

/**
 * {@link PhotoComposite} for {@link Photo}s that belong a {@link Portal}.
 */
public class PortalPhotoComposite extends PhotoComposite {

	private final Portal portal;


	public PortalPhotoComposite(Composite parent, Portal portal) {
		super(parent, PortalPhotoModel.getInstance(), portal.getId());

		this.portal = portal;
	}


	@Override
	protected void uploadPhotos(List<File> selectedFiles, int targetPosition) throws Exception {
		PortalPhotoModel.getInstance().uploadPhotos(portal, selectedFiles, targetPosition);
	}

}
