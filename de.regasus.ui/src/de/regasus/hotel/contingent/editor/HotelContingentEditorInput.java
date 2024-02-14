package de.regasus.hotel.contingent.editor;

import org.eclipse.jface.resource.ImageDescriptor;

import com.lambdalogic.messeinfo.hotel.data.HotelContingentCVO;
import com.lambdalogic.util.rcp.tree.ILinkableEditorInput;

import de.regasus.IImageKeys;
import de.regasus.IconRegistry;
import de.regasus.core.ui.editor.AbstractEditorInput;


public class HotelContingentEditorInput extends AbstractEditorInput<Long> implements ILinkableEditorInput{
	

	private Long eventPK;
	private Long hotelPK;
	
	
	public HotelContingentEditorInput(Long hotelContingentPK) {
		key = hotelContingentPK;
	}
	
	
	public HotelContingentEditorInput(Long eventPK, Long hotelPK) {
		this.eventPK = eventPK;
		this.hotelPK = hotelPK;
	}

	
	public ImageDescriptor getImageDescriptor() {
		return  IconRegistry.getImageDescriptor(IImageKeys.CONTINGENT);
	}

	
	public Long getEventPK() {
		return eventPK;
	}


	public Long getHotelPK() {
		return hotelPK;
	}

	
	public void setEventPK(Long eventPK) {
		this.eventPK = eventPK;
	}

	
	public void setHotelPK(Long hotelPK) {
		this.hotelPK = hotelPK;
	}

	
	public Class<?> getEntityType() {
		return HotelContingentCVO.class;
	}

}
