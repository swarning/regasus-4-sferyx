package de.regasus.participant.dialog;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import de.regasus.I18N;

public class ForceWizardPage extends WizardPage {

	
	private Button button;

	public boolean isForce() {
		return button.getSelection();
	}

	protected ForceWizardPage() {
		super("ForcePage");

		setTitle(I18N.ForceWizardPage_Title);
		setDescription(I18N.ForceWizardPage_Description);
	}

	public void createControl(Composite parent) {

		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new RowLayout());
		button = new Button(composite, SWT.CHECK);
		button.setText(I18N.ForceWizardPage_Description);
		setControl(composite);
	}
}
