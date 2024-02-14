package de.regasus.programme;

import static de.regasus.LookupService.*;
import static com.lambdalogic.util.CollectionsHelper.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import com.lambdalogic.messeinfo.participant.data.ProgrammeBookingCVO;
import com.lambdalogic.messeinfo.participant.data.ProgrammeOfferingCVO;
import com.lambdalogic.messeinfo.participant.data.ProgrammeOfferingVO;
import com.lambdalogic.messeinfo.participant.data.ProgrammePointVO;
import com.lambdalogic.messeinfo.participant.interfaces.ProgrammeOfferingCVOSettings;
import com.lambdalogic.util.exception.ErrorMessageException;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.model.CacheModelOperation;

import de.regasus.core.OrderPosition;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.model.MICacheModel;
import de.regasus.model.Activator;

public class ProgrammeOfferingModel extends MICacheModel<Long, ProgrammeOfferingCVO> {

	private static final ProgrammeOfferingCVOSettings CVO_SETTINGS;
	static {
		CVO_SETTINGS = new ProgrammeOfferingCVOSettings();
		CVO_SETTINGS.withNumberOfBookings = true;
	}

	private static ProgrammeOfferingModel singleton;

	private ProgrammePointModel ppModel;
	private ProgrammeBookingModel pbModel;


	private ProgrammeOfferingModel() {
		super();

		ppModel = ProgrammePointModel.getInstance();
		ppModel.addListener(programmePointModelListener);

		pbModel = ProgrammeBookingModel.getInstance();
		pbModel.addListener(programmeBookingModelListener);
	}


	public static ProgrammeOfferingModel getInstance() {
		if (singleton == null) {
			singleton = new ProgrammeOfferingModel();
		}
		return singleton;
	}

	@Override
	protected Long getKey(ProgrammeOfferingCVO entity) {
		return entity.getPK();
	}


	@Override
	protected ProgrammeOfferingCVO getEntityFromServer(Long programmOfferingPK) throws Exception {
		ProgrammeOfferingCVO programmOfferingCVO = getProgrammeOfferingMgr().getProgrammeOfferingCVO(
			programmOfferingPK,
			CVO_SETTINGS
		);
		return programmOfferingCVO;
	}


	public ProgrammeOfferingCVO getProgrammeOfferingCVO(Long programmeOfferingPK) throws Exception {
		return super.getEntity(programmeOfferingPK);
	}


	public ProgrammeOfferingVO getProgrammeOfferingVO(Long programmeOfferingPK) throws Exception {
		ProgrammeOfferingVO programmeOfferingVO = null;
		ProgrammeOfferingCVO programmeOfferingCVO = getProgrammeOfferingCVO(programmeOfferingPK);
		if (programmeOfferingCVO != null) {
			programmeOfferingVO = programmeOfferingCVO.getVO();
		}
		return programmeOfferingVO;
	}


	@Override
	protected List<ProgrammeOfferingCVO> getEntitiesFromServer(Collection<Long> programmOfferingPKs) throws Exception {
		List<ProgrammeOfferingCVO> poCVOs = getProgrammeOfferingMgr().getProgrammeOfferingCVOs(programmOfferingPKs, CVO_SETTINGS);
		return poCVOs;
	}


	public List<ProgrammeOfferingCVO> getProgrammeOfferingCVOs(List<Long> programmeOfferingPKs) throws Exception {
		return super.getEntities(programmeOfferingPKs);
	}


	public List<ProgrammeOfferingVO> getProgrammeOfferingVOs(List<Long> programmeOfferingPKs) throws Exception {
		List<ProgrammeOfferingVO> programmeOfferingVOs = null;

		List<ProgrammeOfferingCVO> programmeOfferingCVOs = getProgrammeOfferingCVOs(programmeOfferingPKs);
		if (programmeOfferingCVOs != null) {
			programmeOfferingVOs = ProgrammeOfferingCVO.getVOs(programmeOfferingCVOs);
		}

		return programmeOfferingVOs;
	}


	@Override
	protected ProgrammeOfferingCVO createEntityOnServer(ProgrammeOfferingCVO programmeOfferingCVO) throws Exception {
		ProgrammeOfferingVO programmeOfferingVO = programmeOfferingCVO.getVO();

		// temporarily set the position to pass validation
		programmeOfferingVO.setPosition(0);
		programmeOfferingVO.validate();
		// remove position because it should be calculated automatically by the server
		programmeOfferingVO.setPosition(null);

		programmeOfferingVO = getProgrammeOfferingMgr().createProgrammeOffering(programmeOfferingVO);
		programmeOfferingCVO.setVO(programmeOfferingVO);
		return programmeOfferingCVO;
	}


