package de.regasus.participant.dialog;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.lambdalogic.util.rcp.ModifySupport;

import de.regasus.I18N;

public class LinkParticipantsWizardPage extends WizardPage {
	public static String NAME = "LinkParticipantsWizardPage";

	// Widgets
	private Button linkButton;
	private Button dontLinkButton;


	public LinkParticipantsWizardPage() {
		super(NAME);

		setTitle(I18N.LinkParticipantsWizardPage_Title);
		// description is shown in a Label, because it is too long
	}


	@Override
	public void createControl(Composite parent) {
		try {
			Composite controlComposite = new Composite(parent, SWT.NONE);
			controlComposite.setLayout(new GridLayout(2, false));

			Label description = new Label(controlComposite, SWT.WRAP);
			description.setText(I18N.LinkParticipantsWizardPage_Description);
			description.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));

			GridData buttonLayoutData = new GridData(SWT.CENTER, SWT.CENTER, true, false);
			buttonLayoutData.verticalIndent = 50;

			linkButton = new Button(controlComposite, SWT.RADIO);
			linkButton.setText(I18N.LinkParticipantsWizardPage_LinkButton);


			dontLinkButton = new Button(controlComposite, SWT.RADIO);
			dontLinkButton.setText(I18N.LinkParticipantsWizardPage_DontLinkButton);

			// observe radio buttons
			SelectionListener selectionListener = new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent event) {
					if (!ModifySupport.isDeselectedRadioButton(event)) {
						setPageComplete(isPageComplete());
					}
				}
			};
			linkButton.addSelectionListener(selectionListener);
			dontLinkButton.addSelectionListener(selectionListener);
			
			
			setControl(controlComposite);
			setPageComplete(isPageComplete());
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}


	public Boolean isLink() {
		Boolean link = null;
		if (linkButton.getSelection() || dontLinkButton.getSelection()) {
			link = linkButton.getSelection();
		}
		return link;
	}


	@Override
	public boolean isPageComplete() {
		return isLink() != null;
	}

}
