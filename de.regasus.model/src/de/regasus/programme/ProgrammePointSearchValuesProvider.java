package de.regasus.programme;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import com.lambdalogic.i18n.LanguageString;
import com.lambdalogic.messeinfo.kernel.sql.LongIDSQLField;
import com.lambdalogic.messeinfo.kernel.sql.SearchValuesProvider;
import com.lambdalogic.messeinfo.participant.data.ProgrammePointVO;
import com.lambdalogic.messeinfo.participant.data.ProgrammePointVO_Position_Comparator;
import com.lambdalogic.util.MapHelper;

public class ProgrammePointSearchValuesProvider implements SearchValuesProvider {

	private Long eventPK;
	private boolean withEmptyValue;
	private boolean onlyWaitList;

	private ProgrammePointModel ppModel;


	public ProgrammePointSearchValuesProvider(
		Long eventPK,
		boolean withEmptyValue,
		boolean onlyWaitList
	) {
		this.eventPK = eventPK;
		this.withEmptyValue = withEmptyValue;
		this.onlyWaitList = onlyWaitList;

		ppModel = ProgrammePointModel.getInstance();
	}


	@Override
	public LinkedHashMap<Long, LanguageString> getValues() throws Exception {
		LinkedHashMap<Long, LanguageString> valueMap = null;

		if (eventPK != null) {
			// get Programme Points from model
			List<ProgrammePointVO> programmePointVOs = ppModel.getProgrammePointVOsByEventPK(eventPK);

			// remove Programme Points without wait list, if only those with wait list are requested
			for (Iterator<ProgrammePointVO> it = programmePointVOs.iterator(); it.hasNext();) {
				ProgrammePointVO ppVO = it.next();

				if (onlyWaitList && !ppVO.isWaitList()) {
					it.remove();
				}
			}

			// sort Programme Points by position
			Collections.sort(programmePointVOs, ProgrammePointVO_Position_Comparator.getInstance());

			// build result
			valueMap = MapHelper.createLinkedHashMap(programmePointVOs.size() + 1);

			// add empty value if requested
			if (withEmptyValue) {
				valueMap.put(LongIDSQLField.EMPTY_KEY, new LanguageString());
			}

			// add Programme Points
			for (ProgrammePointVO programmePointVO : programmePointVOs) {
	            valueMap.put(programmePointVO.getID(), programmePointVO.getName());
	        }
		}
		else {
			valueMap = new LinkedHashMap<Long, LanguageString>(2);

			// add empty value if requested
			if (withEmptyValue) {
				valueMap.put(LongIDSQLField.EMPTY_KEY, new LanguageString());
			}
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
