package de.regasus.core.ui.login.pref;

import static de.regasus.core.ui.login.pref.LoginPreferenceDefinition.*;

import java.io.IOException;

import org.eclipse.ui.preferences.ScopedPreferenceStore;

import com.lambdalogic.util.StringHelper;
import com.lambdalogic.util.rcp.pref.AbstractPreference;
import com.lambdalogic.util.rcp.pref.PreferenceHelper;
import com.lambdalogic.util.rcp.pref.PreferenceInitializerHelper;


public class LoginPreference extends AbstractPreference {

	private static final LoginPreference INSTANCE = new LoginPreference();

	private static final ScopedPreferenceStore PREFERENCE_STORE = new ScopedPreferenceStore(
    	SCOPE_CONTEXT,
    	QUALIFIER
	);


	public static LoginPreference getInstance() {
		return INSTANCE;
	}


	private LoginPreference() {
	}


	@Override
	public ScopedPreferenceStore getPreferenceStore() {
		return PREFERENCE_STORE;
	}


	public void save() {
		System.out.println("Save " + QUALIFIER  + " preferences to: " + PreferenceHelper.extractLocation( getPreferenceStore() ));

		try {
			getPreferenceStore().save();
		}
		catch (IOException e) {
			com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
		}
	}


	public void initialize() {
		PreferenceInitializerHelper.initializePreferences(getPreferenceStore(), LoginPreferenceDefinition.class);
	}


	public boolean isEmpty() {
		return StringHelper.isEmpty( getUserName() )
			&& StringHelper.isEmpty( getPassword() )
			&& StringHelper.isEmpty( getHost() )
			&& StringHelper.isEmpty( getConfigURL() );
	}


	// *****************************************************************************************************************
	// * Getter / Setter
	// *

	public String getUserName() {
		return getString(USER_NAME);
	}


	public void setUserName(String userName) {
		setValue(USER_NAME, userName);
	}


	public String getPassword() {
		return getString(PASSWORD);
	}


	public void setPassword(String password) {
		setValue(PASSWORD, password);
	}


	public String getHost() {
		return getString(HOST);
	}


	public void setHost(String host) {
		setValue(HOST, host);
	}


	public boolean isAutoLogin() {
		return getBoolean(AUTO_LOGIN);
	}


	public void setAutoLogin(boolean autoLogin) {
		setValue(AUTO_LOGIN, autoLogin);
	}


	public String getConfigURL() {
		return getString(CONFIG_URL);
	}


	public void setConfigURL(String configURL) {
		setValue(CONFIG_URL, configURL);
	}


	public boolean isDontAskForConfigURL() {
		return getBoolean(DONT_ASK_FOR_CONFIG_URL);
	}


	public void setDontAskForConfigURL(boolean dontAskForConfigURL) {
		setValue(DONT_ASK_FOR_CONFIG_URL, dontAskForConfigURL);
	}


	public String getProxyHost() {
		return getString(PROXY_HOST);
	}


	public void setProxyHost(String proxyHost) {
		setValue(PROXY_HOST, proxyHost);
	}


	public Integer getProxyPort() {
		return getInteger(PROXY_PORT);
	}


	public void setProxyPort(Integer proxyPort) {
		setValue(PROXY_PORT, proxyPort);
	}


	public String getProxyUser() {
		return getString(PROXY_USER);
	}


	public void setProxyUser(String proxyUser) {
		setValue(PROXY_USER, proxyUser);
	}


	public String getProxyPassword() {
		return getString(PROXY_PASSWORD);
	}


	public void setProxyPassword(String proxyPassword) {
		setValue(PROXY_PASSWORD, proxyPassword);
	}

	// *
	// * Getter / Setter
	// *****************************************************************************************************************

}
