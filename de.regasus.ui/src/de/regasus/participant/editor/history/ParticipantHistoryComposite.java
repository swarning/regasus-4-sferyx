package de.regasus.participant.editor.history;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;

import com.lambdalogic.messeinfo.email.EmailDispatch;
import com.lambdalogic.messeinfo.email.EmailDispatchOrder;
import com.lambdalogic.messeinfo.email.EmailTemplate;
import com.lambdalogic.messeinfo.hotel.data.HotelBookingCVO;
import com.lambdalogic.messeinfo.invoice.data.AccountancyCVO;
import com.lambdalogic.messeinfo.invoice.data.InvoiceVO;
import com.lambdalogic.messeinfo.invoice.data.PaymentVO;
import com.lambdalogic.messeinfo.participant.Participant;
import com.lambdalogic.messeinfo.participant.ParticipantCorrespondence;
import com.lambdalogic.messeinfo.participant.data.ProgrammeBookingCVO;
import com.lambdalogic.util.CollectionsHelper;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.rcp.LazyComposite;
import com.lambdalogic.util.rcp.html.BrowserFactory;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.core.ServerModel;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.email.EmailDispatchOrderModel;
import de.regasus.email.EmailDispatchRecipientModel;
import de.regasus.email.EmailTemplateModel;
import de.regasus.finance.AccountancyModel;
import de.regasus.history.HistoryEventComparator;
import de.regasus.history.HistoryEventList2HmlConverter;
import de.regasus.history.IHistoryEvent;
import de.regasus.hotel.HotelBookingModel;
import de.regasus.participant.ParticipantCorrespondenceModel;
import de.regasus.participant.ParticipantFileModel;
import de.regasus.participant.ParticipantHistoryModel;
import de.regasus.participant.ParticipantModel;
import de.regasus.programme.ProgrammeBookingModel;
import de.regasus.ui.Activator;

