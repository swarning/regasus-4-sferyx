package de.regasus.core.ui.openoffice.pref;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;

import com.lambdalogic.util.rcp.pref.PreferenceInitializerHelper;

public class OpenOfficePreferenceInitializer extends AbstractPreferenceInitializer {

	@Override
	public void initializeDefaultPreferences() {
		PreferenceInitializerHelper.initializeDefaultPreferences(
			OpenOfficePreference.getInstance().getPreferenceStore(),
			OpenOfficePreferenceDefinition.class
		);
	}

}
