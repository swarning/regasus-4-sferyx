package de.regasus.email.dispatch.pref;

import static com.lambdalogic.util.rcp.pref.PreferenceType.RADIO;

import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.core.runtime.preferences.IScopeContext;

import com.lambdalogic.util.rcp.pref.PreferenceField;

import de.regasus.email.EmailI18N;
import de.regasus.email.dispatch.DispatchMode;

public interface EmailDispatchPreferenceDefinition {

    /**
     * Define the scope of the Preference and therewith the place where they are stored.
     * Preferences with Configuration Scope are stored in the application's Configuration Area in the .settings directory.
     */
	IScopeContext SCOPE_CONTEXT = ConfigurationScope.INSTANCE;

	/**
	 * The qualifier defines the base name of the file in which preference values are stored.
	 * The final file name has the ending .prefs.
	 */
	String QUALIFIER = "email.dispatch";


	// preference values
	PreferenceField DISPATCH_MODE = new PreferenceField(QUALIFIER, "dispatchMode", RADIO)
		.label(EmailI18N.DispatchModePreferencePage_Mode)
		.labelsAndValues(
			new String[][] {
				{EmailI18N.DispatchMode_IMMEDIATE_CLIENT, DispatchMode.IMMEDIATE_CLIENT.name()},
				{EmailI18N.DispatchMode_IMMEDIATE_SERVER, DispatchMode.IMMEDIATE_SERVER.name()},
				{EmailI18N.DispatchMode_SCHEDULED_SERVER, DispatchMode.SCHEDULED_SERVER.name()}
			}
		)
		.defaultValue( DispatchMode.IMMEDIATE_SERVER.name() );

}
