package de.regasus.hotel;

import com.lambdalogic.messeinfo.config.parameterset.ConfigParameterSet;
import com.lambdalogic.messeinfo.hotelcontignent.sql.HotelContingentSearch;

public class ClientHotelContingentSearch extends HotelContingentSearch {

	private static final long serialVersionUID = 1L;
	
	
	protected HotelChainSearchValuesProvider hotelChainSearchValuesProvider;

	
	public ClientHotelContingentSearch(Long eventPK, ConfigParameterSet configParameterSet)
	throws Exception {
        super(eventPK, configParameterSet);

        hotelChainSearchValuesProvider = new HotelChainSearchValuesProvider();
        HOTEL_CHAIN.setSearchValuesProvider(hotelChainSearchValuesProvider);
	}

}
