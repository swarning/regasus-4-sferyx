package de.regasus.core;

import static de.regasus.LookupService.getCountryMgr;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import de.regasus.common.Country;
import de.regasus.core.model.MICacheModel;


public class CountryModel extends MICacheModel<String, Country> {
	private static CountryModel singleton = null;

	private CountryModel() {
		super();
	}


	public static CountryModel getInstance() {
		if (singleton == null) {
			singleton = new CountryModel();
		}
		return singleton;
	}


	@Override
	protected boolean isForeignKeySupported() {
		return false;
	}


	@Override
	protected String getKey(Country entity) {
		return entity.getId();
	}


	@Override
	protected Country getEntityFromServer(String countryCode) throws Exception {
		Country country = getCountryMgr().read(countryCode);
		return country;
	}


	public Country getCountry(String countryCode) throws Exception {
		return super.getEntity(countryCode);
	}


	@Override
	protected List<Country> getEntitiesFromServer(Collection<String> countryCodes) throws Exception {
		List<Country> countryList = getCountryMgr().read(countryCodes);
		return countryList;
	}


	public List<Country> getCountrys(Collection<String> countryCodes) throws Exception {
		return super.getEntities(countryCodes);
	}


	@Override
	protected List<Country> getAllEntitiesFromServer() throws Exception {
		List<Country> countryList = null;

		if (serverModel.isLoggedIn()) {
			countryList = getCountryMgr().readAllWithDeleted();
		}
		else {
			countryList = Collections.emptyList();
		}

		return countryList;
	}


	public List<Country> getAllUndeletedCountries() throws Exception {
		Collection<Country> allCountryList = getAllEntities();
		List<Country> undeletedCountryList = new ArrayList<>(allCountryList.size());
		for (Country country : allCountryList) {
			if (!country.isDeleted()) {
				undeletedCountryList.add(country);
			}
		}

		return undeletedCountryList;
	}


	@Override
	protected Country createEntityOnServer(Country country) throws Exception {
		country.validate();
		country = getCountryMgr().create(country);
		return country;
	}


	@Override
	public Country create(Country country) throws Exception {
		return super.create(country);
	}


	@Override
	protected Country updateEntityOnServer(Country country) throws Exception {
		country.validate();
		country = getCountryMgr().update(country);
		return country;
	}


	@Override
	public Country update(Country country) throws Exception {
		return super.update(country);
	}


	@Override
	protected void deleteEntityOnServer(Country country) throws Exception {
		String countryCode = country.getId();
		getCountryMgr().delete(countryCode);
	}


	@Override
	public void delete(Country country) throws Exception {
		super.delete(country);
	}


	@Override
	protected void deleteEntitiesOnServer(Collection<Country> countryCol) throws Exception {
		List<String> countryIds = Country.getPKs(countryCol);
		getCountryMgr().delete(countryIds);
	}


	@Override
	public void delete(Collection<Country> countryCol) throws Exception {
		super.delete(countryCol);
	}

}
