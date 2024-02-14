package de.regasus.portal.page.editor;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

public class ParticipantFieldComponentComposite extends FieldComponentComposite {

	public ParticipantFieldComponentComposite(Composite parent, int style, Long portalPK) throws Exception {
		super(parent, style, portalPK);
	}


	@Override
	protected FieldCombo buildFieldComboWidget(Composite parent) throws Exception {
		ParticipantFieldCombo fieldCombo = new ParticipantFieldCombo(this, SWT.NONE);
		return fieldCombo;
	}

}
