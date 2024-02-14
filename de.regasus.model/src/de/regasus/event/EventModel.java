package de.regasus.event;

import static com.lambdalogic.util.CollectionsHelper.createArrayList;
import static de.regasus.LookupService.getEventMgr;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.lambdalogic.messeinfo.participant.data.EventCVO;
import com.lambdalogic.messeinfo.participant.data.EventVO;
import com.lambdalogic.messeinfo.participant.interfaces.EventCVOSettings;
import com.lambdalogic.util.DateHelper;
import com.lambdalogic.util.exception.ErrorMessageException;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.model.CacheModelOperation;

import de.regasus.LookupService;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.model.MICacheModel;
import de.regasus.finance.PaymentSystemSetupModel;
import de.regasus.model.Activator;
import de.regasus.programme.ProgrammePointModel;


public class EventModel extends MICacheModel<Long, EventCVO> {
	private static EventModel singleton = null;


	private static final EventCVOSettings standardEventCVOSettings;
	private static final EventCVOSettings extendedEventCVOSettings;

	private boolean showClosedEvents = false;

	static {
		/* If the settings change, the methods update() and copyEvent() must be adapted!
		 */
		standardEventCVOSettings = new EventCVOSettings();

		extendedEventCVOSettings = new EventCVOSettings();
		extendedEventCVOSettings.withParticipantTypePKs = true;
	}


	private PaymentSystemSetupModel paymentSystemSetupModel;
	private ProgrammePointModel programmePointModel;


	private EventModel() {
	}


