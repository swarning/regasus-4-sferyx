package de.regasus.programme;

import static de.regasus.LookupService.getProgrammePointTypeMgr;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.lambdalogic.messeinfo.participant.data.ProgrammePointTypeVO;

import de.regasus.core.model.MICacheModel;

public class ProgrammePointTypeModel extends MICacheModel<Long, ProgrammePointTypeVO> {

	private static ProgrammePointTypeModel singleton = null;


	private ProgrammePointTypeModel() {
	}


	public static ProgrammePointTypeModel getInstance() {
		if (singleton == null) {
			singleton = new ProgrammePointTypeModel();
		}
		return singleton;
	}


	@Override
	protected boolean isForeignKeySupported() {
		return false;
	}


	@Override
	protected Long getKey(ProgrammePointTypeVO entity) {
		return entity.getPK();
	}


	@Override
	protected ProgrammePointTypeVO getEntityFromServer(Long pk) throws Exception {
		ProgrammePointTypeVO programmePointTypeVO = getProgrammePointTypeMgr().getProgrammePointTypeVO(pk);
		return programmePointTypeVO;
	}


	@Override
	protected List<ProgrammePointTypeVO> getEntitiesFromServer(Collection<Long> pkList) throws Exception {
		List<ProgrammePointTypeVO> ppTypeVOs = getProgrammePointTypeMgr().getProgrammePointTypeVOs(pkList);
		return ppTypeVOs;
	}


	@Override
	protected List<ProgrammePointTypeVO> getAllEntitiesFromServer() throws Exception {
		List<ProgrammePointTypeVO> programmePointTypeVOs = null;

		if (serverModel.isLoggedIn()) {
			programmePointTypeVOs = getProgrammePointTypeMgr().getProgrammePointTypeVOs(false);
		}
		else {
			programmePointTypeVOs = Collections.emptyList();
		}

		return programmePointTypeVOs;
	}


	public List<ProgrammePointTypeVO> getAllUndeletedProgrammePointTypeVOs() throws Exception {
		List<ProgrammePointTypeVO> allProgrammePointTypeVOs = getAllEntities();
		List<ProgrammePointTypeVO> undeletedProgrammePointTypeVOs = new ArrayList<>(allProgrammePointTypeVOs.size());
		for (ProgrammePointTypeVO programmePointTypeVO : allProgrammePointTypeVOs) {
			if (!programmePointTypeVO.isDeleted()) {
				undeletedProgrammePointTypeVOs.add(programmePointTypeVO);
			}
		}
		return undeletedProgrammePointTypeVOs;
	}


	public ProgrammePointTypeVO getProgrammePointTypeVO(Long pk) throws Exception {
		return super.getEntity(pk);
	}


	public List<ProgrammePointTypeVO> getProgrammePointTypeVOs(Collection<Long> pkCol) throws Exception {
		return super.getEntities(pkCol);
	}


	@Override
	protected ProgrammePointTypeVO createEntityOnServer(ProgrammePointTypeVO programmePointTypeVO) throws Exception {
		programmePointTypeVO.validate();

		programmePointTypeVO = getProgrammePointTypeMgr().createProgrammePointType(programmePointTypeVO);
		return programmePointTypeVO;
	}


	@Override
	public ProgrammePointTypeVO create(ProgrammePointTypeVO programmePointTypeVO) throws Exception {
		return super.create(programmePointTypeVO);
	}


	@Override
	protected void deleteEntityOnServer(ProgrammePointTypeVO programmePointTypeVO) throws Exception {
		if (programmePointTypeVO != null) {
			Long id = programmePointTypeVO.getPK();
			getProgrammePointTypeMgr().deleteProgrammePointType(id);
		}
	}


	@Override
	public void delete(ProgrammePointTypeVO programmePointTypeVO) throws Exception {
		super.delete(programmePointTypeVO);
	}


	@Override
	protected ProgrammePointTypeVO updateEntityOnServer(ProgrammePointTypeVO programmePointTypeVO) throws Exception {
		programmePointTypeVO.validate();
		programmePointTypeVO = getProgrammePointTypeMgr().updateProgrammePointType(programmePointTypeVO);
		return programmePointTypeVO;
	}


	@Override
	public ProgrammePointTypeVO update(ProgrammePointTypeVO programmePointTypeVO) throws Exception {
		return super.update(programmePointTypeVO);
	}

}
