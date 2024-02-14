package de.regasus.users.common;

import java.lang.invoke.MethodHandles;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lambdalogic.messeinfo.account.AccountLabel;
import com.lambdalogic.messeinfo.account.data.AccessControlEntryCVO;
import com.lambdalogic.messeinfo.account.data.AccessControlEntryVO;
import com.lambdalogic.messeinfo.kernel.data.AbstractCvoComparator;
import com.lambdalogic.util.rcp.SelectionHelper;
import com.lambdalogic.util.rcp.UtilI18N;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.dialog.StringFilterDialog;
import de.regasus.core.ui.editor.AbstractEditor;
import de.regasus.users.UsersAdministrationHelper;
import de.regasus.users.UsersI18N;
import de.regasus.users.ui.Activator;
import de.regasus.users.user.dialog.AddRightWizard;

public class AccessControlEntryTableComposite extends Group {

	private static final Logger log = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );

	private TableViewer tableViewer;

	private AccessControlEntryTable accessControlEntryTable;

	private Button addRightButton;

	private Button removeRightButton;

	private Button filterActiveToggleButton;

	private Button filterSettingsButton;

	private String owner;

	private AccessControlRightTypeViewerFilter viewerFilter = new AccessControlRightTypeViewerFilter();

	private List<AccessControlEntryCVO> accessControlEntryCVOs;

	private String[] checkedRightTypeLabels;

	private AbstractEditor<?> editor;


	public AccessControlEntryTableComposite(Composite parent, int style) {
		super(parent, style);

		setText(AccountLabel.Rights.getString());

		setLayout(new GridLayout());

		// The table
		Composite tableComposite = new Composite(this, SWT.NONE);
		tableComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		TableColumnLayout layout = new TableColumnLayout();
		tableComposite.setLayout(layout);
		Table table = new Table(tableComposite, SWT.SINGLE | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		final TableColumn typeTableColumn = new TableColumn(table, SWT.NONE);
		layout.setColumnData(typeTableColumn, new ColumnWeightData(125));
		typeTableColumn.setText(AccountLabel.Type.getString());

		final TableColumn ownerTableColumn = new TableColumn(table, SWT.NONE);
		layout.setColumnData(ownerTableColumn, new ColumnWeightData(100));
		ownerTableColumn.setText(AccountLabel.Owner.getString());

		final TableColumn priorityTableColumn = new TableColumn(table, SWT.RIGHT);
		layout.setColumnData(priorityTableColumn, new ColumnWeightData(50));
		priorityTableColumn.setText(AccountLabel.Priority.getString());

		final TableColumn constraintTypeTableColumn = new TableColumn(table, SWT.NONE);
		layout.setColumnData(constraintTypeTableColumn, new ColumnWeightData(100));
		constraintTypeTableColumn.setText(AccountLabel.ConstraintType.getString());

		final TableColumn constraintTableColumn = new TableColumn(table, SWT.NONE);
		layout.setColumnData(constraintTableColumn, new ColumnWeightData(100));
		constraintTableColumn.setText(AccountLabel.Constraint.getString());

		int widthForSwitchColumns = 60;

		final TableColumn readTableColumn = new TableColumn(table, SWT.NONE);
		layout.setColumnData(readTableColumn, new ColumnWeightData(widthForSwitchColumns));
		readTableColumn.setText(AccountLabel.Read.getString());

		final TableColumn writeTableColumn = new TableColumn(table, SWT.NONE);
		layout.setColumnData(writeTableColumn, new ColumnWeightData(widthForSwitchColumns));
		writeTableColumn.setText(AccountLabel.Write.getString());

		final TableColumn createTableColumn = new TableColumn(table, SWT.NONE);
		layout.setColumnData(createTableColumn, new ColumnWeightData(widthForSwitchColumns));
		createTableColumn.setText(AccountLabel.Create.getString());

		final TableColumn deleteTableColumn = new TableColumn(table, SWT.NONE);
		layout.setColumnData(deleteTableColumn, new ColumnWeightData(widthForSwitchColumns));
		deleteTableColumn.setText(AccountLabel.Delete.getString());

		final TableColumn activeTableColumn = new TableColumn(table, SWT.NONE);
		layout.setColumnData(activeTableColumn, new ColumnWeightData(widthForSwitchColumns));
		activeTableColumn.setText(AccountLabel.Active.getString());

		accessControlEntryTable = new AccessControlEntryTable(table);
		tableViewer = accessControlEntryTable.getViewer();
		tableViewer.addFilter(viewerFilter);

		accessControlEntryTable.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateButtonStates();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				updateButtonStates();
			}
		});


		// The buttons
		Composite buttonComposite = new Composite(this, SWT.NONE);

		buttonComposite.setLayout(new RowLayout(SWT.HORIZONTAL));
		addRightButton = new Button(buttonComposite, SWT.PUSH);
		addRightButton.setText(UsersI18N.AddRight);
		addRightButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				addRight();
			}
		});

		removeRightButton = new Button(buttonComposite, SWT.PUSH);
		removeRightButton.setText(UsersI18N.RemoveRight);
		removeRightButton.setEnabled(false);
		removeRightButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				removeSelectedRight();
			}
		});

		filterActiveToggleButton = new Button(buttonComposite, SWT.TOGGLE);
		filterActiveToggleButton.setText(UtilI18N.Filter);
		filterActiveToggleButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					switchFilterActive();
				}
				catch (Exception e1) {
					RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e1);
				}
			}
		});
