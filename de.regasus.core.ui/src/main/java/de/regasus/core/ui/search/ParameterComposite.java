package de.regasus.core.ui.search;

import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Text;

import com.lambdalogic.time.I18NDate;
import com.lambdalogic.time.I18NDateMinute;
import com.lambdalogic.time.I18NDateSecond;
import com.lambdalogic.time.I18NMonth;
import com.lambdalogic.time.I18NTimestamp;
import com.lambdalogic.util.MapEntry;
import com.lambdalogic.util.TypeHelper;
import com.lambdalogic.util.rcp.UtilI18N;
import com.lambdalogic.util.rcp.datetime.AbstractDateControl;
import com.lambdalogic.util.rcp.datetime.DateControl;
import com.lambdalogic.util.rcp.datetime.DateTimeControl;
import com.lambdalogic.util.rcp.datetime.MonthControl;
import com.lambdalogic.util.rcp.i18n.I18NLabelProvider;
import com.lambdalogic.util.rcp.widget.NullableSpinner;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.Activator;

public class ParameterComposite extends Composite implements SelectionListener, ModifyListener, KeyListener {

	private Class<?> parameterType = String.class;

	// Widget for entering values
	private Control control;
	// ComboViewer, if control is of type Combo
	private ComboViewer comboViewer;
	private Map<?, ?> valueMap;
	private int preferredWidth = -1;

	private WhereComposite whereComposite;

	private List<SelectionListener> selectionListenerList = new ArrayList<>();

	private final I18NLabelProvider labelProvider = new I18NLabelProvider();


	/**
	 * Create the composite
	 * @param whereComposite
	 * @param style
	 */
	public ParameterComposite(WhereComposite whereComposite, int style) {
		super(whereComposite, style);
		setLayout(new FillLayout());

		this.whereComposite = whereComposite;

		control = new Text(this, SWT.SEARCH);
	}


	public void setValueType(Class<?> parameterType, Map<?, ?> parameterValues) {
		Objects.requireNonNull(parameterType);

		this.parameterType = parameterType;

		detachListener();

		if (parameterValues != null) {
			setListControl(parameterValues);
		}
		else if (parameterType == String.class) {
			setStringControl();
		}
		else if (parameterType == Boolean.class) {
			setBooleanControl();
		}
		else if (   parameterType == LocalDateTime.class
			     || parameterType == I18NDateMinute.class
			     || parameterType == I18NDateSecond.class
			     || parameterType == I18NTimestamp.class
		) {
			setDateTimeControl();
		}
		else if (   parameterType == LocalDate.class
				 || parameterType == I18NDate.class
		) {
			setDateControl();
		}
		else if (   parameterType == Month.class
				 || parameterType == I18NMonth.class
		) {
			setMonthControl();
		}
		// Integer and Long must be handled before Number, because they are also a Numbers
		else if (parameterType == Integer.class) {
			setIntegerControl();
		}
		else if (parameterType == Long.class) {
			setLongControl();
		}
		else if (parameterType == Number.class) {
			setNumberControl();
		}
		else {
			setStringControl();
		}

		attachListener();
	}


	public void setValue(Object value) throws ParseException {

		detachListener();

		if (valueMap !=  null) {
			setListValue(value);
		}
		else if (parameterType == String.class) {
			setStringValue(TypeHelper.toString(value, true));
		}
		else if (parameterType == Boolean.class) {
			setBooleanValue(TypeHelper.toBoolean(value, false));
		}


		else if (   parameterType == LocalDateTime.class
    		     || parameterType == I18NDateMinute.class
    		     || parameterType == I18NDateSecond.class
    		     || parameterType == I18NTimestamp.class
    	) {
			setDateTimeValue((Temporal) value);
    	}
    	else if (   parameterType == LocalDate.class
    			 || parameterType == I18NDate.class
    	) {
    		setDateValue((Temporal) value);
    	}
    	else if (   parameterType == Month.class
    			 || parameterType == I18NMonth.class
    	) {
    		setMonthValue((Temporal) value);
    	}


		else if (parameterType == Integer.class) {
			setIntegerValue(TypeHelper.toInteger(value));
		}
		else if (parameterType == Long.class) {
			setLongValue(TypeHelper.toLong(value));
		}
		else if (parameterType == Double.class) {
			setDoubleValue(TypeHelper.toDouble(value));
		}
		else {
			String strValue = null;
			if (value != null) {
				strValue = value.toString();
			}
			setStringValue(strValue);
		}

		attachListener();
	}


