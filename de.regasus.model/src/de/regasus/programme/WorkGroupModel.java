package de.regasus.programme;

import static de.regasus.LookupService.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.lambdalogic.messeinfo.participant.data.ProgrammePointVO;
import com.lambdalogic.messeinfo.participant.data.WorkGroupCVO;
import com.lambdalogic.messeinfo.participant.data.WorkGroupVO;
import com.lambdalogic.messeinfo.participant.interfaces.WorkGroupCVOSettings;
import com.lambdalogic.util.exception.ErrorMessageException;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.model.CacheModelOperation;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.model.MICacheModel;
import de.regasus.model.Activator;

public class WorkGroupModel extends MICacheModel<Long, WorkGroupVO> {

	/**
	 * Size of the cache for single work groups.
	 */
	public static final int ENTITY_CACHE_SIZE = 1000;

	/**
	 * Size of the cache for work group lists of a programme point.
	 */
	public static final int FOREIGN_KEY_CACHE_SIZE = 1000;

	private static WorkGroupModel singleton;

	private ProgrammePointModel programmePointModel;


	private WorkGroupModel() {
		super(ENTITY_CACHE_SIZE, FOREIGN_KEY_CACHE_SIZE);

		programmePointModel = ProgrammePointModel.getInstance();
		programmePointModel.addListener(programmePointModelListener);
	}


	public static WorkGroupModel getInstance() {
		if (singleton == null) {
			singleton = new WorkGroupModel();
		}
		return singleton;
	}

	@Override
	protected Long getKey(WorkGroupVO entity) {
		return entity.getID();
	}

	@Override
	protected WorkGroupVO getEntityFromServer(Long workGroupPK) throws Exception {
		WorkGroupVO workGroupVO = getWorkGroupMgr().getWorkGroupVO(workGroupPK);
		return workGroupVO;
	}

	public WorkGroupVO getWorkGroupVO(Long workGroupPK) throws Exception {
		return super.getEntity(workGroupPK);
	}


	@Override
	protected List<WorkGroupVO> getEntitiesFromServer(Collection<Long> workGroupPKs) throws Exception {
		List<WorkGroupVO> workGroupVOs =
			getWorkGroupMgr().getWorkGroupVOs(workGroupPKs);
		return workGroupVOs;
	}


	public List<WorkGroupVO> getWorkGroupVOs(List<Long> workGroupPKs) throws Exception {
		return super.getEntities(workGroupPKs);
	}


	@Override
	protected WorkGroupVO createEntityOnServer(WorkGroupVO workGroupVO) throws Exception {
		workGroupVO.validate();
		workGroupVO = getWorkGroupMgr().createWorkGroup(workGroupVO);
		return workGroupVO;
	}


	@Override
	public WorkGroupVO create(WorkGroupVO workGroupVO) throws Exception {
		return super.create(workGroupVO);
	}


	@Override
	protected WorkGroupVO updateEntityOnServer(WorkGroupVO workGroupVO) throws Exception {
		workGroupVO.validate();
		workGroupVO = getWorkGroupMgr().updateWorkGroup(workGroupVO);
		return workGroupVO;
	}


	@Override
	public WorkGroupVO update(WorkGroupVO workGroupVO) throws Exception {
		return super.update(workGroupVO);
	}


	@Override
	protected void deleteEntityOnServer(WorkGroupVO workGroupVO) throws Exception {
		if (workGroupVO != null) {
			Long id = workGroupVO.getID();
			getWorkGroupMgr().deleteWorkGroup(id);
		}
	}

	@Override
	public void delete(WorkGroupVO workGroupVO) throws Exception {
		super.delete(workGroupVO);
	}


	@Override
	protected boolean isForeignKeySupported() {
		return true;
	}


	@Override
	protected Object getForeignKey(WorkGroupVO workGroupVO) {
		Long fk = null;
		if (workGroupVO != null) {
			fk= workGroupVO.getProgrammePointPK();
		}
		return fk;
	}


	@Override
	protected List<WorkGroupVO> getEntitiesByForeignKeyFromServer(Object foreignKey) throws Exception {
		Long programmePointPK = (Long) foreignKey;

		List<WorkGroupVO> workGroupVOs = getWorkGroupMgr().getWorkGroupVOsByProgrammePointPK(
			programmePointPK
		);

		return workGroupVOs;
	}


	public List<WorkGroupVO> getWorkGroupVOsByProgrammePointPK(Long programmePointPK) throws Exception {
		return getEntityListByForeignKey(programmePointPK);
	}


	public WorkGroupVO copyWorkGroup(
		Long sourceWorkGroupPK,
		Long destProgrammePointPK
	)
	throws ErrorMessageException {
		WorkGroupVO wgVO = getWorkGroupMgr().copyWorkGroup(
			sourceWorkGroupPK,
			destProgrammePointPK,
			null	// dayShift
		);

		put(wgVO);

		List<Long> primaryKeyList = Collections.singletonList(wgVO.getID());

		try {
			fireCreate(primaryKeyList);
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}

		return wgVO;
	}


	// **************************************************************************
	// * Delegate Methods
	// *

	public List<WorkGroupCVO> getWorkGroupCVOsByEventPK(Long eventPK, WorkGroupCVOSettings settings) throws Exception {
		return getWorkGroupMgr().getWorkGroupCVOsByEventPK(eventPK, settings);
	}

	// *
	// * Delegate Methods
	// **************************************************************************


	private CacheModelListener<Long> programmePointModelListener = new CacheModelListener<Long>() {
    	@Override
    	public void dataChange(CacheModelEvent<Long> event) {
    		if (!serverModel.isLoggedIn()) {
    			return;
    		}

    		try {
    			if (   event.getOperation() == CacheModelOperation.UPDATE
    				|| event.getOperation() == CacheModelOperation.REFRESH
    			) {
    				Collection<Long> affectedPKs = new ArrayList<>();
    				for (Long programmePointPK : event.getKeyList()) {
    					/* Only the cancellation of a Programme Point affects its Work Groups.
    					 * Therefore we ignore Programme Points that are not cancelled.
    					 */
    					ProgrammePointVO programmePointVO = programmePointModel.getProgrammePointVO(programmePointPK);
    					if ( programmePointVO.isCancelled() ) {
        					for (WorkGroupVO workGroupVO : getLoadedAndCachedEntities()) {
        						if (programmePointPK.equals(workGroupVO.getProgrammePointPK())) {
        							affectedPKs.add(workGroupVO.getPK());
        						}
        					}
    					}
    				}

    				if (!affectedPKs.isEmpty()) {
    					refresh(affectedPKs);
    				}
    			}
    			else if (event.getOperation() == CacheModelOperation.DELETE) {
    				Collection<Long> affectedPKs = new ArrayList<>();
    				for (Long programmePointPK : event.getKeyList()) {
    					for (WorkGroupVO workGroupVO : getLoadedAndCachedEntities()) {
    						if (programmePointPK.equals(workGroupVO.getProgrammePointPK())) {
    							affectedPKs.add(workGroupVO.getPK());
    						}
    					}
    				}

    				if (!affectedPKs.isEmpty()) {
    					handleDeleteByKeyList(affectedPKs, true);
    				}
    			}
    		}
    		catch (Exception e) {
    			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
    		}
    	}
	};

}
