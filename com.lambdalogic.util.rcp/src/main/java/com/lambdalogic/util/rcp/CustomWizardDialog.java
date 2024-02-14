package com.lambdalogic.util.rcp;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

public class CustomWizardDialog extends WizardDialog {
	
	protected IWizard wizard;
	protected String finishButtonText;
	
	
	public CustomWizardDialog(Shell parentShell, IWizard wizard) {
		super(parentShell, wizard);
		this.wizard = wizard;
	}

	
	@Override
	protected Button createButton(Composite parent, int id, String label, boolean defaultButton) {
		if (id == IDialogConstants.FINISH_ID) {
			label = finishButtonText;
		}
		return super.createButton(parent, id, label, defaultButton);
	}

	
	public String getFinishButtonText() {
		return finishButtonText;
	}


	public void setFinishButtonText(String finishButtonText) {
		this.finishButtonText = finishButtonText;
	}
	
}
