package de.regasus.common.composite;

import static com.lambdalogic.util.StringHelper.avoidNull;

import java.util.Objects;

import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.lambdalogic.messeinfo.config.parameterset.ParticipantConfigParameterSet;
import com.lambdalogic.messeinfo.config.parameterset.PersonConfigParameterSet;
import com.lambdalogic.messeinfo.config.parameterset.ProfileConfigParameterSet;
import com.lambdalogic.messeinfo.contact.ContactLabel;
import com.lambdalogic.messeinfo.contact.Person;
import com.lambdalogic.util.rcp.ModifySupport;
import com.lambdalogic.util.rcp.widget.SWTHelper;
import com.lambdalogic.util.rcp.widget.SuperText;

import de.regasus.I18N;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.dialog.ChangePasswordDialog;
import de.regasus.ui.Activator;

public class UserCredentialsGroup extends Group {

	// the entity
	private Person person;

	private ModifySupport modifySupport = new ModifySupport(this);


	// Widgets
	private SuperText userNameText;
	private Text passwordText;
	private Button setPasswordButton;


	/**
	 * Create the composite
	 * @param parent
	 * @param style
	 * @param configParameterSet
	 * @throws Exception
	 */
	public UserCredentialsGroup(
		Composite parent,
		int style,
		PersonConfigParameterSet configParameterSet
	)
	throws Exception {
		super(parent, style);

		Objects.requireNonNull(configParameterSet);

		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 3;
		setLayout(gridLayout);
		setText( ContactLabel.UserCredentials.getString() );

		if ( configParameterSet.getUserName().isVisible() ) {
			Label userNameLabel = new Label(this, SWT.NONE);
			userNameLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
			userNameLabel.setText(I18N.UserCredentialsGroup_UserName_Label);

			if (configParameterSet instanceof ProfileConfigParameterSet) {
				userNameLabel.setToolTipText(I18N.UserCredentialsGroup_Profile_UserName_ToolTip);
			}
			else if (configParameterSet instanceof ParticipantConfigParameterSet) {
				userNameLabel.setToolTipText(I18N.UserCredentialsGroup_Participant_UserName_ToolTip);
			}

			userNameText = new SuperText(this, SWT.BORDER);
			userNameText.setInvalidChars( Person.USER_NAME_INVALID_CHARS );
			userNameText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			userNameText.setTextLimit( Person.USER_NAME.getMaxLength() );

			userNameText.addModifyListener(modifySupport);

			new Label(this, SWT.NONE);
		}

		if ( configParameterSet.getPassword().isVisible() ) {
			final Label passwordLabel = new Label(this, SWT.NONE);
			passwordLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
			passwordLabel.setText(I18N.UserCredentialsGroup_PasswordHash_Label);
			passwordLabel.setToolTipText(I18N.UserCredentialsGroup_PasswordHash_ToolTip);

			passwordText = new Text(this, SWT.BORDER);
			passwordText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			passwordText.setTextLimit( Person.PASSWORD.getMaxLength() );

			passwordText.addModifyListener(modifySupport);

			setPasswordButton = new Button(this, SWT.PUSH);
			setPasswordButton.setText(I18N.UserCredentialsGroup_SetPassword_Label);
			setPasswordButton.setToolTipText(I18N.UserCredentialsGroup_SetPassword_ToolTip);
			setPasswordButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					setPassword();
				}
			});
		}
	}


	// **************************************************************************
	// * Modifying
	// *

	public void addModifyListener(ModifyListener modifyListener) {
		modifySupport.addListener(modifyListener);
	}


	public void removeModifyListener(ModifyListener modifyListener) {
		modifySupport.removeListener(modifyListener);
	}

	// *
	// * Modifying
	// **************************************************************************

	private void syncWidgetsToEntity() {
		if (person != null) {
			SWTHelper.syncExecDisplayThread(new Runnable() {
				@Override
				public void run() {
					try {
						modifySupport.setEnabled(false);

						if (userNameText != null) {
							userNameText.setText( avoidNull(person.getUserName()) );
						}

						if (passwordText != null) {
							passwordText.setText( avoidNull(person.getPassword()) );
						}
					}
					catch (Exception e) {
						RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
					}
					finally {
						modifySupport.setEnabled(true);
					}
				}
			});
		}
	}


	public void syncEntityToWidgets() {
		if (person != null) {
			if (userNameText != null) {
				person.setUserName(userNameText.getText());
			}

			if (passwordText != null) {
				person.setPassword( passwordText.getText() );
			}
		}
	}


	public void setPerson(Person person) {
		this.person = person;
		syncWidgetsToEntity();
	}


	@Override
	public void setEnabled (boolean enabled) {
		if (userNameText != null) {
			userNameText.setEnabled(enabled);
		}

		if (passwordText != null) {
			passwordText.setEnabled(enabled);
		}
	}


	protected void setPassword() {
		try {
			Shell shell = Display.getDefault().getActiveShell();
//			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();

			ChangePasswordDialog changePasswordDialog = new ChangePasswordDialog(
				shell,
				I18N.UserCredentialsGroup_ChangePasswordDialog_Title,
				I18N.UserCredentialsGroup_ChangePasswordDialog_Message,
				false	// showOldPasswordField
			);

			int result = changePasswordDialog.open();
			if (result == Window.OK) {
				String newPassword = changePasswordDialog.getNewPassword();

				if (passwordText != null) {
					this.passwordText.setText(newPassword);
				}
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

}
