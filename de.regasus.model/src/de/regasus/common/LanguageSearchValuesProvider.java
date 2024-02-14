package de.regasus.common;

import java.text.Collator;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;

import com.lambdalogic.i18n.LanguageString;
import com.lambdalogic.messeinfo.kernel.sql.SearchValuesProvider;
import com.lambdalogic.util.MapHelper;

import de.regasus.core.LanguageModel;

public class LanguageSearchValuesProvider implements SearchValuesProvider {

	@Override
	public LinkedHashMap<String, LanguageString> getValues() throws Exception {
		List<Language> languages = LanguageModel.getInstance().getAllUndeletedLanguages();

		// sort Languages by name in current default language
		languages.sort(new Comparator<Language>() {
			@Override
			public int compare(Language language1, Language language2) {
				String name1 = language1.getName().getString();
				String name2 = language2.getName().getString();
				return Collator.getInstance().compare(name1, name2);
			}
		});

		LinkedHashMap<String, LanguageString> languageMap = MapHelper.createLinkedHashMap( languages.size() );
		for (Language language : languages) {
			languageMap.put(language.getId(), language.getName());
		}

		return languageMap;
	}

}
