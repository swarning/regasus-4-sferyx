package de.regasus.participant.editor.hotelbooking;

import static com.lambdalogic.util.CollectionsHelper.*;
import static com.lambdalogic.util.rcp.widget.SWTHelper.buildMenuItem;
import static java.util.Collections.emptyList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;

import com.lambdalogic.messeinfo.config.parameterset.HotelConfigParameterSet;
import com.lambdalogic.messeinfo.hotel.Hotel;
import com.lambdalogic.messeinfo.hotel.HotelLabel;
import com.lambdalogic.messeinfo.hotel.data.BookingCVO_BookingNo_Position_Comparator;
import com.lambdalogic.messeinfo.hotel.data.HotelBookingCVO;
import com.lambdalogic.messeinfo.hotel.data.HotelBookingVO;
import com.lambdalogic.messeinfo.hotel.data.HotelContingentVO;
import com.lambdalogic.messeinfo.hotel.data.HotelOfferingHelper;
import com.lambdalogic.messeinfo.hotel.data.HotelOfferingVO;
import com.lambdalogic.messeinfo.invoice.InvoiceLabel;
import com.lambdalogic.messeinfo.invoice.data.BookingCVO;
import com.lambdalogic.messeinfo.invoice.data.BookingsCurrencyAmountsEvaluator;
import com.lambdalogic.messeinfo.kernel.interfaces.SQLOperator;
import com.lambdalogic.messeinfo.kernel.sql.SQLParameter;
import com.lambdalogic.messeinfo.participant.Participant;
import com.lambdalogic.messeinfo.participant.ParticipantLabel;
import com.lambdalogic.messeinfo.participant.data.IParticipant;
import com.lambdalogic.messeinfo.participant.data.ParticipantSearchData;
import com.lambdalogic.messeinfo.participant.sql.ParticipantSearch;
import com.lambdalogic.time.I18NDate;
import com.lambdalogic.util.CollectionsHelper;
import com.lambdalogic.util.CurrencyAmount;
import com.lambdalogic.util.EqualsHelper;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.model.CacheModelOperation;
import com.lambdalogic.util.rcp.ClipboardHelper;
import com.lambdalogic.util.rcp.LazyComposite;
import com.lambdalogic.util.rcp.ModifySupport;
import com.lambdalogic.util.rcp.UtilI18N;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.I18N;
import de.regasus.core.ServerModel;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.dialog.StringFilterDialog;
import de.regasus.hotel.HotelBookingModel;
import de.regasus.hotel.HotelContingentModel;
import de.regasus.hotel.HotelModel;
import de.regasus.hotel.HotelOfferingModel;
import de.regasus.hotel.booking.dialog.CancelHotelBookingsWizard;
import de.regasus.hotel.booking.dialog.ChangeBenefitRecipientWizard;
import de.regasus.hotel.booking.dialog.CreateHotelBookingDialog;
import de.regasus.participant.ParticipantModel;
import de.regasus.participant.editor.ParticipantEditor;
import de.regasus.participant.search.OneParticipantSelectionDialogConfig;
import de.regasus.participant.search.ParticipantSelectionDialog;
import de.regasus.ui.Activator;


@SuppressWarnings("unused")
public class HotelBookingsTableComposite extends LazyComposite {

	// ConfigParameterSet
	private HotelConfigParameterSet hotelConfigParameterSet;

	// models
	private HotelBookingModel hbModel;
	private ParticipantModel participantModel;
	private ServerModel serverModel;


	private static boolean showCancelledBookings;

	private Participant participant;
	private List<HotelBookingCVO> hotelBookingCVOs;

	private String[] checkedHotelNames;

	protected ModifySupport modifySupport = new ModifySupport(this);

	// *****************************************************************************************************************
	// * Widgets
	// *

	private Table table;
	private TableViewer tableViewer;
	private HotelNamesViewerFilter viewerFilter;
	private HotelBookingsTable hotelBookingsTable;

	// TableColumns that hide or show dynamically dependent on the data
	private TableColumnLayout tableColumnLayout;
	private TableColumn lodgePriceTableColumn;
	private TableColumn bfPriceTableColumn;
	private TableColumn addPrice1TableColumn;
	private TableColumn addPrice2TableColumn;

	private Button createBookingButton;
	private Button cancelButton;
	private Button showCancelledButton;
	private Button editButton;
	private Button changeInvoiceRecipientButton;
	private Button changeBenefitRecipientButton;
	private Button filterActiveButton;
	private Button filterSettingsButton;

	private Text totalText;
	private Text openText;
	private Text paidText;

	private HotelCostCoverageGroup costCoverageGroup;

	// *
	// * Widgets
	// *****************************************************************************************************************


