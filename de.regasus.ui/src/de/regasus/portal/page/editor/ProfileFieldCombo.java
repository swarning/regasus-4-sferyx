package de.regasus.portal.page.editor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import de.regasus.portal.PortalProfileFieldsModel;
import de.regasus.portal.component.Field;
import de.regasus.portal.profile.PortalProfileFields;

public class ProfileFieldCombo extends FieldCombo {

	// Model
	private PortalProfileFieldsModel model;


	public ProfileFieldCombo(Composite parent, int style) throws Exception {
		super(parent, SWT.NONE);
	}


	@Override
	protected Collection<Field> getModelData() throws Exception {
		List<Field> modelData = Collections.emptyList();

		PortalProfileFields portalProfileFields = model.getPortalProfileFields();
		modelData = new ArrayList<>( portalProfileFields.getFields() );

		return modelData;
	}


	@Override
	protected void initModel() {
		model = PortalProfileFieldsModel.getInstance();
	}


	@Override
	protected void disposeModel() {
		if (model != null) {
			model.removeListener(modelListener);
		}
	}


	@Override
	public Long getEventID() {
		return null;
	}


	@Override
	public void setEventID(Long eventID) throws Exception {
		model.addListener(modelListener);
	}

}
