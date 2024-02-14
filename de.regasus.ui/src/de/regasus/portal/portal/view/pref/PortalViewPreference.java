package de.regasus.portal.portal.view.pref;

import static de.regasus.portal.portal.view.pref.PortalViewPreferenceDefinition.*;

import java.io.IOException;

import org.eclipse.ui.preferences.ScopedPreferenceStore;

import com.lambdalogic.util.rcp.pref.AbstractPreference;
import com.lambdalogic.util.rcp.pref.PreferenceHelper;
import com.lambdalogic.util.rcp.pref.PreferenceInitializerHelper;

public class PortalViewPreference extends AbstractPreference {

	private static final PortalViewPreference INSTANCE = new PortalViewPreference();

	private static final ScopedPreferenceStore PREFERENCE_STORE = new ScopedPreferenceStore(
		SCOPE_CONTEXT,
    	QUALIFIER
	);


	public static PortalViewPreference getInstance() {
		return INSTANCE;
	}


	private PortalViewPreference() {
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
		PreferenceInitializerHelper.initializePreferences(getPreferenceStore(), PortalViewPreferenceDefinition.class);
	}


	// *****************************************************************************************************************
	// * Getter / Setter
	// *

	public boolean isLinkWithEditor() {
		return getBoolean(LINK_WITH_EDITOR);
	}


	public void setLinkWithEditor(boolean linkWithEditor) {
		setValue(LINK_WITH_EDITOR, linkWithEditor);
	}

	// *
	// * Getter / Setter
	// *****************************************************************************************************************

}
