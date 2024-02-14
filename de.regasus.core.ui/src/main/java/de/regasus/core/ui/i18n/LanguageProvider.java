package de.regasus.core.ui.i18n;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.lambdalogic.i18n.ILanguageProvider;
import com.lambdalogic.i18n.Language;
import com.lambdalogic.i18n.Language_Code_Comparator;
import com.lambdalogic.i18n.LanguageString;
import com.lambdalogic.util.CollectionsHelper;
import com.lambdalogic.util.MapHelper;
import com.lambdalogic.util.StringHelper;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;

import de.regasus.core.CorePropertyKey;
import de.regasus.core.LanguageModel;
import de.regasus.core.PropertyModel;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.Activator;

/**
 * Implementation of <code>ILanguageProvider</code> which gets its data form the LanguageModel.
 *
 * Provides a list of currently relevant languages by its ISO code.
 */
public class LanguageProvider implements ILanguageProvider {

	/*
	 * ==========================================================================================================
	 * Infrastructure for Singleton Pattern
	 */

	private static ILanguageProvider instance;


	public static ILanguageProvider getInstance() {
		if (instance == null) {
			instance = new LanguageProvider();
		}
		return instance;
	}

	/*
	 * ==========================================================================================================
	 * Attributes
	 */

	private List<Language> languageList;
	private Map<String, Language> languageMap;
	private List<Language> defaultLanguageList;


	/*
	 * ========================================================================================================== Public
	 * instance methods
	 */


	/**
	 * private-constructor, so everyone must use getInstance()
	 */
	private LanguageProvider() {
	}


	private void initLanguageData() {
		if (languageList == null) {
			refreshLanguageData();

			LanguageModel.getInstance().addListener(new CacheModelListener<String>() {
				@Override
				public void dataChange(CacheModelEvent<String> event) {
					refreshLanguageData();
				}
			});
		}
	}


	private void refreshLanguageData() {
		try {
			/* We don't register/deregister, because this class is a
			 * singleton and we don't know if it is not used anymore.
			 */
			Collection<de.regasus.common.Language> languages = LanguageModel.getInstance().getAllUndeletedLanguages();

			if (languageList == null) {
				languageList = new ArrayList<>( languages.size() );
				languageMap = MapHelper.createHashMap( languages.size() );
			}
			else {
				languageList.clear();
				languageMap.clear();
			}

			for (de.regasus.common.Language languageEntity : languages) {
				String languageId = languageEntity.getId();
				LanguageString languageNameLS = languageEntity.getName();

				Language language = new Language(languageId, languageNameLS);
				languageList.add(language);

				// Add reverse mapping
				languageMap.put(languageId, language);
			}

			// Sort the list
			Collections.sort(languageList, Language_Code_Comparator.getInstance());
		}
		catch (Exception e) {
			System.err.println("Exception when getting language list!");
			throw new IllegalStateException("Could not load language list: ", e);
		}
	}


	@Override
	public List<Language> getLanguageList() {
		initLanguageData();
		return languageList;
	}


	private Map<String, Language> getLanguageMap() {
		initLanguageData();
		return languageMap;
	}


	@Override
	public Language getDefaultLanguage() {
		Language defaultLanguage = null;

		List<Language> defaultLanguageList = getDefaultLanguageList();
		if (defaultLanguageList != null && !defaultLanguageList.isEmpty()) {
			defaultLanguage = defaultLanguageList.get(0);
		}

		return defaultLanguage;
	}


	@Override
	public List<Language> getDefaultLanguageList() {
		if (defaultLanguageList == null) {
			refreshDefaultLanguageList();

			PropertyModel.getInstance().addListener(
				new CacheModelListener<String>() {
					@Override
					public void dataChange(CacheModelEvent<String> event) {
						refreshDefaultLanguageList();
					}
				},
				CorePropertyKey.DEFAULT_LANGUAGES // key
			);
		}
		return defaultLanguageList;
	}


	@Override
	public List<String> getDefaultLanguagePKList() {

		getDefaultLanguageList();

		if (CollectionsHelper.empty(defaultLanguageList)) {
			return CollectionsHelper.emptyList();
		}

		List<String> languagePKList = new ArrayList<>(defaultLanguageList.size());
		for (Language language : defaultLanguageList) {
			languagePKList.add(language.getLanguageCode());
		}
		return languagePKList;
	}


	private void refreshDefaultLanguageList() {
		try {
			if (defaultLanguageList == null) {
				defaultLanguageList = new ArrayList<>();
			}
			else {
				defaultLanguageList.clear();
			}


			// get default languages from table PROPERTY as comma separated String
			String langCodes = PropertyModel.getInstance().getDefaultLanguages();

			// alternatively get system default language
			if (StringHelper.isEmpty(langCodes)) {
				langCodes = Locale.getDefault().getLanguage();
			}


			// convert comma separated String into List<Language>
			List<String> defaultLanguageCodeList = StringHelper.getSegments(langCodes);
			for (String languageCode : defaultLanguageCodeList) {
				Language language = getLanguageByCode(languageCode);
				if (language != null) {
					defaultLanguageList.add(language);
				}
				else {
					System.err.println("Invalid language code in table PROPERTY (key: "
						+ CorePropertyKey.DEFAULT_LANGUAGES + ")");
				}
			}

			// if the language codes from table PROPERTY are not available
			if (defaultLanguageList.isEmpty()) {
				String defaultLanguagePK = Locale.getDefault().getLanguage();
				Language language = getLanguageByCode(defaultLanguagePK);
				if (language == null) {
					System.err.println("System default language code '" + defaultLanguagePK + "' not available.");
					// create Language with language code as name
					language = new Language(defaultLanguagePK, new LanguageString(defaultLanguagePK, defaultLanguagePK));
				}
				defaultLanguageList.add(language);
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	@Override
	public Language getLanguageByCode(String languageCode) {
		return getLanguageMap().get(languageCode);
	}

}
