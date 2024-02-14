/**
 * HotelBookingModel.java
 * created on 07.10.2013 11:25:37
 */
package de.regasus.hotel;

import static com.lambdalogic.util.CollectionsHelper.notEmpty;
import static de.regasus.LookupService.getHotelBookingMgr;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.lambdalogic.i18n.LanguageString;
import com.lambdalogic.messeinfo.hotel.data.ArrivalInfo;
import com.lambdalogic.messeinfo.hotel.data.HotelBookingCVO;
import com.lambdalogic.messeinfo.hotel.data.HotelBookingCVOSettings;
import com.lambdalogic.messeinfo.hotel.data.HotelBookingChangeFlags;
import com.lambdalogic.messeinfo.hotel.data.HotelBookingParameter;
import com.lambdalogic.messeinfo.hotel.data.HotelBookingPaymentCondition;
import com.lambdalogic.messeinfo.hotel.data.HotelBookingVO;
import com.lambdalogic.messeinfo.hotel.data.HotelContingentCVOSettings;
import com.lambdalogic.messeinfo.hotel.data.HotelOfferingCVOSettings;
import com.lambdalogic.messeinfo.hotel.data.SmokerType;
import com.lambdalogic.messeinfo.kernel.data.AbstractCVO;
import com.lambdalogic.time.I18NDate;
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

