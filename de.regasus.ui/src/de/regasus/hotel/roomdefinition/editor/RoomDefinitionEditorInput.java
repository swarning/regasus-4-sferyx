package de.regasus.hotel.roomdefinition.editor;

import org.eclipse.jface.resource.ImageDescriptor;

import com.lambdalogic.messeinfo.hotel.data.RoomDefinitionVO;
import com.lambdalogic.util.rcp.tree.ILinkableEditorInput;

import de.regasus.core.ui.IconRegistry;
import de.regasus.core.ui.editor.AbstractEditorInput;

public class RoomDefinitionEditorInput extends AbstractEditorInput<Long> implements ILinkableEditorInput{

	private Long hotelPK;
	
	// For creating a new room definition for a selected hotel or for the same hotel as a selected room definition
	public RoomDefinitionEditorInput(Long hotelPK) {
		this.hotelPK = hotelPK;
	}

	
	public RoomDefinitionEditorInput(RoomDefinitionVO roomDefinitionVO) {
		this.key = roomDefinitionVO.getPK();
		this.hotelPK = roomDefinitionVO.getHotelPK();
	}

	public ImageDescriptor getImageDescriptor() {
		ImageDescriptor imageDescriptor = IconRegistry.getImageDescriptor("/icons/room.png");
		return imageDescriptor;
	}


	public Long getHotelPK() {
		return hotelPK;
	}


	public Class<?> getEntityType() {
		return RoomDefinitionVO.class;
	}

}
