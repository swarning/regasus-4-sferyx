package de.regasus.hotel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

import com.lambdalogic.messeinfo.hotel.data.HotelContingentCVO;
import com.lambdalogic.messeinfo.hotel.data.HotelContingentVO;
import com.lambdalogic.messeinfo.kernel.sql.SearchValuesProvider;
import com.lambdalogic.util.MapHelper;
import com.lambdalogic.util.Tuple;
import com.lambdalogic.util.TupleComparator;

public class HotelContingentSearchValuesProvider implements SearchValuesProvider {

	private Long eventPK;


	public HotelContingentSearchValuesProvider(Long eventPK) {
		this.eventPK = eventPK;
	}


	@Override
	public LinkedHashMap<Object, Object> getValues() throws Exception {
        List<HotelContingentCVO> hotelContingentCVOs = Collections.emptyList();
        if (eventPK != null) {
        	hotelContingentCVOs = HotelContingentModel.getInstance().getHotelContingentCVOsByEventPK(eventPK);
        }


        // create List of Tuples with the IDs and names
        List<Tuple<Long, String>> valueList = new ArrayList<>( hotelContingentCVOs.size() );

        for (HotelContingentCVO hotelContingentCVO : hotelContingentCVOs) {
            HotelContingentVO hotelContingentVO = hotelContingentCVO.getHotelContingentVO();

            valueList.add(
            	new Tuple<Long, String>(
            		hotelContingentVO.getID(),
            		hotelContingentVO.getName()
            	)
            );
        }

        // sort Tuples by B (name)
        Collections.sort(valueList, TupleComparator.buildBAInstance());

        LinkedHashMap<Object, Object> valueMap = MapHelper.createLinkedHashMap( valueList.size() );

        // add values in right order
        for (Tuple<Long, String> tuple : valueList) {
			valueMap.put(tuple.getA(), tuple.getB());
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