	protected void disposeControl() {
		detachListener();

		if (control !=  null) {

			control.dispose();
			control = null;
			valueMap = null;
		}
		comboViewer = null;
	}


	private void setStringControl() {
		if ( ! (control instanceof Text)) {
			disposeControl();
			Text text = new Text(this, SWT.SEARCH);
			preferredWidth = 200;
			control = text;
			refreshGUI();
		}
	}


	private void setStringValue(String value) {
		if ( ! (control instanceof Text)) {
			setStringControl();
		}
		Text text = (Text) control;

		if (value == null) {
			value = "";
		}
		text.setText(value);
		refreshGUI();
	}


	private void setBooleanControl() {
		if ( ! (control instanceof Button)) {
			disposeControl();
			Button button = new Button(this, SWT.CHECK);
			button.setSelection(true);
			preferredWidth = button.computeSize(SWT.DEFAULT, SWT.DEFAULT).x;
			control = button;
			refreshGUI();
		}
	}


	private void setBooleanValue(Boolean value) {
		if ( ! (control instanceof Button)) {
			setBooleanControl();
		}
		Button button = (Button) control;

		if (value != null) {
			button.setSelection(value.booleanValue());
		}

		refreshGUI();
	}


	private void setDateTimeControl() {
		if ( ! (control instanceof AbstractDateControl && ((AbstractDateControl) control).isWithTime())) {
			disposeControl();
			AbstractDateControl dateTimeControl = new DateTimeControl(this, SWT.NONE);
			dateTimeControl.setEmptyLabelText(UtilI18N.Empty);

			preferredWidth = dateTimeControl.computeSize(SWT.DEFAULT, SWT.DEFAULT).x;
			control = dateTimeControl;
			refreshGUI();
		}
	}


	private void setDateTimeValue(Temporal temporal) {
		setDateTimeControl();
		AbstractDateControl dateControl = (AbstractDateControl) control;

		dateControl.setTemporal(temporal);

		refreshGUI();
	}


	private void setDateControl() {
		if ( ! (control instanceof AbstractDateControl && ! ((AbstractDateControl) control).isWithTime())) {
			disposeControl();
			AbstractDateControl dateControl = new DateControl(this, SWT.NONE);
			dateControl.setEmptyLabelText(UtilI18N.Empty);
			preferredWidth = dateControl.computeSize(SWT.DEFAULT, SWT.DEFAULT).x;
			control = dateControl;
			refreshGUI();
		}
	}


	private void setDateValue(Temporal temporal) {
		setDateControl();
		AbstractDateControl dateControl = (AbstractDateControl) control;

		dateControl.setTemporal(temporal);

		refreshGUI();
	}


	private void setMonthControl() {
		if ( ! (control instanceof MonthControl)) {
			disposeControl();
			MonthControl monthControl = new MonthControl(this, SWT.NONE, UtilI18N.Empty);
			preferredWidth = monthControl.computeSize(SWT.DEFAULT, SWT.DEFAULT).x;
			control = monthControl;
			refreshGUI();
		}
	}


	private void setMonthValue(Temporal temporal) {
		setMonthControl();
		MonthControl monthControl = (MonthControl) control;

		monthControl.setTemporal(temporal);

		refreshGUI();
	}


	private void setIntegerControl() {
		if (control instanceof NullableSpinner) {
			// set minimum and maximum because it might have the values of Long
			NullableSpinner spinner = (NullableSpinner) control;
			spinner.setMinimumAndMaximum(Integer.MIN_VALUE, Integer.MAX_VALUE);
		}
		else {
			disposeControl();
			NullableSpinner spinner = new NullableSpinner(this, SWT.SEARCH);
			spinner.setMinimumAndMaximum(Integer.MIN_VALUE, Integer.MAX_VALUE);
			preferredWidth = 200;
			control = spinner;
			refreshGUI();
		}
	}


	private void setLongControl() {
		if (control instanceof NullableSpinner) {
			// set minimum and maximum because it might have the values of Integer
			NullableSpinner spinner = (NullableSpinner) control;
			spinner.setMinimumAndMaximum(Long.MIN_VALUE, Long.MAX_VALUE);
		}
		else {
			disposeControl();
			NullableSpinner spinner = new NullableSpinner(this, SWT.SEARCH);
			spinner.setMinimumAndMaximum(Long.MIN_VALUE, Long.MAX_VALUE);
			preferredWidth = 200;
			control = spinner;
			refreshGUI();
		}
	}


	private void setIntegerValue(Integer value) {
		if ( ! (control instanceof NullableSpinner)) {
			setIntegerControl();
		}
		NullableSpinner spinner = (NullableSpinner) control;

		spinner.setValue(value);
		refreshGUI();
	}


