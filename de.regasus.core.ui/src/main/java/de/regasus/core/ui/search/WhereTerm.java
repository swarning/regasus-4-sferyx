package de.regasus.core.ui.search;

import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;

import com.lambdalogic.i18n.I18NCollator;
import com.lambdalogic.messeinfo.exception.InvalidValuesException;
import com.lambdalogic.messeinfo.kernel.interfaces.SQLField;
import com.lambdalogic.messeinfo.kernel.interfaces.SQLOperator;
import com.lambdalogic.messeinfo.kernel.sql.SQLParameter;
import com.lambdalogic.util.SystemHelper;
import com.lambdalogic.util.rcp.SelectionHelper;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.Activator;
import de.regasus.core.ui.CoreI18N;

/**
 * Collection of the Widgets of one Where-Parameter. The Widgets' parent is a {@link WhereComposite}
 *
 * @author sacha
 */
public class WhereTerm implements SelectionListener {

	private final WhereComposite whereComposite;

	// Widgets
	private Button addButton;
	private Button removeButton;
	private ComboViewer fieldComboViewer;
	private Combo fieldCombo;
	private ComboViewer operatorComboViewer;
	private Combo operatorCombo;
	private ParameterComposite valueWidget;
	private Button activeButton;


	public WhereTerm(WhereComposite parent, SQLParameter sqlParameter) {
		this.whereComposite = parent;
		createControls();
		setSqlParameter(sqlParameter);
	}


