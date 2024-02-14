package de.regasus.hotel.roomdefinition.editor;

import java.io.File;
import java.util.List;

import org.eclipse.swt.widgets.Composite;

import com.lambdalogic.messeinfo.hotel.data.RoomDefinitionVO;

import de.regasus.common.Photo;
import de.regasus.common.photo.PhotoComposite;
import de.regasus.hotel.RoomDefinitionPhotoModel;

/**
 * {@link PhotoComposite} for {@link Photo}s that belong a {@link RoomDefinitionVO}.
 */
public class RoomDefinitionPhotoComposite extends PhotoComposite {

	private final RoomDefinitionVO roomDefinition;


	public RoomDefinitionPhotoComposite(Composite parent, RoomDefinitionVO roomDefinition) {
		super(parent, RoomDefinitionPhotoModel.getInstance(), roomDefinition.getId());

		this.roomDefinition = roomDefinition;
	}


	@Override
	protected void uploadPhotos(List<File> selectedFiles, int targetPosition) throws Exception {
		RoomDefinitionPhotoModel.getInstance().uploadPhotos(roomDefinition, selectedFiles, targetPosition);
	}

}
