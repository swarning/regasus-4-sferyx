package de.regasus.programme.booking.dialog;

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

import com.lambdalogic.messeinfo.invoice.InvoiceLabel;
import com.lambdalogic.messeinfo.participant.data.ProgrammeBookingCVO;
import com.lambdalogic.messeinfo.participant.data.ProgrammeCancelationTermVO;

import de.regasus.I18N;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.ui.Activator;

public class SelectCancelationTermsPage extends WizardPage {

	public static final String NAME = "SelectCancelationTermsPage";

	// *************************************************************************
	// * Domain Attributes
	// *



	/**
	 * The list of programme offerings fetched from the server
	 */
	private List<ProgrammeBookingCVO> bookingsCVO;


	private ProgrammeBookingCancelationTable programmeOfferingsTable;


	// *************************************************************************
	// * Constructor
	// *

	/**
	 * @param participantTypePK
	 */
	protected SelectCancelationTermsPage(List<ProgrammeBookingCVO> bookingsCVO) {
		super(NAME);

		this.bookingsCVO = bookingsCVO;

		setTitle(I18N.CancelBooking);
		setMessage(I18N.CancelBooking_OptionallyChooseCancelationTerms);	}


	// *************************************************************************
	// * Methods
	// *

	@Override
	public void createControl(Composite parent) {
		Composite controlComposite = new Composite(parent, SWT.NONE);

		controlComposite.setLayout(new GridLayout(2, false));

		try {

			// Table to show ProgrammeOfferings and to allow the entry of a number of bookings
			Composite tableComposite = new Composite(controlComposite, SWT.BORDER);
			tableComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));

			TableColumnLayout layout = new TableColumnLayout();
			tableComposite.setLayout(layout);
			final Table table = new Table(tableComposite, SWT.SINGLE | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL);
			table.setHeaderVisible(true);
			table.setLinesVisible(true);

			// Description
			final TableColumn programmeBookingTableColumn = new TableColumn(table, SWT.NONE);
			layout.setColumnData(programmeBookingTableColumn, new ColumnWeightData(60));
			programmeBookingTableColumn.setText(I18N.ProgrammeBooking);

			// Term
			final TableColumn cancellationTermTableColumn = new TableColumn(table, SWT.NONE);
			layout.setColumnData(cancellationTermTableColumn, new ColumnWeightData(40));
			cancellationTermTableColumn.setText(InvoiceLabel.CancellationTerm.getString());

			programmeOfferingsTable = new ProgrammeBookingCancelationTable(table, bookingsCVO);
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
		setControl(controlComposite);
	}


	protected ProgrammeCancelationTermVO getChosenCancelationTermForBooking(ProgrammeBookingCVO programmeBookingCVO) {
		return programmeOfferingsTable.getChosenCancelationTermForBooking(programmeBookingCVO);
	}
}