public class HotelBookingModel
extends MICacheModel<Long, HotelBookingCVO>
implements CacheModelListener<Long> {

	private static final HotelBookingCVOSettings HB_CVO_SETTINGS;

	static {
		HB_CVO_SETTINGS = new HotelBookingCVOSettings();
		HB_CVO_SETTINGS.hotelOfferingCVOSettings = new HotelOfferingCVOSettings();
		HB_CVO_SETTINGS.hotelOfferingCVOSettings.hotelContingentCVOSettings = new HotelContingentCVOSettings();
		HB_CVO_SETTINGS.withRoomDefinition = true;
		HB_CVO_SETTINGS.withOpenAmount = true;
		HB_CVO_SETTINGS.withHotelName = true;
	}


	private static HotelBookingModel singleton;

	// models
	private ParticipantModel participantModel;
	private AccountancyModel accountancyModel;


	public HotelBookingModel() {
	}


	public static HotelBookingModel getInstance() {
		if (singleton == null) {
			singleton = new HotelBookingModel();
			singleton.initModels();
		}
		return singleton;
	}


	private void initModels() {
		participantModel = ParticipantModel.getInstance();
		participantModel.addListener(this);

		accountancyModel = AccountancyModel.getInstance();
		accountancyModel.addListener(this);
	}


	public List<Long> bookHotel(List<HotelBookingParameter> hotelBookingParameters)
	throws Exception {
		List<Long> hotelBookingPKs = null;
		if ( notEmpty(hotelBookingParameters) ) {
			// Collect the involved participantPKs, each at most once
			Collection<Long> participantPKs = new HashSet<Long>();
			for (HotelBookingParameter hbp : hotelBookingParameters) {
				participantPKs.add( hbp.getInvoiceRecipientPK() );
				for (Long benefitRecipientPK : hbp.getBenefitRecipientPKs()) {
					participantPKs.add(benefitRecipientPK);
				}
			}

			hotelBookingPKs = getHotelBookingMgr().book(hotelBookingParameters);

			List<HotelBookingCVO> hotelBookingCVOs = getEntitiesFromServer(hotelBookingPKs);
			put(hotelBookingCVOs);

			fireCreate(hotelBookingPKs);

			// refresh Participant data of all benefit recipients, because their HotelCostCoverage might have changed
			refreshBenefitRecipients(hotelBookingParameters);
		}
		return hotelBookingPKs;
	}


	private void refreshBenefitRecipients(Collection<?> hotelBookingSomethingCol) throws Exception {
		// collect IDs of benefit recipients
		Set<Long> benefitRecipientIds = new HashSet<>();
		for (Object hotelBookingSomething : hotelBookingSomethingCol) {
			if (hotelBookingSomething instanceof HotelBookingParameter) {
				HotelBookingParameter hbp = (HotelBookingParameter) hotelBookingSomething;
				benefitRecipientIds.addAll( hbp.getBenefitRecipientPKs() );
			}
			else if (hotelBookingSomething instanceof HotelBookingCVO) {
				HotelBookingCVO hbCVO = (HotelBookingCVO) hotelBookingSomething;
				benefitRecipientIds.addAll( hbCVO.getHotelBookingVO().getBenefitRecipientPKs() );
			}
		}

		// refresh Participant data
		ParticipantModel.getInstance().refresh(benefitRecipientIds);
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
					List<HotelBookingCVO> affectedBookings = getLoadedAndCachedBookingsByRecipientPKs(participantIDs);

					// List to collect the PKs of deleted Bookings
					List<Long> deletedBookingPKs = new ArrayList<>(participantIDs.size() * 2);
					// List to collect the PKs of updated Bookings
					List<Long> updatedBookingPKs = new ArrayList<>(participantIDs.size() * 2);

					// determine which Bookings have been deleted or updated
					for (Iterator<HotelBookingCVO> it = affectedBookings.iterator(); it.hasNext();) {
						HotelBookingCVO hotelBookingCVO = it.next();

						if (   participantIDs.contains( hotelBookingCVO.getVO().getInvoiceRecipientPK() )
							&& participantIDs.containsAll( hotelBookingCVO.getVO().getBenefitRecipientPKs() )
						) {
							deletedBookingPKs.add(hotelBookingCVO.getPK());
						}
						else {
							updatedBookingPKs.add(hotelBookingCVO.getPK());
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
					/* Refresh a Hotel Booking if any of its recipients was updated,
					 * because a cancellation or undoing cancellation may effect the Hotel Booking, too.
					 */
					List<Long> participantIDs = event.getKeyList();
					List<HotelBookingCVO> changedBookings = getLoadedAndCachedBookingsByRecipientPKs(participantIDs);
					if ( ! changedBookings.isEmpty()) {
						List<Long> changedPKs = HotelBookingCVO.getPKs(changedBookings);
						handleUpdate(changedPKs);
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


	private List<HotelBookingCVO> getLoadedAndCachedBookingsByRecipientPKs(List<Long> participantIDs) {
		List<HotelBookingCVO> hotelBookingCVOs = new ArrayList<>(participantIDs.size() * 10);
		for (Long participantID : participantIDs) {
			for (HotelBookingCVO hotelBookingCVO : getLoadedAndCachedEntities()) {
				if (hotelBookingCVO.getVO().getInvoiceRecipientPK().equals(participantID)
					||
					hotelBookingCVO.getVO().getBenefitRecipientPKs().contains(participantID)
				) {
					hotelBookingCVOs.add(hotelBookingCVO);
				}
			}
		}
		return hotelBookingCVOs;
	}


	@Override
	protected Long getKey(HotelBookingCVO hotelBookingCVO) {
		return hotelBookingCVO.getPK();
	}


	@Override
	protected boolean isSameVersion(HotelBookingCVO hbCVO1, HotelBookingCVO hbCVO2) {
		boolean sameVersion = false;

		if (hbCVO1 != null && hbCVO2 != null) {
			HotelBookingVO hbVO1 = hbCVO1.getVO();
			HotelBookingVO hbVO2 = hbCVO2.getVO();

			// check editTime
			Date editTime1 = hbVO1.getEditTime();
			Date editTime2 = hbVO2.getEditTime();
			sameVersion = EqualsHelper.isEqual(editTime1, editTime2);

			// check recipients
			if (sameVersion) {
				// check benefit recipients
				List<Long> benefitRecipientPKs1 = hbVO1.getBenefitRecipientPKs();
				List<Long> benefitRecipientPKs2 = hbVO2.getBenefitRecipientPKs();
				sameVersion = CollectionsHelper.isEqual(benefitRecipientPKs1, benefitRecipientPKs2);

				if (sameVersion) {
					// check invoice recipients
					Long invoiceRecipientPK1 = hbVO1.getInvoiceRecipientPK();
					Long invoiceRecipientPK2 = hbVO2.getInvoiceRecipientPK();
					sameVersion = invoiceRecipientPK1.equals(invoiceRecipientPK2);
				}
			}

			// check openAmounts
			if (sameVersion) {
				BigDecimal openAmount1 = hbCVO1.getOpenAmount();
				BigDecimal openAmount2 = hbCVO2.getOpenAmount();

				sameVersion = openAmount1.compareTo(openAmount2) == 0;
			}
		}

		return sameVersion;
	}


	@Override
	protected HotelBookingCVO getEntityFromServer(Long hotelBookingPK) throws Exception {
		HotelBookingVO hotelBookingVO = getHotelBookingMgr().getHotelBookingVO(hotelBookingPK);
		HotelBookingCVO hotelBookingCVO = null;
		if (hotelBookingVO != null) {
			List<HotelBookingCVO> hotelBookingCVOs = HotelBookingCVO.convertHotelBookingVO2CVO(Collections.singletonList(hotelBookingVO));
			List<HotelBookingCVO> hotelBookingCVOList = getHotelBookingMgr().enrichHotelBookingCVOs(hotelBookingCVOs, HB_CVO_SETTINGS);
			hotelBookingCVO = hotelBookingCVOList.iterator().next();
		}
		return hotelBookingCVO;
	}


	@Override
	protected boolean isForeignKeySupported() {
		return true;
	}


	@Override
	protected List<Long> getForeignKeyList(HotelBookingCVO hotelBookingCVO) {
		List<Long> foreignKeyList = null;

		if (hotelBookingCVO != null) {
			foreignKeyList = new ArrayList<Long>();
			List<Long> benefitRecipientPKs = hotelBookingCVO.getVO().getBenefitRecipientPKs();
			if (benefitRecipientPKs != null && !benefitRecipientPKs.isEmpty()) {
				foreignKeyList.addAll(benefitRecipientPKs);
			}

			Collection<Long> invoiceRecipientPKs = hotelBookingCVO.getVO().getInvoiceRecipientPKs();
			if (invoiceRecipientPKs != null && !invoiceRecipientPKs.isEmpty()) {
				for (Long invoiceRecipientPK : invoiceRecipientPKs) {
					if (!foreignKeyList.contains(invoiceRecipientPK)) {
						foreignKeyList.add(invoiceRecipientPK);
					}
				}
			}
		}

		return foreignKeyList;
	}


	@Override
	protected List<HotelBookingCVO> getEntitiesByForeignKeyFromServer(Object foreignKey) throws Exception {
		Long recipientPK = (Long) foreignKey;
		List<HotelBookingCVO> hotelBookingCVOs = getHotelBookingMgr().getHotelBookingCVOsByRecipient(recipientPK, HB_CVO_SETTINGS);
		return hotelBookingCVOs;
	}


	@Override
	protected List<HotelBookingCVO> getEntitiesFromServer(Collection<Long> keyList) throws Exception {
		List<HotelBookingCVO> hotelBookingCVOs = getHotelBookingMgr().getHotelBookingCVOs(keyList, HB_CVO_SETTINGS);
		return hotelBookingCVOs;
	}


	public List<HotelBookingCVO> getHotelBookingCVOs(Collection<Long> keyList) throws Exception {
		return super.getEntities(keyList);
	}


	public List<HotelBookingCVO> getHotelBookingCVOsByRecipient(Long recipientPK) throws Exception {
		return getEntityListByForeignKey(recipientPK);
	}


	public void changeHotelBooking(
        HotelBookingVO hotelBookingVO,
        Long newHotelOfferingPK,
        HotelBookingPaymentCondition newPaymentCondition,
        BigDecimal newDepositAmount,
        I18NDate newArrival,
        I18NDate newDeparture,
        String newHotelInfo,
        String newHotelPaymentInfo,
        String newAdditionalGuests,
        ArrivalInfo arrivalInfo,
        String arrivalNote,
        SmokerType newSmokerType,
        boolean newTwinRoom,
        LanguageString newInfo
	)
	throws Exception {
		getHotelBookingMgr().updateHotelBooking(
			hotelBookingVO.getID(),
			newHotelOfferingPK,
	        newPaymentCondition,
	        newDepositAmount,
			newArrival,
			newDeparture,
			newHotelInfo,
			newHotelPaymentInfo,
			newAdditionalGuests,
			arrivalInfo,
			arrivalNote,
			newSmokerType,
			newTwinRoom,
			newInfo
		);

		handleUpdate(hotelBookingVO.getID());
	}


	public void changeHotelBooking(
		HotelBookingChangeFlags flags,
		HotelBookingVO changeDataCarrierHotelBookingVO,
		List<HotelBookingVO> hotelBookingVOs,
		BigDecimal newDepositAmount
	)
	throws Exception {
		getHotelBookingMgr().updateHotelBooking(
			flags,
			changeDataCarrierHotelBookingVO,
			hotelBookingVOs,
			newDepositAmount
		);

		List<Long> hotelBookingPKs = HotelBookingVO.getPKs(hotelBookingVOs);
		handleUpdate(hotelBookingPKs);
	}


	public void cancelHotelBookingsWithCurrentTerms(List<HotelBookingCVO> hotelBookingCVOs) {
		try {
			if ( notEmpty(hotelBookingCVOs) ){
				List<Long> hotelBookingPKs = AbstractCVO.getPKs(hotelBookingCVOs);
				getHotelBookingMgr().cancel(hotelBookingPKs);

				handleUpdate(hotelBookingPKs);

				// refresh Participant data of all benefit recipients, because their HotelCostCoverage might have changed
				refreshBenefitRecipients(hotelBookingCVOs);
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	public void cancelHotelBookingsWithoutFee(List<HotelBookingCVO> hotelBookingCVOs) {
		try {
			if (CollectionsHelper.notEmpty(hotelBookingCVOs)){
				Map<Long, Long> bookingPK2cancelationTermPkMap = MapHelper.createHashMap(hotelBookingCVOs.size());
				for (HotelBookingCVO bookingCVO : hotelBookingCVOs) {
					Long bookingPK = bookingCVO.getPK();
					bookingPK2cancelationTermPkMap.put(bookingPK, null);
				}

				getHotelBookingMgr().cancel(bookingPK2cancelationTermPkMap);

				handleUpdate(bookingPK2cancelationTermPkMap.keySet());

				// refresh Participant data of all benefit recipients, because their HotelCostCoverage might have changed
				refreshBenefitRecipients(hotelBookingCVOs);
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	/**
	 * Cancels hotel bookings with cancelation terms.
	 *
	 * @param bookingPK2cancelationTermPkMap Map with the hotel bookings to be canceled as keys and
	 *        the corresponding cancelation term as value
	 */
	public void cancelHotelBookings(Map<HotelBookingCVO, Long> bookingCVO2cancelTermPkMap) {
		try {
    		if (bookingCVO2cancelTermPkMap != null && !bookingCVO2cancelTermPkMap.isEmpty()){
    			Map<Long, Long> bookingPK2cancelationTermPkMap = MapHelper.createHashMap(bookingCVO2cancelTermPkMap.size());
    			for (Map.Entry<HotelBookingCVO, Long> entry : bookingCVO2cancelTermPkMap.entrySet()) {
    				HotelBookingCVO bookingCVO = entry.getKey();
    				Long cancelationTermPK = entry.getValue();

    				bookingPK2cancelationTermPkMap.put(bookingCVO.getPK(), cancelationTermPK);
    			}

				getHotelBookingMgr().cancel(bookingPK2cancelationTermPkMap);

				handleUpdate(bookingPK2cancelationTermPkMap.keySet());

				// refresh Participant data of all benefit recipients, because their HotelCostCoverage might have changed
				refreshBenefitRecipients( bookingCVO2cancelTermPkMap.keySet() );
    		}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	public void changeBenefitRecipientsOfHotelBookings(
		Collection<HotelBookingCVO> hotelBookingCVOs,
		List<Long> newBenefitRecipientPKs
	)
	throws Exception {
		// Extract the PKs of bookings to be changed
		List<Long> hotelBookingPKs = AbstractCVO.getPKs(hotelBookingCVOs);

		// Calling Server
		getHotelBookingMgr().updateBenefitRecipients(
			hotelBookingPKs,
			newBenefitRecipientPKs
		);

		handleUpdate(hotelBookingPKs);
	}


	public void changeInvoiceRecipientsOfHotelBookings(
		Collection<HotelBookingCVO> hotelBookingCVOs,
		Long newInvoiceRecipientPK
	)
	throws Exception {
		// extract PKs of Bookings
		List<Long> hotelBookingPKs = AbstractCVO.getPKs(hotelBookingCVOs);

		// Calling Server
		getHotelBookingMgr().updateInvoiceRecipients(hotelBookingPKs, newInvoiceRecipientPK);

		handleUpdate(hotelBookingPKs);
	}

}
