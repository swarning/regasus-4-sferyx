package de.regasus.hotel.combo;

import java.util.Collection;
import java.util.Collections;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import com.lambdalogic.messeinfo.hotel.Hotel;
import com.lambdalogic.util.StringHelper;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.model.EntityNotFoundException;
import com.lambdalogic.util.rcp.widget.AbstractComboComposite;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.Activator;
import de.regasus.hotel.HotelContingentModel;
import de.regasus.hotel.HotelModel;


public class HotelCombo
extends AbstractComboComposite<Hotel>
implements CacheModelListener<Hotel> {

	// Model
	private HotelModel model;

	private Long eventPK;


	public HotelCombo(Composite parent, int style) throws Exception {
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

				Hotel hotel = (Hotel) element;
				return StringHelper.avoidNull(hotel.getName1());
			}
		};
	}


	@Override
	protected Collection<Hotel> getModelData() throws Exception {
		Collection<Hotel> modelData = null;

		if (eventPK != null) {
			Collection<Long> hotelPKs = HotelContingentModel.getInstance().getHotelPKsByEventPK(eventPK);
			modelData = HotelModel.getInstance().getHotels(hotelPKs);
		}
		else {
			modelData = Collections.emptyList();
		}

		return modelData;
	}


	@Override
	protected void initModel() {
		// TODO: get modelData from a new HotelModel that supports event as foreign key
		model = HotelModel.getInstance();
		model.addListener(this);
	}


	@Override
	protected void disposeModel() {
		if (model != null) {
			model.removeListener(this);
		}
	}


	@Override
	public void dataChange(CacheModelEvent<Hotel> event) {
		try {
			handleModelChange();
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	public void setEventPK(Long eventPK) {
		try {
			this.eventPK = eventPK;
			handleModelChange();
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	public Long getHotelPK() {
		Long hotelPK = null;
		if (entity != null) {
			hotelPK = entity.getID();
		}
		return hotelPK;
	}


	public void setHotelPK(Long hotelPK) {
		try {
			Hotel hotel = null;
			if (hotelPK != null) {
				hotel = model.getHotel(hotelPK);
				if (hotel == null) {
					throw new EntityNotFoundException("Hotel", hotelPK);
				}
			}
			setEntity(hotel);
		}
		catch (EntityNotFoundException e) {
			RegasusErrorHandler.handleSilentError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
		catch (Throwable t) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t);
		}
	}

}
