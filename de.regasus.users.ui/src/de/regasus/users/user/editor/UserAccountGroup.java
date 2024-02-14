package de.regasus.users.user.editor;

import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.lambdalogic.messeinfo.account.AccountLabel;
import com.lambdalogic.messeinfo.account.data.UserAccountCVO;
import com.lambdalogic.messeinfo.account.data.UserAccountVO;
import com.lambdalogic.messeinfo.contact.ContactLabel;
import com.lambdalogic.messeinfo.contact.Person;
import com.lambdalogic.messeinfo.exception.InvalidValuesException;
import com.lambdalogic.util.PasswordHelper;
import com.lambdalogic.util.StringHelper;
import com.lambdalogic.util.exception.ErrorMessageException;
import com.lambdalogic.util.rcp.ModifySupport;
import com.lambdalogic.util.rcp.widget.DecorationController;
import com.lambdalogic.util.rcp.widget.SuperText;

import de.regasus.core.ui.dialog.ChangePasswordDialog;
import de.regasus.users.UsersI18N;

public class UserAccountGroup extends Group {

	private static final String PASSWORD_DUMMY = "        ";
	private static final String[] USER_ID_INVALID_CHARS = new String[UserAccountVO.USER_ID_INVALID_CHARS.length];
	static {
		for (int i = 0; i < USER_ID_INVALID_CHARS.length; i++) {
			USER_ID_INVALID_CHARS[i] = String.valueOf( UserAccountVO.USER_ID_INVALID_CHARS[i] );
		}
	}

	// the entity
	private UserAccountCVO userAccountCVO;

	// Widgets
	private Button resetFailCountButton;

	private SuperText userIdText;

	private SuperText password1Text;
	private SuperText password2Text;
	private ControlDecoration password1ControlDecoration;
	private ControlDecoration password2ControlDecoration;

	private Text firstNameText;
	private Text lastNameText;
	private Text emailText;

	private DecorationController decorationController = new DecorationController();

	private boolean sync = false;


	// Modifying
	private ModifySupport modifySupport = new ModifySupport(this);