	// context menu
	private MenuItem createMenuItem;
	private MenuItem cancelMenuItem;
	private MenuItem editMenuItem;
	private MenuItem changeInvoiceRecipientMenuItem;
	private MenuItem changeBenefitRecipientMenuItem;


	public HotelBookingsTableComposite(Composite parent, int style, HotelConfigParameterSet hotelConfigParameterSet) {
		super(parent, style);

		this.hotelConfigParameterSet = Objects.requireNonNull(hotelConfigParameterSet);

		hbModel = HotelBookingModel.getInstance();
		participantModel = ParticipantModel.getInstance();
		serverModel = ServerModel.getInstance();

		addDisposeListener(disposeListener);
	}


	@Override
	protected void createPartControl() throws Exception {
		GridLayoutFactory.swtDefaults().numColumns(2).margins(0, 0).applyTo(this);

		Composite tableComposite = buildTableComposite(this);
		tableComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));

		Composite buttonComposite = buildButtonComposite(this);
		buttonComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		Composite amountsComposite = buildAmountsComposite(this);
		amountsComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));

		if ( hotelConfigParameterSet.getCostCoverage().isVisible() ) {
    		costCoverageGroup = new HotelCostCoverageGroup(this, SWT.NONE);
    		costCoverageGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
    		costCoverageGroup.addModifyListener(modifySupport);
		}

		participantModel.addListener(participantModelListener);

		syncWidgetsToEntity();
	}


	private Composite buildTableComposite(Composite parent) {
		Composite composite = new Composite(parent, SWT.BORDER);

		tableColumnLayout = new TableColumnLayout();
		composite.setLayout(tableColumnLayout);
		table = new Table(composite, SWT.MULTI | SWT.FULL_SELECTION);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);


		// special tooltip which uses a table to format its content
		HotelBookingTableToolTip myTooltipLabel = new HotelBookingTableToolTip(table);
		myTooltipLabel.setShift(new Point(-5, -5));
		myTooltipLabel.setHideOnMouseDown(true);
		myTooltipLabel.setPopupDelay(800);
		myTooltipLabel.activate();


		// DisplayIndex
		TableColumn displayIndexTableColumn = new TableColumn(table, SWT.RIGHT);
		tableColumnLayout.setColumnData(displayIndexTableColumn, new ColumnWeightData(20, 50));

		// Hotel
		TableColumn nameTableColumn = new TableColumn(table, SWT.NONE);
		tableColumnLayout.setColumnData(nameTableColumn, new ColumnWeightData(250));
		nameTableColumn.setText(HotelLabel.Hotel.getString());

		// Offering
		TableColumn offeringTableColumn = new TableColumn(table, SWT.NONE);
		tableColumnLayout.setColumnData(offeringTableColumn, new ColumnWeightData(250));
		offeringTableColumn.setText(HotelLabel.HotelOffering.getString());

		// Arrival
		TableColumn arrivalTableColumn = new TableColumn(table, SWT.NONE);
		tableColumnLayout.setColumnData(arrivalTableColumn, new ColumnWeightData(100));
		arrivalTableColumn.setText(HotelLabel.HotelBooking_Arrival.getString());

		// Departure
		TableColumn departureTableColumn = new TableColumn(table, SWT.NONE);
		tableColumnLayout.setColumnData(departureTableColumn, new ColumnWeightData(100));
		departureTableColumn.setText(HotelLabel.HotelBooking_Departure.getString());

		// Benefit Recipient
		TableColumn benefitRecipientTableColumn = new TableColumn(table, SWT.NONE);
		tableColumnLayout.setColumnData(benefitRecipientTableColumn, new ColumnWeightData(150));
		benefitRecipientTableColumn.setText(ParticipantLabel.Bookings_BenefitRecipient.getString());

		// Invoice Recipient
		TableColumn invoiceRecipientTableColumn = new TableColumn(table, SWT.NONE);
		tableColumnLayout.setColumnData(invoiceRecipientTableColumn, new ColumnWeightData(150));
		invoiceRecipientTableColumn.setText(ParticipantLabel.Bookings_InvoiceRecipient.getString());

		// Lodge Price
		lodgePriceTableColumn = new TableColumn(table, SWT.RIGHT);
		// setting default ColumnData to avoid Exception, because dynamical setting is done too late
		tableColumnLayout.setColumnData(lodgePriceTableColumn, new ColumnWeightData(100));

		// Breakfast Price (hide/show dynamically)
		bfPriceTableColumn = new TableColumn(table, SWT.RIGHT);
		// setting default ColumnData to avoid Exception, because dynamical setting is done too late
		tableColumnLayout.setColumnData(bfPriceTableColumn, new ColumnWeightData(100));

		// Additional Price 1 (hide/show dynamically)
		addPrice1TableColumn = new TableColumn(table, SWT.RIGHT);
		// setting default ColumnData to avoid Exception, because dynamical setting is done too late
		tableColumnLayout.setColumnData(addPrice1TableColumn, new ColumnWeightData(100));

		// Additional Price 2 (hide/show dynamically)
		addPrice2TableColumn = new TableColumn(table, SWT.RIGHT);
		// setting default ColumnData to avoid Exception, because dynamical setting is done too late
		tableColumnLayout.setColumnData(addPrice2TableColumn, new ColumnWeightData(100));

		// Total Price
		TableColumn totalPriceTableColumn = new TableColumn(table, SWT.RIGHT);
		tableColumnLayout.setColumnData(totalPriceTableColumn, new ColumnWeightData(100));
		totalPriceTableColumn.setText(InvoiceLabel.TotalPrice.getString());


		hotelBookingsTable = new HotelBookingsTable(table);

		viewerFilter = new HotelNamesViewerFilter();
		tableViewer = hotelBookingsTable.getViewer();
		tableViewer.setFilters(new ViewerFilter[] { viewerFilter });

		tableViewer.addDoubleClickListener(e -> openDetailsDialog());
		table.addSelectionListener(tableSelectionListener);
		table.addKeyListener(tableKeyListener);

		buildContextMenu();

		return composite;
	}


	private void buildContextMenu() {
		Menu menu = new Menu (getShell(), SWT.POP_UP);

		createMenuItem = buildMenuItem(menu, I18N.CreateBookings, e -> createBooking());
		cancelMenuItem = buildMenuItem(menu, I18N.CancelBooking, e -> cancelBooking());
		editMenuItem = buildMenuItem(menu, UtilI18N.Details, e -> openDetailsDialog());
		new MenuItem(menu, SWT.SEPARATOR);
		changeInvoiceRecipientMenuItem = buildMenuItem(menu, I18N.ChangeInvoiceRecipient, e -> changeInvoiceRecipient());
		changeBenefitRecipientMenuItem = buildMenuItem(menu, I18N.ChangeBenefitRecipient, e -> changeBenefitRecipient());

		hotelBookingsTable.getViewer().getTable().setMenu(menu);
	}


	private Composite buildButtonComposite(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(4, false));

		GridDataFactory gridDataFactory = GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER);


		// 1st row

		createBookingButton = buildCreateBookingButton(composite);
		gridDataFactory.applyTo(createBookingButton);

		changeInvoiceRecipientButton = buildChangeInvoiceRecipientButton(composite);
		gridDataFactory.applyTo(changeInvoiceRecipientButton);

		editButton = buildShowDetailsButton(composite);
		gridDataFactory.applyTo(editButton);

		showCancelledButton = buildShowCancelledButton(composite);
		gridDataFactory.applyTo(showCancelledButton);


		// 2nd Row

		cancelButton = buildCancelBookingButton(composite);
		gridDataFactory.applyTo(cancelButton);

		changeBenefitRecipientButton = buildChangeBenefitRecipientButton(composite);
		gridDataFactory.applyTo(changeBenefitRecipientButton);

		Button filterSettingsButton = buildFilterSettingsButton(composite);
		gridDataFactory.applyTo(filterSettingsButton);

		filterActiveButton = buildFilterActiveButton(composite);
		gridDataFactory.applyTo(filterActiveButton);



		setShowCancelledBookings(showCancelledBookings);

		return composite;
	}


	private Button buildCreateBookingButton(Composite parent) {
		Button button = new Button(parent, SWT.PUSH);
		button.setText(I18N.CreateBookings);
		button.setEnabled(false);
		button.addListener(SWT.Selection, e -> createBooking());
		return button;
	}


	private Button buildCancelBookingButton(Composite parent) {
		Button button = new Button(parent, SWT.PUSH);
		button.setText(I18N.CancelBooking);
		button.setEnabled(false);
		button.addListener(SWT.Selection, e -> cancelBooking());
		return button;
	}


	private Button buildChangeInvoiceRecipientButton(Composite parent) {
		Button button = new Button(parent, SWT.PUSH);
		button.setText(I18N.ChangeInvoiceRecipient);
		button.setEnabled(false);
		button.addListener(SWT.Selection, e -> changeInvoiceRecipient());
		return button;
	}


	private Button buildChangeBenefitRecipientButton(Composite parent) {
		Button button = new Button(parent, SWT.PUSH);
		button.setText(I18N.ChangeBenefitRecipient);
		button.setEnabled(false);
		button.addListener(SWT.Selection, e -> changeBenefitRecipient());
		return button;
	}


	private Button buildFilterSettingsButton(Composite parent) {
    	Button button = new Button(parent, SWT.PUSH);
    	button.setText(I18N.OpenFilterDialog);
		button.addListener(SWT.Selection, e -> openFilterDialog());
    	return button;
	}


	private Button buildFilterActiveButton(Composite parent) {
    	Button button = new Button(parent, SWT.CHECK);
    	button.setText(I18N.FilterActive);
    	button.addListener(SWT.Selection, e -> switchFilterActive());
    	return button;
	}


	private Button buildShowDetailsButton(Composite parent) {
		Button button = new Button(parent, SWT.PUSH);
    	button.setText(UtilI18N.Details);
    	button.setEnabled(false);
    	button.addListener(SWT.Selection, e -> openDetailsDialog());
    	return button;
	}


	private Button buildShowCancelledButton(Composite parent) {
    	Button button = new Button(parent, SWT.CHECK);
    	button.setSelection(showCancelledBookings);
    	button.setText(I18N.ShowCancelled);
    	button.addListener(SWT.Selection, e -> switchShowCancelled());
    	return button;
	}


	private Composite buildAmountsComposite(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(2, false));

		Label totalLabel = new Label(composite, SWT.NONE);
		totalLabel.setText(InvoiceLabel.Total.getString());
		totalText = new Text(composite, SWT.BORDER | SWT.READ_ONLY | SWT.RIGHT);

		GridData gd = new GridData(SWT.FILL, SWT.CENTER, false, false);
		int recommendedWidth = gd.widthHint = SWTHelper.computeTextWidgetWidthForCharCount(totalText, 14);
		totalText.setLayoutData(gd);

		Label openLabel = new Label(composite, SWT.NONE);
		openLabel.setText(InvoiceLabel.Open.getString());
		openText = new Text(composite, SWT.BORDER | SWT.READ_ONLY | SWT.RIGHT);
		gd = new GridData(SWT.FILL, SWT.CENTER, false, false);
		gd.widthHint = recommendedWidth;
		openText.setLayoutData(gd);

		Label paidLabel = new Label(composite, SWT.NONE);
		paidLabel.setText(InvoiceLabel.Paid.getString());
		paidText = new Text(composite, SWT.BORDER | SWT.READ_ONLY | SWT.RIGHT);
		gd = new GridData(SWT.FILL, SWT.CENTER, false, false);
		gd.widthHint = recommendedWidth;
		paidText.setLayoutData(gd);

		return composite;
	}


	private SelectionListener tableSelectionListener = new SelectionAdapter() {
		@Override
		public void widgetSelected(SelectionEvent e) {
			updateButtonStates();
		}
	};


	private KeyListener tableKeyListener = new KeyAdapter() {
		@Override
		public void keyPressed(KeyEvent e) {
			if (e.keyCode == SWT.DEL && e.stateMask == 0) {
				cancelBooking();
			}
			// run CopyAction when user presses ctrl+c or ⌘+c
			else if (e.keyCode == 'c' && e.stateMask == SWT.MOD1) {
				ClipboardHelper.copyToClipboard( getSelectionInfo() );
			}
			else if (e.keyCode == 'c' && e.stateMask == (SWT.MOD1 | SWT.SHIFT)) {
				ClipboardHelper.copyToClipboard( getSelectedIdsAsText() );
			}
		}
	};


	private String getSelectionInfo() {
		List<HotelBookingCVO> hbList = getSelection();
		StringBuilder text = new StringBuilder(hbList.size() * 128);

		try {
    		for (HotelBookingCVO hbCVO : hbList) {
    			if (text.length() > 0) {
    				text.append("\n");
    			}

    			Long hbPK = hbCVO.getPK();
    			Long hoPK = hbCVO.getVO().getOfferingPK();
    			HotelOfferingVO hoVO = HotelOfferingModel.getInstance().getHotelOfferingVO(hoPK);
    			Long hcPK = hoVO.getHotelContingentPK();
    			HotelContingentVO hcVO = HotelContingentModel.getInstance().getHotelContingentVO(hcPK);
    			Long hPK = hcVO.getHotelPK();
    			Hotel hotel = HotelModel.getInstance().getHotel(hPK);

    	        text.append("HotelBookingCVO [");
    	        text.append("id=").append(hbPK);
    	        text.append(", offeringID=").append(hoPK);
    	        text.append(", hotelContingentID=").append(hcPK);
    	        text.append(", hotelID=").append(hPK);
    	        text.append(", Hotel=\"").append( hotel.getName1() ).append("\"");
    	        text.append(", arrival=").append( I18NDate.from(hbCVO.getArrival()).getString() );
    	        text.append(", departure=").append( I18NDate.from(hbCVO.getDeparture()).getString() );
    	        text.append("]");
    		}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}

		return text.toString();
	}


	private String getSelectedIdsAsText() {
		List<HotelBookingCVO> hbList = getSelection();
		StringBuilder text = new StringBuilder(hbList.size() * 128);
		for (HotelBookingCVO hbCVO : hbList) {
			if (text.length() > 0) {
				text.append(", ");
			}

			text.append( hbCVO.getPK() );
		}

		return text.toString();
	}


	private List<HotelBookingCVO> getSelection() {
		List<HotelBookingCVO> selectedEntities = emptyList();
		IStructuredSelection selection = (IStructuredSelection) tableViewer.getSelection();
		if (selection != null) {
			selectedEntities = selection.toList();
		}
		return selectedEntities;
	}


	protected void cancelBooking() {
		boolean editorSaveCkeckOK = ParticipantEditor.saveEditor(participant.getID());
		if (editorSaveCkeckOK) {
			Wizard wizard = new CancelHotelBookingsWizard( getSelection() );
			WizardDialog dialog = new WizardDialog(this.getShell(), wizard);
			dialog.create();
			dialog.getShell().setSize(700, 600);
			dialog.open();

			updateButtonStates();
		}
	}


	protected void createBooking() {
		 try {
			boolean editorSaveCkeckOK = ParticipantEditor.saveEditor( participant.getID() );
			if (editorSaveCkeckOK) {
				ParticipantSearchData psd = new ParticipantSearchData(participant);
				List<IParticipant> participantList = Collections.singletonList(psd);


				CreateHotelBookingDialog.create(getShell(), participantList).open();
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	protected void changeInvoiceRecipient() {
		try {
			if (ParticipantEditor.saveEditor(participant.getID())) {
				List<SQLParameter> sqlParameterListForSameGroup = getSqlParameterForSameGroup();

				ParticipantSelectionDialog participantSelectionDialog = new ParticipantSelectionDialog(
					getShell(),
					OneParticipantSelectionDialogConfig.INSTANCE,
					participant.getEventId()
				);
				participantSelectionDialog.setTitle(I18N.ChangeInvoiceRecipient);
				participantSelectionDialog.setInitialSQLParameters(sqlParameterListForSameGroup);
				participantSelectionDialog.create();
				if (CollectionsHelper.notEmpty(sqlParameterListForSameGroup)) {
					participantSelectionDialog.doSearch();
				}
				participantSelectionDialog.open();

				if (!participantSelectionDialog.isCancelled()) {
					Long newInvoiceRecipientPK = participantSelectionDialog.getSelectedPKs().get(0);
					HotelBookingModel.getInstance().changeInvoiceRecipientsOfHotelBookings(
						getSelection(),
						newInvoiceRecipientPK
					);
				}

				updateButtonStates();
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	protected void changeBenefitRecipient() {
		if (ParticipantEditor.saveEditor(participant.getID())) {
			ArrayList<SQLParameter> sqlParameterListForSameGroup = getSqlParameterForSameGroup();

			Wizard wizard = new ChangeBenefitRecipientWizard(
				participant,
				sqlParameterListForSameGroup,
				getSelection()
			);
			WizardDialog dialog = new WizardDialog(this.getShell(), wizard);
			dialog.create();
			dialog.getShell().setSize(900, 600);
			dialog.open();

			updateButtonStates();
		}
	}


	private ArrayList<SQLParameter> getSqlParameterForSameGroup() {
		ArrayList<SQLParameter> sqlParameterList = new ArrayList<>();
		try {
			// Define search categories to find participants "in the same group"
			Integer groupManagerNo = null;

			if (participant.isGroupManager()) {
				groupManagerNo = participant.getNumber();
			}
			else if (participant.isInGroup()) {
				Long groupManagerPK = participant.getGroupManagerPK();
				Participant groupManager = ParticipantModel.getInstance().getParticipant(groupManagerPK);
				groupManagerNo = groupManager.getNumber();
			}
			if (groupManagerNo != null) {
				sqlParameterList.add(ParticipantSearch.GROUP_MANAGER_NO.getSQLParameter(
					groupManagerNo,
					SQLOperator.EQUAL));
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
		return sqlParameterList;
	}


	protected void switchShowCancelled() {
		showCancelledBookings = showCancelledButton.getSelection();
		setShowCancelledBookings(showCancelledBookings);

	}


	protected void openFilterDialog() {
		try {
    		// load hotel bookings
    		Long participantPK = participant.getID();
    		List<HotelBookingCVO> hotelBookingCVOs = hbModel.getHotelBookingCVOsByRecipient(participantPK);

    		Set<String> hotelSet = new TreeSet<>();
    		for (HotelBookingCVO hotelBookingCVO : hotelBookingCVOs) {
    			String hotelName = hotelBookingCVO.getHotelName();
    			hotelSet.add(hotelName);
    		}
    		List<String> hotelList = new ArrayList<>();
    		hotelList.addAll(hotelSet);

    		StringFilterDialog hotelFilterDialog = new StringFilterDialog(getShell(), hotelList);
    		if (checkedHotelNames != null) {
    			hotelFilterDialog.setCheckedStrings(checkedHotelNames);
    		}
    		int code = hotelFilterDialog.open();

    		if (code == IDialogConstants.CLIENT_ID + 1) {
    			checkedHotelNames = hotelFilterDialog.getCheckedStrings();
    			filterActiveButton.setSelection(true);
    			setVisibleHotels(checkedHotelNames);
    		}
    		else if (code == IDialogConstants.CLIENT_ID + 2) {
    			filterActiveButton.setSelection(false);
    			setFilterOff();
    		}
		}
		catch (Exception e1) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e1);
		}
	}


	protected void switchFilterActive() {
		boolean on = filterActiveButton.getSelection();
		if (on) {
			if (checkedHotelNames != null) {
				setVisibleHotels(checkedHotelNames);
			}
		}
		else {
			setFilterOff();
		}
	}


	private void syncWidgetsToEntity() {
		if (participant != null && hotelBookingsTable != null) {

			SWTHelper.syncExecDisplayThread(new Runnable() {
				@Override
				public void run() {
					try {
						hotelBookingsTable.setParticipant(participant);

						Long participantPK = participant.getID();
						hotelBookingCVOs = hbModel.getHotelBookingCVOsByRecipient(participantPK);
						hotelBookingCVOs = createArrayList(hotelBookingCVOs);
						sortHotelBookings(hotelBookingCVOs);
						hotelBookingsTable.setInput(hotelBookingCVOs);

						BookingsCurrencyAmountsEvaluator bcae = new BookingsCurrencyAmountsEvaluator(
							hotelBookingCVOs,
							participant.getID()
						);

						// set total amount
						CurrencyAmount totalAmount = bcae.getTotalAmount();
						if (totalAmount != null) {
							totalText.setText(totalAmount.format());
						}
						else {
							totalText.setText("");
						}

						// set paid amount
						CurrencyAmount paidAmount = bcae.getTotalPaidAmount();
						if (paidAmount != null) {
							paidText.setText(paidAmount.format());
						}
						else {
							paidText.setText("");
						}

						// set open amount
						CurrencyAmount openAmount = bcae.getTotalOpenAmount();
						if (openAmount != null) {
							openText.setText(openAmount.format());
						}
						else {
							openText.setText("");
						}

						createBookingButton.setEnabled(participant.getID() != null);


						// hide/show columns and set column headers for breakfast and additional prices

						// build List of HotelOfferingVOs
						Set<HotelOfferingVO> hotelOfferingVOs = createHashSet(hotelBookingCVOs.size());
						for (HotelBookingCVO hbCVO : hotelBookingCVOs) {
							hotelOfferingVOs.add(hbCVO.getHotelOfferingCVO().getVO());
						}

						String lang = Locale.getDefault().getLanguage();


						// *************************************************************************************
						// * hide/show column for breakfast price
						// *

						boolean showBfPrice = false;
						{
							/* Determine if column for breakfast price is visible.
							 *
							 * This depends on the bookings, not on the offerings, because the
							 * latter might have changed since the booking was created.
							 *
							 * Config settings don't matter. If a booking has a breakfast price it
							 * has to be shown.
							 */
							for (HotelBookingCVO hotelBookingCVO : hotelBookingCVOs) {
								if (hotelBookingCVO.getVO().getBfAmountGross().signum() != 0) {
									showBfPrice = true;
									break;
								}
							}


			    			// show or hide column of additional price 1
			    			if (showBfPrice) {
			        			// show column
			    				tableColumnLayout.setColumnData(bfPriceTableColumn, new ColumnWeightData(100));
			    				bfPriceTableColumn.setText( HotelLabel.Breakfast.getString() );
			    			}
			    			else {
			    				// hide column
			    				bfPriceTableColumn.setWidth(0);
			    				tableColumnLayout.setColumnData(bfPriceTableColumn, new ColumnWeightData(0, 0, false));
			    				bfPriceTableColumn.setText("");
			    			}

			    			bfPriceTableColumn.setResizable(showBfPrice);
						}

						// *
						// * hide/show column for breakfast price
						// *************************************************************************************

						// *************************************************************************************
						// * hide/show column for additional price 1
						// *

						boolean showAdd1Price = false;
						{
							/* Determine if column for additional price 1 is visible.
							 *
							 * This depends on the bookings, not on the offerings, because the
							 * latter might have changed since the booking was created.
							 *
							 * Config settings don't matter. If a booking has a breakfast price it
							 * has to be shown.
							 */
							for (HotelBookingCVO hotelBookingCVO : hotelBookingCVOs) {
								if (hotelBookingCVO.getVO().getAdd1AmountGross().signum() != 0) {
									showAdd1Price = true;
									break;
								}
							}

							/* Determine column name for column of additional price 1:
							 * If all Offerings that have an additional price 1 share the same name, use it as
							 * column name. Otherwise use the default name.
							 */
							String columnName = HotelOfferingHelper.getAddPrice1Name(hotelOfferingVOs, lang);

			    			// show or hide column of additional price 1
			    			if (showAdd1Price) {
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

						boolean showAdd2Price = false;
						// *************************************************************************************
						// * hide/show column for additional price 2
						// *

						{
							/* Determine if column for additional price 1 is visible.
							 *
							 * This depends on the bookings, not on the offerings, because the
							 * latter might have changed since the booking was created.
							 *
							 * Config settings don't matter. If a booking has a breakfast price it
							 * has to be shown.
							 */
							for (HotelBookingCVO hotelBookingCVO : hotelBookingCVOs) {
								if (hotelBookingCVO.getVO().getAdd2AmountGross().signum() != 0) {
									showAdd2Price = true;
									break;
								}
							}

							/* Determine column name for column of additional price 2:
							 * If all Offerings that have an additional price 2 share the same name, use it as
							 * column name. Otherwise use the default name.
							 */
							String columnName = HotelOfferingHelper.getAddPrice2Name(hotelOfferingVOs, lang);

			    			// show or hide column of additional price 2
			    			if (showAdd2Price) {
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
						// * hide/show column for lodge price
						// *

						{
							/* The column for lodge price is visible if either the column for
							 * breakfast price, additional price 1 or additional price 2 is visible.
							 * Otherwise the lodge price is equal to total price and should not be
							 * visible.
							 */
							boolean showLodgePrice = showBfPrice || showAdd1Price || showAdd2Price;

							String columnName = HotelLabel.Lodge.getString();

			    			// show or hide column of lodge price
			    			if (showLodgePrice) {
			        			// show column
			    				tableColumnLayout.setColumnData(lodgePriceTableColumn, new ColumnWeightData(100));
			    				lodgePriceTableColumn.setText(columnName);
			    			}
			    			else {
			    				// hide column
			    				lodgePriceTableColumn.setWidth(0);
			    				tableColumnLayout.setColumnData(lodgePriceTableColumn, new ColumnWeightData(0, 0, false));
			    				lodgePriceTableColumn.setText("");
			    			}

			    			lodgePriceTableColumn.setResizable(showLodgePrice);
						}

						// *
						// * hide/show column for lodge price
						// *************************************************************************************

						// refresh table layout to hide/show dynamic columns
						SWTHelper.deferredLayout(300, table.getParent());


						if (costCoverageGroup != null) {
							costCoverageGroup.setEntity(participant);
						}


						updateButtonStates();
					}
					catch (Exception e) {
						RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
					}
				}
			});

		}
	}


	public void syncEntityToWidgets() {
		if (costCoverageGroup != null) {
			costCoverageGroup.syncEntityToWidgets();
		}
	}


	public void openDetailsDialog() {
		List<HotelBookingCVO> bookingCVOs = getSelection();

		TitleAreaDialog dialog = null;

		// If one booking is selected, open the dialog to show and change details of one
		if (bookingCVOs.size() == 1) {
			HotelBookingCVO bookingCVO = bookingCVOs.get(0);
			dialog = new HotelBookingDetailsDialog(getShell(), bookingCVO);
		}
		// If several bookings are selected, open the dialog to show and change details of several
		else if (bookingCVOs.size() > 1) {
			dialog = new HotelBookingMultipleDetailsDialog(getShell() , bookingCVOs);
		}

		dialog.create();
		dialog.getShell().setSize(900, 600);
		dialog.open();
	}


	public void setParticipant(Participant participant) {
		Long oldPK = this.participant == null ? null : this.participant.getID();
		Long newPK =      participant == null ? null :      participant.getID();
		boolean pkChanged = !EqualsHelper.isEqual(oldPK, newPK);


		if (pkChanged && oldPK != null) {
			hbModel.removeForeignKeyListener(hotelBookingModelListener, oldPK);
		}


		this.participant = participant;


		if (pkChanged && newPK != null) {
			hbModel.addForeignKeyListener(hotelBookingModelListener, newPK);
		}


		syncWidgetsToEntity();
	}


	private DisposeListener disposeListener = new DisposeListener() {
		@Override
		public void widgetDisposed(DisposeEvent event) {
			try {
				if (hbModel != null &&
					participant != null &&
					participant.getID() != null
				) {
					Long participantPK = participant.getID();
					hbModel.removeForeignKeyListener(hotelBookingModelListener, participantPK);
				}
			}
			catch (Exception e) {
				// ignore
			}

			try {
				if (participantModel != null) {
					participantModel.removeListener(participantModelListener);
				}
			}
			catch (Exception e) {
				// ignore
			}
		}
	};


	public void setVisibleHotels(String[] checkedHotelNames) {
		viewerFilter.setCheckedHotelNames(checkedHotelNames);
		viewerFilter.setActive(true);
		hotelBookingsTable.getViewer().refresh();
	}


	public void setFilterOff() {
		viewerFilter.setActive(false);
		hotelBookingsTable.getViewer().refresh();
	}


	public void setShowCancelledBookings(boolean showCancelledBookings) {
		viewerFilter.setShowCancelledBookings(showCancelledBookings);
		hotelBookingsTable.getViewer().refresh();
	}


	private void updateButtonStates() {
		boolean bookingsAreSelected = !getSelection().isEmpty();
		boolean aCancelledBookingIsSelected = false;
		if (bookingsAreSelected) {
			for (HotelBookingCVO hotelBookingCVO : getSelection()) {
				if (hotelBookingCVO.isCanceled()) {
					aCancelledBookingIsSelected = true;
					break;
				}
			}
		}


		editButton.setEnabled(bookingsAreSelected);
		editMenuItem.setEnabled(bookingsAreSelected);

		cancelButton.setEnabled(bookingsAreSelected);
		cancelMenuItem.setEnabled(bookingsAreSelected);

		changeBenefitRecipientButton.setEnabled(bookingsAreSelected && !aCancelledBookingIsSelected);
		changeBenefitRecipientMenuItem.setEnabled(bookingsAreSelected && !aCancelledBookingIsSelected);

		changeInvoiceRecipientButton.setEnabled(bookingsAreSelected && !aCancelledBookingIsSelected);
		changeInvoiceRecipientMenuItem.setEnabled(bookingsAreSelected && !aCancelledBookingIsSelected);

		filterActiveButton.setEnabled(bookingsAreSelected);
	}


	private CacheModelListener<Long> hotelBookingModelListener = new CacheModelListener<Long>() {
		@Override
		public void dataChange(CacheModelEvent<Long> event) throws Exception {
			if (serverModel.isLoggedIn()) {
	    		if (participant != null && participant.getID() != null && hotelBookingsTable != null) {
	    			syncWidgetsToEntity();
	    		}
			}
		}
	};


	private CacheModelListener<Long> participantModelListener = new CacheModelListener<Long>() {
		@Override
		public void dataChange(CacheModelEvent<Long> event) throws Exception {
			if (serverModel.isLoggedIn()) {
	    		if (hotelBookingCVOs != null && hotelBookingsTable != null) {
					List<Long> participantIDs = event.getKeyList();

					if (participantIDs.contains(participant.getID()) &&
						event.getOperation() == CacheModelOperation.DELETE
					) {
						// do nothing, because the editor will be closed
					}
					else {
	        			// check if the updates participant is a recipient of any booking
	        			boolean relevant = false;

	        			for (Long participantID : participantIDs) {
	        				Long participantPK = participantID;
	        				for (HotelBookingCVO hbCVO : hotelBookingCVOs) {
	        					HotelBookingVO bookingVO = hbCVO.getBookingVO();
	        					if (bookingVO.getInvoiceRecipientPK().equals(participantPK)
	        						||
	        						bookingVO.getBenefitRecipientPKs().contains(participantPK)
	        					) {
	        						relevant = true;
	        						break;
	        					}
	        				}
	        				if (relevant) {
	        					break;
	        				}
	    				}

	        			if (relevant) {
							/*
							 * If any of the participant of the bookings has been deleted, its bookings
							 * have been deleted, too. So we have do refresh the list of bookings.
							 * Otherwise just refresh the Viewer, to refresh the names of the recipients.
							 */
							if (event.getOperation() == CacheModelOperation.DELETE) {
								syncWidgetsToEntity();
							}
							else {
								SWTHelper.asyncExecDisplayThread(new Runnable() {
									@Override
									public void run() {
										hotelBookingsTable.getViewer().refresh();
									}
								});
							}
	        			}
					}
	    		}
			}
		}
	};


	private void sortHotelBookings(List<HotelBookingCVO> hotelBookingCVOs) {
		if (hotelBookingCVOs != null) {
			Collections.sort(hotelBookingCVOs, BookingCVO_BookingNo_Position_Comparator.getInstance());

			BookingCVO.initDisplayIndices(hotelBookingCVOs);
		}
	}


	// **************************************************************************
	// * Modifying
	// *

	public void addModifyListener(ModifyListener modifyListener) {
		modifySupport.addListener(modifyListener);
	}


	public void removeModifyListener(ModifyListener modifyListener) {
		modifySupport.removeListener(modifyListener);
	}

	// *
	// * Modifying
	// **************************************************************************

}

