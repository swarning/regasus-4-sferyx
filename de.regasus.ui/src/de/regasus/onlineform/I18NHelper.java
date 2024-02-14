package de.regasus.onlineform;

import java.util.ArrayList;
import java.util.List;

import com.lambdalogic.messeinfo.regasus.RegistrationFormConfig;

public class I18NHelper {

	
	
	public static List<String>  getAvailableLanguages(RegistrationFormConfig config) {
		ArrayList<String> languages = new ArrayList<String>(2);
		
		if (config.isGermanAvailable() ) {
			languages.add("de");
		}
		if (config.isEnglishAvailable()) {
			languages.add("en");
		}
		
		if (languages.size() == 0) {
			languages.add("de");
		}
		
		return languages;
	}

	public static String[] getAvailableLanguagesArray(RegistrationFormConfig config) {
		List<String> languages = getAvailableLanguages(config);
		return languages.toArray(new String[languages.size()]);
	}
}
