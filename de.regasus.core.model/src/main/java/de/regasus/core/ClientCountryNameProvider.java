package de.regasus.core;

import com.lambdalogic.messeinfo.contact.ICountryNameProvider;
import de.regasus.common.Country;

public class ClientCountryNameProvider implements ICountryNameProvider {

	private static ClientCountryNameProvider countryNameProvider;


	public static ClientCountryNameProvider getInstance() {
		if (countryNameProvider == null) {
			countryNameProvider = new ClientCountryNameProvider();
		}
		return countryNameProvider;
	}


	private ClientCountryNameProvider() {
	}


	@Override
	public String getCountryName(String countryID, String language) throws Exception {
		String name = null;

		Country country = CountryModel.getInstance().getCountry(countryID);
		if (country != null) {
			name = country.getName().getString(language);
		}

		return name;
	}

}
