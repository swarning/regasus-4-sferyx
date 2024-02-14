package de.regasus.core.ui.dialog;

import org.eclipse.jface.dialogs.IDialogConstants;
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
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.lambdalogic.messeinfo.profile.Profile;
import com.lambdalogic.util.PasswordHelper;
import com.lambdalogic.util.rcp.widget.SuperText;

import de.regasus.core.ui.CoreI18N;

/**
 * This dialog shows text fields to enter
 * <ul>
 * <li>Old Password</li>
 * <li>New Password</li>
 * <li>New Password Repeated</li>
 * </ul>
 *
 * Additionally there is the possibility to generate a random password with one
 * extra button and put it in the two latter text fields with another button.
 */
public class ChangePasswordDialog extends TitleAreaDialog implements ModifyListener {

	// *************************************************************************
	// * Widgets
	// *

	private SuperText oldPasswordText;
	private SuperText newPasswordText;
	private SuperText repeatNewPasswordText;
	private Button okButton;
	private Button generatePasswordButton;
	private Text generatedPasswordText;
	private Button useGeneratedPasswordButton;

	// *************************************************************************
	// * Attribute
	// *

	private String title;
	private String message;
	private String newPassword;
	private String oldPassword;

	private boolean showOldPasswordField;



	// *************************************************************************
	// * Konstruktor
	// *



	public ChangePasswordDialog(
		Shell parentShell,
		String title,
		String message,
		boolean showOldPasswordField
	) {
		super(parentShell);

		this.showOldPasswordField = showOldPasswordField;
		this.title = title;
		this.message = message;
	}



	// *************************************************************************
	// * Overriden superclass methods
	// *

	/**
	 * Create contents of the dialog
	 *
	 * @param parent
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		setTitle(title);
		setMessage(message);

		Composite area = (Composite) super.createDialogArea(parent);
		Composite container = new Composite(area, SWT.NONE);
		container.setLayout(new GridLayout(2, false));
		container.setLayoutData(new GridData(GridData.FILL_BOTH));


		if (showOldPasswordField) {
			// Users Password
			final Label usersPasswordLabel = new Label(container, SWT.NONE);
			usersPasswordLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
			usersPasswordLabel.setText(CoreI18N.Enter_Your_Own_Password);

			// use style SWT.PASSWORD to make the input unreadable
			oldPasswordText = new SuperText(container, SWT.PASSWORD | SWT.BORDER);
			oldPasswordText.setInvalidChars( Profile.PASSWORD_INVALID_CHARS );
			oldPasswordText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			oldPasswordText.setFocus();
			oldPasswordText.addModifyListener(this);
		}


		// New Password
		final Label newPasswordLabel = new Label(container, SWT.NONE);
		newPasswordLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		newPasswordLabel.setText(CoreI18N.ChangePasswordDialog_NewPasswordLabel);

		// use style SWT.PASSWORD to make the input unreadable
		newPasswordText = new SuperText(container, SWT.PASSWORD | SWT.BORDER);
		newPasswordText.setInvalidChars( Profile.PASSWORD_INVALID_CHARS );
		newPasswordText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		newPasswordText.addModifyListener(this);


		if (oldPasswordText == null) {
			newPasswordText.setFocus();
		}

		// Repeat new Password
		final Label newPasswordRepeatLabel = new Label(container, SWT.NONE);
		newPasswordRepeatLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		newPasswordRepeatLabel.setText(CoreI18N.ChangePasswordDialog_RepeatNewPasswordLabel);

		// use style SWT.PASSWORD to make the input unreadable
		repeatNewPasswordText = new SuperText(container, SWT.PASSWORD | SWT.BORDER);
		repeatNewPasswordText.setInvalidChars( Profile.PASSWORD_INVALID_CHARS );
		repeatNewPasswordText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		repeatNewPasswordText.addModifyListener(this);

		Label label = new Label(container, SWT.SEPARATOR | SWT.HORIZONTAL);
		label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

		// Generate new Password
		generatePasswordButton = new Button(container, SWT.PUSH);
		generatePasswordButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		generatePasswordButton.setText(CoreI18N.ChangePasswordDialog_Generate);
		generatePasswordButton.addSelectionListener(new SelectionAdapter(){
			@Override
			public void widgetSelected(SelectionEvent e) {
				generatePassword();
			}
		});

		generatedPasswordText = new Text(container,  SWT.BORDER);
		generatedPasswordText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		generatedPasswordText.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_BLACK));
		generatedPasswordText.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_GREEN));

		useGeneratedPasswordButton = new Button(container, SWT.PUSH);
		useGeneratedPasswordButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		useGeneratedPasswordButton.setText(CoreI18N.ChangePasswordDialog_UseGenerated);
		useGeneratedPasswordButton.setEnabled(false);
		useGeneratedPasswordButton.addSelectionListener(new SelectionAdapter(){
			@Override
			public void widgetSelected(SelectionEvent e) {
				useGeneratedPassword();
			}
		});
		return area;
	}


	protected void updateState() {
	}

	/**
	 * Create contents of the button bar
	 *
	 * @param parent
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		okButton = createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		okButton.setEnabled(false);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}

	/**
	 * When the OK-Button is pressed, the contents of the widgets are copied to the attributs,
	 * because the widgets are soon disposed and the contents would be lost.
	 */
	@Override
	protected void okPressed() {
		if (showOldPasswordField && oldPasswordText != null) {
			oldPassword = oldPasswordText.getText();
		}
		newPassword = newPasswordText.getText();
		super.okPressed();
	}

	/**
	 * Return the initial size of the dialog
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(500, 375);
	}

	// *************************************************************************
	// * Event Handling
	// *

	/**
	 * For almost each change, check whether the input contains at least the
	 * username and two equal new passwords. The old password however might
	 * be empty. The dialog can only be left with OK if the input has passed
	 * the check.
	 */
	@Override
	public void modifyText(ModifyEvent e) {
		boolean newPasswordEntered = newPasswordText.getText().trim().length() > 0;
		boolean newPasswordAndRepeatAreIdentical = newPasswordText.getText().equals(repeatNewPasswordText.getText());

		okButton.setEnabled(newPasswordEntered && newPasswordAndRepeatAreIdentical);
	}


	/**
	 * When the "Generate Password" button is pressed, a random sequence of {@link #PASSWORD_LENGTH}
	 * characters is produced, which are taken from {@link #PASSWORD_CHARS}.
	 */
	protected void generatePassword() {
		String password = PasswordHelper.generatePassword();
		generatedPasswordText.setText(password);
		useGeneratedPasswordButton.setEnabled(true);
	}


	/**
	 * When the "Use Generated Password" button is pressed, the contents of the
	 * Text widget which shows the generated password is copied to the two
	 * widgets in which the new password is to be entered twice.
	 */
	protected void useGeneratedPassword() {
		String generatedPassword = generatedPasswordText.getText();
		newPasswordText.setText(generatedPassword);
		repeatNewPasswordText.setText(generatedPassword);
	}


	// *************************************************************************
	// * Generated getters and setters
	// *


	public String getNewPassword() {
		return newPassword;
	}


	public String getOldPassword() {
		return oldPassword;
	}

}
