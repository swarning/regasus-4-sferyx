package de.regasus.programme.programmepointtype;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.lambdalogic.messeinfo.participant.data.ProgrammePointTypeVO;
import com.lambdalogic.util.TypeHelper;
import com.lambdalogic.util.rcp.widget.EntityProvider;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.programme.ProgrammePointTypeModel;
import de.regasus.ui.Activator;

public class ProgrammePointTypeProvider implements EntityProvider<ProgrammePointTypeVO> {

	private static final ProgrammePointTypeModel pptModel = ProgrammePointTypeModel.getInstance();

	private Collection<Long> blackListProgrammePointTypePKs;


	public ProgrammePointTypeProvider() {
	}


	@Override
	public List<ProgrammePointTypeVO> getEntityList() {
		try {
			List<ProgrammePointTypeVO> allProgrammePointTypeVOs = pptModel.getAllUndeletedProgrammePointTypeVOs();

			List<ProgrammePointTypeVO> withoutBlackListProgrammePointTypeVOs = allProgrammePointTypeVOs;

			// remove Programme Point Types which are on the black list
			if (blackListProgrammePointTypePKs != null) {
				withoutBlackListProgrammePointTypeVOs = new ArrayList<>( allProgrammePointTypeVOs.size() );
				for (ProgrammePointTypeVO programmePointTypeVO : allProgrammePointTypeVOs) {
					if ( ! blackListProgrammePointTypePKs.contains( programmePointTypeVO.getPK() )) {
						withoutBlackListProgrammePointTypeVOs.add(programmePointTypeVO);
					}
				}
			}

			return withoutBlackListProgrammePointTypeVOs;
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			return Collections.emptyList();
		}
	}


	@Override
	public ProgrammePointTypeVO findEntity(Object entityId) {
		try {
			Long programmePointTypePK = TypeHelper.toLong(entityId);
			return pptModel.getProgrammePointTypeVO(programmePointTypePK);
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			return null;
		}
	}


	@Override
	public Collection<ProgrammePointTypeVO> findEntities(Collection<?> entityIds) {
		List<ProgrammePointTypeVO> programmePointTypeVOs = new ArrayList<>( entityIds.size() );
		for (Object entityId : entityIds) {
			ProgrammePointTypeVO programmePointTypeVO = findEntity(entityId);
			programmePointTypeVOs.add(programmePointTypeVO);
		}
		return programmePointTypeVOs;
	}


	public Collection<Long> getBlackListProgrammePointTypePKs() {
		return blackListProgrammePointTypePKs;
	}


	public void setBlackListProgrammePointTypePKs(Collection<Long> blackListProgrammePointTypePKs) {
		this.blackListProgrammePointTypePKs = blackListProgrammePointTypePKs;
	}

}
