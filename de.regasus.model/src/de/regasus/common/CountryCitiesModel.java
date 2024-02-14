package de.regasus.common;

import static de.regasus.LookupService.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.lambdalogic.messeinfo.contact.Address;
import com.lambdalogic.messeinfo.hotel.Hotel;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.model.MICacheModel;
import de.regasus.hotel.HotelModel;
import de.regasus.model.Activator;

public class CountryCitiesModel 
extends MICacheModel<String, CountryCities>
implements CacheModelListener<Long> {

	private static CountryCitiesModel singleton;
	
	
	private HotelModel hotelModel;
	
	
	private CountryCitiesModel() {
		super();
	}
	

	public static CountryCitiesModel getInstance() {
		if (singleton == null) {
			singleton = new CountryCitiesModel();
			singleton.initModels();
		}
		return singleton;
	}
	
	
	/**
	 * Initialize references to other Models.
	 * Models are initialized outside the constructor to avoid OutOfMemoryErrors when two Models
	 * reference each other.
	 * This happens because the variable is set after the constructor is finished.
	 * If the constructor calls getInstance() of another Model that calls getInstance() of this Model,
	 * the variable instance is still null. So this Model would be created again and so on.
	 * To avoid this, the constructor has to finish before calling getInstance() of another Model.
	 * The initialization of references to other Models is done in getInstance() right after
	 * the constructor has finished. 
	 */
	private void initModels() {
		hotelModel = HotelModel.getInstance();
		hotelModel.addListener(this);
	}


	@Override
	protected boolean isForeignKeySupported() {
		return false;
	}

	
	@Override
	protected String getKey(CountryCities countryCities) {
		return countryCities.getCountryCode();
	}

	
	@Override
	protected CountryCities getEntityFromServer(String countryCode) throws Exception {
		List<String> cityList = getHotelMgr().getHotelCities(countryCode);
		CountryCities countryCities = new CountryCities(countryCode, cityList);
		return countryCities;
	}
	
	public CountryCities getCountryCities(String countryCode) throws Exception {
		return super.getEntity(countryCode);
	}

	
//	@Override
//	protected List<HotelCityData> getEntitiesFromServer(Collection<String> countryCodes) throws Exception {
//		return super.getEntitiesFromServer(countryCodes);
//	}

	
	public List<CountryCities> getCountryCitiesList(List<String> countryCodes) throws Exception {
		return super.getEntities(countryCodes);
	}


	@Override
	public void dataChange(CacheModelEvent<Long> event) {
		if (!serverModel.isLoggedIn()) {
			return;
		}
		
		try {
			List<Long> hotelPKs = event.getKeyList();
			if (hotelPKs != null) {
				// get data of all updated hotels
				List<Hotel> hotelList = hotelModel.getHotels(hotelPKs);

				// collect all combinations of city and country
				Set<CountryCity> countryCitySet = new HashSet<CountryCity>();
				for (Hotel hotel : hotelList) {
					// create CountryCity
					Address address = hotel.getMainAddress();
					String city = address.getCity();
					String countryCode = address.getCountryPK();
					if (city != null && countryCode != null) {
						CountryCity countryCity = new CountryCity(city, countryCode);
						countryCitySet.add(countryCity);
					}
				}
				
				Set<String> changedCountryCodes = new HashSet<String>();
				
				// Add missing country-city-combinations and remember which countryCodes have changed
				for (CountryCity countryCity : countryCitySet) {
					CountryCities countryCities = getCountryCities(countryCity.getCountryCode());
					
					if ( ! countryCities.getCityList().contains(countryCity.getCity())) {
						countryCities.addCity(countryCity.getCity());
						changedCountryCodes.add(countryCity.getCountryCode());
					}
				}
				
				// fireDataChange for changed countryCodes
				if ( ! changedCountryCodes.isEmpty()) {
					fireRefresh(changedCountryCodes);
				}
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}

}
