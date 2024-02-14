package de.regasus.hotel.booking.dialog;

import java.util.List;

import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import com.lambdalogic.messeinfo.hotel.data.HotelBookingCVO;
import com.lambdalogic.messeinfo.hotel.data.HotelCancelationTermVO;
import com.lambdalogic.messeinfo.invoice.InvoiceLabel;

import de.regasus.I18N;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.ui.Activator;

public class SelectCancelationTermsPage extends WizardPage {

	public static final String NAME = "SelectCancelationTermsPage";

	// *************************************************************************
	// * Domain Attributes
	// *



	/**
	 * The list of hotel offerings fetched from the server
	 */
	private List<HotelBookingCVO> bookingsCVO;


	private HotelBookingCancelationTable hotelOfferingsTable;


	// *************************************************************************
	// * Constructor
	// *

	/**
	 * @param participantTypePK
	 */
	protected SelectCancelationTermsPage(List<HotelBookingCVO> bookingsCVO) {
		super(NAME);

		this.bookingsCVO = bookingsCVO;

		setTitle(I18N.CancelBooking);
		setMessage(I18N.CancelBooking_OptionallyChooseCancelationTerms);
	}


	// *************************************************************************
	// * Methods
	// *

	@Override
	public void createControl(Composite parent) {
		Composite controlComposite = new Composite(parent, SWT.NONE);

		controlComposite.setLayout(new GridLayout(2, false));

		try {

			// Table to show HotelOfferings and to allow the entry of a number of bookings
			Composite tableComposite = new Composite(controlComposite, SWT.BORDER);
			tableComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));

			TableColumnLayout layout = new TableColumnLayout();
			tableComposite.setLayout(layout);
			final Table table = new Table(tableComposite, SWT.NONE );
			table.setHeaderVisible(true);
			table.setLinesVisible(true);

			// Description
			final TableColumn hotelBookingTableColumn = new TableColumn(table, SWT.NONE);
			layout.setColumnData(hotelBookingTableColumn, new ColumnWeightData(60));
			hotelBookingTableColumn.setText(I18N.HotelBooking);

			// Term
			final TableColumn cancellationTermTableColumn = new TableColumn(table, SWT.NONE);
			layout.setColumnData(cancellationTermTableColumn, new ColumnWeightData(40));
			cancellationTermTableColumn.setText(InvoiceLabel.CancellationTerm.getString());

			hotelOfferingsTable = new HotelBookingCancelationTable(table, bookingsCVO);
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
		setControl(controlComposite);
	}


	protected HotelCancelationTermVO getChosenCancelationTermForBooking(HotelBookingCVO hotelBookingCVO) {
		return hotelOfferingsTable.getChosenCancelationTermForBooking(hotelBookingCVO);
	}
}
