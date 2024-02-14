package de.regasus.hotel.booking.dialog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import com.lambdalogic.messeinfo.hotel.HotelLabel;
import com.lambdalogic.messeinfo.hotel.data.HotelBookingParameter;
import com.lambdalogic.messeinfo.invoice.InvoiceLabel;
import com.lambdalogic.messeinfo.participant.ParticipantLabel;

import de.regasus.I18N;
import com.lambdalogic.util.rcp.UtilI18N;

/**
 * A page in the wizard to create hotel bookings where all participants are listed who
 * get a booked hotel room. 
 * <p>
 * In case there are in the meantime not enough rooms left, the user can uncheck some
 * participants to perform the booking anyway.
 * <p>
 * See https://mi2.lambdalogic.de/jira/browse/MIRCP-84
 */
public class CreateHotelBookingOverviewPage extends WizardPage {

	public static final String NAME = "OverviewPage";

	private HotelBookingParametersTable bookingParametersTable;

	private List<HotelBookingParameter> bookingParameters;

	private HashMap<HotelBookingParameter, Boolean> bookingMap = new HashMap<HotelBookingParameter, Boolean>();


	// *************************************************************************
	// * Constructor
	// *

	
	public CreateHotelBookingOverviewPage() {
		super(NAME);

		setTitle(I18N.CreateHotelBookingOverviewPage_Title);
		setMessage(I18N.CreateHotelBookingOverviewPage_Message);
	}
	

	@Override
	public CreateHotelBookingWizard getWizard() {
		return (CreateHotelBookingWizard) super.getWizard();
	}


	/**
	 * Whenever this page is made visible, it tries to get the bookingParamters from the wizard to show them in the
	 * table.
	 */
	@Override
	public void setVisible(boolean visible) {

		// Don't(!) do anything if this page is made invisible!
		if (visible) {
			try {
				bookingParameters = getWizard().createBookingParameters();

				bookingMap.clear();
				for (HotelBookingParameter bp : bookingParameters) {
					bookingMap.put(bp, Boolean.TRUE);
				}

				bookingParametersTable.getViewer().setInput(bookingParameters);

			}
			catch (Exception e) {
				com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
			}
		}

		super.setVisible(visible);
	}


	@Override
	public void createControl(Composite parent) {
		Composite controlComposite = new Composite(parent, SWT.NONE);

		TableColumnLayout layout = new TableColumnLayout();
		controlComposite.setLayout(layout);

		final Table table = new Table(controlComposite, SWT.BORDER);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		// PARTICIPANT_NR, PARTICIPANT, HOTEL, INVOICE_RECIPIENT, TOTAL_AMOUNT

		// BOOK
		final TableColumn bookTableColumn = new TableColumn(table, SWT.CENTER);
		layout.setColumnData(bookTableColumn, new ColumnWeightData(20));
		// bookTableColumn.setText(UtilI18N.NumberAbreviation);

		// PARTICIPANT_NR
		final TableColumn participantNrTableColumn = new TableColumn(table, SWT.RIGHT);
		layout.setColumnData(participantNrTableColumn, new ColumnWeightData(30));
		participantNrTableColumn.setText(UtilI18N.NumberAbreviation);

		// PARTICIPANT
		final TableColumn participantTableColumn = new TableColumn(table, SWT.NONE);
		layout.setColumnData(participantTableColumn, new ColumnWeightData(100));
		participantTableColumn.setText(ParticipantLabel.Participant.getString());
		
		// INVOICE_RECIPIENT
		final TableColumn invoiceRecipientTableColumn = new TableColumn(table, SWT.NONE);
		layout.setColumnData(invoiceRecipientTableColumn, new ColumnWeightData(100));
		invoiceRecipientTableColumn.setText(ParticipantLabel.Bookings_InvoiceRecipient.getString());

		// HOTEL
		final TableColumn pointTableColumn = new TableColumn(table, SWT.LEFT);
		layout.setColumnData(pointTableColumn, new ColumnWeightData(100));
		pointTableColumn.setText(HotelLabel.Hotel.getString());

		// TOTAL_AMOUNT
		final TableColumn totalAmountTableColumn = new TableColumn(table, SWT.NONE);
		layout.setColumnData(totalAmountTableColumn, new ColumnWeightData(100));
		totalAmountTableColumn.setText(InvoiceLabel.TotalPrice.getString());

		bookingParametersTable = new HotelBookingParametersTable(table, bookingMap);

		setControl(controlComposite);
	}


	public List<HotelBookingParameter> getBookingParameters() {
		List<HotelBookingParameter> checkedBookingParameters = new ArrayList<>();
		for (HotelBookingParameter hotelBookingParameter : bookingParameters) {
			if (Boolean.TRUE.equals(bookingMap.get(hotelBookingParameter))) {
				checkedBookingParameters.add(hotelBookingParameter);
			}
		}

		return checkedBookingParameters;
	}

}
