package com.lambdalogic.util.rcp.html.pref;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;

import com.lambdalogic.util.rcp.pref.PreferenceInitializerHelper;


public class HtmlPreferenceInitializer extends AbstractPreferenceInitializer {

	@Override
	public void initializeDefaultPreferences() {
		PreferenceInitializerHelper.initializeDefaultPreferences(
			HtmlPreference.getInstance().getPreferenceStore(),
			HtmlPreferenceDefinition.class
		);
	}

}
