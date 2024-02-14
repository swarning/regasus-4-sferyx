package de.regasus.hotel.offering.combo;

import java.util.Collection;
import java.util.Collections;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import com.lambdalogic.util.rcp.widget.AbstractComboComposite;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.Activator;
import de.regasus.hotel.HotelOfferingModel;

public class HotelOfferingCategoryCombo extends AbstractComboComposite<String> {

	private Long eventPK;


	public HotelOfferingCategoryCombo(Composite parent, int style) throws Exception {
		super(parent, SWT.NONE);
	}


	@Override
	protected String getEmptyEntity() {
		return EMPTY_ELEMENT;
	}


	@Override
	protected LabelProvider getLabelProvider() {
		return new LabelProvider();
	}


	@Override
	protected Collection<String> getModelData() throws Exception {
		Collection<String> modelData = null;

		if (eventPK != null) {
			modelData = HotelOfferingModel.getInstance().getHotelOfferingCategoriesByEvent(eventPK);
		}
		else {
			modelData = Collections.emptyList();
		}

		return modelData;
	}


	@Override
	protected void initModel() {
	}


	@Override
	protected void disposeModel() {
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


	public String getCategory() {
		return entity;
	}


	public void setCategory(String category) {
		try {
			setEntity(category);
		}
		catch (Throwable t) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t);
		}
	}

}
