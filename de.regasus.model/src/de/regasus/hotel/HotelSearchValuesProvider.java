package de.regasus.hotel;

import static de.regasus.LookupService.getHotelContingentMgr;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

import com.lambdalogic.messeinfo.hotel.data.HotelCVOSettings;
import com.lambdalogic.messeinfo.hotel.data.HotelContingentCVO;
import com.lambdalogic.messeinfo.hotel.data.HotelContingentCVOSettings;
import com.lambdalogic.messeinfo.hotel.data.HotelContingentCVO_HotelName_Comparator;
import com.lambdalogic.messeinfo.kernel.sql.SearchValuesProvider;
import com.lambdalogic.util.MapHelper;

public class HotelSearchValuesProvider implements SearchValuesProvider {

	private Long eventPK;


	public HotelSearchValuesProvider(Long eventPK) {
		this.eventPK = eventPK;
	}


	@Override
	public LinkedHashMap getValues() throws Exception {
        List<HotelContingentCVO> hotelContingentCVOs = Collections.emptyList();
        if (eventPK != null) {
            HotelContingentCVOSettings settings = new HotelContingentCVOSettings();
            settings.hotelCVOSettings = new HotelCVOSettings();

            hotelContingentCVOs = getHotelContingentMgr().getHotelContingentCVOsByEvent(
	            eventPK,
	            null, // hotelPKs
	            settings
	        );

	        // sort HotelContingentCVOs by Hotel name
	        Collections.sort(hotelContingentCVOs, HotelContingentCVO_HotelName_Comparator.getInstance());
        }


        // build valueMap
        LinkedHashMap valueMap = MapHelper.createLinkedHashMap( hotelContingentCVOs.size() );
        for (HotelContingentCVO hotelContingentCVO : hotelContingentCVOs) {
            valueMap.put(
                hotelContingentCVO.getHotelContingentVO().getHotelPK(),
                hotelContingentCVO.getHotelName()
            );
        }


		return valueMap;
	}


	public Long getEventPK() {
		return eventPK;
	}


	public void setEventPK(Long eventPK) {
		this.eventPK = eventPK;
	}

}
