package de.regasus.view.pref;

import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.core.runtime.preferences.IScopeContext;

import com.lambdalogic.util.rcp.pref.PreferenceField;
import com.lambdalogic.util.rcp.pref.PreferenceType;

public interface GroovyConsoleViewPreferenceDefinition {

	/**
     * Define the scope of the Preference and therewith the place where they are stored.
     * Preferences with Configuration Scope are stored in the application's Configuration Area in the .settings directory.
     */
	IScopeContext SCOPE_CONTEXT = ConfigurationScope.INSTANCE;

	/**
	 * The qualifier defines the base name of the file in which preference values are stored.
	 * The final file name has the ending .prefs.
	 */
	String QUALIFIER = "groovy.console.view";


	// preference values
	PreferenceField ID = new PreferenceField(QUALIFIER, "id", PreferenceType.STRING);
	PreferenceField SCRIPT = new PreferenceField(QUALIFIER, "script", PreferenceType.STRING);
	PreferenceField VARIABLES = new PreferenceField(QUALIFIER, "variables", PreferenceType.STRING);

}
