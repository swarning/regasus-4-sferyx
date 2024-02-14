package de.regasus.programme.booking.dialog;

import static de.regasus.LookupService.getProgrammeOfferingMgr;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TreeMap;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import com.lambdalogic.messeinfo.invoice.InvoiceLabel;
import com.lambdalogic.messeinfo.invoice.data.PriceVO;
import com.lambdalogic.messeinfo.kernel.data.AbstractCVO_PK_Comparator;
import com.lambdalogic.messeinfo.participant.Participant;
import com.lambdalogic.messeinfo.participant.ParticipantLabel;
import com.lambdalogic.messeinfo.participant.data.ProgrammeBookingParameter;
import com.lambdalogic.messeinfo.participant.data.ProgrammeOfferingCVO;
import com.lambdalogic.messeinfo.participant.data.ProgrammeOfferingHelper;
import com.lambdalogic.messeinfo.participant.data.ProgrammeOfferingVO;
import com.lambdalogic.messeinfo.participant.data.WorkGroupVO;
import com.lambdalogic.messeinfo.participant.interfaces.ProgrammeOfferingCVOSettings;
import com.lambdalogic.messeinfo.participant.interfaces.ProgrammePointCVOSettings;
import com.lambdalogic.messeinfo.participant.interfaces.WorkGroupCVOSettings;
import com.lambdalogic.util.CollectionsHelper;
import com.lambdalogic.util.TypeHelper;
import com.lambdalogic.util.rcp.ModifyListenerAdapter;
import com.lambdalogic.util.rcp.UtilI18N;
import com.lambdalogic.util.rcp.datetime.DateTimeComposite;
import com.lambdalogic.util.rcp.simpleviewer.ITableEditListener;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.I18N;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.participant.booking.SelectInvoiceRecipientPage;
import de.regasus.participant.type.combo.ParticipantTypeCombo;
import de.regasus.programme.WorkGroupModel;
import de.regasus.ui.Activator;

/**
 *
 * A wizard page showing a table with program offerings that the user can
 * <ul>
 * <li>check,</li>
 * <li>give a count to be booked,</li>
 * <li>give a different price, if it is editable.</li>
 * </ul>
 * When the wizard is finished, he can use the methods {@link #getPrice(ProgrammeOfferingCVO)} and
 * {@link #getCount(ProgrammeOfferingCVO)} to obtain the user input.
 * <p>
 * The actual program offerings are not given by the wizard, but loaded within this page, since the user might change
 * the participant type and/or reference date, and in that case, a different set of bookings is to be loaded.
 * <p>
 * See https://mi2.lambdalogic.de/jira/browse/MIRCP-104
 *
 * @author manfred
 *
 */
public class SelectProgrammeOfferingsPage extends WizardPage {

	public static final String NAME = "SelectProgrammeOfferingsPage";

	public static final String DIALOG_SETTING_SHOW_FULLY_BOOKED_OFFERINGS = "SelectProgrammeOfferingsPage.SHOW_FULLY_BOOKED_OFFERINGS";


	// *************************************************************************
	// * Domain Attributes
	// *

	/**
	 * For each ProgrammeOfferingCVO, this map stores the number of places booked (0 if none)
	 */
	private TreeMap<ProgrammeOfferingCVO, Integer> bookingMap =
		new TreeMap<ProgrammeOfferingCVO, Integer>(AbstractCVO_PK_Comparator.getInstance());

	/**
	 * For each ProgrammeOfferingCVO, this map stores the booked work group
	 * 	(or null if none, of {@link ProgrammeBookingParameter#AUTO_WORK_GROUP})
	 */
	private TreeMap<ProgrammeOfferingCVO, Long> workGroupMap =
		new TreeMap<ProgrammeOfferingCVO, Long>(AbstractCVO_PK_Comparator.getInstance());

	/**
	 * For each ProgrammeOfferingCVO, this map stores the possibly edited prices
	 */
	private TreeMap<ProgrammeOfferingCVO, PriceVO> priceMap =
		new TreeMap<ProgrammeOfferingCVO, PriceVO>(AbstractCVO_PK_Comparator.getInstance());

	/**
	 * The event for which offerings are to be shown
	 */
	private Long eventPK;

	/**
	 * The initial participant type, which can be changed in a combo box
	 */
	private Long participantTypePK;

	/**
	 * The list of programme offerings fetched from the server
	 */
	private List<ProgrammeOfferingCVO> offeringCVOs = Collections.emptyList();

