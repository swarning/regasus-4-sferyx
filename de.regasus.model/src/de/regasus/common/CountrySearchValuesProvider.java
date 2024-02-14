package de.regasus.common;

import static com.lambdalogic.util.MapHelper.createLinkedHashMap;

import java.text.Collator;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;

import com.lambdalogic.i18n.LanguageString;
import com.lambdalogic.messeinfo.kernel.sql.SearchValuesProvider;

import de.regasus.core.CountryModel;

public class CountrySearchValuesProvider implements SearchValuesProvider {

	@Override
	public LinkedHashMap<String, LanguageString> getValues() throws Exception {
		List<Country> countryList = CountryModel.getInstance().getAllUndeletedCountries();

		// sort Countries by name in current default language
		countryList.sort(new Comparator<Country>() {
			@Override
			public int compare(Country country1, Country country2) {
				String name1 = country1.getName().getString();
				String name2 = country2.getName().getString();
				return Collator.getInstance().compare(name1, name2);
			}
		});

		LinkedHashMap<String, LanguageString> countryMap = createLinkedHashMap( countryList.size() );
		for (Country country : countryList) {
			countryMap.put(country.getId(), country.getName());
		}

		return countryMap;
	}

}
