package de.regasus.participant.badge.pref;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;

import com.lambdalogic.util.rcp.pref.PreferenceInitializerHelper;

public class BadgePrintPreferenceInitializer extends AbstractPreferenceInitializer {

	@Override
	public void initializeDefaultPreferences() {
		PreferenceInitializerHelper.initializeDefaultPreferences(
			BadgePrintPreference.getInstance().getPreferenceStore(),
			BadgePrintPreferenceDefinition.class
		);
	}

}
