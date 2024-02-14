package de.regasus.core.ui.impex.pref;


import static de.regasus.core.ui.impex.pref.ExportPreferenceDefinition.*;

import java.io.IOException;

import org.eclipse.ui.preferences.ScopedPreferenceStore;

import com.lambdalogic.util.rcp.pref.AbstractPreference;
import com.lambdalogic.util.rcp.pref.PreferenceHelper;
import com.lambdalogic.util.rcp.pref.PreferenceInitializerHelper;


public class ExportPreference extends AbstractPreference {

	private static final ExportPreference INSTANCE = new ExportPreference();

	private static final ScopedPreferenceStore PREFERENCE_STORE = new ScopedPreferenceStore(
    	SCOPE_CONTEXT,
    	QUALIFIER
	);


	public static ExportPreference getInstance() {
		return INSTANCE;
	}


	private ExportPreference() {
	}


	@Override
	public ScopedPreferenceStore getPreferenceStore() {
		return PREFERENCE_STORE;
	}


	public void save() {
		System.out.println("Save " + QUALIFIER  + " preferences to: " + PreferenceHelper.extractLocation( getPreferenceStore() ));

		try {
			getPreferenceStore().save();
		}
		catch (IOException e) {
			com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
		}
	}


	public void initialize() {
		PreferenceInitializerHelper.initializePreferences(getPreferenceStore(), ExportPreferenceDefinition.class);
	}


	// *****************************************************************************************************************
	// * Getter / Setter
	// *

	public String getDir() {
		return getString(DIR);
	}


	public void setDir(String dir) {
		setValue(DIR, dir);
	}


	public boolean isIncludePhoto() {
		return getBoolean(INCLUDE_PHOTO);
	}


	public void setIncludePhoto(boolean includePhoto) {
		setValue(INCLUDE_PHOTO, includePhoto);
	}


	public boolean isIncludeParticipantCorrespondence() {
		return getBoolean(INCLUDE_PARTICIPANT_CORRESPONDENCE);
	}


	public void setIncludeParticipantCorrespondence(boolean includeParticipantCorrespondence) {
		setValue(INCLUDE_PARTICIPANT_CORRESPONDENCE, includeParticipantCorrespondence);
	}


	public boolean isIncludeParticipantFile() {
		return getBoolean(INCLUDE_PARTICIPANT_FILE);
	}


	public void setIncludeParticipantFile(boolean includeParticipantFile) {
		setValue(INCLUDE_PARTICIPANT_FILE, includeParticipantFile);
	}

	// *
	// * Getter / Setter
	// *****************************************************************************************************************

}
