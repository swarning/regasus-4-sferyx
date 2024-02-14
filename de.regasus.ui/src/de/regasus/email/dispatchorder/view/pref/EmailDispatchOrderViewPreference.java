package de.regasus.email.dispatchorder.view.pref;

import static de.regasus.email.dispatchorder.view.pref.EmailDispatchOrderViewPreferenceDefinition.*;

import java.io.IOException;

import org.eclipse.ui.preferences.ScopedPreferenceStore;

import com.lambdalogic.util.rcp.pref.AbstractPreference;
import com.lambdalogic.util.rcp.pref.PreferenceHelper;
import com.lambdalogic.util.rcp.pref.PreferenceInitializerHelper;

public class EmailDispatchOrderViewPreference extends AbstractPreference {

	private static final EmailDispatchOrderViewPreference INSTANCE = new EmailDispatchOrderViewPreference();

	private static final ScopedPreferenceStore PREFERENCE_STORE = new ScopedPreferenceStore(
		SCOPE_CONTEXT,
    	QUALIFIER
	);


	public static EmailDispatchOrderViewPreference getInstance() {
		return INSTANCE;
	}


	private EmailDispatchOrderViewPreference() {
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
		PreferenceInitializerHelper.initializePreferences(getPreferenceStore(), EmailDispatchOrderViewPreferenceDefinition.class);
	}


	// *****************************************************************************************************************
	// * Getter / Setter
	// *

	public Long getEventId() {
		return getLong(EVENT_ID);
	}


	public void setEventId(Long eventId) {
		setValue(EVENT_ID, eventId);
	}


	public String getEventFilter() {
		return getString(EVENT_FILTER);
	}


	public void setEventFilter(String eventFilter) {
		setValue(EVENT_FILTER, eventFilter);
	}

	// *
	// * Getter / Setter
	// *****************************************************************************************************************

}
