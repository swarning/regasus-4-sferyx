package de.regasus.programme;

import static de.regasus.LookupService.getProgrammePointMgr;
import static com.lambdalogic.util.CollectionsHelper.createArrayList;
import static com.lambdalogic.util.CollectionsHelper.createHashSet;
import static com.lambdalogic.util.CollectionsHelper.notEmpty;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.lambdalogic.messeinfo.participant.data.ProgrammeBookingCVO;
import com.lambdalogic.messeinfo.participant.data.ProgrammePointCVO;
import com.lambdalogic.messeinfo.participant.data.ProgrammePointVO;
import com.lambdalogic.messeinfo.participant.interfaces.ProgrammePointCVOSettings;
import com.lambdalogic.util.exception.ErrorMessageException;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.model.CacheModelOperation;

import de.regasus.core.OrderPosition;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.model.MICacheModel;
import de.regasus.event.EventModel;
import de.regasus.model.Activator;


public class ProgrammePointModel extends MICacheModel<Long, ProgrammePointCVO> {

	private static final ProgrammePointCVOSettings CVO_SETTINGS;
	static {
		CVO_SETTINGS = new ProgrammePointCVOSettings();
		CVO_SETTINGS.withNumberOfBookings = true;
	}

	private static ProgrammePointModel singleton;


	private EventModel eventModel;
	private ProgrammeBookingModel pbModel;


	private ProgrammePointModel() {
		super();
	}


	public static ProgrammePointModel getInstance() {
		if (singleton == null) {
			singleton = new ProgrammePointModel();
			singleton.initModels();
		}
		return singleton;
	}


	/**
	 * Initialize references to other Models.
	 * Models are initialized outside the constructor to avoid OutOfMemoryErrors when two Models
	 * reference each other.
	 * This happens because the variable is set after the constructor is finished.
	 * If the constructor calls getInstance() of another Model that calls getInstance() of this Model,
	 * the variable instance is still null. So this Model would be created again and so on.
	 * To avoid this, the constructor has to finish before calling getInstance() of another Model.
	 * The initialization of references to other Models is done in getInstance() right after
	 * the constructor has finished.
	 */
	private void initModels() {
		eventModel = EventModel.getInstance();
		eventModel.addListener(eventModelListener);

		pbModel = ProgrammeBookingModel.getInstance();
		pbModel.addListener(programmeBookingModelListener);
	}


