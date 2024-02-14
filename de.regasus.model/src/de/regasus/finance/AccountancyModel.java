package de.regasus.finance;

import static de.regasus.LookupService.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.lambdalogic.messeinfo.contact.data.BookingRecipientVO;
import com.lambdalogic.messeinfo.hotel.data.HotelBookingCVO;
import com.lambdalogic.messeinfo.hotel.data.HotelBookingVO;
import com.lambdalogic.messeinfo.invoice.data.AccountancyCVO;
import com.lambdalogic.messeinfo.invoice.data.ClearingVO;
import com.lambdalogic.messeinfo.invoice.data.InvoicePositionVO;
import com.lambdalogic.messeinfo.invoice.data.InvoiceVO;
import com.lambdalogic.messeinfo.invoice.data.PaymentVO;
import com.lambdalogic.messeinfo.kernel.data.AbstractVO;
import com.lambdalogic.messeinfo.participant.Participant;
import com.lambdalogic.messeinfo.participant.data.ProgrammeBookingCVO;
import com.lambdalogic.messeinfo.participant.data.ProgrammeBookingVO;
import com.lambdalogic.report.DocumentContainer;
import com.lambdalogic.util.CollectionsHelper;
import com.lambdalogic.util.EqualsHelper;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.model.CacheModelOperation;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.model.MICacheModel;
import de.regasus.hotel.HotelBookingModel;
import de.regasus.model.Activator;
import de.regasus.programme.ProgrammeBookingModel;


