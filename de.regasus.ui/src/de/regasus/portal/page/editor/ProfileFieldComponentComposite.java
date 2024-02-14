package de.regasus.portal.page.editor;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

public class ProfileFieldComponentComposite extends FieldComponentComposite {

	public ProfileFieldComponentComposite(Composite parent, int style, Long portalPK) throws Exception {
		super(parent, style, portalPK);
	}


	@Override
	protected FieldCombo buildFieldComboWidget(Composite parent) throws Exception {
		ProfileFieldCombo fieldCombo = new ProfileFieldCombo(this, SWT.NONE);
		return fieldCombo;
	}

}
