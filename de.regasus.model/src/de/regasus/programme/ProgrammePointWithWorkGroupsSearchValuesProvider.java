package de.regasus.programme;

import static de.regasus.LookupService.getProgrammePointMgr;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

import com.lambdalogic.i18n.I18NString;
import com.lambdalogic.messeinfo.kernel.KernelLabel;
import com.lambdalogic.messeinfo.kernel.sql.SearchValuesProvider;
import com.lambdalogic.messeinfo.participant.data.ProgrammePointVO;
import com.lambdalogic.messeinfo.participant.data.ProgrammePointVO_Position_Comparator;
import com.lambdalogic.util.MapHelper;

public class ProgrammePointWithWorkGroupsSearchValuesProvider implements SearchValuesProvider {

	private Long eventPK;


	public ProgrammePointWithWorkGroupsSearchValuesProvider(Long eventPK) {
		this.eventPK = eventPK;
	}


	@Override
	public LinkedHashMap getValues() throws Exception {
		/* The ProgrammePointVOs managed by ProgrammePointModel don't contain information about their Work Groups.
		 * That's why we get the Programme Points directly from the server.
		 */

		List<ProgrammePointVO> programmePointVOs = null;
		if (eventPK != null) {
	        programmePointVOs = getProgrammePointMgr().getProgrammePointVOsByEventPK(
	        	eventPK,
	        	true, // onlyWithWorkGroup
	        	false // onlyWithWaitList
	        );


			// sort Programme Points by position
			Collections.sort(programmePointVOs, ProgrammePointVO_Position_Comparator.getInstance());
		}
		else {
			programmePointVOs = Collections.emptyList();
		}

		LinkedHashMap<Long, I18NString> valueMap = MapHelper.createLinkedHashMap(programmePointVOs.size() + 1);

		valueMap.put(YES_KEY, KernelLabel.Yes);

        for (ProgrammePointVO programmePointVO : programmePointVOs) {
            valueMap.put(programmePointVO.getID(), programmePointVO.getName());
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
