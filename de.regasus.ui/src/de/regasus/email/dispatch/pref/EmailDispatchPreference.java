package de.regasus.email.dispatch.pref;

import static de.regasus.email.dispatch.pref.EmailDispatchPreferenceDefinition.*;

import java.io.IOException;

import org.eclipse.ui.preferences.ScopedPreferenceStore;

import com.lambdalogic.util.rcp.pref.AbstractPreference;
import com.lambdalogic.util.rcp.pref.PreferenceHelper;
import com.lambdalogic.util.rcp.pref.PreferenceInitializerHelper;

import de.regasus.email.dispatch.DispatchMode;


public class EmailDispatchPreference extends AbstractPreference {

	private static final EmailDispatchPreference INSTANCE = new EmailDispatchPreference();

	private static final ScopedPreferenceStore PREFERENCE_STORE = new ScopedPreferenceStore(
		SCOPE_CONTEXT,
    	QUALIFIER
	);


	public static EmailDispatchPreference getInstance() {
		return INSTANCE;
	}


	private EmailDispatchPreference() {
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
		PreferenceInitializerHelper.initializePreferences(getPreferenceStore(), EmailDispatchPreferenceDefinition.class);
	}


	// *****************************************************************************************************************
	// * Getter / Setter
	// *

	public DispatchMode getDispatchMode() {
		try {
			String strValue = getString(DISPATCH_MODE);
			DispatchMode dispatchMode = DispatchMode.valueOf(strValue);
			return dispatchMode;
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}


	public void setDispatchMode(DispatchMode dispatchMode) {
		String strValue = dispatchMode != null ? dispatchMode.name() : null;
		setValue(DISPATCH_MODE, strValue);
	}

	// *
	// * Getter / Setter
	// *****************************************************************************************************************

}