	public ProgrammeOfferingVO create(ProgrammeOfferingVO programmeOfferingVO) throws Exception {
		ProgrammeOfferingCVO programmeOfferingCVO = new ProgrammeOfferingCVO();
		programmeOfferingCVO.setVO(programmeOfferingVO);

		// init ProgrammeOfferingCVO
		programmeOfferingCVO.setNumberOfBookings(0);

		programmeOfferingCVO = super.create(programmeOfferingCVO);
		return programmeOfferingCVO.getVO();
	}


	@Override
	protected ProgrammeOfferingCVO updateEntityOnServer(ProgrammeOfferingCVO programmeOfferingCVO) throws Exception {
		ProgrammeOfferingVO programmeOfferingVO = programmeOfferingCVO.getVO();
		programmeOfferingVO.validate();
		programmeOfferingVO = getProgrammeOfferingMgr().updateProgrammeOffering(programmeOfferingVO);
		programmeOfferingCVO.setVO(programmeOfferingVO);
		return programmeOfferingCVO;
	}


	public ProgrammeOfferingVO update(ProgrammeOfferingVO programmeOfferingVO) throws Exception {
		ProgrammeOfferingCVO programmeOfferingCVO = getProgrammeOfferingCVO(programmeOfferingVO.getPK());

		/* The entity that will be updated has to be cloned, because CacheModel does not accept the same instance as in
		 * the cache if the Model supports foreign keys.
		 */
		programmeOfferingCVO = programmeOfferingCVO.clone();

		// put VO with new data into CVO
		programmeOfferingCVO.setVO(programmeOfferingVO);

		programmeOfferingCVO = super.update(programmeOfferingCVO);

		return programmeOfferingCVO.getVO();
	}


	/**
	 * Move a {@link ProgrammeOfferingVO} before or after another one.
	 * The target Programme Offering must belong to the same Programme Point.
	 *
	 * @param movedProgrammeOfferingId
	 * @param orderPosition
	 * @param targetProgrammeOfferingId
	 * @throws Exception
	 */
	public void move(Long movedProgrammeOfferingId, OrderPosition orderPosition, Long targetProgrammeOfferingId)
	throws Exception {
		List<ProgrammeOfferingVO> poVOs = getProgrammeOfferingMgr().move(
			movedProgrammeOfferingId,
			orderPosition,
			targetProgrammeOfferingId
		);

		// copy extensions of possibly existing data from the cache
		List<Long> poIds = ProgrammeOfferingVO.getPKs(poVOs);
		HashMap<Long, ProgrammeOfferingVO> poMap = ProgrammeOfferingVO.abstractVOs2Map(poVOs);

		List<ProgrammeOfferingCVO> poCVOs = getProgrammeOfferingCVOs(poIds);

		for (ProgrammeOfferingCVO poCVO : poCVOs) {
			ProgrammeOfferingVO poVO = poMap.get( poCVO.getId() );
			poCVO.setVO(poVO);
		}

		put(poCVOs);

		fireDataChange(CacheModelOperation.UPDATE, poIds);
	}


	@Override
	protected void deleteEntityOnServer(ProgrammeOfferingCVO programmeOfferingCVO) throws Exception {
		if (programmeOfferingCVO != null) {
			Long id = programmeOfferingCVO.getPK();
			getProgrammeOfferingMgr().deleteProgrammeOffering(id);
		}
	}


	public void delete(Long programmeOfferingPK) throws Exception {
		ProgrammeOfferingCVO programmeOfferingCVO = getProgrammeOfferingCVO(programmeOfferingPK);
		super.delete(programmeOfferingCVO);
	}


	@Override
	protected boolean isForeignKeySupported() {
		return true;
	}


	@Override
	protected Object getForeignKey(ProgrammeOfferingCVO programmeOfferingCVO) {
		Long programmePointPK = null;
		if (programmeOfferingCVO != null) {
			programmePointPK= programmeOfferingCVO.getVO().getProgrammePointPK();
		}
		return programmePointPK;
	}


	@Override
	protected List<ProgrammeOfferingCVO> getEntitiesByForeignKeyFromServer(Object foreignKey) throws Exception {
		Long programmePointPK = (Long) foreignKey;

		List<ProgrammeOfferingCVO> programmeOfferingVOs = getProgrammeOfferingMgr().getProgrammeOfferingCVOsByProgrammePointPK(
			programmePointPK,
			CVO_SETTINGS
		);

		return programmeOfferingVOs;
	}


