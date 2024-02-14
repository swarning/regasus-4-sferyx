package de.regasus.event.view.pref;

import static de.regasus.event.view.pref.EventMasterDataViewPreferenceDefinition.*;

import java.io.IOException;

import org.eclipse.ui.preferences.ScopedPreferenceStore;

import com.lambdalogic.util.rcp.pref.AbstractPreference;
import com.lambdalogic.util.rcp.pref.PreferenceHelper;
import com.lambdalogic.util.rcp.pref.PreferenceInitializerHelper;


public class EventMasterDataViewPreference extends AbstractPreference {

	private static final EventMasterDataViewPreference INSTANCE = new EventMasterDataViewPreference();

	private static final ScopedPreferenceStore PREFERENCE_STORE = new ScopedPreferenceStore(
		SCOPE_CONTEXT,
    	QUALIFIER
	);


	public static EventMasterDataViewPreference getInstance() {
		return INSTANCE;
	}


	private EventMasterDataViewPreference() {
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
		PreferenceInitializerHelper.initializePreferences(getPreferenceStore(), EventMasterDataViewPreferenceDefinition.class);
	}


	// *****************************************************************************************************************
	// * Getter / Setter
	// *

	public String getEventFilter() {
		return getString(EVENT_FILTER);
	}


	public void setEventFilter(String eventFilter) {
		setValue(EVENT_FILTER, eventFilter);
	}


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
