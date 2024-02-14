package com.lambdalogic.util.rcp.html.pref;

import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.core.runtime.preferences.IScopeContext;

import com.lambdalogic.util.rcp.UtilI18N;
import com.lambdalogic.util.rcp.pref.PreferenceField;
import com.lambdalogic.util.rcp.pref.PreferenceType;


public interface HtmlPreferenceDefinition {

    /**
     * Define the scope of the Preference and therewith the place where they are stored.
     * Preferences with Configuration Scope are stored in the application's Configuration Area in the .settings directory.
     */
	IScopeContext SCOPE_CONTEXT = ConfigurationScope.INSTANCE;

	/**
	 * The qualifier defines the base name of the file in which preference values are stored.
	 * The final file name has the ending .prefs.
	 */
	String QUALIFIER = "html";


	// preference values
	PreferenceField BROWSER = new PreferenceField(QUALIFIER, "browser", PreferenceType.RADIO)
		.label(UtilI18N.Browser)
		.labelsAndValues(
			new String[][] {
				{UtilI18N.Default, Browser.DEFAULT.name()},
				{UtilI18N.WebKit,  Browser.WEBKIT.name()}
			}
		)
		.defaultValue( Browser.DEFAULT.name() );

}
