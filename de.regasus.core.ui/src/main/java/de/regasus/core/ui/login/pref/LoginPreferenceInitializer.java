package de.regasus.core.ui.login.pref;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;

import com.lambdalogic.util.rcp.pref.PreferenceInitializerHelper;


public class LoginPreferenceInitializer extends AbstractPreferenceInitializer {

	@Override
	public void initializeDefaultPreferences() {
		PreferenceInitializerHelper.initializeDefaultPreferences(
			LoginPreference.getInstance().getPreferenceStore(),
			LoginPreferenceDefinition.class
		);
	}

}
