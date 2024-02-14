package de.regasus.hotel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

import com.lambdalogic.messeinfo.kernel.sql.SearchValuesProvider;
import com.lambdalogic.util.MapHelper;

public class HotelChainSearchValuesProvider implements SearchValuesProvider {

	@Override
	public LinkedHashMap<Long, String> getValues() throws Exception {
		List<HotelChain> hotelChains = new ArrayList<>( HotelChainModel.getInstance().getAllHotelChains() );

		Collections.sort(hotelChains, HotelChainComparator.getInstance());

		LinkedHashMap<Long, String> values = MapHelper.createLinkedHashMap(hotelChains.size());
		for (HotelChain hotelChain : hotelChains) {
			values.put(hotelChain.getId(), hotelChain.getName());
		}

		return values;
	}

}
