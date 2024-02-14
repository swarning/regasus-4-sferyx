package de.regasus.programme.waitlist.editor;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.actions.ActionFactory;

import com.lambdalogic.messeinfo.invoice.InvoiceLabel;
import com.lambdalogic.messeinfo.kernel.ServerMessage;
import com.lambdalogic.messeinfo.participant.ParticipantLabel;
import com.lambdalogic.messeinfo.participant.data.EventVO;
import com.lambdalogic.messeinfo.participant.data.ProgrammeBookingCVO;
import com.lambdalogic.messeinfo.participant.data.ProgrammeBookingVO;
import com.lambdalogic.messeinfo.participant.data.ProgrammePointVO;
import com.lambdalogic.util.CollectionsHelper;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.model.CacheModelOperation;
import com.lambdalogic.util.rcp.BusyCursorHelper;
import com.lambdalogic.util.rcp.CopyAction;
import com.lambdalogic.util.rcp.SelectionHelper;
import com.lambdalogic.util.rcp.UtilI18N;

import de.regasus.I18N;
import de.regasus.core.ServerModel;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.dialog.ServerMessageDialog;
import de.regasus.core.ui.editor.AbstractEditor;
import de.regasus.core.ui.editor.IRefreshableEditorPart;
import de.regasus.event.EventIdProvider;
import de.regasus.event.EventModel;
import de.regasus.programme.ProgrammePointModel;
import de.regasus.programme.WaitList;
import de.regasus.programme.WaitListModel;
import de.regasus.ui.Activator;

