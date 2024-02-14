package de.regasus.programme.booking.dialog;

import static de.regasus.LookupService.getProgrammePointMgr;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.TreeMap;

import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import com.lambdalogic.messeinfo.kernel.data.AbstractCVO_PK_Comparator;
import com.lambdalogic.messeinfo.participant.ParticipantLabel;
import com.lambdalogic.messeinfo.participant.data.ProgrammeBookingParameter;
import com.lambdalogic.messeinfo.participant.data.ProgrammePointCVO;
import com.lambdalogic.messeinfo.participant.data.WorkGroupVO;
import com.lambdalogic.messeinfo.participant.interfaces.ProgrammePointCVOSettings;
import com.lambdalogic.util.CollectionsHelper;
import com.lambdalogic.util.rcp.simpleviewer.ITableEditListener;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.I18N;
import de.regasus.core.error.RegasusErrorHandler;
import com.lambdalogic.util.rcp.UtilI18N;
import de.regasus.participant.booking.SelectInvoiceRecipientPage;
import de.regasus.programme.WorkGroupModel;
import de.regasus.ui.Activator;

/**
 *
 * A wizard page showing a table with program points that the user can
 * <ul>
 * <li>check,</li>
 * <li>give a count to be booked,</li>
 * <li>give a different price, if it is editable.</li>
 * </ul>
 * When the wizard is finished, he can use the methods {@link #getPrice(ProgrammePointCVO)} and
 * {@link #getCount(ProgrammePointCVO)} to obtain the user input.
 * <p>
 * See https://mi2.lambdalogic.de/jira/browse/MIRCP-105
 *
 * @author manfred
 *
 */
public class SelectProgrammePointsPage extends WizardPage {

	public static final String NAME = "SelectProgrammePointsPage";

	// *************************************************************************
	// * Domain Attributes
	// *

	/**
	 * For each ProgrammePointCVO, this map stores the number of places booked (0 if none)
	 */
	private TreeMap<ProgrammePointCVO, Integer> bookingMap = new TreeMap<ProgrammePointCVO, Integer>(AbstractCVO_PK_Comparator.getInstance());

	/**
	 * For each ProgrammePointCVO, this map stores the booked work group
	 * 	(or null if none, of {@link ProgrammeBookingParameter#AUTO_WORK_GROUP})
	 */
	private TreeMap<ProgrammePointCVO, Long> workGroupMap =
		new TreeMap<ProgrammePointCVO, Long>(AbstractCVO_PK_Comparator.getInstance());


	/**
	 * The event for which offerings are to be shown
	 */
	private Long eventPK;


	/**
	 * The list of programme offerings fetched from the server
	 */
	private Collection<ProgrammePointCVO> programmePointsCVO = Collections.emptyList();

	/**
	 * The settings for the amount of data is to be fetched from the server
	 */
	private ProgrammePointCVOSettings settings = new ProgrammePointCVOSettings();

	// *************************************************************************
	// * Widgets
	// *

	private ProgrammePointsTable programmePointsTable;


	// *************************************************************************
	// * Constructor
	// *

	/**
	 * @param participantTypePK
	 */
	protected SelectProgrammePointsPage(Long eventPK) {
		super(NAME);

		this.eventPK = eventPK;

		setTitle(I18N.CreateProgrammeBookings_Text);
		setMessage(I18N.CreateProgrammeBookings_SelectProgrammPoints);


		// settings.programmeOfferingCVOSettings = new ProgrammeOfferingCVOSettings();
		settings.withProgrammePointTypeVO = true;
		settings.withNumberOfBookings = true;
	}


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
			final Table table = new Table(tableComposite, SWT.NONE /* SWT.SINGLE | SWT.FULL_SELECTION */);
			table.setHeaderVisible(true);
			table.setLinesVisible(true);

			// Book
			final TableColumn bookTableColumn = new TableColumn(table, SWT.NONE);
			layout.setColumnData(bookTableColumn, new ColumnWeightData(40));
			// bookTableColumn.setText(ParticipantLabel.Bookings_Book.getString());

			// Count
			final TableColumn countTableColumn = new TableColumn(table, SWT.RIGHT);
			layout.setColumnData(countTableColumn, new ColumnWeightData(60));
			countTableColumn.setText(UtilI18N.Count);

			// Description
			final TableColumn descTableColumn = new TableColumn(table, SWT.NONE);
			layout.setColumnData(descTableColumn, new ColumnWeightData(400));
			descTableColumn.setText(UtilI18N.Description);

