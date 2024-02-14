package de.regasus.participant.view.pref;

import static de.regasus.participant.view.pref.ParticipantTreeViewPreferenceDefinition.*;

import java.io.IOException;

import org.eclipse.ui.preferences.ScopedPreferenceStore;

import com.lambdalogic.util.rcp.pref.AbstractPreference;
import com.lambdalogic.util.rcp.pref.PreferenceHelper;
import com.lambdalogic.util.rcp.pref.PreferenceInitializerHelper;

public class ParticipantTreeViewPreference extends AbstractPreference {

	private static final ParticipantTreeViewPreference INSTANCE = new ParticipantTreeViewPreference();

	private static final ScopedPreferenceStore PREFERENCE_STORE = new ScopedPreferenceStore(
		SCOPE_CONTEXT,
    	QUALIFIER
	);


	public static ParticipantTreeViewPreference getInstance() {
		return INSTANCE;
	}


	private ParticipantTreeViewPreference() {
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
		PreferenceInitializerHelper.initializePreferences(getPreferenceStore(), ParticipantTreeViewPreferenceDefinition.class);
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
