package de.regasus.hotel.booking.dialog;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Objects;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;

import com.lambdalogic.messeinfo.config.parameterset.ConfigParameterSet;
import com.lambdalogic.messeinfo.config.parameterset.HotelConfigParameterSet;
import com.lambdalogic.messeinfo.hotel.data.HotelBookingParameter;
import com.lambdalogic.messeinfo.participant.data.HotelCostCoverage;
import com.lambdalogic.messeinfo.participant.data.IParticipant;
import com.lambdalogic.util.CollectionsHelper;
import com.lambdalogic.util.rcp.BusyCursorHelper;

import de.regasus.I18N;
import de.regasus.core.ConfigParameterSetModel;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.hotel.HotelBookingModel;
import de.regasus.participant.booking.SelectInvoiceRecipientPage;
import de.regasus.ui.Activator;

/**
 * A wizard to create hotel bookings for one or more participants which all have the same participant type.
 */
class CreateHotelBookingWizard extends Wizard {

	private List<? extends IParticipant> participantList;

	private Long eventId;
	private HotelCostCoverage hotelCostCoverage;


	// data from WizardPages


	// *************************************************************************
	// * Pages
	// *

	// page 1
	private HotelSelectionCriteriaPage hotelSelectionCriteriaPage;

	// page 2
	private HotelOfferingsTablePage hotelOfferingsTablePage;

	// page 3
	private CreateHotelBookingPaymentConditionsPage hotelPaymentConditionsPage;

	// page 4
	private SelectInvoiceRecipientPage selectInvoiceRecipientPage;

	// page 5
	private CreateHotelBookingInfoPage infoPage;

	// page 6
	private CreateHotelBookingOverviewPage overviewPage;


	public CreateHotelBookingWizard(List<? extends IParticipant> participantList) {
		Objects.requireNonNull(participantList);
		if ( participantList.isEmpty() ) {
			throw new IllegalArgumentException("There must be at least one participant.");
		}
		this.participantList = participantList;


		// determine eventId
		for (IParticipant participant : participantList) {
			Long currentEventId = participant.getEventId();
			if (eventId == null) {
				eventId = currentEventId;
			}
			else if ( !eventId.equals(currentEventId) ) {
				throw new IllegalArgumentException("The selected participants must belong to the same event.");
			}
		}


		// Common title for wizard
		String title = I18N.CreateHotelBookingForNParticipants.replace("<n>", String.valueOf(participantList.size()));
		setWindowTitle(title);
	}


	public HotelCostCoverage getHotelCostCoverage() {
		return hotelCostCoverage;
	}


	public void setHotelCostCoverage(HotelCostCoverage hotelCostCoverage) {
		this.hotelCostCoverage = hotelCostCoverage;
	}


	public HotelConfigParameterSet getHotelConfigParameterSet() {
		HotelConfigParameterSet hotelConfigParameterSet;
		try {
			ConfigParameterSetModel configParameterSetModel = ConfigParameterSetModel.getInstance();
			ConfigParameterSet configParameterSet = configParameterSetModel.getConfigParameterSet(eventId);
			hotelConfigParameterSet = configParameterSet.getEvent().getHotel();
		}
		catch (Exception e) {
			com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
			hotelConfigParameterSet = new HotelConfigParameterSet();
		}

		return hotelConfigParameterSet;
	}


	public List<? extends IParticipant> getParticipantList() {
		return participantList;
	}


	public Long getEventId() {
		return eventId;
	}


	@Override
	public void addPages() {
		hotelSelectionCriteriaPage = new HotelSelectionCriteriaPage();
		hotelSelectionCriteriaPage.setHotelCostCoverage(hotelCostCoverage);
		addPage(hotelSelectionCriteriaPage);

		hotelOfferingsTablePage = new HotelOfferingsTablePage();
		addPage(hotelOfferingsTablePage);

		hotelPaymentConditionsPage = new CreateHotelBookingPaymentConditionsPage();
		addPage(hotelPaymentConditionsPage);

		selectInvoiceRecipientPage = new SelectInvoiceRecipientPage(eventId);

		addPage(selectInvoiceRecipientPage);

		infoPage = new CreateHotelBookingInfoPage(eventId);
		addPage(infoPage);

		overviewPage = new CreateHotelBookingOverviewPage();
		addPage(overviewPage);
	}


	// *********************************************************************************************
	// * Getter for WizardPages
	// *

	public HotelSelectionCriteriaPage getHotelSelectionCriteriaPage() {
		return hotelSelectionCriteriaPage;
	}


	public HotelOfferingsTablePage getHotelOfferingsTablePage() {
		return hotelOfferingsTablePage;
	}


	public CreateHotelBookingPaymentConditionsPage getHotelPaymentPage() {
		return hotelPaymentConditionsPage;
	}


	public SelectInvoiceRecipientPage getSelectInvoiceRecipientPage() {
		return selectInvoiceRecipientPage;
	}


	public CreateHotelBookingInfoPage getInfoPage() {
		return infoPage;
	}


	public CreateHotelBookingOverviewPage getOverviewPage() {
		return overviewPage;
	}

	// *
	// * Getter for WizardPages
	// *********************************************************************************************


	public List<HotelBookingParameter> createBookingParameters() throws Exception {
		return HotelBookingParameterHelper.createBookingParameter(
			participantList,
			hotelSelectionCriteriaPage,
			hotelOfferingsTablePage,
			hotelPaymentConditionsPage,
			selectInvoiceRecipientPage,
			infoPage
		);
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
			final List<HotelBookingParameter> hotelBookingParameters;

			IWizardPage currentPage = getContainer().getCurrentPage();
			if (currentPage instanceof CreateHotelBookingOverviewPage) {
				hotelBookingParameters = ((CreateHotelBookingOverviewPage)currentPage).getBookingParameters();
			}
			else {
				hotelBookingParameters = createBookingParameters();
			}

			if (CollectionsHelper.notEmpty(hotelBookingParameters)) {
				// now we have a non-empty list of booking parameters and create the booking on the server
				BusyCursorHelper.busyCursorWhile(new IRunnableWithProgress() {
					@Override
					public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {

						try {
							// create Hotel Booking
							HotelBookingModel.getInstance().bookHotel(hotelBookingParameters);
							bookingSucceeded[0] = true;

							/* A newly created Hotel Booking has only one Benefit Recipient. Though it would make sense
							 * the Hotel Booking dialog does not support to assign additional Benefit Recipients.
							 * For that reason it is not necessary to refresh any other Participant.
							 *
							 * However, if the Hotel Booking diaog might be able to create Hotel Bookings with multiple
							 * Benefit Recipients, refreshing these Participant should also be unnecessary. Instead the
							 * refresh should be handled by the Models and not by the GUI.
							 */
						}
						catch (Exception e) {
							RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
						}

					}
				});
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}

		return bookingSucceeded[0];
	}

}