			// Number of Bookings
			final TableColumn numberOfBookingsTableColumn = new TableColumn(table, SWT.RIGHT);
			layout.setColumnData(numberOfBookingsTableColumn, new ColumnWeightData(100));
			numberOfBookingsTableColumn.setText(ParticipantLabel.NumberOfBookings_Capacity.getString());

			// WorkGroup
			final TableColumn workGroupTableColumn = new TableColumn(table, SWT.RIGHT);
			layout.setColumnData(workGroupTableColumn, new ColumnWeightData(100));
			workGroupTableColumn.setText(ParticipantLabel.WorkGroup.getString());

			programmePointsTable = new ProgrammePointsTable(table, bookingMap, workGroupMap);

			programmePointsTable.addEditListener(new ITableEditListener(){
				@Override
				public void tableCellChanged() {
					setPageComplete(programmePointsTable.isAnythingBooked());
				}
			});

			/* Get the Programme Points via LoadHelper and not via ProgrammePointModel, because
			 * - the ProgrammePointModel works with VOs and here we need CVOs with the numberOfBookings
			 * - the value of numberOfBookings must always be actual
			 */
			programmePointsCVO = getProgrammePointMgr().getProgrammePointCVOsByEventPK(eventPK, settings);

			for (ProgrammePointCVO programmePointCVO : programmePointsCVO) {
				bookingMap.put(programmePointCVO, 0);
			}


			boolean thereIsAtLeastOneWorkGroup = false;

			WorkGroupModel workGroupModel = WorkGroupModel.getInstance();
			for (ProgrammePointCVO programmePointCVO : programmePointsCVO) {
				Long programmePointPK = programmePointCVO.getPK();
				List<WorkGroupVO> workGroupVOs = workGroupModel.getWorkGroupVOsByProgrammePointPK(programmePointPK);
				if (CollectionsHelper.notEmpty(workGroupVOs)) {
					thereIsAtLeastOneWorkGroup = true;
					break;
				}
			}

			// If needed, hide the workgoup column as good as possible
			if (! thereIsAtLeastOneWorkGroup) {
				layout.setColumnData(workGroupTableColumn, new ColumnWeightData(0, false));
				workGroupTableColumn.setText("");
			}

			// Propagate the information to the wizard, so that the overview page can also hide the column
			if (getWizard() instanceof CreateProgrammeBookingsWizard) {
				CreateProgrammeBookingsWizard createProgrammeBookingsWizard =
					(CreateProgrammeBookingsWizard) getWizard();
				createProgrammeBookingsWizard.setAtLeastOneWorkGroup(thereIsAtLeastOneWorkGroup);
			}
			else if (getWizard() instanceof CreateProgrammeBookingsWizardSeveralParticipantTypes) {
				CreateProgrammeBookingsWizardSeveralParticipantTypes createProgrammeBookingsWizard =
					(CreateProgrammeBookingsWizardSeveralParticipantTypes) getWizard();
				createProgrammeBookingsWizard.setAtLeastOneWorkGroup(thereIsAtLeastOneWorkGroup);
			}


			programmePointsTable.setInput(programmePointsCVO);

			SWTHelper.deferredLayout(300, programmePointsTable.getViewer().getTable().getParent());


			setPageComplete(false);

		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
		setControl(controlComposite);
	}


	@Override
	public IWizardPage getNextPage() {
		return getWizard().getPage(SelectInvoiceRecipientPage.NAME);
	}


	// *************************************************************************
	// * Methods to obtain the relevant data for actually performing the booking
	// *

	public Collection<ProgrammePointCVO> getAllProgrammePointsCVO() {
		return programmePointsCVO;
	}

	public List<ProgrammePointCVO> getBookedProgrammePointsCVO() {
		List<ProgrammePointCVO> bookedProgrammePoints = new ArrayList<>();
		for (ProgrammePointCVO programmePointCVO : programmePointsCVO) {
			if (bookingMap.get(programmePointCVO).intValue() > 0) {
				bookedProgrammePoints.add(programmePointCVO);
			}
		}
		return bookedProgrammePoints;
	}



	public int getCount(ProgrammePointCVO programmePointCVO) {
		return bookingMap.get(programmePointCVO);
	}

	public Long getWorkGroupPK(ProgrammePointCVO programmePointCVO) {
		return workGroupMap.get(programmePointCVO);
	}

}