	public static EventModel getInstance() {
		if (singleton == null) {
			singleton = new EventModel();
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
		paymentSystemSetupModel = PaymentSystemSetupModel.getInstance();
		paymentSystemSetupModel.addListener(paymentSystemSetupListener);

		programmePointModel = ProgrammePointModel.getInstance();
		programmePointModel.addListener(programmePointModelListener);
	}


	private CacheModelListener<Long> paymentSystemSetupListener = new CacheModelListener<Long>() {
    	@Override
    	public void dataChange(CacheModelEvent<Long> event) {
    		if (!serverModel.isLoggedIn()) {
    			return;
    		}

    		try {
				if (event.getOperation() == CacheModelOperation.DELETE) {
					List<Long> eventPKsToRefresh = new ArrayList<>();
					for (Long payEngineSetupID : event.getKeyList()) {
						for (EventCVO eventCVO : getLoadedAndCachedEntities()) {
							if (payEngineSetupID.equals(eventCVO.getEventVO().getPaymentSystemSetupPK())) {
								eventPKsToRefresh.add(eventCVO.getPK());
							}
						}
					}
					if (!eventPKsToRefresh.isEmpty()) {
						refresh(eventPKsToRefresh);
					}
				}
    		}
    		catch (Exception e) {
    			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
    		}
    	}
	};


	private CacheModelListener<Long> programmePointModelListener = new CacheModelListener<Long>() {
		@Override
		public void dataChange(CacheModelEvent<Long> event) throws Exception {
    		if (!serverModel.isLoggedIn()) {
    			return;
    		}

    		try {
				if (event.getOperation() == CacheModelOperation.DELETE) {
					for (Long programmePointId : event.getKeyList()) {
						for (EventCVO eventCVO : getLoadedAndCachedEntities()) {
							if (programmePointId.equals(eventCVO.getEventVO().getDigitalEventLeadProgrammePointId())) {
								refresh( eventCVO.getPK() );
							}
						}
					}
				}
    		}
    		catch (Exception e) {
    			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
    		}
		}
	};


	@Override
	protected Long getKey(EventCVO entity) {
		return entity.getPK();
	}


	// *****************************************************************************************************************
	// * get one Event
	// *

	@Override
	protected EventCVO getEntityFromServer(Long eventPK) throws Exception {
		EventCVO eventCVO = getEventMgr().getEventCVO(eventPK, standardEventCVOSettings);
		return eventCVO;
	}


	public EventCVO getEventCVO(Long eventPK) throws Exception {
		return super.getEntity(eventPK);
	}


	public EventVO getEventVO(Long eventPK) throws Exception {
		EventCVO eventCVO = getEventCVO(eventPK);
		if (eventCVO != null) {
			return eventCVO.getEventVO();
		}
		return null;
	}

	// *
	// * get one Event
	// *****************************************************************************************************************

	// *****************************************************************************************************************
	// * get many Events
	// *

	@Override
	protected List<EventCVO> getEntitiesFromServer(Collection<Long> eventPKs) throws Exception {
		List<EventCVO> eventCVOs = getEventMgr().getEventCVOs(eventPKs, standardEventCVOSettings);
		return eventCVOs;
	}


	public List<EventCVO> getEventCVOs(Collection<Long> eventPKs) throws Exception {
		return super.getEntities( createArrayList(eventPKs) );
	}


	public List<EventVO> getEventVOs(Collection<Long> eventPKs) throws Exception {
		List<EventCVO> eventCVOs = getEventCVOs(eventPKs);
		if (eventCVOs != null) {
			return EventCVO.getVOs(eventCVOs);
		}
		return null;
	}

	// *
	// * get many Events
	// *****************************************************************************************************************

	// *****************************************************************************************************************
	// * get Events of an Event Group
	// *

	@Override
	protected boolean isForeignKeySupported() {
		return true;
	}


	@Override
	protected Object getForeignKey(EventCVO eventCVO) {
		Long fk = null;
		if (eventCVO != null) {
			fk = eventCVO.getEventGroupPK();
		}
		return fk;
	}


	@Override
	protected List<EventCVO> getEntitiesByForeignKeyFromServer(Object foreignKey)
	throws Exception {
		Long eventGroupPK = (Long) foreignKey;

		List<EventCVO> eventVOs = getEventMgr().getEventCVOsByEventGroupPK(
			eventGroupPK,
			showClosedEvents,
			standardEventCVOSettings
		);

		return eventVOs;
	}


	public Collection<EventCVO> getEventCVOsByGroup(Long eventGroupPK, boolean onlyUnclosed) throws Exception {
		Collection<EventCVO> resultEventCVOs = null;

		// get Events (if closed Events are included depends on showClosedEvents)
		Collection<EventCVO> eventCVOs = getEntityListByForeignKey(eventGroupPK);

		// if closed Events are included (isShowClosedEvents()) but only unclosed should be returned
		if (onlyUnclosed && isShowClosedEvents()) {
			// filter out closed Events
			resultEventCVOs = createArrayList(eventCVOs.size());
			for (EventCVO eventCVO : eventCVOs) {
				if (!eventCVO.getVO().isClosed()) {
					resultEventCVOs.add(eventCVO);
				}
			}
		}
		else {
			resultEventCVOs = eventCVOs;
		}

		return resultEventCVOs;
	}


	public Collection<EventCVO> getEventCVOsByGroup(Long eventGroupPK) throws Exception {
		return getEventCVOsByGroup(eventGroupPK, false);
	}


	public Collection<EventVO> getEventVOsByGroup(Long eventGroupPK, boolean onlyUnclosed) throws Exception {
		return EventCVO.getVOs( getEventCVOsByGroup(eventGroupPK, onlyUnclosed) );
	}


	public Collection<EventVO> getEventVOsByGroup(Long eventGroupPK) throws Exception {
		return getEventVOsByGroup(eventGroupPK, false);
	}

	// *
	// * get Events of an Event Group
	// *****************************************************************************************************************

	// *****************************************************************************************************************
	// * get all Events
	// *

	@Override
	protected List<EventCVO> getAllEntitiesFromServer() throws Exception {
		List<EventCVO> eventCVOs = getEventMgr().getEventCVOs(standardEventCVOSettings, showClosedEvents);
		return eventCVOs;
	}


	public Collection<EventCVO> getAllEventCVOs(boolean onlyUnclosed) throws Exception {
		Collection<EventCVO> resultEventCVOs = null;

		// get all Events (if closed Events are included depends on showClosedEvents)
		Collection<EventCVO> allEventCVOs = getAllEntities();

		if (onlyUnclosed) {
			// filter out closed Events
			resultEventCVOs = createArrayList(allEventCVOs.size());
			for (EventCVO eventCVO : allEventCVOs) {
				if (!eventCVO.getVO().isClosed()) {
					resultEventCVOs.add(eventCVO);
				}
			}
		}
		else {
			resultEventCVOs = allEventCVOs;
		}

		return resultEventCVOs;
	}


	public Collection<EventCVO> getAllEventCVOs() throws Exception {
		return getAllEventCVOs(false);
	}


	public Collection<EventVO> getAllEventVOs(boolean onlyUnclosed) throws Exception {
		return EventCVO.getVOs( getAllEventCVOs(onlyUnclosed) );
	}


	public Collection<EventVO> getAllEventVOs() throws Exception {
		return getAllEventVOs(false);
	}

	// *
	// * get all Events
	// *****************************************************************************************************************


	public boolean isShowClosedEvents() throws Exception {
		return showClosedEvents;
	}


	public void setShowClosedEvents(boolean b) throws Exception {
		showClosedEvents = b;
		refresh();
	}


	public boolean toggleShowClosedEvents() throws Exception {
		showClosedEvents = !showClosedEvents;
		refresh();
		return showClosedEvents;
	}


	@Override
	protected EventCVO createEntityOnServer(EventCVO eventCVO) throws Exception {
		EventVO eventVO = eventCVO.getVO();
		eventVO.validate();
		eventVO = getEventMgr().createEvent(eventVO);
		eventCVO.setVO(eventVO);
		return eventCVO;
	}


	public EventVO create(EventVO eventVO) throws Exception {
		EventCVO eventCVO = new EventCVO();
		eventCVO.setVO(eventVO);
		eventCVO = super.create(eventCVO);
		return eventCVO.getVO();
	}


	@Override
	protected EventCVO updateEntityOnServer(EventCVO eventCVO) throws Exception {
		EventVO eventVO = eventCVO.getVO();
		eventVO.validate();
		eventVO = getEventMgr().updateEvent(eventVO);
		eventCVO.setVO(eventVO);
		return eventCVO;
	}


	public EventVO update(EventVO eventVO) throws Exception {
		EventCVO eventCVO = new EventCVO();
		eventCVO.setVO(eventVO);
		eventCVO = super.update(eventCVO);
		return eventCVO.getVO();
	}


	@Override
	protected void deleteEntityOnServer(EventCVO eventCVO) throws Exception {
		if (eventCVO != null) {
			Long eventPK = eventCVO.getPK();

			long startTime = System.currentTimeMillis();
			System.out.println("Deleting Event " + eventCVO.getLabel().getString());
			try {
				LookupService.setReadTimeout(24 * 60 * 60 * 1000); // 24 hours
				getEventMgr().deleteEvent(eventPK, true);
			}
			finally {
				LookupService.initReadTimeout();

				long duration = System.currentTimeMillis() - startTime;
				System.out.println("Deleting Event " + eventCVO.getLabel().getString() + " lasted " + DateHelper.getTimeLagString(duration));
			}
		}
	}


	public void delete(EventVO eventVO) throws Exception {
		EventCVO eventCVO = new EventCVO();
		eventCVO.setVO(eventVO);
		super.delete(eventCVO);
	}


	public void updateNextParticipantNumber(EventVO eventVO, Integer nextNumber) throws Exception {
		getEventMgr().setNextParticipantNo(eventVO.getPK(), nextNumber);
		handleUpdate(eventVO.getPK());
	}


	public void closeEvent(
		EventVO eventVO,
		boolean deleteHistory,
		boolean deleteACL,
		boolean deleteLeads,
		boolean deleteCreditCard,
		boolean deletePortalPhotos
	)
	throws Exception {
		if ( ! eventVO.isClosed() ) {
			getEventMgr().closeEvent(
				eventVO.getPK(),
				deleteHistory,
				deleteACL,
				deleteLeads,
				deleteCreditCard,
				deletePortalPhotos
			);
			handleUpdate(eventVO.getPK());
			fireOpenCloseEvent(eventVO.getPK());
		}
	}


	public void openEvent(EventVO eventVO) throws Exception  {
		if ( eventVO.isClosed() ) {
			getEventMgr().openEvent(
				eventVO.getPK()
			);
			handleUpdate(eventVO.getPK());
			fireOpenCloseEvent(eventVO.getPK());
		}
	}


	public void resetEvent(Long eventPK) throws Exception {
		getEventMgr().resetEvent(eventPK);
		handleUpdate(eventPK);
	}


	public EventVO copyEvent(Long sourceEventPK, String mnemonic, int dayShift)
	throws Exception {
		EventVO eventVO = getEventMgr().copyEvent(
			sourceEventPK,
			mnemonic,
			dayShift,
			true,	// withProgrammePoints
			true,	// withProgrammeOfferings
			true,	// withProgrammeCancelationTerms
			true,	// withWorkGroups
			true,	// withHotelContingents
			true,	// withHotelOfferings
			true,	// withHotelCancelationTerms
			true,	// withInvoiceNoRanges
			true	// withParticipantCustomFields
		);

		EventCVO eventCVO = new EventCVO();
		eventCVO.setEventVO(eventVO);
		put(eventCVO);

		List<Long> primaryKeyList = Collections.singletonList(eventVO.getID());
		fireCreate(primaryKeyList);

		return eventVO;
	}


//	public void deleteTemplate(DataStoreVO dataStoreVO, boolean force)
//	throws Exception {
//		getEmailTemplateMgr().deleteNoteTemplate(dataStoreVO.getPK(), force);
//		// As long as we still use DataStore we need to call getDataStoreMgr().delete(Long id).
//		getDataStoreMgr().delete(dataStoreVO.getPK());
//		handleUpdate(dataStoreVO.getEventPK());
//	}
//
//
//	public void deleteTemplates(Collection<DataStoreVO> dataStoreVOs, boolean force)
//	throws Exception {
//		Set<Long> eventPKs = new HashSet<>();
//
//		for (DataStoreVO dataStoreVO : dataStoreVOs) {
//			getEmailTemplateMgr().deleteNoteTemplate(dataStoreVO.getPK(), force);
//			// As long as we still use DataStore we need to call getDataStoreMgr().delete(Long id).
//			getDataStoreMgr().delete(dataStoreVO.getPK());
//			eventPKs.add(dataStoreVO.getEventPK());
//		}
//
//		handleUpdate(eventPKs);
//	}


	// **************************************************************************
	// * Extensions
	// *

	@Override
	protected boolean isExtended(EventCVO eventCVO) {
		// Do not check the data, but always the settings, because the data may initialized lazily.
		// Here there are no settings, so we can only check the data.
		return
			eventCVO != null &&
			eventCVO.getParticipantTypePKs() != null;
	}


	@Override
	protected void copyExtendedValues(EventCVO from, EventCVO to) {
		to.copyTransientValuesFrom(from, false);
	}


	@Override
	protected EventCVO getExtendedEntityFromServer(Long pk) throws Exception {
		final EventCVO eventCVO = getEventMgr().getEventCVO(pk, extendedEventCVOSettings);
		if (eventCVO == null) {
			throw new ErrorMessageException("Event not found: " + pk);
		}

		return eventCVO;
	}

	@Override
	protected List<EventCVO> getExtendedEntitiesFromServer(List<Long> eventPKs)
	throws Exception {
		List<EventCVO> eventCVOs = getEventMgr().getEventCVOs(eventPKs, extendedEventCVOSettings);

		return eventCVOs;
	}


	public EventCVO getExtendedEventCVO(Long eventPK)
	throws Exception {
		return super.getExtendedEntity(eventPK);
	}


	public List<EventCVO> getExtendedEventCVOs(List<Long> eventPKs)
	throws Exception {
		return super.getExtendedEntities(eventPKs);
	}

	// *
	// * Extensions
	// **************************************************************************


	// *****************************************************************************************************************
	// * Listener for Open/Close Events
	// *

	private Set<CacheModelListener<Long>> openCloseListeners = null;


	public void addOpenCloseListener(CacheModelListener<Long> listener) {
		if (openCloseListeners == null) {
			openCloseListeners = new HashSet<>();
		}
		openCloseListeners.add(listener);
	}


	public void removeOpenCloseListener(CacheModelListener<Long> listener) {
		if (openCloseListeners != null) {
			openCloseListeners.remove(listener);
		}
	}


	private void fireOpenCloseEvent(Long eventPK) {
		if (openCloseListeners != null) {
			CacheModelEvent<Long> event = new CacheModelEvent<>(this, CacheModelOperation.UPDATE, eventPK);
			for (CacheModelListener<Long> cacheModelListener : openCloseListeners) {
				try {
					cacheModelListener.dataChange(event);
				}
				catch (Exception e) {
					com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
				}
			}
		}
	}

	// *
	// * Listener for Open/Close Events
	// *****************************************************************************************************************

}
