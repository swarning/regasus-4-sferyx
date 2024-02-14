package de.regasus.hotel.cancelterm.editor;

import org.eclipse.jface.resource.ImageDescriptor;

import com.lambdalogic.messeinfo.hotel.data.HotelCancelationTermVO;
import com.lambdalogic.util.rcp.tree.ILinkableEditorInput;

import de.regasus.IImageKeys;
import de.regasus.core.ui.Activator;
import de.regasus.core.ui.editor.AbstractEditorInput;


public class HotelCancelationTermEditorInput extends AbstractEditorInput<Long> implements ILinkableEditorInput {

	/**
	 * The PK of the parent.
	 */
	protected  Long offeringPK;


	private HotelCancelationTermEditorInput(Long cancelationTermPK, Long offeringPK) {
		key = cancelationTermPK;
		this.offeringPK = offeringPK;
	}


	public static HotelCancelationTermEditorInput getEditInstance(
		Long hotelCancelationTermPK,
		Long hotelOfferingPK
	) {
		HotelCancelationTermEditorInput pctEditorInput = new HotelCancelationTermEditorInput(
			hotelCancelationTermPK,
			hotelOfferingPK
		);
		return pctEditorInput;
	}


	public static HotelCancelationTermEditorInput getCreateInstance(
		Long hotelOfferingPK
	) {
		HotelCancelationTermEditorInput pctEditorInput = new HotelCancelationTermEditorInput(
			null, // hotelCancelationTermPK
			hotelOfferingPK
		);
		return pctEditorInput;
	}


	@Override
	public ImageDescriptor getImageDescriptor() {
		return Activator.getImageDescriptor(IImageKeys.HOTEL_CANCELATION_TERM);
	}


	@Override
	public Class<?> getEntityType() {
		return HotelCancelationTermVO.class;
	}


	public Long getOfferingPK() {
		return offeringPK;
	}

}
