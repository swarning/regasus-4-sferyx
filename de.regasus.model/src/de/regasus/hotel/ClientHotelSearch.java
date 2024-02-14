package de.regasus.hotel;

import com.lambdalogic.messeinfo.config.parameterset.ConfigParameterSet;
import com.lambdalogic.messeinfo.hotel.sql.HotelSearch;

import de.regasus.person.ClientOrganisationSearch;

public class ClientHotelSearch extends HotelSearch {

	private static final long serialVersionUID = 1L;
	
	
	protected HotelChainSearchValuesProvider hotelChainSearchValuesProvider;
	
	static {
		ClientOrganisationSearch.initStaticFields();
	}

	
	public ClientHotelSearch(Long eventPK, ConfigParameterSet configParameterSet)
	throws Exception {
        super(eventPK, configParameterSet);

        hotelChainSearchValuesProvider = new HotelChainSearchValuesProvider();
        HOTEL_CHAIN.setSearchValuesProvider(hotelChainSearchValuesProvider);
	}

}
