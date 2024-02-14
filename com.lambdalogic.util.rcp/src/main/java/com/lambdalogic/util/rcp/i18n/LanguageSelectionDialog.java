package com.lambdalogic.util.rcp.i18n;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import com.lambdalogic.i18n.DefaultLanguageProvider;
import com.lambdalogic.i18n.ILanguageProvider;
import com.lambdalogic.i18n.Language;
import com.lambdalogic.i18n.Language_Code_Comparator;
import com.lambdalogic.i18n.Language_Name_Comparator;
import com.lambdalogic.util.rcp.Activator;
import com.lambdalogic.util.rcp.Images;
import com.lambdalogic.util.rcp.UtilI18N;

/**
 * A dialog that shows all languages in a CheckboxTableViewer. Those languages that are present as tabs (and given in
 * the constructor) are checked. There is a button that filters out all but common languages of our customers.
 */
public class LanguageSelectionDialog extends TitleAreaDialog implements SelectionListener {

	private ILanguageProvider languageProvider;

	/**
	 * A filter that is used to show optionally only the most common languages.
	 */
	private CommonLanguagesFilter commonLanguagesFilter = new CommonLanguagesFilter();

	/**
	 * A JFace-Viewer that shows the in each row a checkbox, a language code and the language name.
	 */
	private CheckboxTableViewer languageTableViewer;

	/**
	 * A Button that is used to activate or deactivate the {@link #commonLanguagesFilter}, in order to show more or less
	 * buttons.
	 */
	private Button moreLessButton;

	/**
	 * The list of languages which are initially to be checked
	 */
	private Language[] tabFolderLanguageItems;

	private Table table;

	private List<Language> languageList;

	private TableColumn nameTableColumn;

	private TableColumn codeTableColumn;


	/*
	 * ================================================================ Constructor
	 */

	public LanguageSelectionDialog(
		Shell parentShell,
		ILanguageProvider languageProvider,
		List<Language> languageList
	) {
		super(parentShell);

		if (languageProvider == null) {
			languageProvider = DefaultLanguageProvider.getInstance();
		}
		this.languageProvider = languageProvider;

		if (languageList == null) {
			tabFolderLanguageItems = new Language[0];
		}
		else {
			tabFolderLanguageItems = new Language[languageList.size()];
			tabFolderLanguageItems = languageList.toArray(tabFolderLanguageItems);
		}
	}


	/*
	 * ================================================================ Overriden Methods
	 */