	private void setLongValue(Long value) {
		if ( ! (control instanceof NullableSpinner)) {
			setLongControl();
		}
		NullableSpinner spinner = (NullableSpinner) control;

		spinner.setValue(value);
		refreshGUI();
	}


	private void setNumberControl() {
		setStringControl();
	}


	private void setDoubleValue(Double value) {
		String s = null;
		if (value != null) {
			s = value.toString();
		}
		setStringValue(s);
	}


	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void setListControl(Map<?, ?> parameterValues) {
		Combo comboControl = null;
		if (control instanceof Combo) {
			comboControl = (Combo) control;
		}
		else {
			disposeControl();
			comboViewer = new ComboViewer(this, SWT.READ_ONLY);
			comboControl = comboViewer.getCombo();
			comboControl.setVisibleItemCount(12);
			preferredWidth = comboControl.computeSize(SWT.DEFAULT, SWT.DEFAULT).x;
			control = comboControl;

			comboViewer.setContentProvider(new ArrayContentProvider());
			comboViewer.setLabelProvider(labelProvider);

			/* Do not sort but keep the original order!
			 * Sorting is done in the SearchValuesProvider classes, e.g.
			 * CountrySearchValuesProvider
			 *
			 * The order of ParticipantCustomFields is defined in ParticipantCustomFieldListSQLField.getValues().
			 */

//			comboViewer.setSorter(new ViewerSorter(new I18NCollator(Locale.getDefault())));
		}

		this.valueMap = parameterValues;

		comboViewer.setInput( parameterValues.entrySet() );


		comboControl.removeSelectionListener(this);
		comboControl.addSelectionListener(this);
		refreshGUI();
	}


	private void setListValue(Object value) {
		try {
			Object valueObj = null;
			if (value != null) {
				valueObj = valueMap.get(value);
				valueObj = new MapEntry(value, valueObj);
			}

			ISelection selection = null;
			if (valueObj == null) {
				selection = new StructuredSelection();
			}
			else {
				selection = new StructuredSelection(valueObj);
			}
			comboViewer.setSelection(selection, true /*reveal*/);
			refreshGUI();
		}
		catch (Throwable e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	public void init() {
		setValueType(String.class, null);
		setStringValue("");
	}


	public Object getValue() {
		Object value = null;

		if (valueMap != null) {
			IStructuredSelection selection = (IStructuredSelection) comboViewer.getSelection();
			if (selection.size() == 1) {
				Map.Entry<?, ?> entry = (Map.Entry<?, ?>) selection.getFirstElement();
				value = entry.getKey();
			}
		}
		else if (parameterType == String.class) {
			Text text = (Text) control;
			String s = text.getText();
			s = s.trim();
			if (s.length() > 0) {
				value = s;
			}
		}
		else if (parameterType == Boolean.class) {
			Button button = (Button) control;
			value = new Boolean(button.getSelection());
		}
		else if (parameterType != null && Temporal.class.isAssignableFrom(parameterType)) {
			AbstractDateControl dateControl = (AbstractDateControl) control;
			value = dateControl.getTemporal();
		}
		else if (parameterType == Integer.class) {
			try {
				NullableSpinner spinner = (NullableSpinner) control;
				value = spinner.getValueAsInteger();
			}
			catch (Exception e) {
				com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
				RegasusErrorHandler.handleWarnError(Activator.PLUGIN_ID, getClass().getName(), e);
			}
		}
		else if (parameterType == Long.class) {
			try {
				NullableSpinner spinner = (NullableSpinner) control;
				value = spinner.getValue();
			}
			catch (Exception e) {
				com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
				RegasusErrorHandler.handleWarnError(Activator.PLUGIN_ID, getClass().getName(), e);
			}
		}
		else if (parameterType == Double.class) {
			try {
				Text text = (Text) control;
				String s = text.getText();
				s = s.trim();
				if (s.length() > 0) {
					value = TypeHelper.toDouble(s);
				}
			}
			catch (Exception e) {
				com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
				RegasusErrorHandler.handleWarnError(Activator.PLUGIN_ID, getClass().getName(), e);
			}
		}
		else {
			try {
				Text text = (Text) control;
				String s = text.getText();
				s = s.trim();
				if (s.length() > 0) {
					value = s;
				}
			}
			catch (Exception e) {
				com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
				RegasusErrorHandler.handleWarnError(Activator.PLUGIN_ID, getClass().getName(), e);
			}
		}

		return value;
	}


	public String getString() {
		String result = null;

		if (control instanceof Combo) {
			IStructuredSelection selection = (IStructuredSelection) comboViewer.getSelection();
			if (selection.size() == 1) {
				result = labelProvider.getText(selection.getFirstElement());
			}
		}
		else if (control instanceof Text) {
			Text text = (Text) control;
			String s = text.getText();
			s = s.trim();
			if (s.length() > 0) {
				result = s;
			}
		}
		else if (control instanceof Button) {
			Button button = (Button) control;
			if (button.getSelection()) {
				result = UtilI18N.Yes;
			}
			else {
				result = UtilI18N.No;
			}
		}
		else if (control instanceof AbstractDateControl) {
			AbstractDateControl dateControl = (AbstractDateControl) control;
			result = TypeHelper.toString(dateControl.getTemporal());
		}
		else if (control instanceof NullableSpinner) {
			NullableSpinner spinner = (NullableSpinner) control;
			result = TypeHelper.toString(spinner.getValue());
		}
		else {
			try {
				Text text = (Text) control;
				String s = text.getText();
				s = s.trim();
				if (s.length() > 0) {
					result = s;
				}
			}
			catch (Exception e) {
				com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
				RegasusErrorHandler.handleWarnError(Activator.PLUGIN_ID, getClass().getName(), e);
			}
		}

		return result;
	}


	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}


	private void refreshGUI() {
		pack();
		layout();
	}


	public int getPreferredWidth() {
		return preferredWidth;
	}


	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		control.setEnabled(enabled);
	}

