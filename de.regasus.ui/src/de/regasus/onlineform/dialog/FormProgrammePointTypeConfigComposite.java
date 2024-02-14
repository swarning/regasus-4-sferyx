package de.regasus.onlineform.dialog;

import java.util.List;

import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import com.lambdalogic.messeinfo.participant.data.FormProgrammePointTypeConfigVO;
import com.lambdalogic.util.rcp.SelectionHelper;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import com.lambdalogic.util.rcp.UtilI18N;
import de.regasus.onlineform.OnlineFormI18N;

public class FormProgrammePointTypeConfigComposite extends Composite {

	private FormProgrammePointTypeConfigTable formProgrammePointTypeConfigTable;

	private Button moveFirstButton;
	private Button moveUpButton;
	private Button moveDownButton;
	private Button moveLastButton;

	private List<FormProgrammePointTypeConfigVO> configVOs;

	private Button useProgrammePointTypeNamesAsHeadersButton;

	
	
	public FormProgrammePointTypeConfigComposite(Composite parent, int style) {
		super(parent, style);
		
		this.setLayout(new GridLayout(3, false));
		
		Composite tableComposite = new Composite(this, SWT.BORDER);
		tableComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));

		TableColumnLayout tableColumnLayout = new TableColumnLayout();
		tableComposite.setLayout(tableColumnLayout);
		Table table = new Table(tableComposite, SWT.MULTI | SWT.FULL_SELECTION);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		// Name
		final TableColumn nameTableColumn = new TableColumn(table, SWT.LEFT);
		tableColumnLayout.setColumnData(nameTableColumn, new ColumnWeightData(20));
		nameTableColumn.setText(UtilI18N.Name);

		// Single
		final TableColumn singleTableColumn = new TableColumn(table, SWT.CENTER);
		tableColumnLayout.setColumnData(singleTableColumn, new ColumnWeightData(20));
		singleTableColumn.setText(OnlineFormI18N.SingleBooking);

		// Required
		final TableColumn requiredTableColumn = new TableColumn(table, SWT.CENTER);
		tableColumnLayout.setColumnData(requiredTableColumn, new ColumnWeightData(20));
		requiredTableColumn.setText(OnlineFormI18N.RequiredBooking);

		// MaxBookingCount
		final TableColumn maxBookingCountTableColumn = new TableColumn(table, SWT.RIGHT);
		tableColumnLayout.setColumnData(maxBookingCountTableColumn, new ColumnWeightData(10));
		maxBookingCountTableColumn.setText(OnlineFormI18N.MaxBookingCount);

		// Text Header
		final TableColumn textHeaderTableColumn = new TableColumn(table, SWT.LEFT);
		tableColumnLayout.setColumnData(textHeaderTableColumn, new ColumnWeightData(20));
		textHeaderTableColumn.setText(OnlineFormI18N.TextHeader);

		// Text Subtotal Line
		final TableColumn textSubtotalLineTableColumn = new TableColumn(table, SWT.LEFT);
		tableColumnLayout.setColumnData(textSubtotalLineTableColumn, new ColumnWeightData(20));
		textSubtotalLineTableColumn.setText(OnlineFormI18N.TextSubtotalLine);

		formProgrammePointTypeConfigTable = new FormProgrammePointTypeConfigTable(table);
		formProgrammePointTypeConfigTable.getViewer().addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				updateButtonStates();
			}
		});
		
		
		Composite composite = new Composite(this, SWT.NONE);
		composite.setLayout(new GridLayout(1, false));
		
		moveFirstButton = new Button(composite, SWT.NONE);
		moveFirstButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		moveFirstButton.setText(UtilI18N.MoveFirst);
		moveFirstButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				moveFirst();
			}
		});
		moveUpButton = new Button(composite, SWT.NONE);
		moveUpButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		moveUpButton.setText(UtilI18N.MoveUp);
		moveUpButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				moveUp();
			}
		});
		moveDownButton = new Button(composite, SWT.NONE);
		moveDownButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		moveDownButton.setText(UtilI18N.MoveDown);
		moveDownButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				moveDown();
			}
		});
		moveLastButton = new Button(composite, SWT.NONE);
		moveLastButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		moveLastButton.setText(UtilI18N.MoveLast);
		moveLastButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				moveLast();
			}
		});
		
		
		// Second row
		SWTHelper.createLabel(this, OnlineFormI18N.UseProgrammePointTypeNamesAsHeaders);
		useProgrammePointTypeNamesAsHeadersButton = new Button(this, SWT.CHECK);

		updateButtonStates();
	}

	

	protected void moveUp() {
		List<FormProgrammePointTypeConfigVO> selectedProgrammeBookingCVOs = SelectionHelper.getSelection(formProgrammePointTypeConfigTable.getViewer(), FormProgrammePointTypeConfigVO.class);
		if (!selectedProgrammeBookingCVOs.isEmpty()) {
			for (FormProgrammePointTypeConfigVO participantTypeVO : selectedProgrammeBookingCVOs) {
				int pos = configVOs.indexOf(participantTypeVO);
				if (pos == 0) {
					return;
				}
				FormProgrammePointTypeConfigVO pre = configVOs.get(pos - 1);
				configVOs.set(pos, pre);
				configVOs.set(pos - 1, participantTypeVO);
			}

			formProgrammePointTypeConfigTable.getViewer().refresh(true);

			// Signalisieren, dass Editor ungespeicherte Daten enthält
			updateButtonStates();
		}
	}


	protected void moveDown() {
		List<FormProgrammePointTypeConfigVO> selectedProgrammeBookingCVOs = SelectionHelper.getSelection(formProgrammePointTypeConfigTable.getViewer(), FormProgrammePointTypeConfigVO.class);
		if (!selectedProgrammeBookingCVOs.isEmpty()) {
			FormProgrammePointTypeConfigVO last = selectedProgrammeBookingCVOs.get(selectedProgrammeBookingCVOs.size() - 1);
			int pos = configVOs.indexOf(last);
			if (pos == configVOs.size() - 1) {
				return;
			}

			for (int i = selectedProgrammeBookingCVOs.size() - 1; i >= 0; i--) {
				FormProgrammePointTypeConfigVO current = selectedProgrammeBookingCVOs.get(i);
				pos = configVOs.indexOf(current);
				FormProgrammePointTypeConfigVO post = configVOs.get(pos + 1);
				configVOs.set(pos, post);
				configVOs.set(pos + 1, current);
			}

			formProgrammePointTypeConfigTable.getViewer().refresh(true);

			// Signalisieren, dass Editor ungespeicherte Daten enthält
			updateButtonStates();
		}
	}


	/**
	 * All selected bookings are taken out and added again at the end
	 */
	protected void moveLast() {
		List<FormProgrammePointTypeConfigVO> selectedProgrammeBookingCVOs = SelectionHelper.getSelection(formProgrammePointTypeConfigTable.getViewer(), FormProgrammePointTypeConfigVO.class);

		if (!selectedProgrammeBookingCVOs.isEmpty()) {
			configVOs.removeAll(selectedProgrammeBookingCVOs);
			configVOs.addAll(selectedProgrammeBookingCVOs);

			formProgrammePointTypeConfigTable.getViewer().refresh(true);

			// Signalisieren, dass Editor ungespeicherte Daten enthält
			updateButtonStates();

		}
	}

	public void setConfigVOs(List<FormProgrammePointTypeConfigVO> configVOs) {
		formProgrammePointTypeConfigTable.setInput(configVOs);
		this.configVOs = configVOs;
		
		
	}

	
	protected void moveFirst() {
		
		
		List<FormProgrammePointTypeConfigVO> selection = SelectionHelper.getSelection(formProgrammePointTypeConfigTable.getViewer(), FormProgrammePointTypeConfigVO.class);
		if (!selection.isEmpty()) {

			configVOs.removeAll(selection);
			configVOs.addAll(0, selection);

			formProgrammePointTypeConfigTable.getViewer().refresh(true);

			// Signalisieren, dass Editor ungespeicherte Daten enthält
			updateButtonStates();
		}
	}


	private void updateButtonStates() {
		boolean moveFirstEnabled = false;
		boolean moveUpEnabled = false;
		boolean moveDownEnabled = false;
		boolean moveLastEnabled = false;

		List<FormProgrammePointTypeConfigVO> selection = SelectionHelper.getSelection(formProgrammePointTypeConfigTable.getViewer(), FormProgrammePointTypeConfigVO.class);
		boolean selectionEmpty = selection.isEmpty();

		if (!selectionEmpty) {
			// When first selected is not the one in the first row we can move up
			FormProgrammePointTypeConfigVO firstSelected = selection.get(0);
			moveUpEnabled = !configVOs.get(0).equals(firstSelected);

			// When last selected is not the one in last row we can move down
			FormProgrammePointTypeConfigVO lastSelected = selection.get(selection.size() - 1);
			moveDownEnabled = !configVOs.get(configVOs.size() - 1).equals(lastSelected);

			// When the last of the n selected is after the nth position,
			// there must be a gap above, and we can move first
			if (configVOs.indexOf(lastSelected) >= selection.size()) {
				moveFirstEnabled = true;
			}

			// When the first of the n selected is not at the size-nth position,
			// there must be a gap below, and we can move down
			if (configVOs.indexOf(firstSelected) < configVOs.size() - selection.size()) {
				moveLastEnabled = true;
			}
		}

		moveFirstButton.setEnabled(moveFirstEnabled);
		moveLastButton.setEnabled(moveLastEnabled);
		moveUpButton.setEnabled(moveUpEnabled);
		moveDownButton.setEnabled(moveDownEnabled);
	}



	public boolean isUseProgrammePointTypeNamesAsHeaders() {
		return useProgrammePointTypeNamesAsHeadersButton.getSelection();
	}



	public void setUseProgrammePointTypeNamesAsHeaders(boolean useProgrammePointTypeNamesAsHeaders) {
		useProgrammePointTypeNamesAsHeadersButton.setSelection(useProgrammePointTypeNamesAsHeaders);
		
	}


	
}
