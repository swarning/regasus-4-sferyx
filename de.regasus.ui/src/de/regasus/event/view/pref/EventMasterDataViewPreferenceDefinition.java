package de.regasus.event.view.pref;

import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.core.runtime.preferences.IScopeContext;

import com.lambdalogic.util.rcp.pref.PreferenceField;
import com.lambdalogic.util.rcp.pref.PreferenceType;

public interface EventMasterDataViewPreferenceDefinition {

    /**
     * Define the scope of the Preference and therewith the place where they are stored.
     * Preferences with Configuration Scope are stored in the application's Configuration Area in the .settings directory.
     */
	IScopeContext SCOPE_CONTEXT = ConfigurationScope.INSTANCE;

	/**
	 * The qualifier defines the base name of the file in which preference values are stored.
	 * The final file name has the ending .prefs.
	 */
	String QUALIFIER = "event.masterdata.view";


	// preference values
	PreferenceField EVENT_FILTER = new PreferenceField(QUALIFIER, "eventFilter", PreferenceType.STRING);
	PreferenceField LINK_WITH_EDITOR = new PreferenceField(QUALIFIER, "linkWithEditor", PreferenceType.BOOL);

}
