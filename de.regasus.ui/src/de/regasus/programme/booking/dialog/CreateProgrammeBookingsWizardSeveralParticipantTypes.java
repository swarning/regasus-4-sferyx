package de.regasus.programme.booking.dialog;

import static de.regasus.LookupService.getProgrammeBookingMgr;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;

import com.lambdalogic.messeinfo.kernel.ServerMessage;
import com.lambdalogic.messeinfo.kernel.data.AbstractCVO;
import com.lambdalogic.messeinfo.participant.data.IParticipant;
import com.lambdalogic.messeinfo.participant.data.ProgrammeBookingParameter;
import com.lambdalogic.messeinfo.participant.data.ProgrammePointCVO;
import com.lambdalogic.util.CollectionsHelper;
import com.lambdalogic.util.rcp.BusyCursorHelper;

import de.regasus.I18N;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.dialog.ServerMessageDialog;
import de.regasus.participant.booking.BookingParameterHelper;
import de.regasus.participant.booking.SelectInvoiceRecipientPage;
import de.regasus.programme.ProgrammeBookingModel;
import de.regasus.ui.Activator;

/**
 * A wizard to create programme bookings for one or more participants which have different participant types.
 */
public class CreateProgrammeBookingsWizardSeveralParticipantTypes extends Wizard {

	private List<? extends IParticipant> participantList;

	private Long eventPK;

	// *************************************************************************
	// * Pages
	// *

	private DecideBookingModePage decideBookingModePage;

	private SelectProgrammeOfferingsPage selectProgrammeOfferingsPage;

	private SelectProgrammePointsPage selectProgrammePointsPage;

	private SelectInvoiceRecipientPage selectInvoiceRecipientPage;

	private InfoAndOptionsPage infoAndOptionsPage;

	private OverviewPage overviewPage;

	private ProgrammePointCVO programmePointCVO;


	public CreateProgrammeBookingsWizardSeveralParticipantTypes(
		Long eventPK,
		List<? extends IParticipant> participantList
	) {
		this.participantList = participantList;
		this.eventPK = eventPK;
	}


	@Override
	public void addPages() {
		decideBookingModePage = new DecideBookingModePage();
		addPage(decideBookingModePage);

		// =======================================================
		// Only one of the next two pages is made visible, depending on the decision in the first page
		Long participantTypePK = participantList.get(0).getParticipantTypePK();
		selectProgrammeOfferingsPage = new SelectProgrammeOfferingsPage(
			eventPK,
			participantTypePK
		);
		addPage(selectProgrammeOfferingsPage);

		selectProgrammePointsPage = new SelectProgrammePointsPage(eventPK);
		addPage(selectProgrammePointsPage);
		// Only one of the previous two pages is made visible
		// =======================================================

		selectInvoiceRecipientPage = new SelectInvoiceRecipientPage(eventPK);
		selectInvoiceRecipientPage.setTitle(I18N.CreateProgrammeBookings_Text);
		addPage(selectInvoiceRecipientPage);

		infoAndOptionsPage = new InfoAndOptionsPage(eventPK);
		addPage(infoAndOptionsPage);

		overviewPage = new OverviewPage();
		addPage(overviewPage);

	}


