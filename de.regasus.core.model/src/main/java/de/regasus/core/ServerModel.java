package de.regasus.core;

import static de.regasus.LookupService.*;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import com.lambdalogic.messeinfo.account.interfaces.IUserManager;
import com.lambdalogic.messeinfo.kernel.interfaces.ISessionManager;
import com.lambdalogic.util.exception.ErrorMessageException;
import com.lambdalogic.util.model.Model;

import de.regasus.client.JBossClient;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.model.Activator;
import de.regasus.core.model.CoreModelI18N;
import de.regasus.core.model.ServerModelEvent;
import de.regasus.core.model.ServerModelEventType;
import de.regasus.core.model.StatusCheckerNotifier;


/**
 * Model, dass die Verbindung zum Server realisiert. Realisiert die Anmeldung am Server.
 */
public final class ServerModel extends Model<ServerModelData> {
	private static Logger log = Logger.getLogger("model.ServerModel");

	private static ServerModel singleton = null;

	/**
	 * Der Silent-Mode hat 2 Auswirkungen:
	 * - Listener werden nicht benachrichtigt
	 * - es wird keine Session erzeugt
	 *
	 * Der Silent-Mode wird vom EnterLicenseDialog genutzt,
	 * damit sich der Nutzer für das Hochladen einer neuen Lizenz
	 * temporär anmdelden kann.
	 */
	protected boolean silentMode = false;

	/**
	 * The ID of the session that needs to be renewed
	 */
	protected Long sessionID;

	/**
	 * A single thread that calls the server every 60 seconds to renew the session. If the communication
	 * is blocked, subsequent calls will be done with an interval of 60 seconds anyway, so no avalanche
	 * of calls takes place.
	 */
	private ScheduledExecutorService sessionRenewerExecutor = Executors.newSingleThreadScheduledExecutor();

	/**
	 * A single thread that calls all plugins every 5 minutes that want to check some status and thus implement
	 * an extension to the extension point "de.regasus.core.model.statusChecker"
	 */
	private ScheduledExecutorService statusCheckerExecutor = Executors.newSingleThreadScheduledExecutor();


	/**
	 * A handle to the scheduled runnable that renews the session, is used during logout to tell it to finish
	 */
	private ScheduledFuture<?> scheduledFuture;




	private JBossClient client;

	private static final String UNKNOWN_HOST_NAME = "unknown";

	private String hostName = UNKNOWN_HOST_NAME;

	private ScheduledFuture<?> statusCheckerFuture;


	// cache for URLs
	private String baseUrl = null;
	private String authUrl = null;
	private String webServiceUrl = null;
	private String onlineFormUrl = null;
	private String portalUrl = null;


	private ServerModel() {
		client = new JBossClient();
	}


	public static ServerModel getInstance() {
		if (singleton == null) {
			singleton = new ServerModel();
		}
		return singleton;
	}


	@Override
	protected ServerModelData getModelDataFromServer() {
		ServerModelData serverModelData = new ServerModelData();
		return serverModelData;
	}