	private void createControls() {
		addButton = new Button(whereComposite, SWT.NONE);
		final GridData gd_addButton1 = new GridData(SWT.FILL, SWT.CENTER, false, false);
		if (SystemHelper.isMacOSX()) {
			gd_addButton1.widthHint = 40;
		}
		else {
			gd_addButton1.widthHint = 30;
		}
		addButton.setLayoutData(gd_addButton1);
		addButton.setText("+"); 
		addButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				whereComposite.addWhereTerm();
			}
		});

		removeButton = new Button(whereComposite, SWT.NONE);
		final GridData gd_removeButton1 = new GridData(SWT.FILL, SWT.CENTER, false, false);
		if (SystemHelper.isMacOSX()) {
			gd_removeButton1.widthHint = 40;
		}
		else {
			gd_removeButton1.widthHint = 30;
		}
		removeButton.setLayoutData(gd_removeButton1);
		removeButton.setText("-"); 
		removeButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				whereComposite.removeWhereTerm(WhereTerm.this);
			}
		});

		fieldComboViewer = new ComboViewer(whereComposite, SWT.READ_ONLY);
		fieldCombo = fieldComboViewer.getCombo();
		final GridData gd_fieldCombo = new GridData(SWT.FILL, SWT.CENTER, true, false);
		gd_fieldCombo.widthHint = 60;
		fieldCombo.setLayoutData(gd_fieldCombo);
		fieldCombo.setVisibleItemCount(12);

		fieldComboViewer.setContentProvider(new ArrayContentProvider());
		fieldComboViewer.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				return ((SQLField) element).getLabel();
			}
		});
		fieldComboViewer.addPostSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection selection = (IStructuredSelection) event.getSelection();
				if (selection.size() == 1) {
					SQLField currentSQLField = (SQLField) selection.getFirstElement();

					// set SQL-Operators
					setSqlOperators(currentSQLField.getSQLOperators());
					SQLOperator defaultSqlOperator = currentSQLField.getDefaultSQLOperator();
					operatorComboViewer.setSelection(new StructuredSelection(defaultSqlOperator), true);

					// set Value (Type)
					Class<?> parameterClass = currentSQLField.getParameterType();

					Map<?, ?> parameterMap;
					try {
						parameterMap = currentSQLField.getValues();
					}
					catch (Exception e) {
						RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
						parameterMap = new HashMap();
					}
					valueWidget.setValueType(parameterClass, parameterMap);

					whereComposite.refreshLayout();

					fieldCombo.setToolTipText(fieldCombo.getText());
				}
			}
		});
		fieldComboViewer.setSorter(new ViewerSorter(new I18NCollator(Locale.getDefault())));

		operatorComboViewer = new ComboViewer(whereComposite, SWT.READ_ONLY);
		operatorCombo = operatorComboViewer.getCombo();

		// ToolTip for Operator-Combo
		String tooltip = CoreI18N.WhereTerm_OperatorToolTip;
		tooltip = tooltip.replaceFirst("<EQUAL>", SQLOperator.EQUAL.getSymbol());
		tooltip = tooltip.replaceFirst("<NOT_EQUAL>", SQLOperator.NOT_EQUAL.getSymbol());
		tooltip = tooltip.replaceFirst("<LESS>", SQLOperator.LESS.getSymbol());
		tooltip = tooltip.replaceFirst("<LESS_OR_EQUAL>", SQLOperator.LESS_OR_EQUAL.getSymbol());
		tooltip = tooltip.replaceFirst("<GREATER>", SQLOperator.GREATER.getSymbol());
		tooltip = tooltip.replaceFirst("<GREATER_OR_EQUAL>", SQLOperator.GREATER_OR_EQUAL.getSymbol());
		tooltip = tooltip.replaceFirst("<FUZZY_IGNORE_CASE>", SQLOperator.FUZZY_IGNORE_CASE.getSymbol());
		tooltip = tooltip.replaceFirst("<FUZZY_LOWER_ASCII>", SQLOperator.FUZZY_LOWER_ASCII.getSymbol());
		tooltip = tooltip.replaceFirst("<FUZZY_REGEXP>", SQLOperator.FUZZY_REGEXP.getSymbol());
		tooltip = tooltip.replaceFirst("<FUZZY_SOUNDEX>", SQLOperator.FUZZY_SOUNDEX.getSymbol());
		tooltip = tooltip.replaceFirst("<FUZZY_TRIGRAMM>", SQLOperator.FUZZY_TRIGRAMM.getSymbol());
		operatorCombo.setToolTipText(tooltip);

		final GridData gd_operatorCombo = new GridData(SWT.FILL, SWT.CENTER, false, false);
		gd_operatorCombo.widthHint = 25;
		operatorCombo.setLayoutData(gd_operatorCombo);

		operatorCombo.setVisibleItemCount(12);
		operatorComboViewer.setContentProvider(new ArrayContentProvider());
		operatorComboViewer.setLabelProvider(new SQLOperatorLabelProvider());

		valueWidget = new ParameterComposite(whereComposite, SWT.NONE);
		final GridData gd_valueWidget = new GridData(SWT.FILL, SWT.CENTER, true, false);
		gd_valueWidget.widthHint = gd_fieldCombo.widthHint;
		valueWidget.setLayoutData(gd_valueWidget);

		activeButton = new Button(whereComposite, SWT.CHECK);
		activeButton.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false));

		operatorCombo.addSelectionListener(this);
		fieldCombo.addSelectionListener(this);
		valueWidget.addSelectionListener(this);
	}


	public void dispose() {
		addButton.dispose();
		removeButton.dispose();
		fieldCombo.dispose();
		operatorCombo.dispose();
		valueWidget.dispose();
		activeButton.dispose();
	}


	public void setRemoveEnabled(boolean enable) {
		removeButton.setEnabled(enable);
	}


	private void setSqlOperators(SQLOperator[] sqlOperators) {
		if (sqlOperators == null) {
			sqlOperators = SQLOperator.ALL;
		}

		operatorComboViewer.setInput(sqlOperators);
		if (sqlOperators.length > 0) {
			operatorComboViewer.setSelection(new StructuredSelection(sqlOperators[0]), true);
		}
	}


	public void setSqlFields(final List<SQLField> sqlFields) {

		/* Here, we have to change to the display thread asynchronously, because changing
		 * synchronously sometimes leads to a dead-lock, e.g. when closing an Event.
		 */
		SWTHelper.asyncExecDisplayThread(new Runnable() {
			@Override
			public void run() {

				// save selections
				ISelection fieldSelection = fieldComboViewer.getSelection();
				ISelection operatorSelection = operatorComboViewer.getSelection();
				Object value = valueWidget.getValue();

				// set data
				fieldComboViewer.setInput(sqlFields);

				// restore selections where possible
				fieldComboViewer.setSelection(fieldSelection, true);
				operatorComboViewer.setSelection(operatorSelection, true);

				/* If the fieldComboViewer has no selection, the new sqlFields don't contain their
				 * previously selected SQLField. In this case the valueWidget must be initialized.
				 * So we set the value to an empty String. This assures that no combos with unwanted
				 * data are shown anymore.
				 */
				int selectionIndex = fieldComboViewer.getCombo().getSelectionIndex();
				if (selectionIndex == -1) {
					valueWidget.init();
				}
				else {
					try {
						valueWidget.setValue(value);
					}
					catch (ParseException e) {
						com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
					}
				}
			}
		});

	}


	public void setSqlParameter(final SQLParameter sqlParameter) {
		SWTHelper.asyncExecDisplayThread(new Runnable() {
			@Override
			public void run() {

				if (sqlParameter != null) {
					try {
						// set Search-Field
						SQLField sqlField = sqlParameter.getSQLField();
						fieldComboViewer.setSelection(new StructuredSelection(sqlField), true);

						// set operator
						SQLOperator sqlOperator = sqlParameter.getSQLOperator();
						operatorComboViewer.setSelection(new StructuredSelection(sqlOperator), true);

						// set parameter type
						Class parameterType = sqlField.getParameterType();
						Map values = sqlField.getValues();
						valueWidget.setValueType(
							parameterType,
							values
						);

						// set parameter value
						Object valueObject = sqlParameter.getValue();
						valueWidget.setValue(valueObject);

						activeButton.setSelection(sqlParameter.isActive());
					}
					catch (Throwable e) {
						/* If an error occurs, there may be invalid parameters.
						 * So this WhereTerm must be initialized and deactivated.
						 */
						initialize();
						activeButton.setSelection(false);
						RegasusErrorHandler.handleWarnError(Activator.PLUGIN_ID, getClass().getName(), e);
					}
				}
				else {
					initialize();
				}

			}
		});

	}


	public void initialize() {
		fieldComboViewer.setSelection(new StructuredSelection());
		operatorComboViewer.setSelection(new StructuredSelection());
		// activeButton.setSelection(false);
	}


	public int getValueWidgetsPreferredWidth() {
		return valueWidget.getPreferredWidth();
	}


	/**
	 * Collects from visible widgets (the 2 comboViewers and the valueWidget) the search field, the
	 * operator and the value, and delegates to the specific field to create an instance of SQLParameter.
	 * <p>
	 * However, that specific field will probably do some validation on the operator and value and
	 * might throw an {@link InvalidValuesException}, upon which an error dialog is shown.
	 *
	 * @return If active, returns an SQLParameter with valid value. If inactive, returns null.
	 *         If active, but value is invalid, shows an error and then returns null.
	 *         Null is also returned when no field or no operator is selected
	 */
	public SQLParameter getSQLParameter() {
		SQLParameter sqlParameter = null;

		boolean active = activeButton.getSelection();

		if (active) {
			SQLField sqlField = SelectionHelper.getUniqueSelected(fieldComboViewer);
			SQLOperator sqlOperator = SelectionHelper.getUniqueSelected(operatorComboViewer);
			Object value = valueWidget.getValue();

			if (sqlField != null && sqlOperator != null) {
				try {
					sqlParameter = sqlField.getSQLParameter(value, sqlOperator);
					sqlParameter.setActive(active);
				}
				catch (Exception e) {
					activeButton.setSelection(false);
					RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
				}
			}
		}

		return sqlParameter;
	}


	/**
	 * Collects from visible widgets (the 2 comboViewers and the valueWidget) the search field, the
	 * operator and the value; if at least the field is selected, creates and returns an instance of
	 * SQLParameter without further validation or checking the active-state.
	 * <p>
	 * This SQLParameter might not be usable for any search, but is intended to be saved as part of
	 * the state of the client.
	 *
	 * @return Returns an SQLParameter with at least a selected field. Returns null when no field
	 *         is selected.
	 */
	public SQLParameter getSQLParameterForMemento() {
		SQLParameter sqlParameter = null;

		SQLField sqlField = SelectionHelper.getUniqueSelected(fieldComboViewer);

		if (sqlField != null) {
			SQLOperator sqlOperator = SelectionHelper.getUniqueSelected(operatorComboViewer);
			Object value = valueWidget.getValue();
			boolean active = activeButton.getSelection();

			sqlParameter = new SQLParameter(sqlField, value, sqlOperator, active);
		}
		return sqlParameter;
	}


	public String getDescription() {
		String description = null;

		if (activeButton.getSelection()) {
			StringBuffer sb = new StringBuffer();
			SQLField sqlField = SelectionHelper.getUniqueSelected(fieldComboViewer);
			SQLOperator sqlOperator = SelectionHelper.getUniqueSelected(operatorComboViewer);

			if (sqlField != null && sqlOperator != null) {
				sb.append(sqlField.getLabel());
				sb.append(" "); 

				String valueString = valueWidget.getString();
				if (valueString == null) {
					valueString = CoreI18N.WhereTerm_IsEmpty;
				}
				else {
					sb.append(sqlOperator.getSymbol());
					sb.append(" "); 
				}
				sb.append(valueString);
			}
			description = sb.toString();
		}

		return description;
	}


	public void setActive(boolean active) {
		activeButton.setSelection(active);
	}


	@Override
	public void widgetDefaultSelected(SelectionEvent e) {
	}


	@Override
	public void widgetSelected(SelectionEvent e) {
		activeButton.setSelection(true);
	}

}
