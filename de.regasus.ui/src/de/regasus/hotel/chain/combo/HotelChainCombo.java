package de.regasus.hotel.chain.combo;

import java.util.Collection;
import java.util.Collections;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import com.lambdalogic.util.StringHelper;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.model.EntityNotFoundException;
import com.lambdalogic.util.rcp.widget.AbstractComboComposite;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.hotel.HotelChain;
import de.regasus.hotel.HotelChainModel;
import de.regasus.ui.Activator;


@SuppressWarnings("rawtypes")
public class HotelChainCombo
extends AbstractComboComposite<HotelChain>
implements CacheModelListener {

	// Model
	private HotelChainModel model;


	public HotelChainCombo(Composite parent, int style) throws Exception {
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

				HotelChain hotelChain = (HotelChain) element;
				return StringHelper.avoidNull(hotelChain.getName());
			}
		};
	}


	@Override
	protected Collection<HotelChain> getModelData() throws Exception {
		Collection<HotelChain> modelData = model.getAllHotelChains();
		if (modelData == null) {
			modelData = Collections.emptyList();
		}
		return modelData;
	}


	@Override
	protected void initModel() {
		model = HotelChainModel.getInstance();
		model.addListener(this);
	}


	@Override
	protected void disposeModel() {
		if (model != null) {
			model.removeListener(this);
		}
	}


	@Override
	public void dataChange(CacheModelEvent event) {
		try {
			handleModelChange();
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	public Long getHotelChainPK() {
		Long hotelChainPK = null;
		if (entity != null) {
			hotelChainPK = entity.getId();
		}
		return hotelChainPK;
	}


	public void setHotelChainPK(Long hotelChainPK) {
		try {
			HotelChain hotelChain = null;
			if (model != null && hotelChainPK != null) {
				hotelChain = model.getHotelChain(hotelChainPK);
			}
			setEntity(hotelChain);
		}
		catch (EntityNotFoundException e) {
			RegasusErrorHandler.handleSilentError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
		catch (Throwable t) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t);
		}
	}

}
