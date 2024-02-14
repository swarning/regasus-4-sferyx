package de.regasus.participant.dialog;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import de.regasus.I18N;
import com.lambdalogic.util.rcp.UtilI18N;

public class CopyCustomFieldValuesOptionWizardPage extends WizardPage {
	public static String NAME = "CopyCustomFieldValuesOptionWizardPage";

	// Widgets
	private Button yesButton;
	private Button noButton;
	
	
	public CopyCustomFieldValuesOptionWizardPage() {
		super(NAME);

		setTitle(I18N.CopyCustomFieldValuesOptionWizardPage_Title);
		// description is shown in a Label, because it is too long
	}

	
	public void createControl(Composite parent) {
		try {
			Composite controlComposite = new Composite(parent, SWT.NONE);
			controlComposite.setLayout(new GridLayout(2, false));
			
			Label description = new Label(controlComposite, SWT.WRAP);
			description.setText(I18N.CopyCustomFieldValuesOptionWizardPage_Description);
			description.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));

			GridData buttonLayoutData = new GridData(SWT.CENTER, SWT.CENTER, true, false);
			buttonLayoutData.verticalIndent = 50;

			yesButton = new Button(controlComposite, SWT.RADIO);
			yesButton.setText(UtilI18N.Yes);
			yesButton.setLayoutData(buttonLayoutData);
			yesButton.setSelection(true);
			
			noButton = new Button(controlComposite, SWT.RADIO);
			noButton.setText(UtilI18N.No);
			noButton.setLayoutData(buttonLayoutData);
			
			setControl(controlComposite);
			setPageComplete(true);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}


	public Boolean isYes() {
		return yesButton.getSelection();
	}
	
}
