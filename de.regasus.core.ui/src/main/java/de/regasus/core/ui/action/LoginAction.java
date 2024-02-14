package de.regasus.core.ui.action;

import static com.lambdalogic.util.StringHelper.isNotEmpty;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import com.lambdalogic.util.StringHelper;
import com.lambdalogic.util.model.ModelEvent;
import com.lambdalogic.util.model.ModelListener;
import com.lambdalogic.util.rcp.BusyCursorHelper;

import de.regasus.core.ServerModel;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.Activator;
import de.regasus.core.ui.CoreI18N;
import de.regasus.core.ui.dialog.LoginDialog;
import de.regasus.core.ui.login.pref.LoginPreference;

public class LoginAction extends Action implements ModelListener {

	public static final String ID = "de.regasus.core.ui.action.LoginAction";

	// Models
	private static final ServerModel serverModel;

	static {
		serverModel = ServerModel.getInstance();
	}

	public LoginAction() {
		setId(ID);
		setText(CoreI18N.LoginAction_Text);
		setToolTipText(CoreI18N.LoginAction_ToolTip);
		setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(
			de.regasus.core.ui.Activator.PLUGIN_ID,
			de.regasus.core.ui.IImageKeys.LOGIN));

		// beim ServerModel registrieren
		serverModel.addListener(this);
		setEnabled( ! serverModel.isLoggedIn());
	}


	@Override
	public void dataChange(ModelEvent event) {
		if (event.getSource() instanceof ServerModel) {
			setEnabled( ! serverModel.isLoggedIn());
		}
	}


	public void dispose() {
		serverModel.removeListener(this);
	}


	@Override
	public void run() {
		login();
	}


	public static boolean login() {
		return login(
			null, // _userName
			null, // _password
			null, // _host
			null  // _autoLogin
		);
	}


	/**
	 * Ask user for userName and password, verify both and load UserModel.
	 * @return
	 */
	public static boolean login(
		final String _userName,
		final String _password,
		final String _host,
		final Boolean _autoLogin
	) {
		String userName = null;
		String password = null;
		String host = null;
		boolean autoLogin = false;


		boolean rememberUserName = false;
		boolean rememberPassword = false;
		boolean rememberHost = false;



		// load Login Preferences
		LoginPreference loginPreferences = LoginPreference.getInstance();


		// **************************************************************************
		// * Set Proxy
		// *

		// Read proxy settings from LoginPreferences
		String proxyHost = loginPreferences.getProxyHost();
		String proxyPort = String.valueOf(loginPreferences.getProxyPort());
		String proxyUser = loginPreferences.getProxyUser();
		String proxyPassword = loginPreferences.getProxyPassword();
		String proxySet;

		/*
		 * Only set the proxy data if there is a value for proxyHost.
		 * Otherwise the empty values will overwrite the settings in
		 * the .ini file (messeinfo.ini)!
		 */
		if ( isNotEmpty(proxyHost) ) {
			proxySet = "true";

			System.setProperty("proxySet", proxySet);

			System.setProperty("proxyHost", proxyHost);
			System.setProperty("http.proxyHost", proxyHost);
			System.setProperty("https.proxyHost", proxyHost);

			System.setProperty("proxyPort", proxyPort);
			System.setProperty("http.proxyPort", proxyPort);
			System.setProperty("https.proxyPort", proxyPort);

			System.setProperty("proxyUser", proxyUser);
			System.setProperty("http.proxyUser", proxyUser);
			System.setProperty("https.proxyUser", proxyUser);

			System.setProperty("proxyPassword", proxyPassword);
			System.setProperty("http.proxyPassword", proxyPassword);
			System.setProperty("https.proxyPassword", proxyPassword);
		}
		else {
			proxySet = "false";

			proxyHost = "";
			proxyPort = "";
			proxyUser = "";
			proxyPassword = "";
		}

		// *
		// * Set Proxy
		// **************************************************************************


		// userName
		if (_userName != null) {
			userName = _userName;
		}
		else {
			userName = loginPreferences.getUserName();
			userName = StringHelper.trim(userName);
			rememberUserName = userName != null;
		}

		// password
		if (_password != null) {
			password = _password;
		}
		else {
			password = loginPreferences.getPassword();
			password = StringHelper.trim(password);
			rememberPassword = password != null;
		}

		// host (and port)
		if (_host != null) {
			host = _host;
		}
		else {
			host = loginPreferences.getHost();
			host = StringHelper.trim(host);
			rememberHost = host != null;
		}

		// autoLogin
		if (_userName != null && _password != null && _host != null) {
			autoLogin = _autoLogin != null && _autoLogin.booleanValue();
		}
		else {
			autoLogin = loginPreferences.isAutoLogin();
		}

		if (autoLogin) {
			// try to login automatically
			loginWithProgress(
				userName,
				password,
				host
			);
		}

		LoginDialog loginDialog = null;
		while (!serverModel.isLoggedIn()) {
			if (loginDialog == null) {
				loginDialog = new LoginDialog(null);
			}

			// initialize LoginDialog with Login Preferences
			if (userName != null) {
				loginDialog.setUserName(userName);
				loginDialog.setRememberUserName(rememberUserName);
			}


			if (password != null) {
				loginDialog.setPassword(password);
				loginDialog.setRememberPassword(rememberPassword);
			}


			if (host != null) {
				loginDialog.setHost(host);
				loginDialog.setRememberHost(rememberHost);
			}

			loginDialog.setAutomaticLogin(autoLogin);

			// open LoginDialog
			int loginResult = loginDialog.open();

			if (loginResult == 0) {
				// save Login Preferences
				if (loginDialog.isRememberUserName()) {
					loginPreferences.setUserName(loginDialog.getUserName());
				}
				else {
					loginPreferences.setUserName("");
				}


				if (loginDialog.isRememberPassword()) {
					loginPreferences.setPassword(loginDialog.getPassword());
				}
				else {
					loginPreferences.setPassword("");
				}


				if (loginDialog.isRememberHost()) {
					loginPreferences.setHost(loginDialog.getHost());
				}
				else {
					loginPreferences.setHost("");
				}

				loginPreferences.setAutoLogin(loginDialog.isAutomaticLogin());

				// login
				loginWithProgress(
					loginDialog.getUserName(),
					loginDialog.getPassword(),
					loginDialog.getHost()
				);
			}
			else {
				return false;
			}

			// save Login Preferences
			loginPreferences.save();

		} // while

		return true;
	}