//		filterActiveToggleButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));


		filterSettingsButton = new Button(buttonComposite, SWT.PUSH);
		filterSettingsButton.setText(UtilI18N.FilterSettings);
		filterSettingsButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					openFilterDialog();
				}
				catch (Exception e1) {
					RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e1);
				}
			}
		});

	}


	protected void addRight() {
		try {
			AddRightWizard wizard = new AddRightWizard(owner);

			WizardDialog wizardDialog = new WizardDialog(getShell(), wizard);
			wizardDialog.create();
			wizardDialog.getShell().setSize(680, 500);
			int code = wizardDialog.open();
			if (code == Window.OK) {
				AccessControlEntryVO accessControlEntryVO = wizard.getAccessControlEntryVO();

				AccessControlEntryCVO accessControlEntryCVO = new AccessControlEntryCVO();
				accessControlEntryCVO.setVO(accessControlEntryVO);
				accessControlEntryCVOs.add(accessControlEntryCVO);
				setAccessControlEntryCVOs(accessControlEntryCVOs);
				editor.setDirty(true);
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	protected void removeSelectedRight() {
		try {
			ISelection selection = tableViewer.getSelection();
			if (!selection.isEmpty()) {
				AccessControlEntryCVO aceCVO = SelectionHelper.getUniqueSelected(selection);
				if (aceCVO.getVO().getSubject().equals(owner)) {

					// We don't need to ask whether a right is really to be deleted, because
					// a) the Swing client doesn't do it, and b) the action doesn't really
					// delete the right immediately, but only preliminarily in in the dirty editor.

					// remember selection before deletion
					int selectionIndex = tableViewer.getTable().getSelectionIndex();

					accessControlEntryCVOs.remove(aceCVO);
					tableViewer.refresh();
					editor.setDirty(true);

					// select next item
					if (tableViewer.getTable().getItemCount() <= selectionIndex) {
						selectionIndex = tableViewer.getTable().getItemCount() - 1;
					}
					if (selectionIndex >= 0) {
						tableViewer.getTable().setSelection(selectionIndex);
					}
				}
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}

	}


	protected void updateButtonStates() {
		boolean editableACESelected = false;

		ISelection selection = tableViewer.getSelection();
		if (!selection.isEmpty()) {
			AccessControlEntryCVO aceCVO = SelectionHelper.getUniqueSelected(selection);
			if (aceCVO.getVO().getSubject().equals(owner)) {
				editableACESelected = true;
			}
		}
		removeRightButton.setEnabled(editableACESelected);
	}


	public void setAccessControlEntryCVOs(List<AccessControlEntryCVO> accessControlEntryCVOs) {
		Collections.sort(accessControlEntryCVOs, AbstractCvoComparator.getInstance());
		this.accessControlEntryCVOs = accessControlEntryCVOs;
		tableViewer.setInput(accessControlEntryCVOs);
	}


	public void setOwner(String owner) {
		this.owner = owner;
		accessControlEntryTable.setOwner(owner);
	}


	public void setEditor(AbstractEditor<?> editor) {
		this.editor = editor;
		accessControlEntryTable.setEditor(this.editor);
	}


	@Override
	public boolean setFocus() {
		return addRightButton.setFocus();
	}


	@Override
	protected void checkSubclass() {
	}


	protected void openFilterDialog() throws Exception {
		List<String> types = UsersAdministrationHelper.getAllTypesLabels();

		StringFilterDialog stringFilterDialog = new StringFilterDialog(getShell(), types);

		stringFilterDialog.setCheckedStrings(checkedRightTypeLabels);

		int code = stringFilterDialog.open();

		if (code == IDialogConstants.CLIENT_ID + 1) {
			checkedRightTypeLabels = stringFilterDialog.getCheckedStrings();
			filterActiveToggleButton.setSelection(true);
			setVisibleRights(checkedRightTypeLabels);
		}
		else if (code == IDialogConstants.CLIENT_ID + 2) {
			filterActiveToggleButton.setSelection(false);
			setFilterOff();
		}

	}


	public void setFilterOff() {
		viewerFilter.setActive(false);
		tableViewer.refresh();
	}

	public void setVisibleRights(String[] checkedHotelNames) {
		viewerFilter.setCheckedStrings(checkedHotelNames);
		viewerFilter.setActive(true);
		tableViewer.refresh();
	}

	protected void switchFilterActive() {
		boolean on = filterActiveToggleButton.getSelection();
		if (on) {
			if (checkedRightTypeLabels != null) {
				setVisibleRights(checkedRightTypeLabels);
			}
		}
		else {
			setFilterOff();
		}
	}

}