	private CacheModelListener<Long> eventModelListener = new CacheModelListener<Long>() {
		@Override
		public void dataChange(CacheModelEvent<Long> event) {
			if (!serverModel.isLoggedIn()) {
				return;
			}

			try {
				if (event.getOperation() == CacheModelOperation.DELETE) {

					Collection<Long> deletedPKs = new ArrayList<>(event.getKeyList().size());

					for (Long eventPK : event.getKeyList()) {
						for (ProgrammePointCVO programmePointCVO : getLoadedAndCachedEntities()) {
							if (eventPK.equals(programmePointCVO.getEventPK())) {
								deletedPKs.add(programmePointCVO.getPK());
							}
						}

						/* Remove the foreign key whose entity has been deleted from the model before firing the
						 * corresponding CacheModelEvent. The entities shall exist in the model when firing the
						 * CacheModelEvent, but not the structural information about the foreign keys. If a listener gets
						 * the CacheModelEvent and consequently requests the list of all entities of the foreign key, it
						 * shall get an empty list.
						 */
						removeForeignKeyData(eventPK);
					}

					if (!deletedPKs.isEmpty()) {
						fireDelete(deletedPKs);
						removeEntities(deletedPKs);
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
		public void dataChange(CacheModelEvent<Long> event) {
			if (!serverModel.isLoggedIn()) {
				return;
			}

			try {
				if (
					/* Do not refresh Programme Points when Programme Bookings are refreshed to avoid loading them
					 * every time when any Programme Booking is loaded.
					 */
					event.getOperation() == CacheModelOperation.CREATE ||
					event.getOperation() == CacheModelOperation.UPDATE ||
					event.getOperation() == CacheModelOperation.DELETE
				) {
					List<Long> programmeBookingPKs = event.getKeyList();
					if (notEmpty(programmeBookingPKs)) {
						List<ProgrammeBookingCVO> programmeBookingCVOs = pbModel.getProgrammeBookingCVOs(programmeBookingPKs);
						Set<Long> programmePointPKsToRefresh = createHashSet(programmeBookingCVOs.size());
						for (ProgrammeBookingCVO programmeBookingCVO : programmeBookingCVOs) {
							programmePointPKsToRefresh.add(programmeBookingCVO.getVO().getProgrammePointPK());
						}
						refresh(programmePointPKsToRefresh);
					}
				}
			}
			catch (Exception e) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			}
		}
	};


	@Override
	protected Long getKey(ProgrammePointCVO entity) {
		return entity.getPK();
	}


	@Override
	protected boolean isForeignKeySupported() {
		return true;
	}


	@Override
	protected Object getForeignKey(ProgrammePointCVO programmePointCVO) {
		Long fk = null;
		if (programmePointCVO != null) {
			fk = programmePointCVO.getEventPK();
		}
		return fk;
	}


	@Override
	protected ProgrammePointCVO getEntityFromServer(Long programmPointPK)
	throws Exception {
		ProgrammePointCVO programmPointCVO = getProgrammePointMgr().getProgrammePointCVO(programmPointPK, CVO_SETTINGS);
		return programmPointCVO;
	}


	@Override
	protected List<ProgrammePointCVO> getEntitiesFromServer(Collection<Long> programmPointPKs)
	throws Exception {
		List<ProgrammePointCVO> programmePointCVOs = getProgrammePointMgr().getProgrammePointCVOs(programmPointPKs, CVO_SETTINGS);
		return programmePointCVOs;
	}


	@Override
	protected List<ProgrammePointCVO> getEntitiesByForeignKeyFromServer(Object foreignKey)
	throws Exception {
		Long eventPK = (Long) foreignKey;

		List<ProgrammePointCVO> programmePointVOs = getProgrammePointMgr().getProgrammePointCVOsByEventPK(
			eventPK,
			false, // onlyWithWorkGroup
			false, // onlyWithWaitList
			CVO_SETTINGS
		);

		return programmePointVOs;
	}


	@Override
	protected ProgrammePointCVO createEntityOnServer(ProgrammePointCVO programmePointCVO)
	throws Exception {
		ProgrammePointVO programmePointVO = programmePointCVO.getVO();

		// temporarily set the position to pass validation
		programmePointVO.setPosition(0);
		programmePointVO.validate();
		// remove position because it should be calculated automatically by the server
		programmePointVO.setPosition(null);

		programmePointVO = getProgrammePointMgr().createProgrammePoint(programmePointVO);
		programmePointCVO.setVO(programmePointVO);

		return programmePointCVO;
	}


	@Override
	protected ProgrammePointCVO updateEntityOnServer(ProgrammePointCVO programmePointCVO)
	throws Exception {
		ProgrammePointVO programmePointVO = programmePointCVO.getVO();
		programmePointVO.validate();
		programmePointVO = getProgrammePointMgr().updateProgrammePoint(programmePointVO);
		programmePointCVO.setVO(programmePointVO);
		return programmePointCVO;
	}


	@Override
	protected void deleteEntityOnServer(ProgrammePointCVO programmePointCVO) throws Exception {
		if (programmePointCVO != null) {
			Long id = programmePointCVO.getPK();
			getProgrammePointMgr().deleteProgrammePoint(id);
		}
	}


	public ProgrammePointCVO getProgrammePointCVO(Long programmePointPK)
	throws Exception {
		return super.getEntity(programmePointPK);
	}


	public ProgrammePointVO getProgrammePointVO(Long programmePointPK) throws Exception {
		ProgrammePointVO programmePointVO = null;
		ProgrammePointCVO programmePointCVO = super.getEntity(programmePointPK);
		if (programmePointCVO != null) {
			programmePointVO = programmePointCVO.getVO();
		}
		return programmePointVO;
	}


	public List<ProgrammePointCVO> getProgrammePointCVOs(List<Long> programmePointPKs)
	throws Exception {
		return super.getEntities(programmePointPKs);
	}


	public List<ProgrammePointVO> getProgrammePointVOs(List<Long> programmePointPKs) throws Exception {
		List<ProgrammePointVO> programmePointVOs = null;
		List<ProgrammePointCVO> programmePointCVOs = getProgrammePointCVOs(programmePointPKs);
		if (programmePointCVOs != null) {
			programmePointVOs = ProgrammePointCVO.getVOs(programmePointCVOs);
		}
		return programmePointVOs;
	}


	public ProgrammePointVO create(ProgrammePointVO programmePointVO)
	throws Exception {
		ProgrammePointCVO programmePointCVO = new ProgrammePointCVO();
		programmePointCVO.setVO(programmePointVO);

		// add missing values according to CVO_SETTINGS
		programmePointCVO.setNumberOfBookings( 0 );

		programmePointCVO = super.create(programmePointCVO);
		return programmePointCVO.getVO();
	}


	public ProgrammePointVO update(ProgrammePointVO programmePointVO) throws Exception {
		ProgrammePointCVO programmePointCVO = getProgrammePointCVO(programmePointVO.getPK());

		/* The entity that will be updated has to be cloned, because CacheModel does not accept the same instance as in
		 * the cache if the Model supports foreign keys.
		 */
		programmePointCVO = programmePointCVO.clone();

		// put VO with new data into CVO
		programmePointCVO.setVO(programmePointVO);

		programmePointCVO = super.update(programmePointCVO);

		return programmePointCVO.getVO();
	}


	/**
	 * Move a {@link ProgrammePointVO} before or after another one.
	 * The target Programme Point must belong to the same Event.
	 *
	 * @param movedProgrammePointId
	 * @param orderPosition
	 * @param targetProgrammePointId
	 * @throws Exception
	 */
	public void move(Long movedProgrammePointId, OrderPosition orderPosition, Long targetProgrammePointId)
	throws Exception {
		List<ProgrammePointVO> ppVOs = getProgrammePointMgr().move(
			movedProgrammePointId,
			orderPosition,
			targetProgrammePointId
		);

		// copy extensions of possibly existing data from the cache
		List<Long> ppIds = ProgrammePointVO.getPKs(ppVOs);
		HashMap<Long, ProgrammePointVO> ppMap = ProgrammePointVO.abstractVOs2Map(ppVOs);

		List<ProgrammePointCVO> ppCVOs = getProgrammePointCVOs(ppIds);

		for (ProgrammePointCVO ppCVO : ppCVOs) {
			ProgrammePointVO ppVO = ppMap.get( ppCVO.getId() );
			ppCVO.setVO(ppVO);
		}

		put(ppCVOs);

		fireDataChange(CacheModelOperation.UPDATE, ppIds);
	}


	public void cancel(Long programmePointPK) throws Exception {
		getProgrammePointMgr().cancelProgrammePoint(programmePointPK);
		handleUpdate(programmePointPK);
	}


	public void delete(Long programmePointPK) throws Exception {
		ProgrammePointCVO programmePointCVO = getProgrammePointCVO(programmePointPK);
		super.delete(programmePointCVO);
	}


	public List<ProgrammePointCVO> getProgrammePointCVOsByEventPK(Long eventPK, boolean includeCancelled)
	throws Exception {
		List<ProgrammePointCVO> ppList = getEntityListByForeignKey(eventPK);

		if ( ! includeCancelled) {
			ppList = ppList.stream().filter( pp -> !pp.isCancelled() ).collect( Collectors.toList() );
		}

		return ppList;
	}


	public List<ProgrammePointCVO> getProgrammePointCVOsByEventPK(Long eventPK)
	throws Exception {
		return getProgrammePointCVOsByEventPK(eventPK, false);
	}


	public List<ProgrammePointVO> getProgrammePointVOsByEventPK(Long eventPK, boolean includeCancelled)
	throws Exception {
		List<ProgrammePointVO> programmePointVOs = null;

		List<ProgrammePointCVO> programmePointCVOs = getProgrammePointCVOsByEventPK(eventPK, includeCancelled);
		if (programmePointCVOs != null) {
			programmePointVOs = ProgrammePointCVO.getVOs(programmePointCVOs);
		}

		return programmePointVOs;
	}


	public List<ProgrammePointVO> getProgrammePointVOsByEventPK(Long eventPK) throws Exception {
		return getProgrammePointVOsByEventPK(eventPK, false);
	}


	public ProgrammePointVO copyProgrammePoint(
		Long sourceProgrammePointPK,
		Long destEventPK
	)
	throws ErrorMessageException {
		ProgrammePointVO programmePointVO = getProgrammePointMgr().copyProgrammePoint(
			sourceProgrammePointPK,
			destEventPK,
			null,	// dayShift
			true,	// withOfferings
			true,	// withCancelationTerms
			true	// withWorkGroups
		);

		ProgrammePointCVO programmePointCVO = new ProgrammePointCVO();
		programmePointCVO.setVO(programmePointVO);

		// add missing values according to CVO_SETTINGS
		programmePointCVO.setNumberOfBookings( 0 );

		put(programmePointCVO);

		try {
			fireCreate( Collections.singletonList(programmePointVO.getID()) );
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}

		return programmePointVO;
	}


	/**
	 * Get Programme Points where the current number of bookings exceeds the warn number.
	 * @param eventPK
	 * @return
	 * @throws Exception
	 */
	public List<ProgrammePointCVO> getProgrammePointCVOsWithExceededWarnNumber(Long eventPK)
	throws Exception {
		List<ProgrammePointCVO> newProgrammePointCVOs = null;

		if (eventPK != null) {
			List<ProgrammePointCVO> programmePointCVOs = getProgrammePointCVOsByEventPK(eventPK);
			if (notEmpty(programmePointCVOs)) {
				newProgrammePointCVOs = createArrayList(programmePointCVOs.size());

				// check condition programme points number of bookings exceeds warn number
				for (ProgrammePointCVO ppCVO : programmePointCVOs) {
					Boolean warnNumberExceeded = ppCVO.isWarnNumberExceeded();
					if (warnNumberExceeded == null) {
						throw new ErrorMessageException("ProgrammePointCVO has no numberOfBookings.");
					}

					if (warnNumberExceeded) {
						newProgrammePointCVOs.add(ppCVO);
					}
				}
			}
		}

		if (newProgrammePointCVOs == null) {
			newProgrammePointCVOs = createArrayList(0);
		}

		return newProgrammePointCVOs;
	}


}
