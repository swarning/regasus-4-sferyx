package de.regasus.email.template.search.view.pref;

import static com.lambdalogic.util.rcp.pref.PreferenceType.*;

import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.core.runtime.preferences.IScopeContext;

import com.lambdalogic.util.rcp.pref.PreferenceField;

public interface EmailTemplateSearchViewPreferenceDefinition {

    /**
     * Define the scope of the Preference and therewith the place where they are stored.
     * Preferences with Configuration Scope are stored in the application's Configuration Area in the .settings directory.
     */
	IScopeContext SCOPE_CONTEXT = ConfigurationScope.INSTANCE;

	/**
	 * The qualifier defines the base name of the file in which preference values are stored.
	 * The final file name has the ending .prefs.
	 */
	String QUALIFIER = "email.template.search.view";


	// preference values
	PreferenceField EVENT_ID = new PreferenceField(QUALIFIER, "eventId", LONG);
	PreferenceField EVENT_FILTER = new PreferenceField(QUALIFIER, "eventFilter", STRING);
	PreferenceField SAMPLE_PERSON_ID = new PreferenceField(QUALIFIER, "samplePersonId", LONG);
	PreferenceField SAMPLE_PERSON_NAME = new PreferenceField(QUALIFIER, "samplePersonName", STRING);

}