	/**
	 * Is called by the user in order to reload all data that is currently present in the client anew from the server.
	 *
	 * Currently known models (and other Objects) which actually react on this event are:
	 * <ul>
	 * <li>EventModel</li>
	 * <li>LoginAction</li>
	 * <li>LogoutAction</li>
	 * <li>ChangePasswordAction</li>
	 * <li>EditPropertiesAction</li>
	 * <li>ServerStatusLineContribution</li>
	 * </ul>
	 */
	public void refreshAll() {
		if (!silentMode) {
			try {
				fireDataChange(new ServerModelEvent(this, ServerModelEventType.REFRESH));
			}
			catch (Exception e) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			}
		}
	}


	public void clearServerCache() {
		try {
			getKernelMgr().clearServerCache();
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}

		try {
			CountryModel.getInstance().refresh();
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}

		try {
			LanguageModel.getInstance().refresh();
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	public boolean isLoggedIn() {
		return client.isLoggedIn();
	}


	public void login(String user, String password, String host) {
		if (isLoggedIn()) {
			logout();
		}

		try {
			ServerModelData serverModelData = getModelData();
			serverModelData.setClient(client);

			client.setUserName(user);
			client.setPassword(password);
			client.setHost(host);


			// set user and host as global info to ErrorHandler
			RegasusErrorHandler.putGlobalInfo("User", user);
			RegasusErrorHandler.putGlobalInfo("Host", host);

			if (serverModelData.isConnected()) {
				return;
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			return;
		}


		try {
			client.login(false);

			// Check status every 5 minutes, and almost immediately after client start
			StatusCheckerNotifier statusCheckerNotifier = new StatusCheckerNotifier();
			statusCheckerFuture = statusCheckerExecutor.scheduleWithFixedDelay(
				statusCheckerNotifier,	// command
				5,						// initialDelay
				300,					// delay
				TimeUnit.SECONDS		// unit
			);

			if (   IUserManager.SYSTEM_USER_ADMIN.equals(getUser())
				|| IUserManager.SYSTEM_USER_LICENCE.equals(getUser())
			) {
				// The users "admin" and "licence" don't need a session
				log.info("Login succeeded.");
				Date serverTime = getKernelMgr().getTime();
				log.info("Current Time on Server: " + serverTime);

			}
			else {
				getKernelMgr().assertApplicationAccess();

				if (!silentMode) {
    				// All other users need a session and must renew them every minute
    				ISessionManager sessionManager = getSessionMgr();
    				sessionID = sessionManager.createSession(user, getLocalHostName());
    				log.info("Login succeeded, session ID " + sessionID);
    				SessionRenewer sessionRenewer = new SessionRenewer(this);
    				scheduledFuture = sessionRenewerExecutor.scheduleWithFixedDelay(sessionRenewer, 60, 60, TimeUnit.SECONDS);

				}
			}

			if (!silentMode) {
				fireDataChange(new ServerModelEvent(this, ServerModelEventType.LOGIN));
			}
		}
		catch (ErrorMessageException e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			logout() ;
		}
		catch (Exception e) {
			String title = CoreModelI18N.ServerModel_LoginFailedErrorTitle;
			StringBuilder message = new StringBuilder();

//			String javaVersion = System.getProperty("java.version");
//			message.append("Java-Version: ");
//			message.append(javaVersion);
//
//			String trustStore = System.getProperty("javax.net.ssl.trustStore");
//			message.append('\n');
//			message.append("Trust Store: ");
//			message.append(trustStore);
//
//			message.append('\n');
			message.append(CoreModelI18N.ServerModel_LoginFailedErrorMessage);

			RegasusErrorHandler.handleApplicationError(
				Activator.PLUGIN_ID,
				getClass().getName(),
				e,
				message.toString(),
				title
			);

			logout();
		}
	}


	public void shutdown() {
		modelData.setShutdown(true);
		logout();
	}


	public boolean isShutdown() {
		return modelData.isShutdown();
	}


	public void logout() {
		if (isLoggedIn()) {
			try {

				// inform listeners that logout will start
				if (!silentMode) {
					fireDataChange(new ServerModelEvent(this, ServerModelEventType.BEFORE_LOGOUT));
				}


				// Do not shut down the scheduled excecutors, but cancel the tasks
				// so that after re-login they can continue to work

				if (scheduledFuture != null) {
					scheduledFuture.cancel(true);
					scheduledFuture = null;
				}

				if (statusCheckerFuture != null) {
					statusCheckerFuture.cancel(true);
					statusCheckerFuture = null;
				}

				if (sessionID != null) {
					try {
						getSessionMgr().deleteSession(sessionID);
					}
					catch (Exception e) {
						com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
					}
				}

				try {
					client.logout();
				}
				catch (Exception e) {
					com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
				}

				if (!silentMode) {
					fireDataChange(new ServerModelEvent(this, ServerModelEventType.LOGOUT));
				}
			}
			catch (Exception e) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			}
		}
	}


	public void changePassword(String userID, String oldPassword, String newPassword) throws Exception {
		getUserMgr().changeUserPassword(userID, oldPassword, newPassword);

		// If no exception and the change happened for the current user
		if (client.getUserName().equals(userID)) {
			client.setPassword(newPassword);
			if (isLoggedIn()) {
				client.logout();
				client.login();
			}
		}
	}


	public void setClientPassword(String newPassword) throws Exception {
		client.setPassword(newPassword);
		if (isLoggedIn()) {
			client.logout();
			client.login();
		}
	}


	public String getLocalHostName() {
		if (hostName.equals(UNKNOWN_HOST_NAME)) {
			try {
				InetAddress addr = InetAddress.getLocalHost();
				hostName = addr.getHostName();
			}
			catch (UnknownHostException e) {
				com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
			}
		}
		return hostName;
	}


	public String getHost() {
		if ( isLoggedIn() ) {
			return client.getHost();
		}
		else {
			return null;
		}
	}


	public String getUser() {
		if ( isLoggedIn() ) {
			return client.getUserName();
		}
		else {
			return null;
		}
	}


	public String getPassword() {
		if ( isLoggedIn() ) {
			return client.getPassword();
		}
		else {
			return null;
		}
	}


	public boolean isSilentMode() {
		return silentMode;
	}


	public void setSilentMode(boolean value) {
		this.silentMode = value;
	}

	// *
	// * JBoss connection
	// *************************************************************************


	public void updateLicenceKey(String licenceKey) throws Exception {
		if (licenceKey != null && isLoggedIn()) {
			 getKernelMgr().updateLicenceKey(licenceKey);
		}
	}


    public String getBaseUrl() throws ErrorMessageException {
    	if (baseUrl == null) {
    		baseUrl = getKernelMgr().getBaseURL();
    	}
    	return baseUrl;
    }


    public String getAuthUrl() throws ErrorMessageException {
    	if (authUrl == null) {
    		authUrl = getKernelMgr().getAuthURL();
    	}
    	return authUrl;
    }


    public String getWebServiceUrl() throws ErrorMessageException {
    	if (webServiceUrl == null) {
    		webServiceUrl = getKernelMgr().getWebServiceURL();
    	}
    	return webServiceUrl;
    }


    public String getOnlineFormUrl() throws ErrorMessageException {
    	if (onlineFormUrl == null) {
    		onlineFormUrl = getKernelMgr().getOnlineFormURL();
    	}
    	return onlineFormUrl;
    }


    public String getPortalUrl() throws ErrorMessageException {
    	if (portalUrl == null) {
    		portalUrl = getKernelMgr().getPortalURL();
    	}
    	return portalUrl;
    }


    public void runDatabaseUpdateBeans() {
    	getKernelMgr().runDatabaseUpdateBean();
    }

}
