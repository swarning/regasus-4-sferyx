package de.regasus.programme;

import static de.regasus.LookupService.getProgrammeBookingMgr;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.lambdalogic.i18n.LanguageString;
import com.lambdalogic.messeinfo.kernel.ServerMessage;
import com.lambdalogic.messeinfo.kernel.data.AbstractCVO;
import com.lambdalogic.messeinfo.participant.data.ProgrammeBookingCVO;
import com.lambdalogic.messeinfo.participant.data.ProgrammeBookingParameter;
import com.lambdalogic.messeinfo.participant.data.ProgrammeBookingResult;
import com.lambdalogic.messeinfo.participant.data.ProgrammeBookingVO;
import com.lambdalogic.messeinfo.participant.interfaces.ProgrammeBookingCVOSettings;
import com.lambdalogic.messeinfo.participant.interfaces.ProgrammeOfferingCVOSettings;
import com.lambdalogic.messeinfo.participant.interfaces.ProgrammePointCVOSettings;
import com.lambdalogic.messeinfo.participant.interfaces.WorkGroupCVOSettings;
import com.lambdalogic.util.CollectionsHelper;
import com.lambdalogic.util.EqualsHelper;
import com.lambdalogic.util.MapHelper;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.model.CacheModelOperation;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.model.MICacheModel;
import de.regasus.finance.AccountancyModel;
import de.regasus.model.Activator;
import de.regasus.participant.ParticipantModel;

