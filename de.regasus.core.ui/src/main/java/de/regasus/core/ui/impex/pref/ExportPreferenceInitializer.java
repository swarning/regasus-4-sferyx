package de.regasus.core.ui.impex.pref;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;

import com.lambdalogic.util.rcp.pref.PreferenceInitializerHelper;

public class ExportPreferenceInitializer extends AbstractPreferenceInitializer {

	@Override
	public void initializeDefaultPreferences() {
		PreferenceInitializerHelper.initializeDefaultPreferences(
			ExportPreference.getInstance().getPreferenceStore(),
			ExportPreferenceDefinition.class
		);
	}

}
