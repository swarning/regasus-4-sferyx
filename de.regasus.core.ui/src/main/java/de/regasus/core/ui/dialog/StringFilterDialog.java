package de.regasus.core.ui.dialog;

import java.util.List;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import com.lambdalogic.util.rcp.UtilI18N;

public class StringFilterDialog extends TitleAreaDialog {

	private CheckboxTableViewer stringTableViewer;

	private Table table;

//	private TableColumn stringNameColumn;

	private String[] checkedStrings;

	private List<String> allStrings;


	public StringFilterDialog(Shell parentShell, List<String> allStrings) throws Exception {
		super(parentShell);
		
		this.allStrings = allStrings;
	}


	@Override
	protected Control createDialogArea(Composite parent) {
		setTitle(UtilI18N.Filter);

		Composite area = (Composite) super.createDialogArea(parent);
		area.setLayout(new GridLayout(1, true));

		table = new Table(area, SWT.CHECK | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		table.setHeaderVisible(false);
		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		final TableColumn checkTableColumn = new TableColumn(table, SWT.RIGHT, 0);

//		stringNameColumn = new TableColumn(table, SWT.LEFT, 1);

		stringTableViewer = new CheckboxTableViewer(table);
		stringTableViewer.setContentProvider(new ArrayContentProvider());
//		stringTableViewer.setLabelProvider(new StringFilterLabelProvider());
		stringTableViewer.setInput(allStrings);

		checkTableColumn.pack();
//		stringNameColumn.setWidth(350);

		Composite checkBoxComposite = new Composite(area, SWT.NONE);
		checkBoxComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		RowLayout rowLayout = new RowLayout(SWT.HORIZONTAL);
		rowLayout.pack = false;
		checkBoxComposite.setLayout(rowLayout);

		// Checkboxen
		Button allButton = new Button(checkBoxComposite, SWT.PUSH);
		allButton.setText(UtilI18N.All);
		allButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				stringTableViewer.setCheckedElements(allStrings.toArray());
			}
		});

		Button nothingButton = new Button(checkBoxComposite, SWT.PUSH);
		nothingButton.setText(UtilI18N.None);
		nothingButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				stringTableViewer.setCheckedElements(new Object[0]);
			}
		});

		if (checkedStrings != null) {
			stringTableViewer.setCheckedElements(checkedStrings);
		}
		else {
			stringTableViewer.setCheckedElements(allStrings.toArray());
		}
		return area;
	}


	/**
	 * Create the three buttons of the button bar, one to switch the filter, and the common OK and CANCEL buttons.
	 * 
	 * @param parent
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {

		checkedStrings = getCheckedStringsFromViewer();

		createButton(parent, IDialogConstants.CLIENT_ID + 1, UtilI18N.On, true);
		createButton(parent, IDialogConstants.CLIENT_ID + 2, UtilI18N.Off, false);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}


	/**
	 * The Window Icon and Title are made identical to the icon and the tooltip of the button that opens this dialog.
	 */
	@Override
	protected void configureShell(Shell shell) {
		super.configureShell(shell);

		shell.setText(UtilI18N.Filter);
	}

	private String[] getCheckedStringsFromViewer() {
		Object[] checkedElements = stringTableViewer.getCheckedElements();
		String[] checkedStrings = new String[checkedElements.length];
		for (int i = 0; i < checkedStrings.length; i++) {
			checkedStrings[i] = (String) checkedElements[i];
		}
		return checkedStrings;
	}

	@Override
	protected void buttonPressed(int buttonId) {
		checkedStrings = getCheckedStringsFromViewer();

		setReturnCode(buttonId);
		close();
	}

	/**
	 * Return the initial size of the dialog
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(400, 605);
	}

	public void setCheckedStrings(String[] checkedStrings) {
		this.checkedStrings = checkedStrings;
	}

	public String[] getCheckedStrings() {
		return checkedStrings;
	}
}