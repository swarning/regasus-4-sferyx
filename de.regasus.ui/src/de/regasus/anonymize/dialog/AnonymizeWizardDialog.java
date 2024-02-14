package de.regasus.anonymize.dialog;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

import de.regasus.I18N;


public class AnonymizeWizardDialog extends WizardDialog {

	private Button printButton;


	public AnonymizeWizardDialog(Shell parentShell) {
		super(parentShell, new AnonymizeWizard());
	}


	@Override
	protected Point getInitialSize() {
		return ((AnonymizeWizard) getWizard()).getPreferredSize();
	}


	@Override
	protected Button createButton(Composite parent, int id, String label, boolean defaultButton) {
		// set different label to finish-button
		if (id == IDialogConstants.FINISH_ID) {
			label = I18N.AnonymizeWizardDialog_FinishButtonText;
		}
		return super.createButton(parent, id, label, defaultButton);
	}


	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		printButton = createPrintButton(parent);
		super.createButtonsForButtonBar(parent);
	}


	@Override
	public void updateButtons() {
		super.updateButtons();

		boolean canFinish = getWizard().canFinish();
		printButton.setEnabled(canFinish);
	}


	private Button createPrintButton(Composite parent) {
		// increment the number of columns in the button bar
		((GridLayout) parent.getLayout()).numColumns++;
		Button button = new Button(parent, SWT.PUSH);
		button.setText(I18N.AnonymizeWizardDialog_PrintButtonText);
//		button.setImage(IconRegistry.getImage(IImageKeys.GENERATE_REPORT));
		setButtonLayoutData(button);
		button.setFont(parent.getFont());

		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				((AnonymizeWizard) getWizard()).printAnonymizeDocument();
			}
		});

		return button;
	}

}