	public List<ProgrammeOfferingCVO> getProgrammeOfferingCVOsByProgrammePointPK(Long programmePointPK) throws Exception {
		return getEntityListByForeignKey(programmePointPK);
	}


	public List<ProgrammeOfferingVO> getProgrammeOfferingVOsByProgrammePointPK(Long programmePointPK) throws Exception {
		List<ProgrammeOfferingVO> programmeOfferingVOs = null;

		List<ProgrammeOfferingCVO> programmeOfferingCVOs = getProgrammeOfferingCVOsByProgrammePointPK(programmePointPK);
		if (programmeOfferingCVOs != null) {
			programmeOfferingVOs = ProgrammeOfferingCVO.getVOs(programmeOfferingCVOs);
		}
		return programmeOfferingVOs;
	}


	public ProgrammeOfferingVO copyProgrammeOffering(
		Long sourceProgrammeOfferingPK,
		Long destProgrammePointPK
	)
	throws ErrorMessageException {
		ProgrammeOfferingVO programmeOfferingVO = getProgrammeOfferingMgr().copyProgrammeOffering(
			sourceProgrammeOfferingPK,
			destProgrammePointPK,
			null,	// dayShift
			true	// withCancelationTerms
		);

		ProgrammeOfferingCVO programmeOfferingCVO = new ProgrammeOfferingCVO();
		programmeOfferingCVO.setVO(programmeOfferingVO);

		// enrich ProgrammeOfferingCVO manually
		programmeOfferingCVO.setNumberOfBookings(0);

		put(programmeOfferingCVO);

		try {
			fireCreate(Collections.singletonList(programmeOfferingVO.getID()));
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}

		return programmeOfferingVO;
	}


	private CacheModelListener<Long> programmePointModelListener = new CacheModelListener<Long>() {
		@Override
		public void dataChange(CacheModelEvent<Long> event) throws Exception {
			if (!serverModel.isLoggedIn()) {
				return;
			}

			try {
    			if (   event.getOperation() == CacheModelOperation.UPDATE
    				|| event.getOperation() == CacheModelOperation.REFRESH
    			) {
					Collection<Long> affectedPKs = new ArrayList<>();
					for (Long programmePointPK : event.getKeyList()) {
						/* Only the cancallation of a Programme Point affects its Work Groups.
						 * Therefore we ignore Programme Points that are not cancelled.
						 */
						ProgrammePointVO programmePointVO = ppModel.getProgrammePointVO(programmePointPK);
						if ( programmePointVO.isCancelled() ) {
    						for (ProgrammeOfferingCVO programmeOfferingCVO : getLoadedAndCachedEntities()) {
    							if (programmePointPK.equals(programmeOfferingCVO.getVO().getProgrammePointPK())) {
    								affectedPKs.add(programmeOfferingCVO.getPK());
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
						for (ProgrammeOfferingCVO programmeOfferingCVO : getLoadedAndCachedEntities()) {
							if (programmePointPK.equals(programmeOfferingCVO.getVO().getProgrammePointPK())) {
								affectedPKs.add(programmeOfferingCVO.getPK());
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


	private CacheModelListener<Long> programmeBookingModelListener = new CacheModelListener<Long>() {
		@Override
		public void dataChange(CacheModelEvent<Long> event) throws Exception {
			if (!serverModel.isLoggedIn()) {
				return;
			}

			try {
				if (
					/* Do not refresh Programme Offerings when Programme Bookings are refreshed to avoid loading them
					 * every time when any Programme Booking is loaded.
					 */
					event.getOperation() == CacheModelOperation.CREATE ||
					event.getOperation() == CacheModelOperation.UPDATE ||
					event.getOperation() == CacheModelOperation.DELETE
				) {
					List<Long> keyList = event.getKeyList();
					if ( notEmpty(keyList) ) {
						List<ProgrammeBookingCVO> programmeBookingCVOs = pbModel.getProgrammeBookingCVOs(keyList);
						Set<Long> programmeOfferingPKsToRefresh = createHashSet(programmeBookingCVOs.size());
						for (ProgrammeBookingCVO programmeBookingCVO : programmeBookingCVOs) {
							programmeOfferingPKsToRefresh.add(programmeBookingCVO.getProgrammeOfferingCVO().getPK());
						}
						refresh(programmeOfferingPKsToRefresh);
					}
				}
			}
			catch (Exception e) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			}
		}
	};

}
