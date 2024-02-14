package de.regasus.programme.booking.dialog;

import java.util.List;

import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import com.lambdalogic.messeinfo.invoice.InvoiceLabel;
import com.lambdalogic.messeinfo.participant.ParticipantLabel;
import com.lambdalogic.messeinfo.participant.data.ProgrammeBookingParameter;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.I18N;
import de.regasus.core.error.RegasusErrorHandler;
import com.lambdalogic.util.rcp.UtilI18N;
import de.regasus.ui.Activator;

/**
 * A page that contains a table that shows for participants, what bookings of programme points and offerings are to be
 * done soon.
 * <p>
 * {@link https://mi2.lambdalogic.de/jira/browse/MIRCP-104 }
 * 
 * @author manfred
 * 
 */
public class OverviewPage extends WizardPage {

	public static final String NAME = "OverviewPage";

	private ProgrammeBookingParametersTable bookingParametersTable;

	private List<ProgrammeBookingParameter> bookingParameters;

	private TableColumn workGroupTableColumn;

	private TableColumnLayout layout;


	public List<ProgrammeBookingParameter> getProgrammeBookingParameterList() {
		return bookingParameters;
	}


	public OverviewPage() {
		super(NAME);

		setTitle(I18N.CreateProgrammeBookings_Text);
		setMessage(UtilI18N.Overview);
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
				bookingParameters = null;

				boolean showColumn = false;
				
				if (getWizard() instanceof CreateProgrammeBookingsWizard) {
					CreateProgrammeBookingsWizard createProgrammeBookingsWizard =
						(CreateProgrammeBookingsWizard) getWizard();
					bookingParameters = createProgrammeBookingsWizard.createBookingParameters();
					showColumn = createProgrammeBookingsWizard.isAtLeastOneWorkGroup();
				}
				else if (getWizard() instanceof CreateProgrammeBookingsWizardSeveralParticipantTypes) {
					CreateProgrammeBookingsWizardSeveralParticipantTypes createProgrammeBookingsWizard =
						(CreateProgrammeBookingsWizardSeveralParticipantTypes) getWizard();
					bookingParameters = createProgrammeBookingsWizard.createBookingParameters();
					showColumn = createProgrammeBookingsWizard.isAtLeastOneWorkGroup();
				}
				
				
				// If needed, hide the workgoup column as good as possible 
				if (showColumn) {
					workGroupTableColumn.setText(ParticipantLabel.WorkGroup.getString());
					layout.setColumnData(workGroupTableColumn, new ColumnWeightData(100));
				}
				else {
					layout.setColumnData(workGroupTableColumn, new ColumnWeightData(0, 0, false));
					workGroupTableColumn.setText("");
				}
				
				workGroupTableColumn.setResizable(showColumn);
				
				bookingParametersTable.getViewer().setInput(bookingParameters);
				
				// refresh table layout to hide/show dynamical columns 
				SWTHelper.deferredLayout(300, bookingParametersTable.getViewer().getTable().getParent());
			}
			catch (Exception e) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			}
		}

		super.setVisible(visible);
	}


	public void createControl(Composite parent) {
		Composite controlComposite = new Composite(parent, SWT.NONE);

		layout = new TableColumnLayout();
		controlComposite.setLayout(layout);

		final Table table = new Table(controlComposite, SWT.BORDER);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		// PARTICIPANT, PARTICIPANT_NR, HOTEL, OFFERING_DESC, TOTAL_AMOUNT, COUNT, INVOICE_RECIPIENT

		// PARTICIPANT_NR
		final TableColumn participantNrTableColumn = new TableColumn(table, SWT.RIGHT);
		layout.setColumnData(participantNrTableColumn, new ColumnWeightData(30));
		participantNrTableColumn.setText(UtilI18N.NumberAbreviation);

		// PARTICIPANT
		final TableColumn participantTableColumn = new TableColumn(table, SWT.NONE);
		layout.setColumnData(participantTableColumn, new ColumnWeightData(100));
		participantTableColumn.setText(ParticipantLabel.Participant.getString());
		
		// Invoice Recipient
		final TableColumn invoiceRecipientTableColumn = new TableColumn(table, SWT.NONE);
		layout.setColumnData(invoiceRecipientTableColumn, new ColumnWeightData(100));
		invoiceRecipientTableColumn.setText(ParticipantLabel.Bookings_InvoiceRecipient.getString());

		// Programme Point
		final TableColumn programmePointTableColumn = new TableColumn(table, SWT.LEFT);
		layout.setColumnData(programmePointTableColumn, new ColumnWeightData(100));
		programmePointTableColumn.setText(ParticipantLabel.ProgrammePoint.getString());

		// Offering Description
		final TableColumn descTableColumn = new TableColumn(table, SWT.NONE);
		layout.setColumnData(descTableColumn, new ColumnWeightData(100));
		descTableColumn.setText(ParticipantLabel.ProgrammeOffering.getString());
		
		// Work Group (hide/show dynamically)
		workGroupTableColumn = new TableColumn(table, SWT.NONE);
		// setting default ColumnData to avoid Exception, because dynamical setting is done too late
		layout.setColumnData(workGroupTableColumn, new ColumnWeightData(100));

		// Count
		final TableColumn countTableColumn = new TableColumn(table, SWT.RIGHT);
		layout.setColumnData(countTableColumn, new ColumnWeightData(30));
		countTableColumn.setText(UtilI18N.Count);
		
		// Price (total)
		final TableColumn priceTableColumn = new TableColumn(table, SWT.RIGHT);
		layout.setColumnData(priceTableColumn, new ColumnWeightData(60));
		priceTableColumn.setText(InvoiceLabel.TotalPrice.getString());


		bookingParametersTable = new ProgrammeBookingParametersTable(table);

		setControl(controlComposite);
	}

}
