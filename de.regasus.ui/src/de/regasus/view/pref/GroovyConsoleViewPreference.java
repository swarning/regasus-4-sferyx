package de.regasus.view.pref;

import static de.regasus.view.pref.GroovyConsoleViewPreferenceDefinition.*;

import java.io.IOException;

import org.eclipse.ui.preferences.ScopedPreferenceStore;

import com.lambdalogic.util.rcp.pref.AbstractPreference;
import com.lambdalogic.util.rcp.pref.PreferenceHelper;
import com.lambdalogic.util.rcp.pref.PreferenceInitializerHelper;

public class GroovyConsoleViewPreference extends AbstractPreference {

	private static final GroovyConsoleViewPreference INSTANCE = new GroovyConsoleViewPreference();

	private static final ScopedPreferenceStore PREFERENCE_STORE = new ScopedPreferenceStore(
		SCOPE_CONTEXT,
    	QUALIFIER
	);


	public static GroovyConsoleViewPreference getInstance() {
		return INSTANCE;
	}


	private GroovyConsoleViewPreference() {
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
		PreferenceInitializerHelper.initializePreferences(getPreferenceStore(), GroovyConsoleViewPreferenceDefinition.class);
	}


	// *****************************************************************************************************************
	// * Getter / Setter
	// *

	public String getId() {
		return getString(ID);
	}


	public void setId(String id) {
		setValue(ID, id);
	}


	public String getScript() {
		return getString(SCRIPT);
	}


	public void setScript(String script) {
		setValue(SCRIPT, script);
	}


	public String getVariables() {
		return getString(VARIABLES);
	}


	public void setVariables(String variables) {
		setValue(VARIABLES, variables);
	}

	// *
	// * Getter / Setter
	// *****************************************************************************************************************

}