	@Override
	protected Control createDialogArea(Composite parent) {
		setTitle(UtilI18N.Title_Area_Text);

		Composite area = (Composite) super.createDialogArea(parent);

		table = new Table(area, SWT.CHECK | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		table.setHeaderVisible(true);
		table.setLayoutData(new GridData(GridData.FILL_BOTH));

		final TableColumn checkTableColumn = new TableColumn(table, SWT.RIGHT, 0);

		codeTableColumn = new TableColumn(table, SWT.LEFT, 1);
		codeTableColumn.setText(UtilI18N.Code_Table_Header);

		nameTableColumn = new TableColumn(table, SWT.LEFT, 2);
		nameTableColumn.setText(UtilI18N.Language_Table_Header);

		languageList = new ArrayList<Language>(languageProvider.getLanguageList());
		Collections.sort(languageList, Language_Name_Comparator.getInstance());

		languageTableViewer = new CheckboxTableViewer(table);
		languageTableViewer.setContentProvider(new ArrayContentProvider());
		languageTableViewer.setLabelProvider(new LanguageSelectionLabelProvider());
		languageTableViewer.setInput(languageList);

		languageTableViewer.setCheckedElements(tabFolderLanguageItems);
		languageTableViewer.setFilters(new ViewerFilter[] { commonLanguagesFilter });

		nameTableColumn.addSelectionListener(this);
		codeTableColumn.addSelectionListener(this);
		table.setSortColumn(nameTableColumn);
		table.setSortDirection(SWT.UP);

		checkTableColumn.pack();
		codeTableColumn.setWidth(70);
		nameTableColumn.setWidth(350);

		table.setLayoutData(new GridData(GridData.FILL_BOTH));
		return area;
	}


	/**
	 * When the dialog is about to be closed with OK, we store the checked languageItems in the initial array, so that
	 * the caller can retrieve it from there even after the widget ist disposed.
	 */
	@Override
	protected void okPressed() {
		Object[] checkedElements = languageTableViewer.getCheckedElements();

		// Do NOT cast the array, it will lead to a ClassCastException at runtime. Cast each element instead.
		tabFolderLanguageItems = new Language[checkedElements.length];
		for (int i = 0; i < checkedElements.length; i++) {
			tabFolderLanguageItems[i] = (Language) checkedElements[i];
		}

		super.okPressed();
	}


	/**
	 * The Window Icon and Title are made identical to the icon and the tooltip of the button that opens this dialog.
	 */
	@Override
	protected void configureShell(Shell shell) {
		super.configureShell(shell);

		shell.setText(UtilI18N.SelectLanguages_ToolTip);
		shell.setImage(Activator.getDefault().getImageRegistry().get(Images.LANGUAGES));
	}


	/**
	 * Create the three buttons of the button bar, one to switch the filter, and the common OK and CANCEL buttons.
	 *
	 * @param parent
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {

		// The button to switch the filter
		moreLessButton = createButton(parent, IDialogConstants.CLIENT_ID, UtilI18N.More_Button, false);
		moreLessButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					commonLanguagesFilter.setActive(!commonLanguagesFilter.isActive());
					refresh();
				}
				catch (Exception e1) {
					com.lambdalogic.util.rcp.error.ErrorHandler.logError(e1);
				}
			}
		});

		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}


	/**
	 * Return the initial size of the dialog
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(400, 605);
	}


	/**
	 * This method is never called
	 */
	@Override
	public void widgetDefaultSelected(SelectionEvent e) {
	}


	/**
	 * After a click in one of the headers of the code column or name column, the language
	 * list is sorted with an according comparator, in a DOWN direction if a new column is
	 * clicked, or in the reverse direction of the previous sort direction.
	 */
	@Override
	public void widgetSelected(SelectionEvent e) {
		TableColumn column = (TableColumn) e.widget;

		// Check if the click was in a column which means we don't need to do anything
		if (column != nameTableColumn && column != codeTableColumn) {
			return;
		}

		// Find out the new direction, depending of previous direction and previous sorted column.
		final int direction;
		boolean differentColumn = table.getSortColumn() != column;
		int currentDirection = table.getSortDirection();
		if (differentColumn || currentDirection == SWT.DOWN) {
			direction = SWT.UP;
		}
		else {
			direction = SWT.DOWN;
		}

		// Now sort with a particular Comparator, depending on the column
		Comparator<Language> comparator = null;
		if (column == nameTableColumn) {
			comparator = Language_Name_Comparator.getInstance();
		}
		else if (column == codeTableColumn) {
			comparator = Language_Code_Comparator.getInstance();
		}

		if (direction == SWT.DOWN) {
			comparator = comparator.reversed();
		}

		Collections.sort(languageList, comparator);

		// Sorting done, make the UI reflect the current state
		table.setSortColumn(column);
		table.setSortDirection(direction);
		languageTableViewer.refresh();
	}


	/*
	 * ================================================================ Getter and Setter
	 */

	public Language[] getTabFolderLanguageItems() {
		return tabFolderLanguageItems;
	}


	/*
	 * ================================================================ Private Helper Methods
	 */

	/**
	 * After a possible filter switch, the button text and the visible contents of the CheckboxTableViewer need to be
	 * updated.
	 *
	 * The method is also used when the dialog is initially opened, to have only one algorithm for setting the button
	 * text.
	 */
	private void refresh() {
		languageTableViewer.refresh();
		if (commonLanguagesFilter.isActive()) {
			moreLessButton.setText(UtilI18N.More_Button);

		}
		else {
			moreLessButton.setText(UtilI18N.Less_Button);
		}

	}

}