public class AccountancyModel extends MICacheModel<Long, AccountancyCVO>
implements CacheModelListener<Long> {

	private static AccountancyModel singleton = null;


	// models
	private ProgrammeBookingModel pbModel;
	private HotelBookingModel hbModel;


	public static AccountancyModel getInstance() {
		if (singleton == null) {
			singleton = new AccountancyModel();
			singleton.initModels();
		}
		return singleton;
	}


	private AccountancyModel() {
		super();
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
		pbModel = ProgrammeBookingModel.getInstance();
		pbModel.addListener(this);

		hbModel = HotelBookingModel.getInstance();
		hbModel.addListener(this);
	}


	@Override
	protected boolean isForeignKeySupported() {
		return false;
	}


	@Override
	protected Long getKey(AccountancyCVO entity) {
		Long key = null;
		if (entity != null) {
			key = entity.getParticipantPK();
		}
		return key;
	}


	@Override
	protected boolean isSameVersion(AccountancyCVO acc1, AccountancyCVO acc2) {
		boolean sameVersion = false;

		/* We do not try to find a counterpart in acc2 for each object in acc1.
		 * To be more efficient only we check the numbers of invoices, payments an clearings.
		 * If they are all equal we calculate the latest editTime of all objects in acc1 and acc2.
		 * This is OK, because if a number changes, that means, that an object was deleted or added.
		 * Even if both happens and the number does not change, this leads to a different (newer)
		 * latest editTime, because it is not possible to add something with an old editTime.
		 */

		if (acc1 != null && acc2 != null &&
			acc1.getInvoiceVOs().size() == acc2.getInvoiceVOs().size() &&
			acc1.getPaymentVOs().size() == acc2.getPaymentVOs().size() &&
			acc1.getClearingVOs().size() == acc2.getClearingVOs().size()
		) {

			// determine last editTime of acc1
			Date lastEditTime1 = null;

			// invoices of acc1
			for (InvoiceVO invoiceVO : acc1.getInvoiceVOs()) {
				if (lastEditTime1 == null || lastEditTime1.before(invoiceVO.getEditTime())) {
					lastEditTime1 = invoiceVO.getEditTime();
				}
			}

			// payments of acc1
			for (PaymentVO paymentVO : acc1.getPaymentVOs()) {
				if (lastEditTime1 == null || lastEditTime1.before(paymentVO.getEditTime())) {
					lastEditTime1 = paymentVO.getEditTime();
				}
			}

			// clearings of acc1
			for (ClearingVO clearingVO : acc1.getClearingVOs()) {
				// clearings cannot be edited and have no editTime, so use newTime
				if (lastEditTime1 == null || lastEditTime1.before(clearingVO.getNewTime())) {
					lastEditTime1 = clearingVO.getNewTime();
				}
			}



			// determine last editTime of acc2
			Date lastEditTime2 = null;

			// invoices of acc2
			for (InvoiceVO invoiceVO : acc2.getInvoiceVOs()) {
				if (lastEditTime2 == null || lastEditTime2.before(invoiceVO.getEditTime())) {
					lastEditTime2 = invoiceVO.getEditTime();
				}
			}

			// payments of acc2
			for (PaymentVO paymentVO : acc2.getPaymentVOs()) {
				if (lastEditTime2 == null || lastEditTime2.before(paymentVO.getEditTime())) {
					lastEditTime2 = paymentVO.getEditTime();
				}
			}

			// clearings of acc2
			for (ClearingVO clearingVO : acc2.getClearingVOs()) {
				// clearings cannot be edited and have no editTime, so use newTime
				if (lastEditTime2 == null || lastEditTime2.before(clearingVO.getNewTime())) {
					lastEditTime2 = clearingVO.getNewTime();
				}
			}

			sameVersion = EqualsHelper.isEqual(lastEditTime1, lastEditTime2);
		}

		return sameVersion;
	}


	@Override
	protected AccountancyCVO getEntityFromServer(Long partcipantID) throws Exception {
		AccountancyCVO accountancyCVO = getInvoiceMgr().getAccountancyCVO(partcipantID);
		return accountancyCVO;
	}


	public AccountancyCVO getAccountancyCVO(Long participantPK) throws Exception {
		return super.getEntity(participantPK);
	}


	@SuppressWarnings("deprecation")
	@Override
	protected List<AccountancyCVO> getEntitiesFromServer(Collection<Long> participantIDs)
	throws Exception {
		// TODO
//		List<Long> partcipantPKs = DefaultPK.toPkList(participantIDs);
//		List<AccountancyCVO> accountancyCVOs = invoiceMgr.getAccountancyCVOs(partcipantPKs);
//		return accountancyCVOs;

		return super.getEntitiesFromServer(participantIDs);
	}


	@Override
	public void dataChange(CacheModelEvent<Long> event) {
		if (!serverModel.isLoggedIn()) {
			return;
		}

		try {
			if (event.getSource() == pbModel && event.getOperation() != CacheModelOperation.REFRESH) {
				/* React on ProgrammeBookings, because the server creates/deletes InvoicePositions
				 * when they are created, deleted or updated.
				 */

				// determine invoice recipients of all bookings
				List<Long> pbPKs = event.getKeyList();
				List<ProgrammeBookingCVO> programmeBookingCVOs = pbModel.getProgrammeBookingCVOs(pbPKs);
				Set<Long> recipientPKs = CollectionsHelper.createHashSet(programmeBookingCVOs.size());
				for (ProgrammeBookingCVO programmeBookingCVO : programmeBookingCVOs) {
					// add invoice recipient
					ProgrammeBookingVO programmeBookingVO = programmeBookingCVO.getVO();
					recipientPKs.add(programmeBookingVO.getInvoiceRecipientPK());

					// determine old invoice recipients
					BookingRecipientVO oldInvoiceBookingRecipientVO = programmeBookingVO.getFormerInvoiceBookingRecipientVO();
					if (oldInvoiceBookingRecipientVO != null) {
						/* If the time of the last invoice recipient change if younger or equal than
						 * the time of the last edit of the booking, then the cause of this event
						 * could be the change of the invoice recipient.
						 */
						if (oldInvoiceBookingRecipientVO.getDeleteTime().after(programmeBookingVO.getEditTime())
							||
							oldInvoiceBookingRecipientVO.getDeleteTime().equals(programmeBookingVO.getEditTime())
						) {
							recipientPKs.add(oldInvoiceBookingRecipientVO.getRecipientPK());
						}
					}
				}

				// remove recipients without loaded data
				for (Iterator<Long> it = recipientPKs.iterator(); it.hasNext();) {
					Long recipientPK = it.next();
					if (!isLoaded(recipientPK)) {
						it.remove();
					}
				}

				if (!recipientPKs.isEmpty()) {
					handleUpdate(recipientPKs);
				}
			}
			else if (event.getSource() == hbModel && event.getOperation() != CacheModelOperation.REFRESH) {
				/* React on HotelBookings, because the server creates/deletes InvoicePositions
				 * when they are created, deleted or updated.
				 */

				// determine invoice recipients of all bookings
				List<Long> hbPKs = event.getKeyList();
				List<HotelBookingCVO> hotelBookingCVOs = hbModel.getHotelBookingCVOs(hbPKs);
				Set<Long> recipientPKs = CollectionsHelper.createHashSet(hotelBookingCVOs.size());
				for (HotelBookingCVO hotelBookingCVO : hotelBookingCVOs) {
					// add invoice recipient
					HotelBookingVO hotelBookingVO = hotelBookingCVO.getVO();
					recipientPKs.add(hotelBookingVO.getInvoiceRecipientPK());

					// determine old invoice recipients
					BookingRecipientVO oldInvoiceBookingRecipientVO = hotelBookingVO.getFormerInvoiceBookingRecipientVO();
					if (oldInvoiceBookingRecipientVO != null) {
						/* If the time of the last invoice recipient change if younger or equal than
						 * the time of the last edit of the booking, then the cause of this event
						 * could be the change of the invoice recipient.
						 */
						if (oldInvoiceBookingRecipientVO.getDeleteTime().after(hotelBookingVO.getEditTime())
							||
							oldInvoiceBookingRecipientVO.getDeleteTime().equals(hotelBookingVO.getEditTime())
						) {
							recipientPKs.add(oldInvoiceBookingRecipientVO.getRecipientPK());
						}
					}
				}

				// remove recipients without loaded data
				for (Iterator<Long> it = recipientPKs.iterator(); it.hasNext();) {
					Long recipientPK = it.next();
					if (!isLoaded(recipientPK)) {
						it.remove();
					}
				}

				if (!recipientPKs.isEmpty()) {
					handleUpdate(recipientPKs);
				}
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}



	// from InvoiceModel

	/**
	 * "Mahnen"
	 */
	public void remindInvoices(Collection<InvoiceVO> invoiceVOs) throws Exception {
		// extract invoicePKs and invoiceRecipientPKs
		List<Long> invoicePKs = CollectionsHelper.createArrayList(invoiceVOs.size());
		Set<Long> invoiceRecipientPKs = CollectionsHelper.createHashSet(invoiceVOs.size());
		for (InvoiceVO invoiceVO : invoiceVOs) {
			invoicePKs.add(invoiceVO.getID());
			invoiceRecipientPKs.add(invoiceVO.getRecipientPK());
		}

		getInvoiceMgr().remindInvoices(invoicePKs);

		handleUpdate(invoiceRecipientPKs);
	}


	/**
	 * "Mahnung stoppen"
	 */
	public void stopReminder(Collection<InvoiceVO> invoiceVOs) throws Exception {
		// extract invoicePKs and invoiceRecipientPKs
		List<Long> invoicePKs = CollectionsHelper.createArrayList(invoiceVOs.size());
		Set<Long> invoiceRecipientPKs = CollectionsHelper.createHashSet(invoiceVOs.size());
		for (InvoiceVO invoiceVO : invoiceVOs) {
			invoicePKs.add(invoiceVO.getID());
			invoiceRecipientPKs.add(invoiceVO.getRecipientPK());
		}

		getInvoiceMgr().stopReminder(invoicePKs);

		handleUpdate(invoiceRecipientPKs);
	}


	/**
	 * "Mahnung wieder starten"
	 */
	public void restartReminder(Collection<InvoiceVO> invoiceVOs) throws Exception {
		// extract invoicePKs and invoiceRecipientPKs
		List<Long> invoicePKs = CollectionsHelper.createArrayList(invoiceVOs.size());
		Set<Long> invoiceRecipientPKs = CollectionsHelper.createHashSet(invoiceVOs.size());
		for (InvoiceVO invoiceVO : invoiceVOs) {
			invoicePKs.add(invoiceVO.getID());
			invoiceRecipientPKs.add(invoiceVO.getRecipientPK());
		}

		getInvoiceMgr().restartReminder(invoicePKs);

		handleUpdate(invoiceRecipientPKs);
	}




	// from ParticipantModel

	public List<DocumentContainer> getInvoiceDocuments(
		Collection<InvoiceVO> invoiceVOs,
		String format,
		boolean merge
	)
	throws Exception {
		// extract invoicePKs and invoiceRecipientPKs
		List<Long> invoicePKs = CollectionsHelper.createArrayList(invoiceVOs.size());
		Set<Long> invoiceRecipientPKs = CollectionsHelper.createHashSet(invoiceVOs.size());
		for (InvoiceVO invoiceVO : invoiceVOs) {
			invoicePKs.add(invoiceVO.getID());
			invoiceRecipientPKs.add(invoiceVO.getRecipientPK());
		}


		// close Invoices that are not closed yet
		List<Long> unclosedInvoicePKs = new ArrayList<>();
		for (InvoiceVO invoiceVO : invoiceVOs) {
			if (!invoiceVO.isClosed()) {
				unclosedInvoicePKs.add(invoiceVO.getID());
			}
		}
		if (!unclosedInvoicePKs.isEmpty()) {
			getInvoiceMgr().closeInvoices(
				unclosedInvoicePKs,
				true	// closeZeroInvoice
			);
		}


		// get the invoice documents
		List<DocumentContainer> invoiceDocuments = getInvoiceMgr().getInvoiceDocuments(invoicePKs, format, merge);

		handleUpdate(invoiceRecipientPKs);

		return invoiceDocuments;
	}


	public DocumentContainer getSampleInvoiceDocument(InvoiceVO invoiceVO, String format)
	throws Exception {
		// get the invoice documents
		DocumentContainer invoiceDocument = getInvoiceMgr().getSampleInvoiceDocument(
			invoiceVO.getID(),
			format
		);

		return invoiceDocument;
	}


	public void closeInvoices(Collection<InvoiceVO> invoiceVOs) throws Exception {
		// extract invoicePKs and invoiceRecipientPKs
		List<Long> invoicePKs = CollectionsHelper.createArrayList(invoiceVOs.size());
		Set<Long> invoiceRecipientPKs = CollectionsHelper.createHashSet(invoiceVOs.size());
		for (InvoiceVO invoiceVO : invoiceVOs) {
			invoicePKs.add(invoiceVO.getID());
			invoiceRecipientPKs.add(invoiceVO.getRecipientPK());
		}

		getInvoiceMgr().closeInvoices(invoicePKs, true);

		handleUpdate(invoiceRecipientPKs);
	}


	public void updateInvoiceAddresses(Collection<InvoiceVO> invoiceVOs)
	throws Exception {
		// extract invoicePKs and invoiceRecipientPKs
		List<Long> invoicePKs = CollectionsHelper.createArrayList(invoiceVOs.size());
		Set<Long> invoiceRecipientPKs = CollectionsHelper.createHashSet(invoiceVOs.size());
		for (InvoiceVO invoiceVO : invoiceVOs) {
			invoicePKs.add(invoiceVO.getID());
			invoiceRecipientPKs.add(invoiceVO.getRecipientPK());
		}

		getInvoiceMgr().updateInvoiceAddressees(invoicePKs);

		handleUpdate(invoiceRecipientPKs);
	}


	public Long createPayment(PaymentVO paymentVO) throws Exception {
		Long paymentPK = getPaymentMgr().createPayment(paymentVO);

		Long payerPK = paymentVO.getPayerPK();
		handleUpdate(payerPK);

		return paymentPK;
	}


	public Long createPaymentForInvoices(PaymentVO paymentVO, List<Long> invoicePKs) throws Exception {
		Long paymentPK = getPaymentMgr().createPaymentForInvoices(paymentVO, invoicePKs);

		Long payerPK = paymentVO.getPayerPK();
		handleUpdate(payerPK);

		return paymentPK;
	}


	public Long createPaymentForInvoicePositions(PaymentVO paymentVO, List<Long> invoicePositionPKs) throws Exception {
		Long paymentPK = getPaymentMgr().createPaymentForInvoicePositions(paymentVO, invoicePositionPKs);

		Long payerPK = paymentVO.getPayerPK();
		handleUpdate(payerPK);

		return paymentPK;
	}


	public void updatePayment(PaymentVO paymentVO)
	throws Exception {
		getPaymentMgr().updatePayment(paymentVO);

		Long payerPK = paymentVO.getPayerPK();
		handleUpdate(payerPK);
	}


	public void cancelPayment(PaymentVO paymentVO)
	throws Exception {
		getPaymentMgr().cancelPayment(paymentVO.getPK());

		Long payerPK = paymentVO.getPayerPK();
		handleUpdate(payerPK);
	}


	public void createAutomaticClearings(Long payerPK)
	throws Exception {
		getPaymentMgr().createClearingsForAbstractPerson(payerPK);

		handleUpdate(payerPK);
	}


	public void createClearing(Long payerPK, Map<Long, Collection<Long>> payment2invoicePositionsMap)
	throws Exception {
		getPaymentMgr().createClearings(payment2invoicePositionsMap);

		handleUpdate(payerPK);
	}


	/**
	 * Erzeugt Clearings.
	 * Das übergebene Clearing muss sich auf das übergebene Payment beziehen.
	 * Das zu verrechnende Payment darf nicht storniert sein.
	 * Der Verrechnungsbetrag im Clearing darf nicht größer sein als der offene Betrag des Zahlunseingangs.
	 * Der Verrechnungsbetrag im Clearing darf nicht kleiner sein als der offene Betrag des Zahlungsausgangs.
	 *
	 * @param paymentVO Daten des Payments, mit dem verrechnet wird
	 * @param clearingVO <code>ClearingVO</code>
	 */
	public void createClearing(PaymentVO paymentVO, ClearingVO clearingVO)
	throws Exception {
		List<ClearingVO> clearingVOs = new ArrayList<>(1);
		clearingVOs.add(clearingVO);
		getPaymentMgr().createClearings(paymentVO, clearingVOs);

		Long payerPK = paymentVO.getPayerPK();
		handleUpdate(payerPK);
	}


	public void setInvoicePositionsOffAgainstEachOther(
		Participant participant,
		Collection<InvoicePositionVO> invoicePositionVOs
	)
	throws Exception {
		List<Long> invoicePositionPKs = AbstractVO.getPKs(invoicePositionVOs);
		getPaymentMgr().clearInvoicePositions(invoicePositionPKs);

		Long payerPK = participant.getPrimaryKey();
		handleUpdate(payerPK);
	}


	public void deleteClearings(Long payerPK, Collection<ClearingVO> clearingVOs)
	throws Exception {
		getPaymentMgr().deleteClearingsByVOs(clearingVOs);

		handleUpdate(payerPK);
	}


	public void refundEasyCheckoutPayment(PaymentVO paymentVO, BigDecimal amount)
	throws Exception {
		Long payerPK = paymentVO.getPayerPK();
		int prevPaymentCount = getPaymentMgr().getPaymentVOsByPersonPK(payerPK, true /*withCancelations*/).size();

		getEasyCheckoutMgr().refundPayment(paymentVO.getID(), amount);

		for (int i = 0; i < 5; i++) {
			Thread.sleep(1000);

			int currentPaymentCount = getPaymentMgr().getPaymentVOsByPersonPK(payerPK, true /*withCancelations*/).size();

			if (currentPaymentCount > prevPaymentCount) {
				refresh( paymentVO.getPayerPK() );
				break;
			}
		}
	}

}
