package de.regasus.programme;

import static de.regasus.LookupService.getProgrammeOfferingMgr;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import com.lambdalogic.i18n.I18NPattern;
import com.lambdalogic.messeinfo.kernel.sql.SearchValuesProvider;
import com.lambdalogic.messeinfo.participant.data.ProgrammeOfferingCVO;
import com.lambdalogic.messeinfo.participant.data.ProgrammeOfferingCVO_PpPosition_LongLabel_Comparator;
import com.lambdalogic.messeinfo.participant.data.ProgrammePointVO;
import com.lambdalogic.messeinfo.participant.interfaces.ProgrammeOfferingCVOSettings;
import com.lambdalogic.messeinfo.participant.interfaces.ProgrammePointCVOSettings;
import com.lambdalogic.util.MapHelper;

public class ProgrammeOfferingSearchValuesProvider implements SearchValuesProvider {

	private Long eventPK;
	private boolean onlyWaitList;


	public ProgrammeOfferingSearchValuesProvider(Long eventPK, boolean onlyWaitList) {
		this.eventPK = eventPK;
		this.onlyWaitList = onlyWaitList;
	}


	@Override
	public LinkedHashMap<Long, I18NPattern> getValues() throws Exception {
        ProgrammePointCVOSettings programmePointCVOSettings = new ProgrammePointCVOSettings();
        ProgrammeOfferingCVOSettings programmeOfferingCVOSettings = new ProgrammeOfferingCVOSettings(programmePointCVOSettings);
        programmeOfferingCVOSettings.withParticipantTypeName = true;

        List<ProgrammeOfferingCVO> programmeOfferingCVOs = null;

        if (eventPK != null) {
        	if (!onlyWaitList) {
            	programmeOfferingCVOs = getProgrammeOfferingMgr().getProgrammeOfferingCVOsByEventPK(
        	        eventPK,
        	        null,		// participantTypePK
        	        null,		// programmePointType
        	        null,		// referenceTime
        	        false,		// onlyEnabled
        	        false,		// onlyNotFullyBooked
        	        false,		// onlyUseInOnlineForm
        	        programmeOfferingCVOSettings
    	        );
        	}
        	else {
        		ProgrammePointModel ppModel = ProgrammePointModel.getInstance();
        		List<ProgrammePointVO> programmePointVOs = ppModel.getProgrammePointVOsByEventPK(eventPK);
    			// remove Programme Points without wait list
    			for (Iterator<ProgrammePointVO> it = programmePointVOs.iterator(); it.hasNext();) {
    				ProgrammePointVO ppVO = it.next();

    				if (!ppVO.isWaitList()) {
    					it.remove();
    				}
    			}

    			// load
    			programmeOfferingCVOs = getProgrammeOfferingMgr().getProgrammeOfferingCVOsByProgrammePointPKs(
    	        	ProgrammePointVO.getPKs(programmePointVOs),
    	        	programmeOfferingCVOSettings
            	);
        	}
        }
        else {
        	programmeOfferingCVOs = Collections.emptyList();
        }


        // sort ProgrammeOfferingCVOs by Programme Point position and Programme Offering longLabel
        Collections.sort(programmeOfferingCVOs, ProgrammeOfferingCVO_PpPosition_LongLabel_Comparator.getInstance());


		// create Map with values
        LinkedHashMap<Long, I18NPattern> valueMap = MapHelper.createLinkedHashMap(programmeOfferingCVOs.size());
		for (ProgrammeOfferingCVO programmeOfferingCVO : programmeOfferingCVOs) {
			Long programmeOfferingPK = programmeOfferingCVO.getProgrammeOfferingVO().getID();
			I18NPattern poLabel = programmeOfferingCVO.getLongLabel();
			valueMap.put(programmeOfferingPK, poLabel);
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
