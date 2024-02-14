package de.regasus.participant.editor;

import static com.lambdalogic.util.rcp.widget.SWTHelper.buildMenuItem;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import com.lambdalogic.messeinfo.participant.Participant;
import com.lambdalogic.messeinfo.participant.ParticipantLabel;
import com.lambdalogic.messeinfo.participant.ParticipantState;
import com.lambdalogic.messeinfo.participant.data.BadgeCVO;
import com.lambdalogic.util.exception.ErrorMessageException;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.model.CacheModelOperation;
import com.lambdalogic.util.rcp.SelectionHelper;
import com.lambdalogic.util.rcp.UtilI18N;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.I18N;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.editor.AbstractEditor;
import de.regasus.participant.ParticipantModel;
import de.regasus.participant.ParticipantStateModel;
import de.regasus.participant.badge.BadgeOpenController;
import de.regasus.participant.badge.BadgePrintController;
import de.regasus.participant.badge.BadgesTable;
import de.regasus.ui.Activator;

/**
 * This BadgesComposite realizes the contents of the "Badges" tab in the participant editor.
 * <p>
 * It contains a table showing the participant's badges and allows to print, to (de)activate and
 * to assign them.
 */
public class BadgesComposite extends Composite {

	private Participant participant;

	private MessageDialog scanWaitDialog;

	private BadgeCVO badgeToBeAssigned;

	private ParticipantStateModel participantStateModel;


	// Widgets
	private Table table;
	private TableViewer tableViewer;
	private BadgesTable badgesTable;

	private Button openBadgeButton;
	private Button printBadgeButton;
	private Button enableDisableBadgeButton;
	private Button assignBadgeButton;

	private MenuItem enableDisableBadgeMenuItem;
	private MenuItem assignBadgeMenuItem;


