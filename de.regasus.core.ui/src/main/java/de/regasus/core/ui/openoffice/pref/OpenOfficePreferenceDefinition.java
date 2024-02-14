package de.regasus.core.ui.openoffice.pref;

import static com.lambdalogic.util.rcp.pref.PreferenceType.DIRECTORY;

import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.core.runtime.preferences.IScopeContext;

import com.lambdalogic.util.rcp.pref.PreferenceField;

import de.regasus.core.ui.CoreI18N;

public interface OpenOfficePreferenceDefinition {

    /**
     * Define the scope of the Preference and therewith the place where they are stored.
     * Preferences with Configuration Scope are stored in the application's Configuration Area in the .settings directory.
     */
	IScopeContext SCOPE_CONTEXT = ConfigurationScope.INSTANCE;

	/**
	 * The qualifier defines the base name of the file in which preference values are stored.
	 * The final file name has the ending .prefs.
	 */
	String QUALIFIER = "openoffice";


	// preference values
	PreferenceField PATH = new PreferenceField(QUALIFIER, "path", DIRECTORY)
		.label(CoreI18N.OpenOfficePreferencePage_Path);

}
