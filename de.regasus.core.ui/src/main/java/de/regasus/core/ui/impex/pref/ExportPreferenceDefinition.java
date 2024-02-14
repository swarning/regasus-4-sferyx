package de.regasus.core.ui.impex.pref;

import static com.lambdalogic.util.rcp.pref.PreferenceType.*;

import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.core.runtime.preferences.IScopeContext;

import com.lambdalogic.util.rcp.pref.PreferenceField;

public interface ExportPreferenceDefinition {

    /**
     * Define the scope of the Preference and therewith the place where they are stored.
     * Preferences with Configuration Scope are stored in the application's Configuration Area in the .settings directory.
     */
	IScopeContext SCOPE_CONTEXT = ConfigurationScope.INSTANCE;

	/**
	 * The qualifier defines the base name of the file in which preference values are stored.
	 * The final file name has the ending .prefs.
	 */
	String QUALIFIER = "export";


	// preference values
	PreferenceField DIR = new PreferenceField(QUALIFIER, "dir", STRING);
	PreferenceField INCLUDE_PHOTO = new PreferenceField(QUALIFIER, "includePhoto", BOOL);
	PreferenceField INCLUDE_PARTICIPANT_CORRESPONDENCE = new PreferenceField(QUALIFIER, "includeParticipantCorrespondence", BOOL);
	PreferenceField INCLUDE_PARTICIPANT_FILE = new PreferenceField(QUALIFIER, "includeParticipantFile", BOOL);

}
