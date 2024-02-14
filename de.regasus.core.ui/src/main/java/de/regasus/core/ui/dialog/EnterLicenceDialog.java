package de.regasus.core.ui.dialog;

import static de.regasus.LookupService.*;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.lambdalogic.util.CryptographyHelper;
import com.lambdalogic.util.Licence;
import com.lambdalogic.util.rcp.widget.LicenceGroup;
import com.lambdalogic.util.rcp.widget.MultiLineText;

import de.regasus.core.ServerModel;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.Activator;
import com.lambdalogic.util.rcp.UtilI18N;
import de.regasus.core.ui.CoreI18N;
import de.regasus.core.ui.action.LoginAction;

public class EnterLicenceDialog extends TitleAreaDialog {

	private LicenceGroup licenceGroup;

	private Text licenceKeyText;

	private Button verifyButton;

	private int trialCount;

	/**
	 * Create the dialog
	 * @param parentShell
	 */
	public EnterLicenceDialog(Shell parentShell) {
		super(parentShell);

		setShellStyle(getShellStyle()  | SWT.RESIZE );
	}



	/**
	 * Create contents of the dialog
	 * @param parent
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		setTitle(CoreI18N.EnterLicenceKey);
		setMessage(CoreI18N.PressingOKOverridesTheCurrentLicenceOnTheServer);

		Composite area = (Composite) super.createDialogArea(parent);
		Composite container = new Composite(area, SWT.NONE);

		final GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		container.setLayout(gridLayout);
		container.setLayoutData(new GridData(GridData.FILL_BOTH));


		Label label = new Label(container, SWT.NONE);
		label.setText(CoreI18N.LicenceKey);

		licenceKeyText = new MultiLineText(container, SWT.BORDER | SWT.V_SCROLL, false);
		GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
		layoutData.heightHint = 50;
		licenceKeyText.setLayoutData(layoutData);
		licenceKeyText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				updateButtonStates();
			}
		});


		verifyButton = new Button(container, SWT.PUSH);
		verifyButton.setText(UtilI18N.Details);
		verifyButton.setEnabled(false);

		verifyButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				verify();
			}
		});

		new Label(container, SWT.NONE); // dummy

		// The group to show the details of the licence
		licenceGroup = new LicenceGroup(container, SWT.READ_ONLY);
		licenceGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));

		return area;
	}


	protected String verify() {
		try {
			// The more often one tries, the longer is it to take, to hinder brute-force trial and error
			Thread.sleep(1000 * trialCount);
			trialCount++;

			// Reconstruct licence from entered key
			String encodedLicence = licenceKeyText.getText();

			String flatEncodedLicence = CryptographyHelper.removeLineBreaks(encodedLicence);

			String xmlLicence = CryptographyHelper.decrypt(encodedLicence);
			Licence licence = Licence.createFromXml(xmlLicence);
			licenceGroup.setLicence(licence);

			getButton(IDialogConstants.OK_ID).setEnabled(true);
			return flatEncodedLicence;
		}
		catch (Exception e) {
			com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
			MessageDialog.openError(getShell(), UtilI18N.Error, CoreI18N.LicenceCouldNotBeDecoded);
		}
		return null;

	}

	/**
	 * When OK was pressed, the key is verified again (precautionary measure) and sent to the server
	 */
	@Override
	protected void okPressed() {
		String licenceKey = verify();

		if (licenceKey != null) {
			try {
				if (ServerModel.getInstance().isLoggedIn()) {
					ServerModel.getInstance().updateLicenceKey(licenceKey);
				}
				else {

					try {
						ServerModel.getInstance().setSilentMode(true);
						LoginAction.login();
						getKernelMgr().updateLicenceKey(licenceKey);
					}
					finally {
						ServerModel.getInstance().logout();
						ServerModel.getInstance().setSilentMode(false);
					}

				}
			}
			catch (Exception e) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			}
		}
		else {
			// nothing needs to happen, since a warning already popped up in verify()
		}


		super.okPressed();
	}


	/**
	 * Create contents of the button bar
	 * @param parent
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		Button okButton = createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);

		// only becomes enabled when verified licence is entered
		okButton.setEnabled(false);

		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, true);
	}


	/**
	 * Return the initial size of the dialog
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(500, 375);
	}



	/**
	 * We don't want that the user pumps an invalid licence to the server, so whenever
	 * the licence key gets changed, we disable OK until verification was successfull
	 */
	private void updateButtonStates() {
		verifyButton.setEnabled(licenceKeyText.getText().length() > 0);
		getButton(IDialogConstants.OK_ID).setEnabled(false);
	}


}
