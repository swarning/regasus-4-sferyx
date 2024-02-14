package de.regasus.hotel.offering.editor;

import org.eclipse.jface.resource.ImageDescriptor;

import com.lambdalogic.messeinfo.hotel.data.HotelOfferingVO;
import com.lambdalogic.util.rcp.tree.ILinkableEditorInput;

import de.regasus.IImageKeys;
import de.regasus.IconRegistry;
import de.regasus.core.ui.editor.AbstractEditorInput;

public class HotelOfferingEditorInput extends AbstractEditorInput<Long> implements ILinkableEditorInput{

	private Long hotelContingentPK;

	
	private HotelOfferingEditorInput() {
	}


	public static HotelOfferingEditorInput getCreateInstance(Long hotelContingentPK) {
		HotelOfferingEditorInput editorInput = new HotelOfferingEditorInput();
		editorInput.hotelContingentPK = hotelContingentPK;
		return editorInput;
	}


	public static HotelOfferingEditorInput getEditInstance(Long hotelOfferingPK) {
		HotelOfferingEditorInput editorInput = new HotelOfferingEditorInput();
		editorInput.key = hotelOfferingPK;
		return editorInput;
	}

	
	public void setHotelContingentPK(Long hotelContingentPK) {
		this.hotelContingentPK = hotelContingentPK;
	}
	
	
	public Long getHotelContingentPK() {
		return hotelContingentPK;
	}
	
	
	public ImageDescriptor getImageDescriptor() {
		return IconRegistry.getImageDescriptor(IImageKeys.EURO);
	}


	public Class<?> getEntityType() {
		return HotelOfferingVO.class;
	}

}