	public void addSelectionListener(SelectionListener selectionListener) {
		if (! selectionListenerList.contains(selectionListener)) {
			selectionListenerList.add(selectionListener);
		}
	}

	public void removeSelectionListener(SelectionListener selectionListener) {
		selectionListenerList.remove(selectionListener);
	}


	private void attachListener() {
		if (control instanceof Combo) {
			Combo combo = (Combo)control;
			combo.removeSelectionListener(this);
			combo.addSelectionListener(this);
			combo.addKeyListener(this);
		}
		else if (control instanceof Button) {
			Button button = (Button)control;
			button.removeSelectionListener(this);
			button.addSelectionListener(this);
			button.addKeyListener(this);
		}
		else if (control instanceof NullableSpinner) {
			NullableSpinner spinner = (NullableSpinner) control;
			spinner.removeModifyListener(this);
			spinner.addModifyListener(this);
			spinner.addKeyListener(this);
		}
		else if (control instanceof Text) {
			Text text = (Text) control;
			text.removeModifyListener(this);
			text.addModifyListener(this);
			text.addKeyListener(this);
		}
		else if (control instanceof AbstractDateControl) {
			AbstractDateControl dateControl = (AbstractDateControl) control;
			dateControl.removeModifyListener(this);
			dateControl.addModifyListener(this);
			dateControl.addKeyListener(this);
		}
	}


	private void detachListener() {
		if (control instanceof Combo) {
			Combo combo = (Combo)control;
			combo.removeSelectionListener(this);
			combo.removeKeyListener(this);
		}
		else if (control instanceof Button) {
			Button button = (Button)control;
			button.removeSelectionListener(this);
			button.removeKeyListener(this);
		}
		else if (control instanceof Text) {
			Text text = (Text) control;
			text.removeModifyListener(this);
			text.removeKeyListener(this);
		}
		else if (control instanceof NullableSpinner) {
			NullableSpinner spinner = (NullableSpinner) control;
			spinner.removeModifyListener(this);
			spinner.removeKeyListener(this);
		}
		else if (control instanceof AbstractDateControl) {
			AbstractDateControl dateControl = (AbstractDateControl) control;
			dateControl.removeModifyListener(this);
			dateControl.removeKeyListener(this);
		}
	}


	@Override
	public void widgetDefaultSelected(SelectionEvent e) {
	}


	@Override
	public void widgetSelected(SelectionEvent e) {
		for (SelectionListener sl : selectionListenerList) {
			sl.widgetSelected(e);
		}

	}


	@Override
	public void modifyText(ModifyEvent e) {
		for (SelectionListener sl : selectionListenerList) {
			Event event = new Event();
			event.data = e.data;
			event.widget = e.widget;
			event.display = e.display;
			sl.widgetSelected(new SelectionEvent(event));
		}
	}


	@Override
	public void keyPressed(KeyEvent e) {
		if ((e.keyCode == SWT.KEYPAD_CR || e.keyCode == 13) && e.stateMask == 0) {
			whereComposite.doSearch();
		}

	}


	@Override
	public void keyReleased(KeyEvent e) {
	}

}
