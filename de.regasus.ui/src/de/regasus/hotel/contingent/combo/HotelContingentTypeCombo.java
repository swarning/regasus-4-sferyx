package de.regasus.hotel.contingent.combo;

import java.util.Arrays;
import java.util.Collection;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import com.lambdalogic.messeinfo.hotel.data.HotelContingentType;
import com.lambdalogic.util.rcp.widget.AbstractComboComposite;


public class HotelContingentTypeCombo 
extends AbstractComboComposite<HotelContingentType>{

	public HotelContingentTypeCombo(Composite parent, int style) throws Exception {
		super(parent, SWT.NONE);
	}
	

	@Override
	protected Object getEmptyEntity() {
		return EMPTY_ELEMENT;
	}

	@Override
	protected LabelProvider getLabelProvider() {
		return new LabelProvider() {
			
			@Override
			public String getText(Object element) {
				if (element == EMPTY_ELEMENT) {
					return EMPTY_ELEMENT;
				}

				HotelContingentType hotelContingentType = (HotelContingentType) element;
				return hotelContingentType.getString();
			}
		};
	}

	@Override
	protected Collection<HotelContingentType> getModelData() throws Exception {
		return Arrays.asList(HotelContingentType.values());
	}

	@Override
	protected void initModel() {
		// do nothing		
	}

	@Override
	protected void disposeModel() {
		// do nothing
	}
	
	public HotelContingentType getHotelContingentType() {
		return entity;
	}

	public void setHotelContingentType(HotelContingentType hotelContingentType) {
		setEntity(hotelContingentType);
	}
	
	@Override
	protected ViewerSorter getViewerSorter() {
		/* Returning null because we don't want the
		 * HotelContingentType to be sorted.
		 */
		return null;
	}
}

