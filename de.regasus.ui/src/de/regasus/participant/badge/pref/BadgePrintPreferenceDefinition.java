package de.regasus.participant.badge.pref;

import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.core.runtime.preferences.IScopeContext;

import com.lambdalogic.util.rcp.pref.PreferenceField;
import com.lambdalogic.util.rcp.pref.PreferenceType;

import de.regasus.I18N;

public interface BadgePrintPreferenceDefinition {

    /**
     * Define the scope of the Preference and therewith the place where they are stored.
     * Preferences with Configuration Scope are stored in the application's Configuration Area in the .settings directory.
     */
	IScopeContext SCOPE_CONTEXT = ConfigurationScope.INSTANCE;

	/**
	 * The qualifier defines the base name of the file in which preference values are stored.
	 * The final file name has the ending .prefs.
	 */
	String QUALIFIER = "badge.print";


	// preference values
	PreferenceField WAIT_TIME = new PreferenceField(QUALIFIER, "waitTime", PreferenceType.INTEGER)
		.label(I18N.BadgePrintPreferencePage_BadgePrintWaitTime)
		.defaultValue(0);

}
