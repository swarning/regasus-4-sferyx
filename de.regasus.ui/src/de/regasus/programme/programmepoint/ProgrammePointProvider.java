package de.regasus.programme.programmepoint;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.lambdalogic.messeinfo.participant.data.ProgrammePointCVO;
import com.lambdalogic.util.TypeHelper;
import com.lambdalogic.util.rcp.widget.EntityProvider;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.programme.ProgrammePointModel;
import de.regasus.ui.Activator;

public class ProgrammePointProvider implements EntityProvider<ProgrammePointCVO> {

	private static final ProgrammePointModel ppModel = ProgrammePointModel.getInstance();

	private Long eventPK;
	private Collection<Long> blackListProgrammePointPKs;


	public ProgrammePointProvider(Long eventPK) {
		this.eventPK = eventPK;
	}


	@Override
	public List<ProgrammePointCVO> getEntityList() {
		try {
			List<ProgrammePointCVO> allProgrammePointCVOs = ppModel.getProgrammePointCVOsByEventPK(eventPK);

			List<ProgrammePointCVO> withoutBlackListProgrammePointCVOs = allProgrammePointCVOs;

			// remove Programme Points which are on the black list
			if (blackListProgrammePointPKs != null) {
				withoutBlackListProgrammePointCVOs = new ArrayList<>( allProgrammePointCVOs.size() );
				for (ProgrammePointCVO programmePointCVO : allProgrammePointCVOs) {
					if ( ! blackListProgrammePointPKs.contains( programmePointCVO.getPK() )) {
						withoutBlackListProgrammePointCVOs.add(programmePointCVO);
					}
				}
			}

			return withoutBlackListProgrammePointCVOs;
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			return Collections.emptyList();
		}
	}


	@Override
	public ProgrammePointCVO findEntity(Object entityId) {
		try {
			Long programmePointPK = TypeHelper.toLong(entityId);
			return ppModel.getProgrammePointCVO(programmePointPK);
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			return null;
		}
	}


	@Override
	public Collection<ProgrammePointCVO> findEntities(Collection<?> entityIds) {
		List<ProgrammePointCVO> programmePointCVOs = new ArrayList<>( entityIds.size() );
		for (Object entityId : entityIds) {
			ProgrammePointCVO programmePointCVO = findEntity(entityId);
			programmePointCVOs.add(programmePointCVO);
		}
		return programmePointCVOs;
	}


	public Collection<Long> getBlackListProgrammePointPKs() {
		return blackListProgrammePointPKs;
	}


	public void setBlackListProgrammePointPKs(Collection<Long> blackListProgrammePointPKs) {
		this.blackListProgrammePointPKs = blackListProgrammePointPKs;
	}

}
