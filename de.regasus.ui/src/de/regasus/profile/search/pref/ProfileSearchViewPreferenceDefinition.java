package de.regasus.profile.search.pref;

import static com.lambdalogic.util.rcp.pref.PreferenceType.*;

import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.core.runtime.preferences.IScopeContext;

import com.lambdalogic.util.rcp.pref.PreferenceField;

public interface ProfileSearchViewPreferenceDefinition {

    /**
     * Define the scope of the Preference and therewith the place where they are stored.
     * Preferences with Configuration Scope are stored in the application's Configuration Area in the .settings directory.
     */
	IScopeContext SCOPE_CONTEXT = ConfigurationScope.INSTANCE;

	/**
	 * The qualifier defines the base name of the file in which preference values are stored.
	 * The final file name has the ending .prefs.
	 */
	String QUALIFIER = "profile.search.view";


	// preference values
	PreferenceField SEARCH_FIELDS = new PreferenceField(QUALIFIER, "searchFields", STRING);
	PreferenceField COLUMN_ORDER = new PreferenceField(QUALIFIER, "columnOrder", INTEGER_LIST);
	PreferenceField COLUMN_WIDTHS = new PreferenceField(QUALIFIER, "columnWidths", INTEGER_LIST);
	PreferenceField RESULT_COUNT_CHECKBOX = new PreferenceField(QUALIFIER, "resultCountCheckbox", BOOL);
	PreferenceField RESULT_COUNT = new PreferenceField(QUALIFIER, "resultCount", INTEGER);

}