public class WaitlistEditor
extends AbstractEditor<WaitlistEditorInput>
implements IRefreshableEditorPart, CacheModelListener<Long>, EventIdProvider {

	public static final String ID = "WaitListEditor";


	private Long programmePointPK;

	// Entities
	private ProgrammePointVO programmePointVO;
	private WaitList waitList;

	// Models
	private WaitListModel waitListModel;
	private ProgrammePointModel programmePointModel;


	// Widgets
	private WaitlistTable waitListTable;
	private Button moveFirstButton;
	private Button moveUpButton;
	private Button moveDownButton;
	private Button moveLastButton;
	private Button bookButton;
	private Button cancelButton;


	// *************************************************************************
	// * Implemented abstract methods of AbstractEditor and IRefreshableEditorPart
	// *

	@Override
	protected void init() throws Exception {
		programmePointPK = editorInput.getKey();

		// get models
		waitListModel = WaitListModel.getInstance();
		programmePointModel = ProgrammePointModel.getInstance();

		// get entity
		waitList = waitListModel.getWaitList(programmePointPK);
		programmePointVO = programmePointModel.getProgrammePointVO(programmePointPK);

		// register at models
		waitListModel.addForeignKeyListener(this, programmePointPK);
		programmePointModel.addListener(this, programmePointPK);
	}


	@Override
	protected String getTypeName() {
		return ParticipantLabel.WaitList.getString();
	}

//	@Override
//	protected String getInfoButtonToolTipText() {
//		return EmailI18N.ProgrammePointEditor_InfoButtonToolTip;
//	}

	@Override
	protected void createWidgets(Composite parent) {
		this.parent = parent;

		Composite mainComposite = new Composite(parent, SWT.NONE);
		mainComposite.setLayout(new GridLayout(2, false));

		final Composite tableComposite = new Composite(mainComposite, SWT.NONE);
		final GridData gd_mainComposite = new GridData(SWT.FILL, SWT.FILL, true, true);
		tableComposite.setLayoutData(gd_mainComposite);

		TableColumnLayout tableColumnLayout = new TableColumnLayout();
		tableComposite.setLayout(tableColumnLayout);

		// **************************************************************************
		// * WaitListTable
		// *

		Table table = new Table(tableComposite, SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION);
		table.setLinesVisible(true);
		table.setHeaderVisible(true);
		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		final TableColumn positionTableColumn = new TableColumn(table, SWT.RIGHT);
		tableColumnLayout.setColumnData(positionTableColumn, new ColumnWeightData(60));
		positionTableColumn.setText(UtilI18N.Position);

		final TableColumn creationTableColumn = new TableColumn(table, SWT.NONE);
		tableColumnLayout.setColumnData(creationTableColumn, new ColumnWeightData(100));
		creationTableColumn.setText(UtilI18N.CreateDateTime);

		final TableColumn offeringTableColumn = new TableColumn(table, SWT.NONE);
		tableColumnLayout.setColumnData(offeringTableColumn, new ColumnWeightData(150));
		offeringTableColumn.setText(ParticipantLabel.ProgrammeOffering.getString());

		final TableColumn benRecipTableColumn = new TableColumn(table, SWT.NONE);
		tableColumnLayout.setColumnData(benRecipTableColumn, new ColumnWeightData(140));
		benRecipTableColumn.setText(I18N.WaitListEditor_BenefitRecipientTableColumn);

		final TableColumn invRecipTableColumn = new TableColumn(table, SWT.NONE);
		tableColumnLayout.setColumnData(invRecipTableColumn, new ColumnWeightData(140));
		invRecipTableColumn.setText(I18N.WaitListEditor_InvoiceRecipientTableColumn);

		final TableColumn amountTableColumn = new TableColumn(table, SWT.RIGHT);
		tableColumnLayout.setColumnData(amountTableColumn, new ColumnWeightData(100));
		amountTableColumn.setText(InvoiceLabel.Amount.getString());

		waitListTable = new WaitlistTable(table);

		// MIRCP-284 - Copy und Paste
		getEditorSite().getActionBars().setGlobalActionHandler(ActionFactory.COPY.getId(), new CopyAction());

		// *
		// * WaitListTable
		// **************************************************************************

		final Composite rightComposite = new Composite(mainComposite, SWT.NONE);
		final RowLayout rowLayout = new RowLayout(SWT.VERTICAL);
		rowLayout.fill = true;
		rightComposite.setLayout(rowLayout);
		final GridData gd_rightComposite = new GridData(SWT.LEFT, SWT.FILL, false, false);
		rightComposite.setLayoutData(gd_rightComposite);

		moveFirstButton = new Button(rightComposite, SWT.NONE);
		moveFirstButton.setText(UtilI18N.MoveFirst);
		moveFirstButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				moveFirst();
			}
		});

		moveUpButton = new Button(rightComposite, SWT.NONE);
		moveUpButton.setText(UtilI18N.MoveUp);
		moveUpButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				moveUp();
			}
		});

		moveDownButton = new Button(rightComposite, SWT.NONE);
		moveDownButton.setText(UtilI18N.MoveDown);
		moveDownButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				moveDown();
			}
		});

		moveLastButton = new Button(rightComposite, SWT.NONE);
		moveLastButton.setText(UtilI18N.MoveLast);
		moveLastButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				moveLast();
			}
		});

		final Composite bottomComposite = new Composite(mainComposite, SWT.NONE);
		final GridLayout gridLayout_2 = new GridLayout();
		gridLayout_2.horizontalSpacing = 3;
		gridLayout_2.marginWidth = 3;
		gridLayout_2.marginHeight = 3;
		gridLayout_2.numColumns = 2;
		gridLayout_2.makeColumnsEqualWidth = true;
		bottomComposite.setLayout(gridLayout_2);
		final GridData gd_bottomComposite = new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1);
		bottomComposite.setLayoutData(gd_bottomComposite);

		bookButton = new Button(bottomComposite, SWT.NONE);
		bookButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		bookButton.setText(I18N.WaitListEditor_BookButton);
		bookButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				book();
			}
		});

		cancelButton = new Button(bottomComposite, SWT.NONE);
		cancelButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		cancelButton.setText(I18N.WaitListEditor_CancelButton);
		cancelButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				cancel();
			}
		});

		// initial enable-state
		moveFirstButton.setEnabled(false);
		moveUpButton.setEnabled(false);
		moveDownButton.setEnabled(false);
		moveLastButton.setEnabled(false);

		// dynamical enable-state
		waitListTable.getViewer().addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				setEnableButtons();
			}
		});

		setEntity(waitList);

		setEnableButtons();
	}


	@Override
	public void dispose() {
		try {
			programmePointModel.removeListener(this);
		}
		catch (Exception e) {
			com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
		}

		if (waitListModel != null && programmePointPK != null) {
			try {
				waitListModel.removeForeignKeyListener(this, programmePointPK);
			}
			catch (Exception e) {
				com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
			}
		}

		super.dispose();
	}


	@Override
	public void doSave(IProgressMonitor monitor) {
		try {
			monitor.beginTask(de.regasus.core.ui.CoreI18N.Saving, 2);

			List<Long> programmeBookingPKs = waitList.getProgrammeBookingPKs();

			waitListModel.updateWaitList(programmePointPK, programmeBookingPKs);

			monitor.worked(1);

			refresh();

			monitor.done();
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}

	}


	@Override
	public String getName() {
		return NLS.bind(I18N.WaitlistEditorInput_Name, programmePointVO.getName().getString());
	}


	@Override
	protected String getToolTipText() {
		String toolTip = null;
		try {
			Long eventPK = programmePointVO.getEventPK();
			EventVO eventVO = EventModel.getInstance().getEventVO(eventPK);

			StringBuffer toolTipText = new StringBuffer();
			toolTipText.append(I18N.WaitListEditor_DefaultToolTip);

			toolTipText.append('\n');
			toolTipText.append(ParticipantLabel.ProgrammePoint.getString());
			toolTipText.append(": ");
			toolTipText.append(programmePointVO.getName().getString());

			toolTipText.append('\n');
			toolTipText.append(ParticipantLabel.Event.getString());
			toolTipText.append(": ");
			toolTipText.append(eventVO.getMnemonic());

			toolTip = toolTipText.toString();
		}
		catch (Exception e) {
			// This shouldn't happen
			com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
			toolTip = "";
		}
		return toolTip;
	}


	/**
	 * We cannot "create" new waitlists via a command, so this is always false
	 *
	 * @see de.regasus.core.ui.editor.IRefreshableEditorPart#isNew()
	 */
	@Override
	public boolean isNew() {
		return false;
	}


	@Override
	public void refresh() throws Exception {
		WaitList oldWaitList = waitList;
		waitListModel.refreshForeignKey(programmePointPK);


		/* Reload data if the editor is still dirty.
		 * The models only fire events if the data really changed (isSameVersion()).
		 * So if the data has not changed on the server the editor receives no CacheModelEvent
		 * and is still dirty.
		 */
		if (isDirty() || oldWaitList == waitList) {
			waitList = waitListModel.getWaitList(programmePointPK);
			if (waitList != null) {
				setEntity(waitList);
			}
			else {
				closeBecauseDeletion();
			}
		}
	}


	@Override
	public Long getEventId() {
		if (programmePointVO != null) {
			return programmePointVO.getEventPK();
		}
		else {
			return null;
		}
	}


	protected void setEntity(WaitList waitList) {
		// clone data to avoid impact to cache if save operation fails (see MIRCP-409)
		this.waitList = waitList.clone();

		syncWidgetsToEntity();
	}


	private void syncWidgetsToEntity() {
		syncExecInParentDisplay(new Runnable() {
			@Override
			public void run() {
				try {
					// set editor title
					setPartName(getName());
					firePropertyChange(PROP_TITLE);

					// refresh the EditorInput
					editorInput.setName(getName());
					editorInput.setToolTipText(getToolTipText());


					waitListTable.getViewer().setInput(waitList.getProgrammeBookingCVOs());
					waitListTable.getViewer().refresh();

					// signal that editor has no unsaved data anymore
					setDirty(false);
				}
				catch (Exception e) {
					RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
				}
			}
		});
	}


	// *************************************************************************
	// * Model event handling
	// *

	@Override
	public void dataChange(CacheModelEvent<Long> event) {
		try {
			if (event.getSource() == programmePointModel) {
				if (event.getOperation() == CacheModelOperation.UPDATE) {
					programmePointVO = programmePointModel.getProgrammePointVO(programmePointPK);
				}
				else if (event.getOperation() == CacheModelOperation.DELETE) {
					closeBecauseDeletion();
				}
			}
			else if (event.getSource() == waitListModel) {
				if (waitListTable != null) {
					waitList = waitListModel.getWaitList(programmePointPK);
					if (waitList != null) {
						setEntity(waitList);
					}
					else if (ServerModel.getInstance().isLoggedIn()) {
						closeBecauseDeletion();
					}
				}
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}

	// *
	// * Model event handling
	// *************************************************************************

	// *************************************************************************
	// * Event handling when one of the buttons is pressed
	// *

	private void book() {
		List<ProgrammeBookingCVO> list = getSelection();
		if (list.isEmpty()) {
			return;
		}

		String message = I18N.WaitListEditor_BookConfirm;
		if (isDirty()) {
			message += "\n"+I18N.WaitListEditor_BookConfirmAlsoSave;
		}

		// open Question Dialog
		boolean bookOK = MessageDialog.openQuestion(
			getSite().getWorkbenchWindow().getShell(),
			I18N.WaitListEditor_BookConfirmDialogTitle,
			message
		);

		// abort if user answered 'No'
		if (!bookOK) {
			return;
		}

		// Collect PKs of ProgrameBookings and Recipients
		final List<Long> programmeBookingPKs = new ArrayList<>(list.size());
		final Set<Long> recipientPKs = CollectionsHelper.createHashSet(list.size() * 2);
		for (ProgrammeBookingCVO programmeBookingCVO : list) {
			ProgrammeBookingVO programmeBookingVO = programmeBookingCVO.getProgrammeBookingVO();
			programmeBookingPKs.add(programmeBookingVO.getID());
			recipientPKs.add(programmeBookingVO.getInvoiceRecipientPK());
			recipientPKs.add(programmeBookingVO.getBenefitRecipientPK());
		}

		final List<ServerMessage> srvMsgList = CollectionsHelper.createArrayList();
		// do communicaton with server with hourglass
		try {
			BusyCursorHelper.busyCursorWhile(new IRunnableWithProgress() {

				@Override
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					// if the editor is dirty, save first to preserve the order
					if (isDirty()) {
						doSave(monitor);
					}
					if (monitor.isCanceled()) {
						return;
					}

					try {
						// book the selected waitList-Bookings
						List<ServerMessage> bookWaitListResult = waitListModel.bookWaitList(programmeBookingPKs);
						if (bookWaitListResult != null) {
							srvMsgList.addAll(bookWaitListResult);
						}
					}
					catch (Exception e) {
						RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
					}
				}
			});
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}


		// if necessary, show Server-Messages
		if (!srvMsgList.isEmpty()) {
    		ServerMessageDialog.open(
    			getSite().getShell(),
    			I18N.WaitListEditor_ServerMessageAfterBookingDialogTitle,
    			srvMsgList
    		);
		}
	}


	private void cancel() {
		List<ProgrammeBookingCVO> selectedProgrammeBookingCVOs = getSelection();
		if (!selectedProgrammeBookingCVOs.isEmpty()) {
			// remove from waitList
			List<ProgrammeBookingCVO> programmeBookingCVOs = waitList.getProgrammeBookingCVOs();

			programmeBookingCVOs.removeAll(selectedProgrammeBookingCVOs);

			waitListTable.getViewer().refresh(true);

			// Signalisieren, dass Editor ungespeicherte Daten enthält
			setDirty(true);
			setEnableButtons();
		}
	}


	private void updateWaitPositionValues() {
		// update values of waitPosition
		int pos = 1;
		for (ProgrammeBookingCVO programmeBookingCVO : waitList.getProgrammeBookingCVOs()) {
			programmeBookingCVO.getProgrammeBookingVO().setWaitPosition(pos++);
		}
	}


	protected void moveFirst() {
		List<ProgrammeBookingCVO> selectedProgrammeBookingCVOs = getSelection();
		if (!selectedProgrammeBookingCVOs.isEmpty()) {
			List<ProgrammeBookingCVO> programmeBookingCVOs = waitList.getProgrammeBookingCVOs();

			programmeBookingCVOs.removeAll(selectedProgrammeBookingCVOs);
			programmeBookingCVOs.addAll(0, selectedProgrammeBookingCVOs);

			updateWaitPositionValues();

			waitListTable.getViewer().refresh(true);

			// Signalisieren, dass Editor ungespeicherte Daten enthält
			setDirty(true);
			setEnableButtons();
		}
	}


	protected void moveUp() {
		List<ProgrammeBookingCVO> selectedProgrammeBookingCVOs = getSelection();
		if (!selectedProgrammeBookingCVOs.isEmpty()) {
			for (ProgrammeBookingCVO programmeBookingCVO : selectedProgrammeBookingCVOs) {
				List<ProgrammeBookingCVO> programmeBookingCVOs = waitList.getProgrammeBookingCVOs();

				int pos = programmeBookingCVOs.indexOf(programmeBookingCVO);
				if (pos == 0) {
					return;
				}
				ProgrammeBookingCVO pre = programmeBookingCVOs.get(pos - 1);
				programmeBookingCVOs.set(pos, pre);
				programmeBookingCVOs.set(pos - 1, programmeBookingCVO);
			}

			updateWaitPositionValues();

			waitListTable.getViewer().refresh(true);

			// Signalisieren, dass Editor ungespeicherte Daten enthält
			setDirty(true);
			setEnableButtons();
		}
	}


	protected void moveDown() {
		List<ProgrammeBookingCVO> selectedProgrammeBookingCVOs = getSelection();
		if (!selectedProgrammeBookingCVOs.isEmpty()) {
			List<ProgrammeBookingCVO> programmeBookingCVOs = waitList.getProgrammeBookingCVOs();

			ProgrammeBookingCVO last = selectedProgrammeBookingCVOs.get(selectedProgrammeBookingCVOs.size() - 1);
			int pos = programmeBookingCVOs.indexOf(last);
			if (pos == programmeBookingCVOs.size() - 1) {
				return;
			}

			for (int i = selectedProgrammeBookingCVOs.size() - 1; i >= 0; i--) {
				ProgrammeBookingCVO current = selectedProgrammeBookingCVOs.get(i);
				pos = programmeBookingCVOs.indexOf(current);
				ProgrammeBookingCVO post = programmeBookingCVOs.get(pos + 1);
				programmeBookingCVOs.set(pos, post);
				programmeBookingCVOs.set(pos + 1, current);
			}

			updateWaitPositionValues();

			waitListTable.getViewer().refresh(true);

			// Signalisieren, dass Editor ungespeicherte Daten enthält
			setDirty(true);
			setEnableButtons();
		}
	}


	/**
	 * All selected bookings are taken out and added again at the end
	 */
	protected void moveLast() {
		List<ProgrammeBookingCVO> selectedProgrammeBookingCVOs = getSelection();

		if (!selectedProgrammeBookingCVOs.isEmpty()) {
			List<ProgrammeBookingCVO> programmeBookingCVOs = waitList.getProgrammeBookingCVOs();

			programmeBookingCVOs.removeAll(selectedProgrammeBookingCVOs);
			programmeBookingCVOs.addAll(selectedProgrammeBookingCVOs);

			updateWaitPositionValues();

			waitListTable.getViewer().refresh(true);

			// Signalisieren, dass Editor ungespeicherte Daten enthält
			setDirty(true);
			setEnableButtons();
		}
	}

	// *
	// * Event handling when one of the buttons is pressed
	// *************************************************************************

	// *************************************************************************
	// * Internal helper methods
	// *

	private List<ProgrammeBookingCVO> getSelection() {
		return SelectionHelper.toList(waitListTable.getViewer().getSelection(), ProgrammeBookingCVO.class);
	}


	private void setEnableButtons() {
		boolean moveFirstEnabled = false;
		boolean moveUpEnabled = false;
		boolean moveDownEnabled = false;
		boolean moveLastEnabled = false;

		List<ProgrammeBookingCVO> selectedBookings = getSelection();
		boolean selectionEmpty = selectedBookings.isEmpty();

		// Booking and cancelling is enabled if and only if there is something selected
		bookButton.setEnabled(!selectionEmpty);
		cancelButton.setEnabled(!selectionEmpty);

		if (!selectionEmpty) {
			List<ProgrammeBookingCVO> programmeBookingCVOs = waitList.getProgrammeBookingCVOs();

			// When first selected is not the one in the first row we can move up
			ProgrammeBookingCVO firstSelected = selectedBookings.get(0);
			moveUpEnabled = !programmeBookingCVOs.get(0).equals(firstSelected);

			// When last selected is not the one in last row we can move down
			ProgrammeBookingCVO lastSelected = selectedBookings.get(selectedBookings.size() - 1);
			moveDownEnabled = !programmeBookingCVOs.get(programmeBookingCVOs.size() - 1).equals(lastSelected);

			// When the last of the n selected is after the nth position,
			// there must be a gap above, and we can move first
			if (programmeBookingCVOs.indexOf(lastSelected) >= selectedBookings.size()) {
				moveFirstEnabled = true;
			}

			// When the first of the n selected is not at the size-nth position,
			// there must be a gap below, and we can move down
			if (programmeBookingCVOs.indexOf(firstSelected) < programmeBookingCVOs.size() - selectedBookings.size()) {
				moveLastEnabled = true;
			}
		}

		moveFirstButton.setEnabled(moveFirstEnabled);
		moveLastButton.setEnabled(moveLastEnabled);
		moveUpButton.setEnabled(moveUpEnabled);
		moveDownButton.setEnabled(moveDownEnabled);
	}

}
