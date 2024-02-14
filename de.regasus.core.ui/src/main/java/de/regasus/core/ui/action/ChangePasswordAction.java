package de.regasus.core.ui.action;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import com.lambdalogic.util.model.ModelEvent;
import com.lambdalogic.util.model.ModelListener;
import com.lambdalogic.util.rcp.UtilI18N;

import de.regasus.core.ServerModel;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.Activator;
import de.regasus.core.ui.CoreI18N;
import de.regasus.core.ui.dialog.ChangePasswordDialog;
import de.regasus.core.ui.login.pref.LoginPreference;

public class ChangePasswordAction extends Action implements ModelListener {

	public static final String ID = "de.regasus.core.ui.action.ChangePasswordAction";

	// Models
	private static final ServerModel serverModel;

	static {
		serverModel = ServerModel.getInstance();
	}


	public ChangePasswordAction() {
		setId(ID);
		setText(CoreI18N.ChangePasswordAction_Text);
		setToolTipText(CoreI18N.ChangePasswordAction_ToolTip);

		// beim ServerModel registrieren
		serverModel.addListener(this);
		setEnabled(serverModel.isLoggedIn());
	}


	@Override
	public void dataChange(ModelEvent event) {
		setEnabled(serverModel.isLoggedIn());
	}


	public void dispose() {
		serverModel.removeListener(this);
	}


	/**
	 * Opens a dialog to ask for old and new password, if OK was pressed calls the servermodel to actually perform the
	 * password change, and tells about successfull change. Additionally stores new password to preferences, if
	 * autologin is set to true
	 */
	@Override
	public void run() {
		Shell shell = Display.getDefault().getActiveShell();

		try {
			String user = serverModel.getModelData().getUser();
			String message = CoreI18N.ChangePasswordActionFor_Text;
			message = message.replace("<userId>", user);
			ChangePasswordDialog changePasswordDialog = new ChangePasswordDialog(
				shell,
//				user,
				CoreI18N.ChangePasswordAction_Text,
				message,
				true	// showOldPasswordField
			);

			int result = changePasswordDialog.open();
			if (result == Window.OK) {
				String oldPassword = changePasswordDialog.getOldPassword();
				String newPassword = changePasswordDialog.getNewPassword();

				serverModel.changePassword(user, oldPassword, newPassword);

				MessageDialog.openInformation(shell, UtilI18N.Info, CoreI18N.ChangePasswordAction_Success);

				// Additionally store new password to preferences, if autologin is set to true
				LoginPreference loginPreferences = LoginPreference.getInstance();
				if (loginPreferences.getUserName().equals(user) && loginPreferences.isAutoLogin()) {
					loginPreferences.setPassword(newPassword);
					loginPreferences.save();
				}
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}

}
