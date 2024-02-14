package de.regasus.hotel.combo;

import java.util.Arrays;
import java.util.Collection;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import com.lambdalogic.messeinfo.hotel.data.HotelStars;
import com.lambdalogic.util.rcp.widget.AbstractComboComposite;

public class HotelStarsCombo
extends AbstractComboComposite<HotelStars> {
	
	public HotelStarsCombo(Composite parent, int style) throws Exception {
		super(parent, SWT.NONE);
	}

	
	@Override
	protected Object getEmptyEntity() {
		return EMPTY_ELEMENT;
	}

	
	protected LabelProvider getLabelProvider() {
		return new LabelProvider() {
			
			@Override
			public String getText(Object element) {
				if (element == EMPTY_ELEMENT) {
					return EMPTY_ELEMENT;
				}
				
				HotelStars hotelStars = (HotelStars) element;
				return hotelStars.getString();
			}
		};
	}
	
	
	protected Collection<HotelStars> getModelData() {
		return Arrays.asList(HotelStars.values());
	}
	
	
	protected void initModel() {
		// do nothing because we don't know the event yet
	}
	
	
	protected void disposeModel() {
		// do nothing
	}


	@Override
	protected ViewerSorter getViewerSorter() {
		/* Returning null because we don't want the
		 * ParticipantStazes to be sorted.
		 */
		return null;
	}
	
	
	public HotelStars getHotelStars() {
		return entity;
	}

	
	public void setHotelStars(HotelStars hotelStars) {
		setEntity(hotelStars);
	}
	
}
