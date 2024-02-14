package com.lambdalogic.util.rcp.html.pref;


import static com.lambdalogic.util.StringHelper.isNotEmpty;
import static com.lambdalogic.util.rcp.html.pref.HtmlPreferenceDefinition.*;

import java.io.IOException;

import org.eclipse.ui.preferences.ScopedPreferenceStore;

import com.lambdalogic.util.rcp.pref.AbstractPreference;
import com.lambdalogic.util.rcp.pref.PreferenceHelper;
import com.lambdalogic.util.rcp.pref.PreferenceInitializerHelper;


public class HtmlPreference extends AbstractPreference {

	private static final HtmlPreference INSTANCE = new HtmlPreference();

	private static final ScopedPreferenceStore PREFERENCE_STORE = new ScopedPreferenceStore(
		SCOPE_CONTEXT,
    	QUALIFIER
	);


	public static HtmlPreference getInstance() {
		return INSTANCE;
	}


	private HtmlPreference() {
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
		PreferenceInitializerHelper.initializePreferences(getPreferenceStore(), HtmlPreferenceDefinition.class);
	}


	// *****************************************************************************************************************
	// * Getter / Setter
	// *

	public Browser getBrowser() {
		String strValue = getString(BROWSER);
		if ( isNotEmpty(strValue) ) {
			return Browser.valueOf(strValue);
		}
		return null;
	}

	// *
	// * Getter / Setter
	// *****************************************************************************************************************

}
