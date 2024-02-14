package de.regasus.portal.portal.view.pref;

import static com.lambdalogic.util.rcp.pref.PreferenceType.BOOL;

import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.core.runtime.preferences.IScopeContext;

import com.lambdalogic.util.rcp.pref.PreferenceField;

public interface PortalViewPreferenceDefinition {

	/**
     * Define the scope of the Preference and therewith the place where they are stored.
     * Preferences with Configuration Scope are stored in the application's Configuration Area in the .settings directory.
     */
	IScopeContext SCOPE_CONTEXT = ConfigurationScope.INSTANCE;

	/**
	 * The qualifier defines the base name of the file in which preference values are stored.
	 * The final file name has the ending .prefs.
	 */
	String QUALIFIER = "portal.view";


	// preference values
	PreferenceField LINK_WITH_EDITOR = new PreferenceField(QUALIFIER, "linkWithEditor", BOOL);

}
