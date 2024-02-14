package de.regasus.hotel;

import static de.regasus.LookupService.getHotelMgr;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.lambdalogic.messeinfo.hotel.Hotel;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.model.MIListModel;
import de.regasus.model.Activator;



public class HotelCountriesModel extends MIListModel<String> implements CacheModelListener<Long> {

	private static HotelCountriesModel singleton = null;

	private HotelModel hotelModel;


	private HotelCountriesModel() {
		super();
	}


	public static HotelCountriesModel getInstance() {
		if (singleton == null) {
			singleton = new HotelCountriesModel();
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
	protected List<String> getModelDataFromServer() throws Exception {
		List<String> hotelCountryPKs = getHotelMgr().getHotelCountryPKs();
		return hotelCountryPKs;
	}


	@Override
	public void dataChange(CacheModelEvent<Long> event) {
		if (!serverModel.isLoggedIn()) {
			return;
		}

		try {
			if (modelData != null) {
    			List<Long> hotelPKs = event.getKeyList();
    			if (hotelPKs != null) {
    				// get data of all updated hotels
    				List<Hotel> hotelList = hotelModel.getHotels(hotelPKs);

    				// get all countries
    				Set<String> countryCodes = new HashSet<>();
    				for (Hotel hotel : hotelList) {
    					String countryPK = hotel.getMainAddress().getCountryPK();
    					if (countryPK != null) {
    						countryCodes.add(countryPK);
    					}
    				}

    				boolean modelDataChanged = false;
    				for (String countryCode : countryCodes) {
    					if ( ! modelData.contains(countryCode)) {
    						modelData.add(countryCode);
    						modelDataChanged = true;
    					}
    				}

    				if (modelDataChanged) {
    					fireDataChange();
    				}
    			}
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}

}