//	private static void loginWithProgress(final String userName, final String password, final String host) {
//		ProgressMonitorDialog progressMonitorDialog = new ProgressMonitorDialog(null);
//		progressMonitorDialog.setCancelable(true);
//		try {
//			progressMonitorDialog.run(true, true, new IRunnableWithProgress() {
//
//				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
//					monitor.beginTask("Connecting...", IProgressMonitor.UNKNOWN);
//
//					monitor.subTask("Authenticating with Server...");
//
//					ServerModel.getInstance().login(userName, password, host);
//				}
//
//			});
//		}
//		catch (Throwable e) {
//			ErrorHandler.handleApplicationError(Activator.PLUGIN_ID, LoginAction.class.getSimpleName(), e, null);
//		}
//	}

	private static void loginWithProgress(
		final String userName,
		final String password,
		final String host
	) {
		try {

			if (PlatformUI.isWorkbenchRunning()) {

				BusyCursorHelper.busyCursorWhile(new Runnable() {
					@Override
					public void run() {

						try {
							serverModel.login(userName, password, host);
						}
						catch (Throwable t) {
							RegasusErrorHandler.handleApplicationError(Activator.PLUGIN_ID, LoginAction.class.getName(), t);
						}

					}
				});

			}
			else {

				try {
					ProgressMonitorDialog progressMonitorDialog = new ProgressMonitorDialog(null);
					progressMonitorDialog.setCancelable(false);
					progressMonitorDialog.run(true, true, new IRunnableWithProgress() {

						@Override
						public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
							monitor.beginTask(CoreI18N.LoginAction_ProgressMsg_Connecting, IProgressMonitor.UNKNOWN);

							monitor.subTask(CoreI18N.LoginAction_ProgressMsg_Authenticating);

							serverModel.login(userName, password, host);
						}

					});
				}
				catch (Throwable e) {
					RegasusErrorHandler.handleApplicationError(Activator.PLUGIN_ID, LoginAction.class.getSimpleName(), e);
				}

			}

		}
		catch (Throwable e) {
			RegasusErrorHandler.handleApplicationError(Activator.PLUGIN_ID, LoginAction.class.getSimpleName(), e);
		}
	}

}