	public UserAccountGroup(Composite parent, int style) {
		super(parent, style);

		setText(AccountLabel.User.getString());

		setLayout(new GridLayout(3, false));

		{
			// moreButton
			resetFailCountButton = new Button(this, SWT.NONE);
			resetFailCountButton.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false));
			resetFailCountButton.setText(AccountLabel.ResetFailCount.getString());
			resetFailCountButton.setToolTipText(AccountLabel.ResetFailCountToolTip.getString());
			resetFailCountButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					resetFailCount();
				}
			});
			new Label(this, SWT.NONE);
			new Label(this, SWT.NONE);
		}

		{
			Label label = new Label(this, SWT.RIGHT);
			label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
			label.setText(AccountLabel.UserID.getString());

			userIdText = new SuperText(this, SWT.BORDER);
			userIdText.setInvalidChars( UserAccountVO.USER_ID_INVALID_CHARS );
			GridData layoutData = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
			layoutData.horizontalIndent = 5;
			userIdText.setLayoutData(layoutData);

			userIdText.addModifyListener(modifySupport);

			decorationController.add(label, userIdText);
		}

		{
			Label password1Label = new Label(this, SWT.RIGHT);
			password1Label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
			password1Label.setText(UsersI18N.Password);

			password1Text = new SuperText(this, SWT.BORDER);
			password1Text.setInvalidChars( UserAccountVO.PASSWORD_INVALID_CHARS );
			password1Text.setHidden(true);
			GridData password1LayoutData = new GridData(SWT.FILL, SWT.CENTER, true, false);
			password1LayoutData.horizontalIndent = 5;
			password1Text.setLayoutData(password1LayoutData);

			password1Text.addModifyListener(modifySupport);


			Button generatePasswordButton = new Button(this, SWT.PUSH);
			generatePasswordButton.setText(UsersI18N.UserAccountGroup_GeneratePassword);
			/* GeneratePasswordButton covers vertically 2 cells.
			 * This avoids a gap above and beneath the line with the button.
			 */
			generatePasswordButton.setLayoutData(new GridData(SWT.CENTER, SWT.TOP, false, false, 1, 2));
			generatePasswordButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					try {
						String password = PasswordHelper.generatePassword();

						/* sync wird vor dem ersten setText() auf true gesetzt, um den PasswordModifyListener
						 * daran zu hindern, die ControllerDecorations auf ERROR zu setzen, weil beide
						 * Passwortfelder kurzzeitig unterschiedliche Werte haben.
						 * sync wird bereits vor dem zweiten Aufruf von setText() wieder auf false gsetzt,
						 * damit die ControllerDecorations ggf. auf REQUIRED gesetzt werden, falls sie
						 * vor dem Generieren unterschiedliche Werte hatten.
						 */
						sync = true;
						password1Text.setText(password, false /*hidden*/);
						sync = false;
						password2Text.setText(password, false /*hidden*/);

						password2Text.addModifyListener(modifySupport);
					}
					finally {
						sync = false;
					}
				}
			});

			Label password2Label = new Label(this, SWT.RIGHT);
			password2Label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
			password2Label.setText(UsersI18N.PasswordRepitition);

			password2Text = new SuperText(this, SWT.BORDER);
			password2Text.setInvalidChars( UserAccountVO.PASSWORD_INVALID_CHARS );
			password2Text.setHidden(true);
			GridData password2LayoutData = new GridData(SWT.FILL, SWT.CENTER, true, false);
			password2LayoutData.horizontalIndent = 5;
			password2Text.setLayoutData(password2LayoutData);

			password1ControlDecoration = new ControlDecoration(password1Label, SWT.RIGHT | SWT.TOP);
			password1ControlDecoration.setImage(DecorationController.REQUIRED_IMAGE);
			password1ControlDecoration.setDescriptionText(DecorationController.REQUIRED_TEXT);
			password1ControlDecoration.setShowHover(true);

			password2ControlDecoration = new ControlDecoration(password2Label, SWT.RIGHT | SWT.TOP);
			password2ControlDecoration.setImage(DecorationController.REQUIRED_IMAGE);
			password2ControlDecoration.setDescriptionText(DecorationController.REQUIRED_TEXT);
			password2ControlDecoration.setShowHover(true);

			ModifyListener modifyListener = new PasswordModifyListener();
			password1Text.addModifyListener(modifyListener);
			password2Text.addModifyListener(modifyListener);
		}

		{
			Label label = new Label(this, SWT.RIGHT);
			label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
			label.setText( Person.FIRST_NAME.getString() );

			firstNameText = new Text(this, SWT.BORDER);
			GridData layoutData = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
			layoutData.horizontalIndent = 5;
			firstNameText.setLayoutData(layoutData);

			firstNameText.addModifyListener(modifySupport);
		}

		{
			Label label = new Label(this, SWT.RIGHT);
			label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
			label.setText( Person.LAST_NAME.getString() );

			lastNameText = new Text(this, SWT.BORDER);
			GridData layoutData = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
			layoutData.horizontalIndent = 5;
			lastNameText.setLayoutData(layoutData);

			lastNameText.addModifyListener(modifySupport);
		}

		{
			Label label = new Label(this, SWT.RIGHT);
			label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
			label.setText(ContactLabel.email.getString());

			emailText = new Text(this, SWT.BORDER);
			GridData layoutData = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
			layoutData.horizontalIndent = 5;
			emailText.setLayoutData(layoutData);

			emailText.addModifyListener(modifySupport);
		}

		layout();
	}


	private void resetFailCount() {
		userAccountCVO.getUserAccountVO().setFailCount(0);
		syncWidgetsToEntity();
		modifySupport.fire();
	}


	public void enableErrors() {
		decorationController.enableErrors();
	}


	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

	protected void changePassword() {
		String text = de.regasus.core.ui.CoreI18N.ChangePasswordAction_Text;

		ChangePasswordDialog changePasswordDialog = new ChangePasswordDialog(getShell(), text, "", false);

		int result = changePasswordDialog.open();
		if (result == Window.OK) {
			userAccountCVO.getVO().setPassword(changePasswordDialog.getNewPassword());
			modifySupport.fire();
		}
	}


	public void addModifyListener(ModifyListener modifyListener) {
		modifySupport.addListener(modifyListener);
	}


	public void removeModifyListener(ModifyListener modifyListener) {
		modifySupport.removeListener(modifyListener);
	}


	public void syncWidgetsToEntity() {
		sync = true;

		try {
			UserAccountVO userAccountVO = userAccountCVO.getVO();

			userIdText.setText(StringHelper.avoidNull(userAccountVO.getUserID()));
			userIdText.setEnabled(isNew());

			// disguise the password hash unless the password is empty (which means that the entity is new)
			String pw = PASSWORD_DUMMY;
			if (StringHelper.isEmpty(userAccountVO.getPassword())) {
				pw = "";
			}
			password1Text.setText(pw);
			password2Text.setText(pw);

			firstNameText.setText(StringHelper.avoidNull(userAccountVO.getFirstName()));
			lastNameText.setText(StringHelper.avoidNull(userAccountVO.getLastName()));
			emailText.setText(StringHelper.avoidNull(userAccountVO.getEmail()));
			resetFailCountButton.setEnabled(userAccountVO.getFailCount() > 0);
		}
		finally {
			sync = false;
		}
	}


	public boolean isNew() {
		return userAccountCVO == null || userAccountCVO.getPK() == null;
	}


	public void syncEntityToWidgets() throws ErrorMessageException {
		if (userAccountCVO != null) {
			UserAccountVO userAccountVO = userAccountCVO.getVO();
			userAccountVO.setUserID(StringHelper.trim(userIdText.getText()));

			String password1 = password1Text.getText();
			String password2 = password2Text.getText();
			if (password1 == null || password1.length() == 0) {
				throw new InvalidValuesException(UsersI18N.ErrorMessage_PasswordEmpty);
			}
			else if (!password1.equals(password2)) {
				throw new InvalidValuesException(UsersI18N.ErrorMessage_PasswordsNotEqual);
			}
			// If the user set a new password, set the real password (not the hash). Otherwise leave password untouched.
			else if (!password1.equals(PASSWORD_DUMMY)) {
				userAccountVO.setPassword(password1);
			}

			userAccountVO.setFirstName(StringHelper.trim(firstNameText.getText()));
			userAccountVO.setLastName(StringHelper.trim(lastNameText.getText()));
			userAccountVO.setEmail(StringHelper.trim(emailText.getText()));
		}
	}


	public void setUserAccountCVO(UserAccountCVO userAccountCVO) {
		this.userAccountCVO = userAccountCVO;
	}


	private class PasswordModifyListener implements ModifyListener {

		@Override
		public void modifyText(ModifyEvent e) {
			if (!sync) {
				String password1 = password1Text.getText();
				String password2 = password2Text.getText();

				if (password1 == null || password1.length() == 0) {
					password1ControlDecoration.setImage(DecorationController.ERROR_IMAGE);
					password2ControlDecoration.setImage(DecorationController.ERROR_IMAGE);
				}
				else if (!password1.equals(password2)) {
					password1ControlDecoration.setImage(DecorationController.ERROR_IMAGE);
					password2ControlDecoration.setImage(DecorationController.ERROR_IMAGE);
				}
				else {
					password1ControlDecoration.setImage(DecorationController.REQUIRED_IMAGE);
					password2ControlDecoration.setImage(DecorationController.REQUIRED_IMAGE);
				}
			}
		}

	}

}
