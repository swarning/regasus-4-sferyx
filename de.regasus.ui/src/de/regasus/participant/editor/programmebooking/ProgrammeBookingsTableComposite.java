package de.regasus.participant.editor.programmebooking;

import static com.lambdalogic.util.CollectionsHelper.createHashSet;
import static com.lambdalogic.util.rcp.widget.SWTHelper.buildMenuItem;
import static de.regasus.LookupService.getProgrammePointMgr;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
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
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
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

import com.lambdalogic.messeinfo.hotel.data.BookingCVO_BookingNo_Position_Comparator;
import com.lambdalogic.messeinfo.invoice.InvoiceLabel;
import com.lambdalogic.messeinfo.invoice.data.BookingCVO;
import com.lambdalogic.messeinfo.invoice.data.BookingsCurrencyAmountsEvaluator;
import com.lambdalogic.messeinfo.kernel.interfaces.SQLOperator;
import com.lambdalogic.messeinfo.kernel.sql.SQLParameter;
import com.lambdalogic.messeinfo.participant.Participant;
import com.lambdalogic.messeinfo.participant.ParticipantLabel;
import com.lambdalogic.messeinfo.participant.data.ParticipantSearchData;
import com.lambdalogic.messeinfo.participant.data.ProgrammeBookingCVO;
import com.lambdalogic.messeinfo.participant.data.ProgrammeBookingVO;
import com.lambdalogic.messeinfo.participant.data.ProgrammeOfferingCVO;
import com.lambdalogic.messeinfo.participant.data.ProgrammeOfferingHelper;
import com.lambdalogic.messeinfo.participant.data.ProgrammeOfferingVO;
import com.lambdalogic.messeinfo.participant.data.ProgrammePointCVO;
import com.lambdalogic.messeinfo.participant.data.ProgrammePointVO;
import com.lambdalogic.messeinfo.participant.sql.ParticipantSearch;
import com.lambdalogic.util.CollectionsHelper;
import com.lambdalogic.util.CurrencyAmount;
import com.lambdalogic.util.EqualsHelper;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.model.CacheModelOperation;
import com.lambdalogic.util.rcp.BrowserHelper;
import com.lambdalogic.util.rcp.ClipboardHelper;
import com.lambdalogic.util.rcp.ISelectionDialogConfig;
import com.lambdalogic.util.rcp.LazyComposite;
import com.lambdalogic.util.rcp.SelectionHelper;
import com.lambdalogic.util.rcp.UtilI18N;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.I18N;
import de.regasus.core.ServerModel;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.participant.ParticipantModel;
import de.regasus.participant.editor.ParticipantEditor;
import de.regasus.participant.search.FixedNumberParticipantSelectionDialogConfig;
import de.regasus.participant.search.OneParticipantSelectionDialogConfig;
import de.regasus.participant.search.ParticipantSelectionDialog;
import de.regasus.programme.ProgrammeBookingModel;
import de.regasus.programme.ProgrammeOfferingModel;
import de.regasus.programme.ProgrammePointModel;
import de.regasus.programme.booking.dialog.CancelProgrammeBookingsWizard;
import de.regasus.programme.booking.dialog.CreateProgrammeBookingsWizard;
import de.regasus.ui.Activator;

public class ProgrammeBookingsTableComposite extends LazyComposite {

	private static boolean showCancelledBookings;

	private Participant participant;
	private List<ProgrammeBookingCVO> programmeBookingCVOs;

	private ProgrammePointCVO[] checkedProgrammePoints;

	// models
	private ProgrammeBookingModel pbModel;
	private ParticipantModel participantModel;
	private ServerModel serverModel;

	// Widgets
	private Text totalText;

	private Text paidText;

	private Text openText;


	private Button createBookingButton;
	private Button cancelButton;
	private Button showCancelledButton;
	private Button editButton;
	private Button changeInvoiceRecipientButton;
	private Button changeBenefitRecipientButton;
	private Button filterActiveButton;

	private MenuItem createMenuItem;
	private MenuItem cancelMenuItem;
	private MenuItem editMenuItem;
	private MenuItem changeInvoiceRecipientMenuItem;
	private MenuItem changeBenefitRecipientMenuItem;
	private MenuItem openLiveStreamMenuItem;
	private MenuItem openVideoStreamMenuItem;

