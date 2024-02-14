package de.regasus.programme.booking.dialog;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;

import com.lambdalogic.messeinfo.kernel.ServerMessage;
import com.lambdalogic.messeinfo.participant.ParticipantMessage;
import com.lambdalogic.messeinfo.participant.data.IParticipant;
import com.lambdalogic.messeinfo.participant.data.ProgrammeBookingParameter;
import com.lambdalogic.util.exception.ErrorMessageException;
import com.lambdalogic.util.rcp.BusyCursorHelper;
import com.lambdalogic.util.rcp.error.ErrorHandler.ErrorLevel;

import de.regasus.I18N;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.dialog.ServerMessageDialog;
import de.regasus.participant.booking.SelectInvoiceRecipientPage;
import de.regasus.programme.ProgrammeBookingModel;
import de.regasus.ui.Activator;

/**
 * A wizard to create programme bookings for one or more participants which all have the same participant type.
 * 
 * @author manfred
 * 
 */
public class CreateProgrammeBookingsWizard extends Wizard {

	private List<? extends IParticipant> iParticipantList;

	private Long participantTypePK;

	private Long eventPK;

	// *************************************************************************
	// * Pages
	// *

	/**
	 * A page showing a table with program offerings that can be checked and given a count to be booked
	 */
	private SelectProgrammeOfferingsPage selectProgrammeOfferingsPage;

	private SelectInvoiceRecipientPage selectInvoiceRecipientPage;

	private InfoAndOptionsPage infoAndOptionsPage;

	private OverviewPage overviewPage;


	public CreateProgrammeBookingsWizard(
		Long eventPK,
		List<? extends IParticipant> iParticipantList,
		Long participantTypePK
	) {
		this.iParticipantList = iParticipantList;
		this.participantTypePK = participantTypePK;
		this.eventPK = eventPK;
	}


	@Override
	public void addPages() {
		selectProgrammeOfferingsPage = new SelectProgrammeOfferingsPage(eventPK, participantTypePK);
		addPage(selectProgrammeOfferingsPage);

		selectInvoiceRecipientPage = new SelectInvoiceRecipientPage(eventPK);
		selectInvoiceRecipientPage.setTitle(I18N.CreateProgrammeBookings_Text);

		addPage(selectInvoiceRecipientPage);

		infoAndOptionsPage = new InfoAndOptionsPage(eventPK);
		addPage(infoAndOptionsPage);

		overviewPage = new OverviewPage();
		addPage(overviewPage);
	}


	/**
	 * When the wizard is finished, we perform the booking on the server (directly, without model) and refresh the
	 * involved participants. Parallel to that we give progress information.
	 * 
	 * @return
	 */
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
			
			if (programmeBookingParameters != null && !programmeBookingParameters.isEmpty()) {
				final List<ServerMessage> serverMessages = new ArrayList<ServerMessage>();
				
				// now we have a nonempty list of booking parameters and start the booking on the server
				BusyCursorHelper.busyCursorWhile(new IRunnableWithProgress() {
					public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
						try {
							// book the selected waitlisted Bookings
							List<ServerMessage> srvMsgList = ProgrammeBookingModel.getInstance().bookProgramme(programmeBookingParameters);
							if (srvMsgList != null) {
								serverMessages.addAll(srvMsgList);
							}
							bookingSucceeded[0] = true;
						}
						catch (ErrorMessageException e) {
							ErrorLevel errorLevel = null;
							if (e.getErrorCode().equals("NumberOfBookingsExceedsMaxNumber") ||
								e.getErrorCode().equals(ParticipantMessage.ProgrammePointDemandsWorkGroup.name()) ||
								e.getErrorCode().equals(ParticipantMessage.WorkGroupIsFull.name())
							) {
								errorLevel = ErrorLevel.USER;
							}
							RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e, errorLevel);
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


	// *************************************************************************
	// * Private helper methods
	// *

	/**
	 * This method produces the data needed for the overview page in this wizard and the actual booking on the server.
	 * The data are the Cartesian product of all originally selected participants and all selected programme offerings.
	 */
	protected List<ProgrammeBookingParameter> createBookingParameters() throws Exception {
		return ProgrammeBookingParameterHelper.createProgrammeBookingParameterList(
			iParticipantList,
			selectProgrammeOfferingsPage,
			selectInvoiceRecipientPage,
			infoAndOptionsPage
		);
	}
	
	
	private boolean atLeastOneWorkGroup;
	
	public boolean isAtLeastOneWorkGroup() {
		return atLeastOneWorkGroup;
	}

	public void setAtLeastOneWorkGroup(boolean atLeastOneWorkGroup) {
		this.atLeastOneWorkGroup = atLeastOneWorkGroup;
	}
	

}
