package de.regasus.email.dispatch.pref;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;

import com.lambdalogic.util.rcp.pref.PreferenceInitializerHelper;

public class EmailDispatchPreferenceInitializer extends AbstractPreferenceInitializer {

	@Override
	public void initializeDefaultPreferences() {
		PreferenceInitializerHelper.initializeDefaultPreferences(
			EmailDispatchPreference.getInstance().getPreferenceStore(),
			EmailDispatchPreferenceDefinition.class
		);
	}

}