	/**
	 * The settings for the amount of data is to be fetched from the server
	 */
	private ProgrammeOfferingCVOSettings settings = new ProgrammeOfferingCVOSettings();



	// *************************************************************************
	// * Widgets
	// *

	private ParticipantTypeCombo participantTypeCombo;

	private DateTimeComposite referenceDateWidget;

	/**
	 * Button to set if fully booked offerings are included.
	 */
	private Button showFullyBookedOfferingsButton;

	private ProgrammeOfferingsTable programmeOfferingsTable;

	// TableColumns that hide or show dynamically dependent on the data
	private TableColumnLayout tableColumnLayout;
	private TableColumn workGroupTableColumn;
	private TableColumn mainPriceTableColumn;
	private TableColumn addPrice1TableColumn;
	private TableColumn addPrice2TableColumn;
	private TableColumn totalPriceTableColumn;

	private Table table;



	// *************************************************************************
	// * Constructor
	// *

	/**
	 * @param participantTypePK
	 */
	protected SelectProgrammeOfferingsPage(Long eventPK, Long participantTypePK) {
		super(NAME);

		this.eventPK = eventPK;
		this.participantTypePK = participantTypePK;

		setTitle(I18N.CreateProgrammeBookings_Text);
		setMessage(I18N.CreateProgrammeBookings_SelectProgrammOffers);

		settings.programmePointCVOSettings = new ProgrammePointCVOSettings();
		settings.programmePointCVOSettings.withNumberOfBookings = true;
		settings.programmePointCVOSettings.workGroupCVOSettings = new WorkGroupCVOSettings();
		settings.programmePointCVOSettings.workGroupCVOSettings.withNumberOfBookings = true;
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
			// Combo box for selection of participant type
			Label participantTypeLabel = new Label(controlComposite, SWT.NONE);
			participantTypeLabel.setText( Participant.PARTICIPANT_TYPE.getString() );
			participantTypeLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
			participantTypeCombo = new ParticipantTypeCombo(controlComposite, SWT.NONE);
			participantTypeCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			participantTypeCombo.setEventID(eventPK);
			participantTypeCombo.setParticipantTypePK(participantTypePK);

			// Date composite for entry of a reference date
			Label referenceDateLabel = new Label(controlComposite, SWT.NONE);
			referenceDateLabel.setText(I18N.ReferenceTime);
			referenceDateLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));

			referenceDateWidget = new DateTimeComposite(controlComposite, SWT.BORDER);
			// Registration date should default to today
			referenceDateWidget.setDate(new Date());
			referenceDateWidget.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));


			// Button to set if fully booked offerings are included
			Label showFullyBookedOfferingsLabel = new Label(controlComposite, SWT.NONE);
			// dummy Label
			showFullyBookedOfferingsLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));

			showFullyBookedOfferingsButton = new Button(controlComposite, SWT.CHECK);
			showFullyBookedOfferingsButton.setText(I18N.SelectProgrammeOfferingsPage_showFullyBookedOfferingsButton);
			showFullyBookedOfferingsButton.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));

			showFullyBookedOfferingsButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					saveDialogSettings();
				}
			});


			// Table to show ProgrammeOfferings and to allow the entry of a number of bookings
			Composite tableComposite = new Composite(controlComposite, SWT.BORDER);
			tableComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));

			tableColumnLayout = new TableColumnLayout();
			tableComposite.setLayout(tableColumnLayout);


			// create SWT Table
			table = new Table(tableComposite, SWT.SINGLE | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL);
			table.setHeaderVisible(true);
			table.setLinesVisible(true);

			// Book
			final TableColumn bookTableColumn = new TableColumn(table, SWT.NONE);
			tableColumnLayout.setColumnData(bookTableColumn, new ColumnWeightData(40));

			// Count
			final TableColumn countTableColumn = new TableColumn(table, SWT.RIGHT);
			tableColumnLayout.setColumnData(countTableColumn, new ColumnWeightData(60));
			countTableColumn.setText(UtilI18N.Count);

			// Description
			final TableColumn descTableColumn = new TableColumn(table, SWT.NONE);
			tableColumnLayout.setColumnData(descTableColumn, new ColumnWeightData(400));
			descTableColumn.setText(ParticipantLabel.ProgrammePoint.getString());

			// WorkGroup
			workGroupTableColumn = new TableColumn(table, SWT.RIGHT);
			tableColumnLayout.setColumnData(workGroupTableColumn, new ColumnWeightData(100));
			workGroupTableColumn.setText(ParticipantLabel.WorkGroup.getString());

			// Main Price
			mainPriceTableColumn = new TableColumn(table, SWT.RIGHT);
			// setting default ColumnData to avoid Exception, because dynamical setting is done too late
			tableColumnLayout.setColumnData(mainPriceTableColumn, new ColumnWeightData(100));

			// Additional Price 1 (hide/show dynamically)
			addPrice1TableColumn = new TableColumn(table, SWT.RIGHT);
			// setting default ColumnData to avoid Exception, because dynamical setting is done too late
			tableColumnLayout.setColumnData(addPrice1TableColumn, new ColumnWeightData(100));

			// Additional Price 2 (hide/show dynamically)
			addPrice2TableColumn = new TableColumn(table, SWT.RIGHT);
			// setting default ColumnData to avoid Exception, because dynamical setting is done too late
			tableColumnLayout.setColumnData(addPrice2TableColumn, new ColumnWeightData(100));

			// Total Price
			totalPriceTableColumn = new TableColumn(table, SWT.RIGHT);
			tableColumnLayout.setColumnData(totalPriceTableColumn, new ColumnWeightData(100));
			totalPriceTableColumn.setText(InvoiceLabel.TotalPrice.getString());

			// Number of Bookings
			final TableColumn numberOfBookingsTableColumn = new TableColumn(table, SWT.RIGHT);
			tableColumnLayout.setColumnData(numberOfBookingsTableColumn, new ColumnWeightData(100));
			numberOfBookingsTableColumn.setText(ParticipantLabel.NumberOfBookings_Capacity.getString());


			programmeOfferingsTable = new ProgrammeOfferingsTable(table, bookingMap, priceMap, workGroupMap);

			final ModifyListener internalModifyListener = new ModifyListener() {
				@Override
				public void modifyText(ModifyEvent e) {
					updateOfferingTable();
				}
			};

			programmeOfferingsTable.addModifyListener(internalModifyListener);

			referenceDateWidget.addModifyListener(internalModifyListener);
			participantTypeCombo.addModifyListener(internalModifyListener);
			showFullyBookedOfferingsButton.addSelectionListener(new ModifyListenerAdapter(internalModifyListener));

			programmeOfferingsTable.addEditListener(new ITableEditListener() {
				@Override
				public void tableCellChanged() {
					setPageComplete(programmeOfferingsTable.isAnythingBooked());
				}
			});


			/* restoreDialogSettings() before updateProgrammeOfferingCVOs()
			 * Otherwise the restored setting of showFullyBookedOfferingsButton
			 * would be ignored by updateProgrammeOfferingCVOs().
			 */
			restoreDialogSettings();
			updateOfferingTable();

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


	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);

		if (visible) {

			// Cannot set focus during this method, neither just using
			// SWTHelper.asyncExecDisplayThread is working, so thererfore
			// here is a somewhat complicated construct
			Job job = new Job("focus") {
				@Override
				protected IStatus run(IProgressMonitor monitor) {
					Runnable runnable = new Runnable() {
						@Override
						public void run() {
							try {
								table.setFocus();
							}
							catch (Exception e) {
								com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
							}
						}
					};
					SWTHelper.asyncExecDisplayThread(runnable);
					return Status.OK_STATUS;
				}
			};
			job.setSystem(true);
			job.schedule(50);
		}
	}


	// *************************************************************************
	// * Methods to obtain the relevant data for actually performing the booking
	// *

	public List<ProgrammeOfferingCVO> getAllProgrammeOfferingsCVO() {
		return offeringCVOs;
	}

	public List<ProgrammeOfferingCVO> getBookedProgrammeOfferingsCVO() {
		List<ProgrammeOfferingCVO> bookedProgrammeOfferings = new ArrayList<>();
		for (ProgrammeOfferingCVO programmeOfferingCVO : offeringCVOs) {
			if (bookingMap.get(programmeOfferingCVO).intValue() > 0) {
				bookedProgrammeOfferings.add(programmeOfferingCVO);
			}
		}
		return bookedProgrammeOfferings;
	}


	public PriceVO getPrice(ProgrammeOfferingCVO offering) {
		return priceMap.get(offering);
	}


	public int getCount(ProgrammeOfferingCVO offering) {
		return bookingMap.get(offering);
	}


	public Long getWorkGroupPK(ProgrammeOfferingCVO offeringCVO) {
		return workGroupMap.get(offeringCVO);
	}

	// *************************************************************************
	// * Private helper methods
	// *

	private void updateOfferingTable() {
		try {
			Long participantTypeID = participantTypeCombo.getParticipantTypePK();
			participantTypePK = participantTypeID;
			Date referenceTime = referenceDateWidget.getDate();

			List<ProgrammeOfferingCVO> oldOfferingsCVO = offeringCVOs;

			offeringCVOs = getProgrammeOfferingMgr().getProgrammeOfferingCVOsByEventPK(
				eventPK,
	    		participantTypePK,
	    		null,	// programmePointType
	    		referenceTime,
	            true,	// onlyEnabled
	            !showFullyBookedOfferingsButton.getSelection(),	// onlyNotFullyBooked
	            false,	// onlyUseInOnlineForm
	            settings
	    	);

			// make the oldOfferings list contain only offerings that are not anymore contained for the current
			// date/type
			oldOfferingsCVO.removeAll(offeringCVOs);

			// remove the old offerings from the maps
			for (ProgrammeOfferingCVO oldOfferingCVO : oldOfferingsCVO) {
				bookingMap.remove(oldOfferingCVO);
				priceMap.remove(oldOfferingCVO);
				workGroupMap.remove(oldOfferingCVO.getPK());
			}

			// add the new offerings to the maps
			for (ProgrammeOfferingCVO programmeOfferingCVO : offeringCVOs) {
				if (!bookingMap.containsKey(programmeOfferingCVO)) {
					bookingMap.put(programmeOfferingCVO, 0);
				}
				if (!priceMap.containsKey(programmeOfferingCVO)) {
					priceMap.put(programmeOfferingCVO, programmeOfferingCVO.getOfferingVO().getMainPriceVO().clone());
				}
			}

			List<ProgrammeOfferingVO> programmeOfferingVOs = ProgrammeOfferingCVO.getVOs(offeringCVOs);
			String lang = Locale.getDefault().getLanguage();

			// *************************************************************************************
			// * hide/show column for work group
			// *

			{
    			// determine if at least one Offering has a Programme Point with Work Groups
    			boolean atLeastOneWorkGroup = false;

    			WorkGroupModel workGroupModel = WorkGroupModel.getInstance();
    			for (ProgrammeOfferingVO poVO : programmeOfferingVOs) {
    				Long programmePointPK = poVO.getProgrammePointPK();
    				List<WorkGroupVO> workGroupVOs = workGroupModel.getWorkGroupVOsByProgrammePointPK(programmePointPK);
    				if (CollectionsHelper.notEmpty(workGroupVOs)) {
    					atLeastOneWorkGroup = true;
    					break;
    				}
    			}


    			// show or hide work group column
    			if (atLeastOneWorkGroup) {
    				// show column
    				tableColumnLayout.setColumnData(workGroupTableColumn, new ColumnWeightData(100));
    				workGroupTableColumn.setText(ParticipantLabel.WorkGroup.getString());
    			}
    			else {
    				// hide column
    				workGroupTableColumn.setWidth(0);
    				tableColumnLayout.setColumnData(workGroupTableColumn, new ColumnWeightData(0, 0, false));
    				workGroupTableColumn.setText("");
    			}


    			// propagate the information to the wizard, so that the overview page can also hide the column
    			if (getWizard() instanceof CreateProgrammeBookingsWizard) {
    				CreateProgrammeBookingsWizard wizard = (CreateProgrammeBookingsWizard) getWizard();
    				wizard.setAtLeastOneWorkGroup(atLeastOneWorkGroup);
    			}
    			else if (getWizard() instanceof CreateProgrammeBookingsWizardSeveralParticipantTypes) {
    				CreateProgrammeBookingsWizardSeveralParticipantTypes wizard = (CreateProgrammeBookingsWizardSeveralParticipantTypes) getWizard();
    				wizard.setAtLeastOneWorkGroup(atLeastOneWorkGroup);
    			}
			}

			// *
			// * hide/show column for work group
			// *************************************************************************************

			// *************************************************************************************
			// * hide/show column for additional price 1
			// *

			boolean showAdd1Price = false;
			{
    			// determine if at least one offering has an additional price 1
    			for (ProgrammeOfferingVO programmeOfferingVO : programmeOfferingVOs) {
    				if (programmeOfferingVO.isWithAdd1Price()) {
    					showAdd1Price = true;
    					break;
    				}
    			}


    			// show or hide column of additional price 1
    			if (showAdd1Price) {
    				/* Determine column name for column of additional price 1:
    				 * If all Offerings that have an additional price 1 share the same name, use it as
    				 * column name. Otherwise use the default name.
    				 */
        			String columnName = ProgrammeOfferingHelper.getAddPrice1Name(programmeOfferingVOs, lang);

        			// show column
    				tableColumnLayout.setColumnData(addPrice1TableColumn, new ColumnWeightData(100));
    				addPrice1TableColumn.setText(columnName);
    			}
    			else {
    				// hide column
    				addPrice1TableColumn.setWidth(0);
    				tableColumnLayout.setColumnData(addPrice1TableColumn, new ColumnWeightData(0, 0, false));
    				addPrice1TableColumn.setText("");
    			}

    			addPrice1TableColumn.setResizable(showAdd1Price);
			}

			// *
			// * hide/show column for additional price 1
			// *************************************************************************************

			// *************************************************************************************
			// * hide/show column for additional price 2
			// *

			boolean showAdd2Price = false;
			{
    			// determine if at least one offering has an additional price 2
    			for (ProgrammeOfferingVO programmeOfferingVO : programmeOfferingVOs) {
    				if (programmeOfferingVO.isWithAdd2Price()) {
    					showAdd2Price = true;
    					break;
    				}
    			}


    			// show or hide column of additional price 2
    			if (showAdd2Price) {
    				/* Determine column name for column of additional price 2:
    				 * If all Offerings that have an additional price 2 share the same name, use it as
    				 * column name. Otherwise use the default name.
    				 */
        			String columnName = ProgrammeOfferingHelper.getAddPrice2Name(programmeOfferingVOs, lang);

        			// show column
    				tableColumnLayout.setColumnData(addPrice2TableColumn, new ColumnWeightData(100));
    				addPrice2TableColumn.setText(columnName);
    			}
    			else {
    				// hide column
    				addPrice2TableColumn.setWidth(0);
    				tableColumnLayout.setColumnData(addPrice2TableColumn, new ColumnWeightData(0, 0, false));
    				addPrice2TableColumn.setText("");
    			}

    			addPrice2TableColumn.setResizable(showAdd2Price);
			}

			// *
			// * hide/show column for additional price 2
			// *************************************************************************************

			// *************************************************************************************
			// * hide/show column for main price
			// *

			{
				/* The column for main price is visible if either the column for
				 * additional price 1 or additional price 2 is visible.
				 * Otherwise the main price is equal to total price and should not be
				 * visible.
				 */
				boolean showMainPrice = showAdd1Price || showAdd2Price || isPriceEditable();

				String columnName = ParticipantLabel.Bookings_Price.getString();

    			// show or hide column of additional price 2
    			if (showMainPrice) {
        			// show column
    				tableColumnLayout.setColumnData(mainPriceTableColumn, new ColumnWeightData(100));
    				mainPriceTableColumn.setText(columnName);
    			}
    			else {
    				// hide column
    				mainPriceTableColumn.setWidth(0);
    				tableColumnLayout.setColumnData(mainPriceTableColumn, new ColumnWeightData(0, 0, false));
    				mainPriceTableColumn.setText("");
    			}

    			mainPriceTableColumn.setResizable(showMainPrice);
			}

			// *
			// * hide/show column for main price
			// *************************************************************************************


			// now set the input, not earlier, because the table needs the maps
			programmeOfferingsTable.setInput(offeringCVOs);

			// refresh table layout to hide/show dynamical columns
			SWTHelper.deferredLayout(300, programmeOfferingsTable.getViewer().getTable().getParent());
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	private boolean isPriceEditable() {
		boolean result = false;

		if (CollectionsHelper.notEmpty(offeringCVOs)) {
			for (ProgrammeOfferingCVO offeringCVO : offeringCVOs) {
				if (offeringCVO.getVO().isPriceEditable()) {
					result = true;
					break;
				}
			}
		}

		return result;
	}


	private void restoreDialogSettings() {
		IDialogSettings settings = Activator.getDefault().getDialogSettings();

		String showStr = settings.get(DIALOG_SETTING_SHOW_FULLY_BOOKED_OFFERINGS);
		boolean show = TypeHelper.toBoolean(showStr, false);
		showFullyBookedOfferingsButton.setSelection(show);
	}


	private void saveDialogSettings() {
		IDialogSettings settings = Activator.getDefault().getDialogSettings();
		boolean show = showFullyBookedOfferingsButton.getSelection();
		settings.put(DIALOG_SETTING_SHOW_FULLY_BOOKED_OFFERINGS, show);
	}

}