	/**
	 * This method produces the data needed for the overview page in this wizard and the actual booking on the server.
	 * The data are the Cartesian product of all originally selected participants and all selected programme offerings.
	 */
	protected List<ProgrammeBookingParameter> createBookingParameters() throws Exception {

		if (decideBookingModePage.isBookingViaProgrammeOfferings() && selectProgrammeOfferingsPage != null) {
			// For each of the programme offerings
			return ProgrammeBookingParameterHelper.createProgrammeBookingParameterList(
				participantList,
				selectProgrammeOfferingsPage,
				selectInvoiceRecipientPage,
				infoAndOptionsPage
			);
		}
		else {
			// Collecting parameters needed to ask the server to get the initial booking parameters

			List<ProgrammePointCVO> bookedProgrammePointsCVO = selectProgrammePointsPage.getBookedProgrammePointsCVO();
			HashMap<Long, ProgrammePointCVO> pk2ppMap = ProgrammePointCVO.abstractCVOs2Map(bookedProgrammePointsCVO);

			Collection<Long> programmPointPKs = AbstractCVO.getPKs(bookedProgrammePointsCVO);
			boolean onlyOnce = infoAndOptionsPage.isOnlyOnce();
			Date referenceDate = infoAndOptionsPage.getReferenceDate();

			// Ask the server to get the initial booking parameters
			List<ProgrammeBookingParameter> pbParameters = getProgrammeBookingMgr().getInitialProgrammeBookingParameters(
				participantList,
				programmPointPKs,
				null, // invoiceRecipientPK
				onlyOnce,
				referenceDate
			);

			// Add the programme point names, invoiceRecipientPKs and -searchData, the latter may be null
			for (ProgrammeBookingParameter programmeBookingParameter : pbParameters) {
				programmePointCVO = pk2ppMap.get(programmeBookingParameter.programmePointPK);

				// get the count of programme points to book from the gui
				programmeBookingParameter.count = selectProgrammePointsPage.getCount(programmePointCVO);

				programmeBookingParameter.workGroupPK = selectProgrammePointsPage.getWorkGroupPK(programmePointCVO);

				programmeBookingParameter.programmePointName = programmePointCVO.getPpName();

				programmeBookingParameter.invoiceRecipientPK = BookingParameterHelper.findInvoiceRecipient(
					selectInvoiceRecipientPage,
					programmeBookingParameter.benefitRecipient
				);

				programmeBookingParameter.invoiceRecipient = BookingParameterHelper.findInvoiceRecipientSearchData(
					selectInvoiceRecipientPage,
					programmeBookingParameter.benefitRecipient
				);
			}


			return pbParameters;
		}

	}


	@Override
	public boolean performFinish() {
		final boolean[] bookingSucceeded = new boolean[1];
		bookingSucceeded[0] = false;

		try {
			// If the final OverviewPage is shown, we can get an up-to-date
			// list of bookingParameters from there, otherwise create a new list
			final List<ProgrammeBookingParameter> programmeBookingParameters;

			IWizardPage currentPage = getContainer().getCurrentPage();
			if (currentPage instanceof OverviewPage) {
				programmeBookingParameters = ((OverviewPage)currentPage).getProgrammeBookingParameterList();
			}
			else {
				programmeBookingParameters = createBookingParameters();
			}

			if (CollectionsHelper.notEmpty(programmeBookingParameters)) {
				final List<ServerMessage> serverMessages = new ArrayList<>();

				// now we have a nonempty list of booking parameters and start the booking on the server
				BusyCursorHelper.busyCursorWhile(new IRunnableWithProgress() {
					@Override
					public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
						try {
							// book the selected waitlisted Bookings
							List<ServerMessage> srvMsgList = ProgrammeBookingModel.getInstance().bookProgramme(programmeBookingParameters);
							if (srvMsgList != null) {
								serverMessages.addAll(srvMsgList);
							}
							bookingSucceeded[0] = true;
						}
						catch (Exception e) {
							RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
						}
					}
				});

				// if necessary, show Server-Messages
				if (serverMessages != null && !serverMessages.isEmpty()) {
					ServerMessageDialog.open(getShell(), serverMessages);
				}
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);

		}
		return bookingSucceeded[0];
	}

	private boolean atLeastOneWorkGroup;

	public boolean isAtLeastOneWorkGroup() {
		return atLeastOneWorkGroup;
	}

	public void setAtLeastOneWorkGroup(boolean atLeastOneWorkGroup) {
		this.atLeastOneWorkGroup = atLeastOneWorkGroup;
	}



}
