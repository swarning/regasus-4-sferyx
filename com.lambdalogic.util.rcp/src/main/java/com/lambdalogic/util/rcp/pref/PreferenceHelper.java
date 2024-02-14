package com.lambdalogic.util.rcp.pref;

import java.lang.reflect.Field;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.osgi.service.prefs.Preferences;


public class PreferenceHelper {

	public static String extractLocation(ScopedPreferenceStore scopedPreferenceStore) {
		String location = "";

		IEclipsePreferences[] preferenceNodes = scopedPreferenceStore.getPreferenceNodes(false);
		if (preferenceNodes != null && preferenceNodes.length > 0) {
			location = extractLocation(preferenceNodes[0]);
		}

		return location;
	}


	public static String extractLocation(Preferences preferences) {
    	String location = "";

    	try {
    		Field locationField = preferences.getClass().getDeclaredField("location");
    		if (location != null) {
        		locationField.setAccessible(true);
        		Object locationObj = locationField.get(preferences);
        		if (locationObj != null) {
        			location = locationObj.toString();
        		}
    		}
    	}
    	catch (Throwable t) {
    		com.lambdalogic.util.rcp.error.ErrorHandler.logError(t);
    	}

    	return location;
    }

}