	public BadgesComposite(Composite parent, int style) {
		super(parent, style);

		setLayout(new GridLayout());

		Composite tableComposite = buildTableComposite(this);
		tableComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		Composite buttonComposite = buildButtonComposite(this);
		buttonComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));


		participantStateModel = ParticipantStateModel.getInstance();
		participantStateModel.addListener(participantStateModelListener);

		addDisposeListener(disposeListener);
	}


	private Composite buildTableComposite(Composite parent) {
		Composite composite = new Composite(parent, SWT.BORDER);

		TableColumnLayout layout = new TableColumnLayout();
		composite.setLayout(layout);
		table = new Table(composite, SWT.SINGLE | SWT.FULL_SELECTION);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);


		// Create the columns  No, Id, Created, Last Use, Bad Trials, Disabled, Last Scanned, Type, Note

		// No
		TableColumn numberTableColumn = new TableColumn(table, SWT.RIGHT);
		numberTableColumn.setText(UtilI18N.NumberAbreviation);
		layout.setColumnData(numberTableColumn, new ColumnWeightData(15));

		// ID
		TableColumn idTableColumn = new TableColumn(table, SWT.RIGHT);
		idTableColumn.setText(UtilI18N.ID);
		layout.setColumnData(idTableColumn, new ColumnWeightData(30));

		// Barcode
		TableColumn barcodeTableColumn = new TableColumn(table, SWT.RIGHT);
		barcodeTableColumn.setText(ParticipantLabel.BarCode.getString());
		layout.setColumnData(barcodeTableColumn, new ColumnWeightData(30));

		// Created
		TableColumn createdTableColumn = new TableColumn(table, SWT.RIGHT);
		createdTableColumn.setText(UtilI18N.CreateDateTime);
		layout.setColumnData(createdTableColumn, new ColumnWeightData(30));

		// Bad Trials
		TableColumn badTrialsTableColumn = new TableColumn(table, SWT.RIGHT);
		layout.setColumnData(badTrialsTableColumn, new ColumnWeightData(25));
		badTrialsTableColumn.setText(ParticipantLabel.BadTrials.getString());

		// Disabled
		TableColumn disabledTableColumn = new TableColumn(table, SWT.CENTER);
		layout.setColumnData(disabledTableColumn, new ColumnWeightData(35));
		disabledTableColumn.setText(UtilI18N.Disabled);

		// Last Scanned
		TableColumn lastScannedTableColumn = new TableColumn(table, SWT.CENTER);
		layout.setColumnData(lastScannedTableColumn, new ColumnWeightData(30));
		lastScannedTableColumn.setText(ParticipantLabel.LastScanned.getString());

		// Type
		TableColumn typeTableColumn = new TableColumn(table, SWT.RIGHT);
		layout.setColumnData(typeTableColumn, new ColumnWeightData(10));
		typeTableColumn.setText(UtilI18N.Type);


		badgesTable = new BadgesTable(table);
		tableViewer = badgesTable.getViewer();

		table.addSelectionListener(tableSelectionListener);

		buildContextMenu();

		return composite;
	}


	private void buildContextMenu() {
		Menu menu = new Menu (getShell(), SWT.POP_UP);

		enableDisableBadgeMenuItem = buildMenuItem(menu, I18N.EnableDisableBadgeButton_name, e -> toggleBadgeState());
		assignBadgeMenuItem = buildMenuItem(menu, I18N.AssignBadgeButton_name, e -> assignBadge());

		badgesTable.getViewer().getTable().setMenu(menu);
	}


	private Composite buildButtonComposite(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);

		composite.setLayout(new RowLayout(SWT.HORIZONTAL));

		openBadgeButton = buildOpenBadgeButton(composite);
		printBadgeButton = buildPrintBadgeButton(composite);
		enableDisableBadgeButton = buildEnableDisableBadgeButton(composite);
		assignBadgeButton = buildAssignBadgeButton(composite);

		return composite;
	}


	/**
	 * Button to create a new badge and open it.
	 * @param parent
	 * @return
	 */
	private Button buildOpenBadgeButton(Composite parent) {
		Button button = new Button(parent, SWT.PUSH);
		button.setText(I18N.OpenBadgeButton_name);
		button.setToolTipText(I18N.OpenBadgeButton_tooltip);
		button.addListener(SWT.Selection, e -> openBadge());
		return button;
	}


	/**
	 * Button to create a new badge and print it.
	 * @param parent
	 * @return
	 */
	private Button buildPrintBadgeButton(Composite parent) {
		Button button = new Button(parent, SWT.PUSH);
		button.setText(I18N.PrintBadgeButton_name);
		button.setToolTipText(I18N.PrintBadgeButton_tooltip);
		button.addListener(SWT.Selection, e -> printBadge());
		return button;
	}


	/**
	 * Button to toggle the disabled state.
	 * @param parent
	 * @return
	 */
	private Button buildEnableDisableBadgeButton(Composite parent) {
		Button button = new Button(parent, SWT.PUSH);
		button.setText(I18N.EnableDisableBadgeButton_name);
		button.setToolTipText(I18N.EnableDisableBadgeButton_tooltip);
		button.setEnabled(false);
		button.addListener(SWT.Selection, e -> toggleBadgeState());
		return button;
	}


	/**
	 * Button to toggle the disabled state.
	 * @param parent
	 * @return
	 */
	private Button buildAssignBadgeButton(Composite parent) {
		Button button = new Button(parent, SWT.PUSH);
		button.setText(I18N.AssignBadgeButton_name);
		button.setToolTipText(I18N.AssignBadgeButton_tooltip);
		button.setEnabled(false);
		button.addListener(SWT.Selection, e -> assignBadge());
		return button;
	}


	public void assignBadge() {
		if (!AbstractEditor.saveActiveEditor()) {
			return;
		}

		badgeToBeAssigned = getSelectedBadge();

		// BadgeCVO actually should always be present
		if (badgeToBeAssigned != null) {

			// We open a blocking dialog, which can be cancelled or closed from a
			// different thread when a badge is scanned.
			scanWaitDialog = new MessageDialog(
				getShell(),
				UtilI18N.Info,
				null,
				I18N.WaitingForScannedBadgeToBeAssigned,
				0,
				new String[]{UtilI18N.Cancel},
				0
			);
			scanWaitDialog.open();
			scanWaitDialog = null;
		}
	}


	public void setParticipant(Participant participant) {
		this.participant = participant;
		syncWidgetsToEntity();
	}


	public BadgeCVO getSelectedBadge() {
		BadgeCVO badgeCVO = SelectionHelper.getUniqueSelected(tableViewer);
		return badgeCVO;
	}


	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		tableViewer.addSelectionChangedListener(listener);
	}


	public void removeSelectionChangedListener(ISelectionChangedListener listener) {
		tableViewer.removeSelectionChangedListener(listener);
	}


	private void syncWidgetsToEntity() {
		if (participant != null ) {
			SWTHelper.syncExecDisplayThread(new Runnable() {
				@Override
				public void run() {
					try {
						badgesTable.getViewer().setInput( participant.getBadgeCVOs() );

						updateButtonStates();
					}
					catch (Exception e) {
						RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
					}
				}
			});
		}
	}


	private SelectionListener tableSelectionListener = new SelectionAdapter() {
		@Override
		public void widgetSelected(SelectionEvent event) {
			updateButtonStates();
		}
	};


	private void updateButtonStates() {
		boolean somethingIsSelected = table.getSelectionCount() > 0;

		boolean badgePrintable = false;
		if (participant != null) {
			boolean isNew = participant == null || participant.getID() == null;
			Long participantStatePK = participant.getParticipantStatePK();
			if (participantStatePK != null) {
				ParticipantState participantState;
				try {
					participantState = participantStateModel.getParticipantState(participantStatePK);
					badgePrintable = participantState.isBadgePrint() && !isNew;
				}
				catch (Exception e) {
					RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
				}
			}
		}

		openBadgeButton.setEnabled(badgePrintable);

		printBadgeButton.setEnabled(badgePrintable);

		enableDisableBadgeButton.setEnabled(somethingIsSelected);
		enableDisableBadgeMenuItem.setEnabled(somethingIsSelected);

		assignBadgeButton.setEnabled(somethingIsSelected);
		assignBadgeMenuItem.setEnabled(somethingIsSelected);
	}


	private void openBadge() {
		try {
			if (!AbstractEditor.saveActiveEditor()) {
				return;
			}

			if (participant != null & participant.getID() != null) {
				BadgeOpenController badgeOpenController = new BadgeOpenController();

				badgeOpenController.createAndOpenBadge(
					participant.getID(),
					participant.getName(),
					participant.getNumber()
				);
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	private void printBadge() {
		try {
			if (!AbstractEditor.saveActiveEditor()) {
				return;
			}

			if (participant != null & participant.getID() != null) {
				BadgePrintController badgePrintController = new BadgePrintController();

				badgePrintController.createBadgeWithDocument(
					participant.getID(),
					participant.getName(),
					participant.getNumber()
				);
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	private void toggleBadgeState() {
		try {
			if (!AbstractEditor.saveActiveEditor()) {
				return;
			}

			ISelection selection = tableViewer.getSelection();
			BadgeCVO badgeCVO = SelectionHelper.getUniqueSelected(selection);
			ParticipantModel.getInstance().toggleBadgeState(badgeCVO);
		}
		catch (Exception ex) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), ex);
		}
	}




	public boolean isWaitingForScannedId() {
		return scanWaitDialog != null;
	}


	public void stopWaiting(final byte[] barcodeBytes) {
		// Closing the wait dialog already, making room for the question dialog that might be needed
		Long assignBadgePK = badgeToBeAssigned.getPK();
		badgeToBeAssigned = null;
		scanWaitDialog.close();

		try {
			ParticipantModel.getInstance().setBadgeId(participant, assignBadgePK, barcodeBytes, false);
		}
		catch (ErrorMessageException e) {
			// Comparing against BadgeDAO.ERROR_CODE_BADGE_ID_EXIST, which is not visible here
			if ("BadgeCouldNotBeAssigned_BadgeIDExist".equals(e.getErrorCode())) {

				// Ask whether to try again
				boolean b = MessageDialog.openQuestion(getShell(), UtilI18N.Question, I18N.BadgeIdAlreadyExistingForceAssignmentQuestion);
				if (b) {
					try {
						// Try again, with force-flag set to true
						ParticipantModel.getInstance().setBadgeId(participant, assignBadgePK, barcodeBytes, true);
					}
					catch(Exception ex) {
						RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), ex);
					}
				}
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	public void setLastScanned(byte[] barcodeBytes) {
		badgesTable.setLastScannedCardId(barcodeBytes);
		tableViewer.refresh();
	}


	private CacheModelListener<Long> participantStateModelListener = new CacheModelListener<Long>() {
		@Override
		public void dataChange(CacheModelEvent<Long> event) throws Exception {
			if (event.getOperation() == CacheModelOperation.UPDATE) {
				updateButtonStates();
			}
		}
	};


	private DisposeListener disposeListener = new DisposeListener() {
		@Override
		public void widgetDisposed(DisposeEvent event) {
			if (participantStateModel != null) {
				try {
					participantStateModel.removeListener(participantStateModelListener);
				}
				catch (Exception e) {
					com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
				}
			}
		}
	};

}