public class ProgrammeBookingModel
extends MICacheModel<Long, ProgrammeBookingCVO>
implements CacheModelListener<Long>, WorkGroupActionModelListener {

	public static final ProgrammeBookingCVOSettings PB_CVO_SETTINGS;

	static {
		PB_CVO_SETTINGS = new ProgrammeBookingCVOSettings();
		PB_CVO_SETTINGS.programmeOfferingCVOSettings = new ProgrammeOfferingCVOSettings();
		PB_CVO_SETTINGS.programmeOfferingCVOSettings.programmePointCVOSettings = new ProgrammePointCVOSettings();
		PB_CVO_SETTINGS.workGroupCVOSettings = new WorkGroupCVOSettings();
		PB_CVO_SETTINGS.withOpenAmount = true;
	}

	private static ProgrammeBookingModel singleton;

	// manager

	// models
	private ParticipantModel participantModel;
	private AccountancyModel accountancyModel;
	private WorkGroupActionModel workGroupActionModel;


	private ProgrammeBookingModel() {
	}


	public static ProgrammeBookingModel getInstance() {
		if (singleton == null) {
			singleton = new ProgrammeBookingModel();
			singleton.initModels();
		}
		return singleton;
	}


	private void initModels() {
		WaitListModel.getInstance().addCoModelListener(this);

		participantModel = ParticipantModel.getInstance();
		participantModel.addListener(this);

		accountancyModel = AccountancyModel.getInstance();
		accountancyModel.addListener(this);

		workGroupActionModel = WorkGroupActionModel.getInstance();
		workGroupActionModel.addListener(this);
	}


	@Override
	protected Long getKey(ProgrammeBookingCVO programmeBookingCVO) {
		return programmeBookingCVO.getPK();
	}


	@Override
	protected boolean isSameVersion(ProgrammeBookingCVO pbCVO1, ProgrammeBookingCVO pbCVO2) {
		boolean sameVersion = false;

		if (pbCVO1 != null && pbCVO2 != null) {
			ProgrammeBookingVO pbVO1 = pbCVO1.getVO();
			ProgrammeBookingVO pbVO2 = pbCVO2.getVO();

			// check editTime
			Date editTime1 = pbVO1.getEditTime();
			Date editTime2 = pbVO2.getEditTime();
			sameVersion = EqualsHelper.isEqual(editTime1, editTime2);

			// check recipients
			if (sameVersion) {
				// check benefit recipients
				Long benefitRecipientPK1 = pbVO1.getBenefitRecipientPK();
				Long benefitRecipientPK2 = pbVO2.getBenefitRecipientPK();
				sameVersion = benefitRecipientPK1.equals(benefitRecipientPK2);

				if (sameVersion) {
					// check invoice recipients
					Long invoiceRecipientPK1 = pbVO1.getInvoiceRecipientPK();
					Long invoiceRecipientPK2 = pbVO2.getInvoiceRecipientPK();
					sameVersion = invoiceRecipientPK1.equals(invoiceRecipientPK2);
				}
			}

			// check openAmounts
			if (sameVersion) {
				BigDecimal openAmount1 = pbCVO1.getOpenAmount();
				BigDecimal openAmount2 = pbCVO2.getOpenAmount();

				sameVersion = openAmount1.compareTo(openAmount2) == 0;
			}
		}

		return sameVersion;
	}


	@Override
	protected boolean isForeignKeySupported() {
		return true;
	}


	@Override
	protected List<Long> getForeignKeyList(ProgrammeBookingCVO programmeBookingCVO) {
		List<Long> foreignKeyList = null;

		if (programmeBookingCVO != null) {
			foreignKeyList = new ArrayList<>(2);

			Long benefitRecipientPK = programmeBookingCVO.getVO().getBenefitRecipientPK();
			if (benefitRecipientPK != null) {
				foreignKeyList.add(benefitRecipientPK);
			}

			Long invoiceRecipientPK = programmeBookingCVO.getVO().getInvoiceRecipientPK();
			if (invoiceRecipientPK != null && !invoiceRecipientPK.equals(benefitRecipientPK)) {
				foreignKeyList.add(invoiceRecipientPK);
			}
		}

		return foreignKeyList;
	}


	public ProgrammeBookingCVO getProgrammeBookingCVO(Long programmeBookingPK)
	throws Exception {
		return super.getEntity(programmeBookingPK);
	}


	@Override
	protected ProgrammeBookingCVO getEntityFromServer(Long programmeBookingPK) throws Exception {
		ProgrammeBookingCVO programmeBookingCVO = getProgrammeBookingMgr().getProgrammeBookingCVO(programmeBookingPK, PB_CVO_SETTINGS);
		return programmeBookingCVO;
	}


	public List<ProgrammeBookingCVO> getProgrammeBookingCVOs(Collection<Long> keyList)
	throws Exception {
		return super.getEntities(keyList);
	}


	@Override
	protected List<ProgrammeBookingCVO> getEntitiesFromServer(Collection<Long> keyList)
	throws Exception {
		List<ProgrammeBookingCVO> programmeBookingCVOs = getProgrammeBookingMgr().getProgrammeBookingCVOs(
			keyList,
			PB_CVO_SETTINGS
		);
		return programmeBookingCVOs;
	}


	public List<ProgrammeBookingCVO> getProgrammeBookingCVOsByRecipient(Long recipientPK)
	throws Exception {
		return getEntityListByForeignKey(recipientPK);
	}


	@Override
	protected List<ProgrammeBookingCVO> getEntitiesByForeignKeyFromServer(Object foreignKey)
	throws Exception {
		Long recipientPK = (Long) foreignKey;
		List<ProgrammeBookingCVO> programmeBookingCVOs = getProgrammeBookingMgr().getProgrammeBookingCVOsByRecipientPK(
			recipientPK,
			PB_CVO_SETTINGS
		);
		return programmeBookingCVOs;
	}


	public List<ServerMessage> bookProgramme(List<ProgrammeBookingParameter> pbpList)
	throws Exception {
		/* The standard pattern for creating entities can't be used here, because the result type
		 * of this method is different than the type of the model (ProgrammeBookingCVO).
		 */
		List<ServerMessage> serverMessages = null;

		if (CollectionsHelper.notEmpty(pbpList) && serverModel.isLoggedIn()) {
			ProgrammeBookingResult programmeBookingResult = getProgrammeBookingMgr().book(pbpList);

			// get ServerMessages
			serverMessages = programmeBookingResult.getServerMessages();

			List<Long> programmeBookingPKs = programmeBookingResult.getProgrammeBookingPKs();
			if (CollectionsHelper.notEmpty(programmeBookingPKs)) {
				List<ProgrammeBookingCVO> programmeBookingCVOs = getEntitiesFromServer(programmeBookingPKs);

				// put entities into model cache
				put(programmeBookingCVOs);

				// fire CacheModelEvents
				fireCreate(programmeBookingPKs);
			}

		}

		return serverMessages;
	}


	/**
	 * Create ProgrammeBookings on basis of wait list bookings.
	 * For each wait list booking a new programme booking is created and the wait list booking is
	 * deleted.
	 *
	 * @param programmeBookingPKs the PKs of the programme bookings to book
	 * @return
	 * @throws Exception if for any wait list booking no programme booking can be created
	 */
//	public List<ServerMessage> bookWaitList(List<Long> programmeBookingPKs)
//	throws Exception {
//		List<ServerMessage> serverMessages = null;
//
//		if (CollectionsHelper.notEmpty(programmeBookingPKs)) {
//			// create new programme bookings, delete wait list bookings
//			ProgrammeBookingResult programmeBookingResult = pbMgr.bookWaitList(programmeBookingPKs);
//			serverMessages = programmeBookingResult.getServerMessages();
//			List<Long> newProgrammeBookingPKs = programmeBookingResult.getProgrammeBookingPKs();
//
//			/* At this point new non-waitlist-ProgrammeBookings have been created for each
//			 * Long in programmeBookingPKs.
//			 * The PKs of the new ProgrammeBookings are stored in newProgrammeBookingPKs .
//			 */
//
//			// load new ProgrammeBookings
//			List<ProgrammeBookingCVO> programmeBookingCVOs = getEntitiesFromServer(programmeBookingPKs);
//
//			// put new ProgrammeBookings into model cache
//			put(programmeBookingCVOs);
//
//			// fire CacheModelEvents for new ProgrammeBookings
//			fireCreate(newProgrammeBookingPKs);
//
//
//			// fire CacheModelEvents for deleted wait list ProgrammeBookings
//			fireDelete(programmeBookingPKs);
//
//			// remove deleted wait list ProgrammeBookings
//			removeEntities(programmeBookingPKs);
//		}
//
//		return serverMessages;
//	}


	public void updateProgrammeBooking(
        ProgrammeBookingVO programmeBookingVO,
        LanguageString info,
        Long workGroupPK,
        boolean workGroupFix
	)
	throws Exception {
		getProgrammeBookingMgr().updateProgrammeBooking(
			programmeBookingVO.getID(),
			info,
			workGroupPK,
			workGroupFix
		);

		handleUpdate(programmeBookingVO.getID());
	}


//	public void updateWaitList(Long programmePointPK, List<Long> programmeBookingPKs) {
//		try {
//			pbMgr.updateWaitList(programmePointPK, programmeBookingPKs);
//
//			handleUpdate(programmeBookingPKs);
//		}
//		catch (Exception e) {
//			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
//		}
//	}


	public void cancelProgrammeBookingsWithCurrentTerms(List<ProgrammeBookingCVO> programmeBookingCVOs) {
		if (CollectionsHelper.notEmpty(programmeBookingCVOs)){
			try {
				List<Long> programmeBookingPKs = AbstractCVO.getPKs(programmeBookingCVOs);
				getProgrammeBookingMgr().cancel(programmeBookingPKs);

				handleUpdate(programmeBookingPKs);
			}
			catch (Exception e) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			}
		}
	}


	public void cancelProgrammeBookingsWithoutFee(List<ProgrammeBookingCVO> programmeBookingCVOs) {
		if (CollectionsHelper.notEmpty(programmeBookingCVOs)){
			try {
				Map<Long, Long> bookingPK2cancelationTermPkMap = MapHelper.createHashMap(programmeBookingCVOs.size());

				for (ProgrammeBookingCVO bookingCVO : programmeBookingCVOs) {
					Long bookingPK = bookingCVO.getPK();
					bookingPK2cancelationTermPkMap.put(bookingPK, null);
				}

				getProgrammeBookingMgr().cancel(bookingPK2cancelationTermPkMap);

				handleUpdate(bookingPK2cancelationTermPkMap.keySet());
			}
			catch (Exception e) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			}
		}
	}


	/**
	 * Cancels programme bookings with cancelation terms.
	 *
	 * @param bookingPK2cancelationTermPkMap Map with the programe bookings to be canceled as keys and
	 *        the corresponding cancelation term as value
	 */
	public void cancelProgrammeBookings(Map<ProgrammeBookingCVO, Long> bookingCVO2cancelTermPkMap) {
		if (bookingCVO2cancelTermPkMap != null && !bookingCVO2cancelTermPkMap.isEmpty()){
			Map<Long, Long> bookingPK2cancelationTermPkMap = MapHelper.createHashMap(bookingCVO2cancelTermPkMap.size());

			/* This operation a bit tricky because the waitListPBs are deleted and nonWaitListPBs
			 * are not. Unfortunately, the CacheModel.handleUpdate() does not detect deleted
			 * entities.
			 * For the waitListPBs we have to fire UPDATE events.
			 * For the nonWaitListPBs we have to fire DELETE events.
			 */
			int size = bookingCVO2cancelTermPkMap.size();
			List<ProgrammeBookingCVO> waitListPBs = new ArrayList<>(size);
			List<Long> nonWaitListPBs = new ArrayList<>(size);

			for (Map.Entry<ProgrammeBookingCVO, Long> entry : bookingCVO2cancelTermPkMap.entrySet()) {
				ProgrammeBookingCVO programmeBookingCVO = entry.getKey();
				Long programmeBookingPK = programmeBookingCVO.getPK();
				Long cancelationTermPK = entry.getValue();

				bookingPK2cancelationTermPkMap.put(programmeBookingPK, cancelationTermPK);

				if (programmeBookingCVO.isWaitList()) {
					waitListPBs.add(programmeBookingCVO);
				}
				else {
					nonWaitListPBs.add(programmeBookingPK);
				}
			}

			try {
				getProgrammeBookingMgr().cancel(bookingPK2cancelationTermPkMap);

				handleUpdate(nonWaitListPBs);
				handleDelete(
					waitListPBs,
					true	// fireCoModelEvent
				);
			}
			catch (Exception e) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			}
		}
	}


	public void changeInvoiceRecipientsOfProgrammeBookings(
		Collection<Long> programmeBookingPKs,
		Long newInvoiceRecipientPK
	)
	throws Exception {
		// Calling Server
		getProgrammeBookingMgr().updateInvoiceRecipients(programmeBookingPKs, newInvoiceRecipientPK);

		handleUpdate(programmeBookingPKs);
	}


	public void changeBenefitRecipientsOfProgrammeBookings(
		Collection<Long> programmeBookingPKs,
		Long newBenefitRecipientPK
	)
	throws Exception {
		// Calling Server
		getProgrammeBookingMgr().updateBenefitRecipients(programmeBookingPKs, newBenefitRecipientPK);

		handleUpdate(programmeBookingPKs);
	}


	public void changeBenefitRecipientsOfProgrammeBookings(
		List<Long> programmeBookingPKs,
		List<Long> newBenefitRecipientPKs
	)
	throws Exception {
		// Calling Server
		getProgrammeBookingMgr().updateBenefitRecipients(programmeBookingPKs, newBenefitRecipientPKs);

		handleUpdate(programmeBookingPKs);
	}


	@Override
	public void dataChange(CacheModelEvent<Long> event) {
		if (!serverModel.isLoggedIn()) {
			return;
		}

		try {
			if (event.getSource() == participantModel) {
				if (event.getOperation() == CacheModelOperation.DELETE) {
					// remove Booking if all of its Participants were deleted

					// PKs of deleted Participants
					List<Long> participantIDs = event.getKeyList();

					// load affected Bookings
					List<ProgrammeBookingCVO> affectedBookings = getLoadedAndCachedBookingsByRecipientPKs(participantIDs);

					// List to collect the PKs of deleted Bookings
					List<Long> deletedBookingPKs = new ArrayList<>(participantIDs.size() * 2);
					// List to collect the PKs of updated Bookings
					List<Long> updatedBookingPKs = new ArrayList<>(participantIDs.size() * 2);

					// determine which Bookings have been deleted or updated
					for (Iterator<ProgrammeBookingCVO> it = affectedBookings.iterator(); it.hasNext();) {
						ProgrammeBookingCVO programmeBookingCVO = it.next();

						if (   participantIDs.contains( programmeBookingCVO.getVO().getInvoiceRecipientPK() )
							&& participantIDs.contains( programmeBookingCVO.getVO().getBenefitRecipientPK() )
						) {
							deletedBookingPKs.add(programmeBookingCVO.getPK());
						}
						else {
							updatedBookingPKs.add(programmeBookingCVO.getPK());
						}
					}

					// remove deleted Bookings from model
					if ( ! deletedBookingPKs.isEmpty()) {
						fireDelete(deletedBookingPKs);
						removeEntities(deletedBookingPKs);
					}

					// refresh updated Bookings
					if ( ! updatedBookingPKs.isEmpty()) {
						refresh(updatedBookingPKs);
					}
				}
				else if (event.getOperation() == CacheModelOperation.UPDATE) {
					/* Refresh a ProgrammeBooking if any of its recipients was updated,
					 * because a cancellation or undoing cancellation may effect the PB, too.
					 */
					List<Long> participantIDs = event.getKeyList();
					List<ProgrammeBookingCVO> changedBookings = getLoadedAndCachedBookingsByRecipientPKs(participantIDs);
					if ( ! changedBookings.isEmpty()) {
						/* fire CacheModelEvent, but no CoModelEvent, because the only CoModel
						 * (WaitListModel) is observing the ParticipantModel itself. This is necessary,
						 * because we only fire an event for entities that exist in this model.
						 * But there may be entities in the WaitListModel that doesn't exist here.
						 */
						List<Long> changedPKs = ProgrammeBookingCVO.getPKs(changedBookings);
						handleUpdate(changedPKs, false);
					}
				}
			}
			else if (event.getSource() == accountancyModel) {
				/* React on payment operations, because they change the openAmount of bookings.
				 * Refresh loaded bookings of foreign key.
				 * Calling refreshForeignKey() is not necessary, because there are no new or
				 * deleted bookings and no recipient changed.
				 */
				List<Long> participantIDs = event.getKeyList();
				for (Long participantID : participantIDs) {
					refreshEntitiesOfForeignKey(participantID);
				}
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	private List<ProgrammeBookingCVO> getLoadedAndCachedBookingsByRecipientPKs(List<Long> participantIDs) {
		List<ProgrammeBookingCVO> programmeBookingCVOs = new ArrayList<>(participantIDs.size() * 10);
		for (Long participantID : participantIDs) {
			for (ProgrammeBookingCVO programmeBookingCVO : getLoadedAndCachedEntities()) {
				if (programmeBookingCVO.getVO().getInvoiceRecipientPK().equals(participantID)
					||
					programmeBookingCVO.getVO().getBenefitRecipientPK().equals(participantID)
				) {
					programmeBookingCVOs.add(programmeBookingCVO);
				}
			}
		}
		return programmeBookingCVOs;
	}


	// **************************************************************************
	// * WorkGroupActionModelListener
	// *

	@Override
	public void handleAssignWorkGroups(Long eventPK, Long programmePointPK) {
		if (eventPK != null) {
			handleWorkGroupsChangedByEvent(eventPK);
		}

		if (programmePointPK != null) {
			handleWorkGroupsChangedByProgrammePoint(programmePointPK);
		}
	}

	@Override
	public void handleFixWorkGroups(Long eventPK, Long programmePointPK) {
		if (eventPK != null) {
			handleWorkGroupsChangedByEvent(eventPK);
		}

		if (programmePointPK != null) {
			handleWorkGroupsChangedByProgrammePoint(programmePointPK);
		}
	}

	@Override
	public void handleUnfixWorkGroups(Long eventPK, Long programmePointPK) {
		if (eventPK != null) {
			handleWorkGroupsChangedByEvent(eventPK);
		}

		if (programmePointPK != null) {
			handleWorkGroupsChangedByProgrammePoint(programmePointPK);
		}
	}


	private void handleWorkGroupsChangedByProgrammePoint(Long programmePointPK) {
		try {
			// refresh ProgrammeBookings of programmePointPK
			List<Long> refreshList = new ArrayList<>();
			List<Long> removeList = new ArrayList<>();


			Collection<ProgrammeBookingCVO> loadedEntities = getLoadedEntities();
			for (ProgrammeBookingCVO programmeBookingCVO : loadedEntities) {
				Long ppPK = programmeBookingCVO.getProgrammeOfferingCVO().getProgrammePointCVO().getPK();
				if (ppPK.equals(programmePointPK)) {
					refreshList.add(programmeBookingCVO.getPK());
				}
			}

			Collection<ProgrammeBookingCVO> cachedEntities = getCachedEntities();
			for (ProgrammeBookingCVO programmeBookingCVO : cachedEntities) {
				Long ppPK = programmeBookingCVO.getProgrammeOfferingCVO().getProgrammePointCVO().getPK();
				if (ppPK.equals(programmePointPK)) {
					removeList.add(programmeBookingCVO.getPK());
				}
			}


			if (!refreshList.isEmpty()) {
				refresh(refreshList);
			}

			if (!removeList.isEmpty()) {
				removeEntities(removeList);
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	private void handleWorkGroupsChangedByEvent(Long eventPK) {
		try {
			// refresh all Participants of eventPK
			List<Long> refreshList = new ArrayList<>();
			List<Long> removeList = new ArrayList<>();

			Collection<ProgrammeBookingCVO> loadedEntities = getLoadedEntities();
			for (ProgrammeBookingCVO programmeBookingCVO : loadedEntities) {
				if (programmeBookingCVO.getVO().getEventPK().equals(eventPK)) {
					refreshList.add(programmeBookingCVO.getPK());
				}
			}

			Collection<ProgrammeBookingCVO> cachedEntities = getCachedEntities();
			for (ProgrammeBookingCVO programmeBookingCVO : cachedEntities) {
				if (programmeBookingCVO.getVO().getEventPK().equals(eventPK)) {
					removeList.add(programmeBookingCVO.getPK());
				}
			}


			if (!refreshList.isEmpty()) {
				refresh(refreshList);
			}

			if (!removeList.isEmpty()) {
				removeEntities(removeList);
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}

	// *
	// * WorkGroupActionModelListener
	// **************************************************************************

}
