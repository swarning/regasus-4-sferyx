package de.regasus.core.ui.openoffice.pref;

import static de.regasus.core.ui.openoffice.pref.OpenOfficePreferenceDefinition.*;

import java.io.IOException;

import org.eclipse.ui.preferences.ScopedPreferenceStore;

import com.lambdalogic.util.FileHelper;
import com.lambdalogic.util.rcp.pref.AbstractPreference;
import com.lambdalogic.util.rcp.pref.PreferenceHelper;
import com.lambdalogic.util.rcp.pref.PreferenceInitializerHelper;

public class OpenOfficePreference extends AbstractPreference {

	private static final OpenOfficePreference INSTANCE = new OpenOfficePreference();

	private final ScopedPreferenceStore PREFERENCE_STORE = new ScopedPreferenceStore(
    	SCOPE_CONTEXT,
    	QUALIFIER
	);


	public static OpenOfficePreference getInstance() {
		return INSTANCE;
	}


	public void initFileHelper() {
		String openOfficePath = getPath();
		FileHelper.setOpenOfficePath(openOfficePath);
	}


	private OpenOfficePreference() {
		initFileHelper();
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
		PreferenceInitializerHelper.initializePreferences(getPreferenceStore(), OpenOfficePreferenceDefinition.class);
	}


	// *****************************************************************************************************************
	// * Getter / Setter
	// *

	public String getPath() {
		return getString(PATH);
	}


	public void setPath(String path) {
		setValue(PATH, path);
	}

	// *
	// * Getter / Setter
	// *****************************************************************************************************************

}