	private Table table;
	private TableViewer tableViewer;
	private ProgrammeBookingsCVOViewerFilter viewerFilter;
	private ProgrammeBookingsTable programmeBookingsTable;

	// TableColumns that hide or show dynamically dependent on the data
	private TableColumnLayout tableColumnLayout;
	private TableColumn mainPriceTableColumn;
	private TableColumn addPrice1TableColumn;
	private TableColumn addPrice2TableColumn;


	public ProgrammeBookingsTableComposite(Composite parent, int style) {
		super(parent, style);

		pbModel = ProgrammeBookingModel.getInstance();
		participantModel = ParticipantModel.getInstance();
		serverModel = ServerModel.getInstance();

		addDisposeListener(disposeListener);
	}


	@Override
	protected void createPartControl() {
		GridLayoutFactory.swtDefaults().numColumns(2).margins(0, 0).applyTo(this);

		Composite tableComposite = buildTableComposite(this);
		tableComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));

		Composite buttonComposite = buildButtonComposite(this);
		buttonComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		Composite amountsComposite = buildAmountsComposite(this);
		amountsComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));


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

		// DisplayIndex
		TableColumn displayIndexTableColumn = new TableColumn(table, SWT.RIGHT);
		tableColumnLayout.setColumnData(displayIndexTableColumn, new ColumnWeightData(20, 50));

		// Description
		TableColumn descTableColumn = new TableColumn(table, SWT.NONE);
		tableColumnLayout.setColumnData(descTableColumn, new ColumnWeightData(400));
		descTableColumn.setText(UtilI18N.Description);

		// Workgroup
		TableColumn workgroupTableColumn = new TableColumn(table, SWT.NONE);
		tableColumnLayout.setColumnData(workgroupTableColumn, new ColumnWeightData(100));
		workgroupTableColumn.setText(ParticipantLabel.WorkGroup.getString());

		// Benefit Recipient
		TableColumn benefitRecipientTableColumn = new TableColumn(table, SWT.NONE);
		tableColumnLayout.setColumnData(benefitRecipientTableColumn, new ColumnWeightData(150));
		benefitRecipientTableColumn.setText(ParticipantLabel.Bookings_BenefitRecipient.getString());

		// Invoice Recipient
		TableColumn invoiceRecipientTableColumn = new TableColumn(table, SWT.NONE);
		tableColumnLayout.setColumnData(invoiceRecipientTableColumn, new ColumnWeightData(150));
		invoiceRecipientTableColumn.setText(ParticipantLabel.Bookings_InvoiceRecipient.getString());

		// Main Price
		mainPriceTableColumn = new TableColumn(table, SWT.RIGHT);
		// setting default ColumnData to avoid Exception, because dynamical setting is done too late
		tableColumnLayout.setColumnData(mainPriceTableColumn, new ColumnWeightData(100));

		// Additional Price 1 (hide/show dynamically)
		addPrice1TableColumn = new TableColumn(table, SWT.RIGHT);
		tableColumnLayout.setColumnData(addPrice1TableColumn, new ColumnWeightData(100));

		// Additional Price 2 (hide/show dynamically)
		addPrice2TableColumn = new TableColumn(table, SWT.RIGHT);
		// setting default ColumnData to avoid Exception, because dynamical setting is done too late
		tableColumnLayout.setColumnData(addPrice2TableColumn, new ColumnWeightData(100));

		// Total Price
		TableColumn totalPriceTableColumn = new TableColumn(table, SWT.RIGHT);
		tableColumnLayout.setColumnData(totalPriceTableColumn, new ColumnWeightData(100));
		totalPriceTableColumn.setText(InvoiceLabel.TotalPrice.getString());


		programmeBookingsTable = new ProgrammeBookingsTable(table);

		viewerFilter = new ProgrammeBookingsCVOViewerFilter();
		tableViewer = programmeBookingsTable.getViewer();
		tableViewer.setFilters(new ViewerFilter[] { viewerFilter });

		tableViewer.addDoubleClickListener(tableDoubleClickListener);
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
		new MenuItem(menu, SWT.SEPARATOR);

		openLiveStreamMenuItem = buildMenuItem(menu, I18N.OpenLiveStream, e -> openLiveStream());
		openVideoStreamMenuItem = buildMenuItem(menu, I18N.OpenVideoStream, e -> openVideoStream());

		programmeBookingsTable.getViewer().getTable().setMenu(menu);
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
		button.addListener(SWT.Selection, e -> {
			cancelBooking();
			updateButtonStates();
		});
		return button;
	}


	private Button buildChangeInvoiceRecipientButton(Composite parent) {
		Button button = new Button(parent, SWT.PUSH);
		button.setText(I18N.ChangeInvoiceRecipient);
		button.setEnabled(false);
		button.addListener(SWT.Selection, e -> {
			changeInvoiceRecipient();
			updateButtonStates();
		});
		return button;
	}


	private Button buildChangeBenefitRecipientButton(Composite parent) {
		Button button = new Button(parent, SWT.PUSH);
		button.setText(I18N.ChangeBenefitRecipient);
		button.setEnabled(false);
		button.addListener(SWT.Selection, e -> {
			changeBenefitRecipient();
			updateButtonStates();
		});
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

		Label paidLabel = new Label(composite, SWT.NONE);
		paidLabel.setText(InvoiceLabel.Paid.getString());
		paidText = new Text(composite, SWT.BORDER | SWT.READ_ONLY | SWT.RIGHT);
		gd = new GridData(SWT.FILL, SWT.CENTER, false, false);
		gd.widthHint = recommendedWidth;
		paidText.setLayoutData(gd);

		Label openLabel = new Label(composite, SWT.NONE);
		openLabel.setText(InvoiceLabel.Open.getString());
		openText = new Text(composite, SWT.BORDER | SWT.READ_ONLY | SWT.RIGHT);
		gd = new GridData(SWT.FILL, SWT.CENTER, false, false);
		gd.widthHint = recommendedWidth;
		openText.setLayoutData(gd);

		return composite;
	}


	private IDoubleClickListener tableDoubleClickListener = new IDoubleClickListener() {
		@Override
		public void doubleClick(DoubleClickEvent event) {
			openDetailsDialog();
		}

	};


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
				ClipboardHelper.copyToClipboard( getCopyInfoFromSelection() );
			}
			else if (e.keyCode == 'c' && e.stateMask == (SWT.MOD1 | SWT.SHIFT)) {
				ClipboardHelper.copyToClipboard( getSelectedIdsAsText() );
			}
		}
	};


	private String getCopyInfoFromSelection() {
		List<ProgrammeBookingCVO> pbList = getSelection();
		StringBuilder text = new StringBuilder(pbList.size() * 128);

		try {
    		for (ProgrammeBookingCVO pbCVO : pbList) {
    			if (text.length() > 0) {
    				text.append("\n");
    			}

    			Long pbPK = pbCVO.getPK();
    			Long poPK = pbCVO.getVO().getOfferingPK();
    			ProgrammeOfferingCVO poCVO = ProgrammeOfferingModel.getInstance().getProgrammeOfferingCVO(poPK);
    			Long ppPK = poCVO.getVO().getProgrammePointPK();
    			ProgrammePointCVO ppCVO = ProgrammePointModel.getInstance().getProgrammePointCVO(ppPK);

    	        text.append("ProgrammeBookingCVO [");
    	        text.append("id=").append(pbPK);
    	        text.append(", offeringID=").append(poPK);
    	        text.append(", programmePointID=").append(ppPK);
    	        text.append(", Programme Point=\"").append( ppCVO.getPpName() ).append("\"");
    	        text.append(", amount=").append( pbCVO.getCurrencyAmountGross() );
    	        text.append("]");
    		}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}

		return text.toString();
	}


	private String getSelectedIdsAsText() {
		List<ProgrammeBookingCVO> pbList = getSelection();
		StringBuilder text = new StringBuilder(pbList.size() * 128);
		for (ProgrammeBookingCVO pbCVO : pbList) {
			if (text.length() > 0) {
				text.append(", ");
			}

			text.append( pbCVO.getPK() );
		}

		return text.toString();
	}


	private List<ProgrammeBookingCVO> getSelection() {
		List<ProgrammeBookingCVO> selectedEntities = null;
		IStructuredSelection selection = (IStructuredSelection) tableViewer.getSelection();
		if (selection != null) {
			selectedEntities = selection.toList();
		}
		return selectedEntities;
	}


	protected void createBooking() {
		boolean editorSaveCheckOK = ParticipantEditor.saveActiveEditor();
		if (!editorSaveCheckOK) {
			return;
		}

		Long eventPK = participant.getEventId();

		ParticipantSearchData psd = new ParticipantSearchData(participant);
		List<ParticipantSearchData> participantSearchDataList = Collections.singletonList(psd);

		Long participantTypePK = participant.getParticipantTypePK();

		Wizard wizard = new CreateProgrammeBookingsWizard(eventPK, participantSearchDataList, participantTypePK);

		WizardDialog dialog = new WizardDialog(getShell(), wizard);
		dialog.create();
		dialog.getShell().setSize(900, 600);
		dialog.open();
	}


	protected void cancelBooking() {
		boolean editorSaveCheckOK = ParticipantEditor.saveActiveEditor();
		if (!editorSaveCheckOK) {
			return;
		}

		List<ProgrammeBookingCVO> programmeBookinglist = SelectionHelper.toList(tableViewer.getSelection());
		
		/*
		 * The programmeBookinglist could be empty after ParticipantEditor.saveActiveEditor() is called.
		 * Because the bookings could be cancelled by saving participant if the participant state is changed before to 
		 * "cancel by organizer" or "cancel by participant". If the programmeBookinglist is empty, we don't need to do
		 * anything else.
		 */
		if (CollectionsHelper.notEmpty(programmeBookinglist) ) {
			Wizard wizard = new CancelProgrammeBookingsWizard(programmeBookinglist);
			WizardDialog dialog = new WizardDialog(this.getShell(), wizard);
			dialog.create();
			dialog.getShell().setSize(700, 600);
			dialog.open();
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
					List<ProgrammeBookingCVO> programmeBookingCVOs = SelectionHelper.toList(tableViewer.getSelection());
					List<Long> programmeBookingPKs = ProgrammeBookingCVO.getPKs(programmeBookingCVOs);

					Long newInvoiceRecipientPK = participantSelectionDialog.getSelectedPKs().get(0);
					ProgrammeBookingModel.getInstance().changeInvoiceRecipientsOfProgrammeBookings(
						programmeBookingPKs,
						newInvoiceRecipientPK
					);
				}
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	protected void changeBenefitRecipient() {
		try {
			if (ParticipantEditor.saveEditor(participant.getID())) {
				List<ProgrammeBookingCVO> programmeBookingCVOs = SelectionHelper.toList(tableViewer.getSelection());
				List<Long> programmeBookingPKs = ProgrammeBookingCVO.getPKs(programmeBookingCVOs);

				List<SQLParameter> sqlParameterListForSameGroup = getSqlParameterForSameGroup();

				ISelectionDialogConfig dialogConfig = OneParticipantSelectionDialogConfig.INSTANCE;
				if (programmeBookingPKs.size() > 1) {
					dialogConfig = new FixedNumberParticipantSelectionDialogConfig(
						CollectionsHelper.createArrayList(1, programmeBookingPKs.size())
					);
				}

				ParticipantSelectionDialog participantSelectionDialog = new ParticipantSelectionDialog(
					getShell(),
					dialogConfig,
					participant.getEventId()
				);
				participantSelectionDialog.setTitle(I18N.ChangeBenefitRecipient);
				participantSelectionDialog.setInitialSQLParameters(sqlParameterListForSameGroup);

				// call create() directly to be able to execute the initial search
				participantSelectionDialog.create();
				if (CollectionsHelper.notEmpty(sqlParameterListForSameGroup)) {
					participantSelectionDialog.doSearch();
				}

				participantSelectionDialog.open();

				if (!participantSelectionDialog.isCancelled()) {
					List<Long> participantPKs = participantSelectionDialog.getSelectedPKs();

					if (participantPKs.size() == 1) {
						ProgrammeBookingModel.getInstance().changeBenefitRecipientsOfProgrammeBookings(
							programmeBookingPKs,
							participantPKs.get(0)
						);
					}
					else if (participantPKs.size() == programmeBookingCVOs.size()) {
						ProgrammeBookingModel.getInstance().changeBenefitRecipientsOfProgrammeBookings(
							programmeBookingPKs,
							participantPKs
						);
					}
					else {
						throw new RuntimeException("Wrong number of selected participants. "
							+ "The user has to select 1 or " + programmeBookingCVOs.size() + "participants.");
					}
				}
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	private void openLiveStream() {
		try {
			ISelection selection = programmeBookingsTable.getViewer().getSelection();
			ProgrammeBookingCVO programmeBookingCVO = SelectionHelper.getUniqueSelected(selection);

			Long programmePointPK = programmeBookingCVO.getProgrammeBookingVO().getProgrammePointPK();
			Long participantPK = programmeBookingCVO.getProgrammeBookingVO().getBenefitRecipientPK();

			String url = getProgrammePointMgr().getPersonalLiveStreamUrlForParticipant(programmePointPK, participantPK);

			BrowserHelper.openBrowser(url);
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	private void openVideoStream() {
		try {
			ISelection selection = programmeBookingsTable.getViewer().getSelection();
			ProgrammeBookingCVO programmeBookingCVO = SelectionHelper.getUniqueSelected(selection);

			Long programmePointPK = programmeBookingCVO.getProgrammeBookingVO().getProgrammePointPK();
			Long participantPK = programmeBookingCVO.getProgrammeBookingVO().getBenefitRecipientPK();

			String url = getProgrammePointMgr().getPersonalVideoStreamUrlForParticipant(programmePointPK, participantPK);

			BrowserHelper.openBrowser(url);
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
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
    		if (participant != null) {
    			Long eventPK = participant.getEventId();
    			ProgrammePointFilterDialog dialog = new ProgrammePointFilterDialog(getShell(), eventPK);
    			if (checkedProgrammePoints != null) {
    				dialog.setCheckedProgrammePoints(checkedProgrammePoints);
    			}
    			int code = dialog.open();

    			if (code == IDialogConstants.CLIENT_ID + 1) {
    				checkedProgrammePoints = dialog.getCheckedProgrammePoints();
    				filterActiveButton.setSelection(true);
    				setVisibleProgrammePoints(checkedProgrammePoints);
    			}
    			else if (code == IDialogConstants.CLIENT_ID + 2) {
    				filterActiveButton.setSelection(false);
    				setFilterOff();
    			}

    		}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	protected void switchFilterActive() {
		try {
			boolean on = filterActiveButton.getSelection();
			if (on) {
				if (checkedProgrammePoints != null) {
					setVisibleProgrammePoints(checkedProgrammePoints);
				}
			}
			else {
				setFilterOff();
			}
		}
		catch (Exception e1) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e1);
		}
	}


	private void syncWidgetsToEntity() {
		if (participant != null && programmeBookingsTable != null) {
			SWTHelper.syncExecDisplayThread(new Runnable() {
				@Override
				public void run() {
					try {
						programmeBookingsTable.setParticipant(participant);

						programmeBookingCVOs = pbModel.getProgrammeBookingCVOsByRecipient(participant.getID());
						programmeBookingCVOs = CollectionsHelper.createArrayList(programmeBookingCVOs);

						sortProgrammeBookings(programmeBookingCVOs);
						programmeBookingsTable.setInput(programmeBookingCVOs);

						BookingsCurrencyAmountsEvaluator bcae = new BookingsCurrencyAmountsEvaluator(
							programmeBookingCVOs,
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
						Set<ProgrammeOfferingVO> programmeOfferingVOs = createHashSet(programmeBookingCVOs.size());
						for (ProgrammeBookingCVO pbCVO : programmeBookingCVOs) {
							programmeOfferingVOs.add(pbCVO.getProgrammeOfferingCVO().getVO());
						}

						String lang = Locale.getDefault().getLanguage();


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
							 * Config settings don't matter. If a booking has an additional price
							 * it has to be shown.
							 */
							for (ProgrammeBookingCVO programmeBookingCVO : programmeBookingCVOs) {
								if (programmeBookingCVO.getVO().getAdd1AmountGross().signum() != 0) {
									showAdd1Price = true;
									break;
								}
							}

							/* Determine column name for column of additional price 1:
							 * If all Offerings that have an additional price 1 share the same name,
							 * use it as column name. Otherwise use the default name.
							 */
							String columnName = ProgrammeOfferingHelper.getAddPrice1Name(programmeOfferingVOs, lang);

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

						// *************************************************************************************
						// * hide/show column for additional price 2
						// *

						boolean showAdd2Price = false;
						{
							/* Determine if column for additional price 1 is visible.
							 *
							 * This depends on the bookings, not on the offerings, because the
							 * latter might have changed since the booking was created.
							 *
							 * Config settings don't matter. If a booking has an additional price
							 * it has to be shown.
							 */
							for (ProgrammeBookingCVO programmeBookingCVO : programmeBookingCVOs) {
								if (programmeBookingCVO.getVO().getAdd2AmountGross().signum() != 0) {
									showAdd2Price = true;
									break;
								}
							}

							/* Determine column name for column of additional price 2:
							 * If all Offerings that have an additional price 2 share the same name,
							 * use it as column name. Otherwise use the default name.
							 */
							String columnName = ProgrammeOfferingHelper.getAddPrice2Name(programmeOfferingVOs, lang);

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
						// * hide/show column for main price
						// *

						{
							/* The column for main price is visible if either the column for
							 * additional price 1 or additional price 2 is visible.
							 * Otherwise the main price is equal to total price and should not be
							 * visible.
							 */
							boolean showMainPrice = showAdd1Price || showAdd2Price;

							String columnName = ParticipantLabel.Bookings_Price.getString();

			    			// show or hide column of main price
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

						// refresh table layout to hide/show dynamical columns
						SWTHelper.deferredLayout(300, table.getParent());

						updateButtonStates();
					}
					catch (Exception e) {
						RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
					}
				}
			});
		}
	}


	public void openDetailsDialog() {
		ISelection selection = programmeBookingsTable.getViewer().getSelection();
		ProgrammeBookingCVO programmeBookingCVO = SelectionHelper.getUniqueSelected(selection);

		ProgrammeBookingDetailsDialog dialog = new ProgrammeBookingDetailsDialog(getShell(), programmeBookingCVO);
		dialog.open();
	}


	public void setParticipant(Participant participant) {
		Long oldPK = this.participant == null ? null : this.participant.getID();
		Long newPK =      participant == null ? null : participant.getID();
		boolean pkChanged = !EqualsHelper.isEqual(oldPK, newPK);


		if (pkChanged && oldPK != null) {
			pbModel.removeForeignKeyListener(programmeBookingModelListener, oldPK);
		}


		this.participant = participant;


		if (pkChanged && newPK != null) {
			pbModel.addForeignKeyListener(programmeBookingModelListener, newPK);
		}


		syncWidgetsToEntity();
	}


	private DisposeListener disposeListener = new DisposeListener() {
		@Override
		public void widgetDisposed(DisposeEvent event) {
			try {
				if (pbModel != null && participant != null && participant.getID() != null) {
					pbModel.removeForeignKeyListener(programmeBookingModelListener, participant.getID());
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



	public void setVisibleProgrammePoints(ProgrammePointCVO[] checkedProgrammePoints) {
		viewerFilter.setCheckedProgrammePoints(checkedProgrammePoints);
		viewerFilter.setActive(true);
		programmeBookingsTable.getViewer().refresh();

	}


	public void setFilterOff() {
		viewerFilter.setActive(false);
		programmeBookingsTable.getViewer().refresh();
	}


	public void setShowCancelledBookings(boolean showCancelledBookings) {
		viewerFilter.setShowCancelledBookings(showCancelledBookings);
		programmeBookingsTable.getViewer().refresh();
	}


	private void updateButtonStates() {
		boolean oneBookingIsSelected = table.getSelectionCount() == 1;
		boolean bookingsAreSelected = table.getSelectionCount() > 0;
		boolean aCancelledBookingIsSelected = false;
		boolean isLiveStreamAvailable = false;
		boolean isVideoStreamAvailable = false;

		if (bookingsAreSelected) {
			List<ProgrammeBookingCVO> selectedBookings = SelectionHelper.toList(tableViewer.getSelection());

			for (ProgrammeBookingCVO programmeBookingCVO : selectedBookings) {
				if (programmeBookingCVO.isCanceled()) {
					aCancelledBookingIsSelected = true;
					break;
				}

			}

			if (oneBookingIsSelected) {
				try {
					Long programmePointPK = selectedBookings.get(0).getVO().getProgrammePointPK();
					ProgrammePointVO programmePointVO = ProgrammePointModel.getInstance().getProgrammePointVO(programmePointPK);

					isLiveStreamAvailable = programmePointVO.isLiveStreamAvailable();
					isLiveStreamAvailable &= programmePointVO.getLiveStreamProvider() != null;

					isVideoStreamAvailable = programmePointVO.isVideoStreamAvailable();
					isVideoStreamAvailable &= programmePointVO.getVideoStreamProvider() != null;
				}
				catch (Exception e) {
					RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
				}
			}
		}


		editButton.setEnabled(oneBookingIsSelected);
		editMenuItem.setEnabled(oneBookingIsSelected);

		cancelButton.setEnabled(bookingsAreSelected);
		cancelMenuItem.setEnabled(bookingsAreSelected);

		changeBenefitRecipientButton.setEnabled(bookingsAreSelected && !aCancelledBookingIsSelected);
		changeBenefitRecipientMenuItem.setEnabled(bookingsAreSelected && !aCancelledBookingIsSelected);

		changeInvoiceRecipientButton.setEnabled(bookingsAreSelected && !aCancelledBookingIsSelected);
		changeInvoiceRecipientMenuItem.setEnabled(bookingsAreSelected && !aCancelledBookingIsSelected);

		openLiveStreamMenuItem.setEnabled(oneBookingIsSelected && isLiveStreamAvailable);
		openVideoStreamMenuItem.setEnabled(oneBookingIsSelected && isVideoStreamAvailable);

		filterActiveButton.setEnabled(bookingsAreSelected);
	}


	private CacheModelListener<Long> participantModelListener = new CacheModelListener<Long>() {
		@Override
		public void dataChange(CacheModelEvent<Long> event) throws Exception {
			if (serverModel.isLoggedIn()) {
				if (programmeBookingCVOs != null && programmeBookingsTable != null) {
					List<Long> participantIDs = event.getKeyList();

					if (participantIDs.contains(participant.getID()) &&
						event.getOperation() == CacheModelOperation.DELETE
					) {
						// do nothing, because the editor will be closed
					}
					else {
						// check if the updated participant is a recipient of any booking
						boolean relevant = false;
						for (Long participantID : participantIDs) {
							Long participantPK = participantID;
							for (ProgrammeBookingCVO pbCVO : programmeBookingCVOs) {
								ProgrammeBookingVO bookingVO = pbCVO.getBookingVO();
								if (bookingVO.getInvoiceRecipientPK().equals(participantPK)
									||
									bookingVO.getBenefitRecipientPK().equals(participantPK)
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
							 * If any of the participants of the bookings has been deleted, its bookings
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
										programmeBookingsTable.getViewer().refresh();
									}
								});
							}
						}
					}
				}
			}
		}
	};


	private CacheModelListener<Long> programmeBookingModelListener = new CacheModelListener<Long>() {
		@Override
		public void dataChange(CacheModelEvent<Long> event) throws Exception {
			if (serverModel.isLoggedIn()) {
				if (participant != null && participant.getID() != null &&
					programmeBookingsTable != null
				) {
					syncWidgetsToEntity();
				}
			}
		}
	};


	private void sortProgrammeBookings(List<ProgrammeBookingCVO> programmeBookingCVOs) {
		if (programmeBookingCVOs != null) {
			Collections.sort(programmeBookingCVOs, BookingCVO_BookingNo_Position_Comparator.getInstance());

			BookingCVO.initDisplayIndices(programmeBookingCVOs);
		}
	}

}
