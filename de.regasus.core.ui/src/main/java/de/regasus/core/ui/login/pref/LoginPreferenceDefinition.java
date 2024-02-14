package de.regasus.core.ui.login.pref;

import static com.lambdalogic.util.rcp.pref.PreferenceType.*;

import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.core.runtime.preferences.IScopeContext;

import com.lambdalogic.util.rcp.pref.PreferenceField;
import com.lambdalogic.util.rcp.pref.PreferenceType;

import de.regasus.core.ui.CoreI18N;

public interface LoginPreferenceDefinition {

    /**
     * Define the scope of the Preference and therewith the place where they are stored.
     * Preferences with Configuration Scope are stored in the application's Configuration Area in the .settings directory.
     */
	IScopeContext SCOPE_CONTEXT = ConfigurationScope.INSTANCE;

	/**
	 * The qualifier defines the base name of the file in which preference values are stored.
	 * The final file name has the ending .prefs.
	 */
	String QUALIFIER = "login";


	// preference values
	PreferenceField USER_NAME = new PreferenceField(QUALIFIER, "userName", STRING)
		.label(CoreI18N.LoginPreferencePage_UserName);

	PreferenceField PASSWORD = new PreferenceField(QUALIFIER, "password", PreferenceType.PASSWORD)
		.label(CoreI18N.LoginPreferencePage_Password);

	PreferenceField HOST = new PreferenceField(QUALIFIER, "host", STRING)
		.label(CoreI18N.LoginPreferencePage_Host);

	PreferenceField AUTO_LOGIN = new PreferenceField(QUALIFIER, "autoLogin", BOOL)
		.label(CoreI18N.LoginPreferencePage_AutoLogin);

	PreferenceField CONFIG_URL = new PreferenceField(QUALIFIER, "configURL", STRING)
		.label(CoreI18N.LoginPreferencePage_ConfigURL);

	PreferenceField DONT_ASK_FOR_CONFIG_URL = new PreferenceField(QUALIFIER, "dontAskForConfigURL", BOOL)
		.label(CoreI18N.LoginPreferencePage_DontAskForConfigURL);

	PreferenceField PROXY_HOST = new PreferenceField(QUALIFIER, "proxyHost", STRING)
		.label(CoreI18N.ProxyPreferencePage_ProxyHost);

	PreferenceField PROXY_PORT = new PreferenceField(QUALIFIER, "proxyPort", INTEGER)
		.label(CoreI18N.ProxyPreferencePage_ProxyPort);

	PreferenceField PROXY_USER = new PreferenceField(QUALIFIER, "proxyUser", STRING)
		.label(CoreI18N.ProxyPreferencePage_ProxyUser);

	PreferenceField PROXY_PASSWORD = new PreferenceField(QUALIFIER, "proxyPassword", PreferenceType.PASSWORD)
		.label(CoreI18N.ProxyPreferencePage_ProxyPassword);

}
