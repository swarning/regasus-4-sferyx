package de.regasus.email.template.search.view.pref;


import static de.regasus.email.template.search.view.pref.EmailTemplateSearchViewPreferenceDefinition.*;

import java.io.IOException;

import org.eclipse.ui.preferences.ScopedPreferenceStore;

import com.lambdalogic.util.rcp.pref.AbstractPreference;
import com.lambdalogic.util.rcp.pref.PreferenceHelper;
import com.lambdalogic.util.rcp.pref.PreferenceInitializerHelper;


public class EmailTemplateSearchViewPreference extends AbstractPreference {

	private static final EmailTemplateSearchViewPreference INSTANCE = new EmailTemplateSearchViewPreference();

	private static final ScopedPreferenceStore PREFERENCE_STORE = new ScopedPreferenceStore(
		SCOPE_CONTEXT,
    	QUALIFIER
	);


	public static EmailTemplateSearchViewPreference getInstance() {
		return INSTANCE;
	}


	private EmailTemplateSearchViewPreference() {
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
		PreferenceInitializerHelper.initializePreferences(getPreferenceStore(), EmailTemplateSearchViewPreferenceDefinition.class);
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


	public Long getSamplePersonId() {
		return getLong(SAMPLE_PERSON_ID);
	}


	public void setSamplePersonId(Long samplePersonId) {
		setValue(SAMPLE_PERSON_ID, samplePersonId);
	}


	public String getSamplePersonName() {
		return getString(SAMPLE_PERSON_NAME);
	}


	public void setSamplePersonName(String samplePersonName) {
		setValue(SAMPLE_PERSON_NAME, samplePersonName);
	}

	// *
	// * Getter / Setter
	// *****************************************************************************************************************

}
