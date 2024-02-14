package de.regasus.core.ui.dialog;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import de.regasus.core.ui.CoreI18N;

public class ConfigUrlDialog extends TitleAreaDialog {

	
	// Fields
	private String configurationUrl;
	private boolean dontAskMe;
	
	// Widgets
	private Text configurationUrlText;
	private Button dontAskMeButton;
	
	/**
	 * Create the dialog
	 * @param parentShell
	 */
	public ConfigUrlDialog(Shell parentShell) {
		super(parentShell);
	}


	/**
	 * Create contents of the dialog
	 * @param parent
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		Composite container = new Composite(area, SWT.NONE);
		final GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		container.setLayout(gridLayout);
		container.setLayoutData(new GridData(GridData.FILL_BOTH));

		final Label configurationUrlLabel = new Label(container, SWT.NONE);
		final GridData gd_configurationUrlLabel = new GridData(SWT.FILL, SWT.CENTER, false, false);
		configurationUrlLabel.setLayoutData(gd_configurationUrlLabel);
		configurationUrlLabel.setText(CoreI18N.ConfigUrlDialog_ConfigurationURLLabel);

		configurationUrlText = new Text(container, SWT.BORDER);
		final GridData gd_configurationUrlText = new GridData(SWT.FILL, SWT.CENTER, true, false);
		configurationUrlText.setLayoutData(gd_configurationUrlText);

		dontAskMeButton = new Button(container, SWT.CHECK);
		final GridData gd_dontAskMeButton = new GridData(SWT.LEFT, SWT.TOP, false, false, 2, 1);
		gd_dontAskMeButton.verticalIndent = 10;
		dontAskMeButton.setLayoutData(gd_dontAskMeButton);
		dontAskMeButton.setText(CoreI18N.ConfigUrlDialog_DontAskMeButton);
		setTitle(CoreI18N.ConfigUrlDialog_Title);
		setMessage(CoreI18N.ConfigUrlDialog_Message);
		//
		return area;
	}


	/**
	 * Create contents of the button bar
	 * @param parent
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}


	/**
	 * Return the initial size of the dialog
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(500, 375);
	}

	
	protected void buttonPressed(int buttonId) {
		configurationUrl = configurationUrlText.getText();
		dontAskMe = dontAskMeButton.getSelection();

		super.buttonPressed(buttonId);
	}


	public String getConfigurationUrl() {
		return configurationUrl;
	}


	public boolean isDontAskMe() {
		return dontAskMe;
	}
	
}
