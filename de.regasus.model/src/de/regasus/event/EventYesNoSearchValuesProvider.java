package de.regasus.event;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

import com.lambdalogic.i18n.I18NString;
import com.lambdalogic.messeinfo.kernel.KernelLabel;
import com.lambdalogic.messeinfo.kernel.sql.SearchValuesProvider;
import com.lambdalogic.messeinfo.participant.data.EventVO;
import com.lambdalogic.messeinfo.participant.data.EventVO_Label_Comparator;
import com.lambdalogic.util.MapHelper;

public class EventYesNoSearchValuesProvider implements SearchValuesProvider {

	@Override
	public LinkedHashMap<Long, I18NString> getValues() throws Exception {
		List<EventVO> eventVOs = new ArrayList<>( EventModel.getInstance().getAllEventVOs() );

		Collections.sort(eventVOs, EventVO_Label_Comparator.getInstance());

		LinkedHashMap<Long, I18NString> eventMap = MapHelper.createLinkedHashMap(eventVOs.size() + 2);

		eventMap.put(YES_KEY, KernelLabel.Yes);
		eventMap.put(NO_KEY, KernelLabel.No);

		for (EventVO eventVO : eventVOs) {
			eventMap.put(eventVO.getID(), eventVO.getLabel());
		}

		return eventMap;
	}

}