public class ParticipantHistoryComposite
	extends LazyComposite
	implements CacheModelListener, DisposeListener {

	private Participant participant;

	private boolean sync = false;

	// models
	private ServerModel serverModel;
	private ParticipantHistoryModel paHistModel;
	private ParticipantModel paModel;
	private ProgrammeBookingModel pbModel;
	private HotelBookingModel hbModel;
	private AccountancyModel accModel;
	private EmailDispatchOrderModel emailDispatchOrderModel;
	private EmailDispatchRecipientModel emailDispatchRecipientModel;
	private EmailTemplateModel emailTemplateModel;
	private ParticipantCorrespondenceModel paCorrespondenceModel;
	private ParticipantFileModel paFileModel;


	// widgets
	private Browser historyBrowser;


	public ParticipantHistoryComposite(Composite parent, int style) {
		super(parent, style);

		serverModel = ServerModel.getInstance();
		paModel = ParticipantModel.getInstance();
		paHistModel = ParticipantHistoryModel.getInstance();
		pbModel = ProgrammeBookingModel.getInstance();
		hbModel = HotelBookingModel.getInstance();
		accModel = AccountancyModel.getInstance();
		emailDispatchOrderModel = EmailDispatchOrderModel.getInstance();
		emailDispatchRecipientModel = EmailDispatchRecipientModel.getInstance();
		emailTemplateModel = EmailTemplateModel.getInstance();
		paCorrespondenceModel = ParticipantCorrespondenceModel.getInstance();
		paFileModel = ParticipantFileModel.getInstance();

		addDisposeListener(this);
	}


	@Override
	public void widgetDisposed(DisposeEvent event) {
		if (participant != null && participant.getID() != null) {
			Long id = participant.getID();

			try {
				if (paModel != null) {
					paModel.removeListener(this, id);
				}
			}
			catch (Exception e) {
				// ignore
			}

			try {
				if (paHistModel != null) {
					paHistModel.removeListener(this, id);
				}
			}
			catch (Exception e) {
				// ignore
			}

			try {
				if (pbModel != null) {
					pbModel.removeForeignKeyListener(this, id);
				}
			}
			catch (Exception e) {
				// ignore
			}

			try {
				if (hbModel != null) {
					hbModel.removeForeignKeyListener(this, id);
				}
			}
			catch (Exception e) {
				// ignore
			}

			try {
				if (accModel != null) {
					accModel.removeListener(this, id);
				}
			}
			catch (Exception e) {
				// ignore
			}

			try {
				if (emailDispatchRecipientModel != null) {
					emailDispatchRecipientModel.removeForeignKeyListener(this, id);
				}
			}
			catch (Exception e) {
				// ignore
			}

			try {
				if (paCorrespondenceModel != null) {
					paCorrespondenceModel.removeForeignKeyListener(this, id);
				}
			}
			catch (Exception e) {
				// ignore
			}

			try {
				if (paFileModel != null) {
					paFileModel.removeForeignKeyListener(this, id);
				}
			}
			catch (Exception e) {
				// ignore
			}
		}
	}


	@Override
	protected void createPartControl() throws Exception {
		setLayout(new FillLayout());

		historyBrowser = BrowserFactory.createBrowser(this, SWT.BORDER);

		syncWidgetsToEntity();
	}


	public void setParticipant(Participant participant) {
		Long oldPK = this.participant == null ? null : this.participant.getID();
		Long newPK = participant == null ? null : participant.getID();

		if (oldPK != null && !oldPK.equals(newPK)) {
			throw new IllegalArgumentException("PK of participant must not change.");
		}

		this.participant = participant;

		if (oldPK == null && newPK != null) {
			paModel.addListener(this, newPK);
			pbModel.addForeignKeyListener(this, newPK);
			hbModel.addForeignKeyListener(this, newPK);
			accModel.addListener(this, newPK);
			emailDispatchRecipientModel.addForeignKeyListener(this, newPK);
			paCorrespondenceModel.addForeignKeyListener(this, newPK);
			paFileModel.addForeignKeyListener(this, newPK);
		}

		syncWidgetsToEntity();
	}


	/* Mechanism to delay the call of _syncWidgetsToEntity() until the last call of dataChange().
	 * This Composite gets many calls of dataChange() from different Models in a short term.
	 * To avoid multiple calls of _syncWidgetsToEntity() we start the SyncWidgetsToEntityThread which is waiting
	 * 100 ms before it calls _syncWidgetsToEntity(). If a further call of dataChange() happens during that time,
	 * SyncWidgetsToEntityThread waits another 100 ms.
	 */
	final boolean DEBUG_SyncWidgetsToEntityThread = false;

	private static final long SLEEP_TIME = 100L;

	private SyncWidgetsToEntityThread syncWidgetsToEntityThread = null;

	private class SyncWidgetsToEntityThread extends Thread {
		private boolean sleepOn = false;


		@Override
		public void run() {
			if (DEBUG_SyncWidgetsToEntityThread) System.out.println("SyncWidgetsToEntityThread: start running");
			sleepOn = true;

			while (sleepOn) {
				sleepOn = false;

				if (DEBUG_SyncWidgetsToEntityThread) System.out.println("SyncWidgetsToEntityThread: start sleeping");

				try {
					sleep(SLEEP_TIME);
				}
				catch (InterruptedException e) {
					com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
				}

				if (DEBUG_SyncWidgetsToEntityThread) System.out.println("SyncWidgetsToEntityThread: stop sleeping");
			}

			if (DEBUG_SyncWidgetsToEntityThread) System.out.println("SyncWidgetsToEntityThread: _syncWidgetsToEntity()");

			_syncWidgetsToEntity();

			if (DEBUG_SyncWidgetsToEntityThread) System.out.println("SyncWidgetsToEntityThread: stop running");

			syncWidgetsToEntityThread = null;

			/*
			 * If sleepOn() was called on this Thread while running _syncWidgetsToEntity(), call syncWidgetsToEntity()
			 * to start a new Thread.
			 */
			// if (sleepOn) {
			// syncWidgetsToEntity();
			// }
		}


		public void sleepOn() {
			if (DEBUG_SyncWidgetsToEntityThread) System.out.println("SyncWidgetsToEntityThread.sleepOn()");

			sleepOn = true;
		}
	}


	private void syncWidgetsToEntity() {
		if (!sync && participant != null && historyBrowser != null) {
			try {
				sync = true;
				if (syncWidgetsToEntityThread == null) {
					if (DEBUG_SyncWidgetsToEntityThread) System.out.println("syncWidgetsToEntity(): create new Thread");

					syncWidgetsToEntityThread = new SyncWidgetsToEntityThread();
					syncWidgetsToEntityThread.start();
				}
				else {
					if (DEBUG_SyncWidgetsToEntityThread) System.out.println("syncWidgetsToEntity(): syncWidgetsToEntityThread.sleepOn()");
					syncWidgetsToEntityThread.sleepOn();
				}
			}
			finally {
				sync = false;
			}
		}
		else {
			if (DEBUG_SyncWidgetsToEntityThread) System.out.println("syncWidgetsToEntity(): skip because sync = " + sync);
		}
	}


	private void _syncWidgetsToEntity() {
		try {
			final StringBuffer html = new StringBuffer();

			// The "basic" event list built from the PARTICIPANT_HISTORY table
			List<IHistoryEvent> historyEventList = ParticipantHistoryEventHelper.createParticipantHistoryEventList(participant);

			// The events built from the PAYMENT table
			List<IHistoryEvent> paymentEvents = createPaymentEventList();
			historyEventList.addAll(paymentEvents);

			// The events built from the INVOICE table
			AccountancyCVO accountancyCVO = accModel.getAccountancyCVO(participant.getID());
			List<InvoiceVO> invoiceVOs = accountancyCVO.getInvoiceVOs();

			for (InvoiceVO invoiceVO : invoiceVOs) {
				if (invoiceVO.getPrint() != null) {
					historyEventList.add(new InvoicePrintEvent(invoiceVO));
				}

				if (invoiceVO.isClosed()) {
					historyEventList.add(new InvoiceCloseEvent(invoiceVO));
				}
			}

			// The events built from the PROGRAMME_BOOKING table
			List<IHistoryEvent> programmeBookingEventList = createProgrammeBookingEventList();
			historyEventList.addAll(programmeBookingEventList);

			// The events built from the HOTEL_BOOKING table
			List<IHistoryEvent> hotelBookingEventList = createHotelBookingEventList();
			historyEventList.addAll(hotelBookingEventList);

			// The events built from the PARTICIPANT_CORRENPONDENCE table
			Long participantID = participant.getID();
			if (participantID != null) {
				List<ParticipantCorrespondence> list = paCorrespondenceModel.getCorrespondenceListByParticipantId(participantID);

				for (ParticipantCorrespondence correspondence : list) {
					historyEventList.add( new CorrespondenceEvent(correspondence) );
				}
			}

			// Here we merge Event Lists that are contributed by optional plug ins
			List<IHistoryEvent> emailEventList = createEmailHistoryEventList(participantID);
			historyEventList.addAll(emailEventList);

			Collections.sort(historyEventList, HistoryEventComparator.getInstance());

			html.append(HistoryEventList2HmlConverter.convert(historyEventList));

			SWTHelper.syncExecDisplayThread(new Runnable() {
				@Override
				public void run() {
					try {
						if (html.length() > 0) {
							historyBrowser.setText(html.toString());
						}
					}
					catch (Exception e) {
						RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
					}
				}
			});
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	@Override
	public void dataChange(CacheModelEvent event) {
		if (serverModel.isLoggedIn()) {
			syncWidgetsToEntity();
		}
	}




	private List<IHistoryEvent> createPaymentEventList() throws Exception {
		AccountancyCVO accountancyCVO = accModel.getAccountancyCVO(participant.getID());

		List<IHistoryEvent> paymentEvents = null;
		if (accountancyCVO != null) {
			List<PaymentVO> paymentVOs = accountancyCVO.getPaymentVOs();

			paymentEvents = CollectionsHelper.createArrayList(paymentVOs.size());
			for (PaymentVO paymentVO : paymentVOs) {
				if (paymentVO.isCanceled()) {
					paymentEvents.add(new PaymentEvent(paymentVO, true));
					paymentEvents.add(new PaymentEvent(paymentVO, false));
				}
				else {
					paymentEvents.add(new PaymentEvent(paymentVO, false));
				}
			}
		}
		else {
			paymentEvents = Collections.emptyList();
		}

		return paymentEvents;
	}


	private List<IHistoryEvent> createProgrammeBookingEventList() throws Exception {
		List<ProgrammeBookingCVO> pbCVOs = pbModel.getProgrammeBookingCVOsByRecipient(participant.getID());

		List<IHistoryEvent> programmeBookingEvents = null;
		if (pbCVOs != null) {
			programmeBookingEvents = CollectionsHelper.createArrayList(pbCVOs.size());
			for (ProgrammeBookingCVO programmeBookingCVO : pbCVOs) {
				if (programmeBookingCVO.isCanceled()) {
					programmeBookingEvents.add(new ProgrammeBookingEvent(programmeBookingCVO, true));
				}
				else {
					programmeBookingEvents.add(new ProgrammeBookingEvent(programmeBookingCVO, false));
				}
			}
		}
		else {
			programmeBookingEvents = Collections.emptyList();
		}

		return programmeBookingEvents;
	}


	private List<IHistoryEvent> createHotelBookingEventList() throws Exception {
		List<HotelBookingCVO> hbCVOs = hbModel.getHotelBookingCVOsByRecipient(participant.getID());

		List<IHistoryEvent> hotelBookingEvents = null;
		if (hbCVOs != null) {
			hotelBookingEvents = CollectionsHelper.createArrayList(hbCVOs.size());

			for (HotelBookingCVO hotelBookingCVO : hbCVOs) {
				hotelBookingEvents.add(new HotelBookingEvent(hotelBookingCVO, false));
				if (hotelBookingCVO.isCanceled()) {
					hotelBookingEvents.add(new HotelBookingEvent(hotelBookingCVO, true));
				}
			}
		}
		else {
			hotelBookingEvents = Collections.emptyList();
		}

		return hotelBookingEvents;
	}


	private List<IHistoryEvent> createEmailHistoryEventList(Long participantID) {
		List<IHistoryEvent> result = new ArrayList<IHistoryEvent>();

		try {
			// First ask the model for all EmailDispatches where this participant is recipent
			List<EmailDispatch> emailDispatchesByRecipient = emailDispatchRecipientModel.getEmailDispatchesByRecipient(participantID);

			// Finally build the event objects to be shown
			for (EmailDispatch emailDispatch: emailDispatchesByRecipient) {
				EmailDispatchOrder emailDispatchOrder = emailDispatchOrderModel.getEmailDispatchOrder(emailDispatch.getEmailDispatchOrderPK());
				EmailTemplate emailTemplate = emailTemplateModel.getEmailTemplate(emailDispatchOrder.getEmailTemplatePK());
				result.add(new EmailDispatchEvent(emailTemplate, emailDispatchOrder, emailDispatch));
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}

		return result;
	}
}
