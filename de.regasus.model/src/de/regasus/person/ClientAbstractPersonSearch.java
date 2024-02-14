package de.regasus.person;

import com.lambdalogic.messeinfo.config.parameterset.PersonConfigParameterSet;
import com.lambdalogic.messeinfo.contact.sql.AbstractPersonSearch;
import com.lambdalogic.messeinfo.contact.sql.PersonSearch;

import de.regasus.common.CountrySearchValuesProvider;
import de.regasus.common.LanguageSearchValuesProvider;
import de.regasus.finance.CreditCardTypeSearchValuesProvider;

public abstract class ClientAbstractPersonSearch extends AbstractPersonSearch {

	private static final long serialVersionUID = 1L;

	private static boolean initialized = false; 

	
	protected static CountrySearchValuesProvider countrySearchValuesProvider;

	public static CountrySearchValuesProvider getCountrySearchValuesProvider() {
		if (countrySearchValuesProvider == null) {
			countrySearchValuesProvider = new CountrySearchValuesProvider();
		}
		return countrySearchValuesProvider;
	}
	
	
	public static void initStaticFields() {
		if (! initialized) {
			// init countrySearchValuesProvider
			getCountrySearchValuesProvider();
	        
	        for (int i = 0; i < PersonSearch.COUNTRY_MNEMONICS.length; i++) {
	        	AbstractPersonSearch.COUNTRIES[i].setSearchValuesProvider(countrySearchValuesProvider);
	        }
	        
	        AbstractPersonSearch.COUNTRY.setSearchValuesProvider(countrySearchValuesProvider);
	        AbstractPersonSearch.MAIN_COUNTRY.setSearchValuesProvider(countrySearchValuesProvider);
	        AbstractPersonSearch.INV_COUNTRY.setSearchValuesProvider(countrySearchValuesProvider);
	        
	
			LanguageSearchValuesProvider languageSearchValuesProvider = new LanguageSearchValuesProvider();
			AbstractPersonSearch.LANGUAGE.setSearchValuesProvider(languageSearchValuesProvider);
			
			CreditCardTypeSearchValuesProvider creditCardTypeSearchValuesProvider = new CreditCardTypeSearchValuesProvider();
			AbstractPersonSearch.CC_TYPE_PK.setSearchValuesProvider(creditCardTypeSearchValuesProvider);
			
			
			initialized = true;
		}
	}

	
	public ClientAbstractPersonSearch(PersonConfigParameterSet personConfigParameterSet) {
		super(personConfigParameterSet);
	}

}
