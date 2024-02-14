/**
 * 
 */
package com.lambdalogic.util.rcp.widget;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

import com.lambdalogic.util.DateHelper;

/**
 * @author loi
 *
 */
public class DateText extends Composite implements DisposeListener, Listener {
	
	private Text dateText;
	
	private DateFormat dateFormatter;
	
	private Date date;
	
	private Locale locale;
	
	private String _buffer = "";
	
	/**
	 * Calendar instance, used for date/time calculations.
	 */
	private Calendar calendar;
	
	/**
	 * Aktuelle Position des Cursors in dateText.
	 */
	private int cursorPosition;
	
	protected List<SelectionListener> selectionListenerList = null;
	
	private boolean dontFireSelection = false;

	public DateText(Composite parent, int style, Calendar calendar) {
		super(parent, style);
		
		setLayout(new GridLayout(1, false));
		
		this.calendar = calendar;
		
		date = new Date();
		
		locale = Locale.getDefault();
		
		// um zu testen, ob dieses Widget auch in England und Frankreich auch funktioniert
//		locale = new Locale("en", "US");
//		locale = new Locale("fr", "FR");
		
		dateFormatter = DateFormat.getDateInstance(DateFormat.MEDIUM, locale);
		
		dateText = new Text(this, SWT.BORDER);
		dateText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
 		dateText.setText(getFullDateString());
 		
		// add TextListener
		dateText.addListener(SWT.FocusIn, this);
		dateText.addListener(SWT.FocusOut, this);
		dateText.addListener(SWT.KeyDown, this);
		dateText.addListener(SWT.MouseDown, this);
		dateText.addListener(SWT.MouseWheel, this);
		dateText.addListener(SWT.MouseUp, this);
		dateText.addListener(SWT.Verify, this);
		dateText.addListener(SWT.Traverse, this);
 		
//		System.out.println("getDateString(String date): " + getDateString(getFullDateString()));
//		System.out.println("getMonthString(String date): " + getMonthString(getFullDateString()));
//		System.out.println("getYearString(String date): " + getYearString(getFullDateString()));
	}
	
	public void handleEvent(Event event) {
		switch (event.type) {
			case SWT.KeyDown:
				handleKey(event);
				break;
	
			case SWT.Paint: {
				System.out.println("Paint");
				updateSelection();
				
				initBuffer();
				break;
			}
			
			case SWT.MouseUp:
				updateSelection();
				break;
			default:
				break;
		}
	}
	
	protected void handleKey(Event event) {
		switch (event.keyCode) {
		case SWT.ARROW_DOWN:
			updateByArrowDown();
			break;
		
		case SWT.ARROW_UP:
			updateByArrowUp();
			break;

		case SWT.ARROW_RIGHT: {
			int selectedDatePart = getSelectedDatePart();
			if (isGermanyOrFrance()) {
				if (selectedDatePart == DateFormat.DATE_FIELD) {
					selectMonth();
				} else if (selectedDatePart == DateFormat.MONTH_FIELD) {
					selectYear();
				} else if (selectedDatePart == DateFormat.YEAR_FIELD) {
					selectDate();
				}
			}
			else {
				if (selectedDatePart == DateFormat.DATE_FIELD) {
					selectYear();
				} else if (selectedDatePart == DateFormat.MONTH_FIELD) {
					selectDate();
				} else if (selectedDatePart == DateFormat.YEAR_FIELD) {
					selectMonth();
				}
			}
			initBuffer();
			break;
		}
		case SWT.ARROW_LEFT: {
			int selectedDatePart = getSelectedDatePart();
			if (isGermanyOrFrance()) {
				if (selectedDatePart == DateFormat.YEAR_FIELD) {
					selectMonth();
				} else if (selectedDatePart == DateFormat.MONTH_FIELD) {
					selectDate();
				} else if (selectedDatePart == DateFormat.DATE_FIELD) {
					selectYear();
				}
			}
			else {
				if (selectedDatePart == DateFormat.YEAR_FIELD) {
					selectDate();
				} else if (selectedDatePart == DateFormat.MONTH_FIELD) {
					selectYear();
				} else if (selectedDatePart == DateFormat.DATE_FIELD) {
					selectMonth();
				}
			}
			initBuffer();
			break;
		}
		case SWT.BS:
		case SWT.DEL: {
			setDate(null, true);
			break;
		}
			
		default:
			if (Character.isDigit(event.character)) {
				addChar(event.character);
			}
			break;
		}
		
		// catch all key events
		event.doit = false;
	}
	
	private void addChar(char c) {
		int selectedDatePart = getSelectedDatePart();
		
		if (selectedDatePart == DateFormat.DATE_FIELD) {
			if (getBufferLength() == 2) {
				initBuffer();
			}
			addBuffer(c);	
			
			int day = Integer.parseInt(getBuffer());
			if (day >= 1 && day <= 31) {
				setDay(day);
			}
		}
		else if (selectedDatePart == DateFormat.MONTH_FIELD) {
			if (locale.equals(Locale.GERMANY)) {
				if (getBufferLength() == 2) {
					initBuffer();
				}
				addBuffer(c);
				int month = Integer.parseInt(getBuffer());
				if (month >= 1 && month <= 12) {
					setMonth(month);
				}
			}
		}
		else { // selectedDatePart == Calendar.YEAR
			if (getBufferLength() == 4) {
				initBuffer();
			}
			addBuffer(c);	

			int year = Integer.parseInt(getBuffer());
			if (year >= 0 && year <= 9999) {
				setYear(year);
			}
		}
	}
	
	private void initBuffer() {
		_buffer = "";
		System.out.println("buffer: " + _buffer);
	}
	
	private void addBuffer(char c) {
		_buffer += c;
		System.out.println("buffer: " + _buffer);
	}
	
	private int getBufferLength() {
		return _buffer.length();
	}
	
	private String getBuffer() {
		return _buffer;
	}

	private void setDay(int day) {
		if (date == null) {
			date = new Date();
		}
		
		calendar.setTime(date);
		calendar.set(Calendar.DATE, day);
		date = calendar.getTime();
		updateDateText();
	}

	
	private void setMonth(int month) {
		if (date == null) {
			date = new Date();
		}
		calendar.setTime(date);
		calendar.set(Calendar.MONTH, month - 1);
		date = calendar.getTime();
		updateDateText();
	}

	
	private void setYear(int year) {
		if (date == null) {
			date = new Date();
		}
		calendar.setTime(date);
		calendar.set(Calendar.YEAR, year);
		date = calendar.getTime();
		updateDateText();
	}

	
	private void updateByArrowUp() {
		int datePart = getSelectedDatePart();
		
		if (date == null) {
			date = new Date();
		}
		
		if (datePart == DateFormat.DATE_FIELD) {
			date = DateHelper.addDays(date, 1);
		}
		else if (datePart == DateFormat.MONTH_FIELD) {
			date = DateHelper.addMonths(date, 1);
		}
		else if (datePart == DateFormat.YEAR_FIELD) {
			date = DateHelper.addYears(date, 1);
		}
		
		updateDateText();
	}
	
	private void updateByArrowDown() {
		int datePart = getSelectedDatePart();
		
		if (date == null) {
			date = new Date();
		}
		
		if (datePart == DateFormat.DATE_FIELD) {
			date = DateHelper.addDays(date, -1);
		}
		else if (datePart == DateFormat.MONTH_FIELD) {
			date = DateHelper.addMonths(date, -1);
		}
		else if (datePart == DateFormat.YEAR_FIELD) {
			date = DateHelper.addYears(date, -1);
		}
		
		updateDateText();
	}
	
	private void updateDateText() {
		String text = "";
		if (date != null) {
			text = dateFormatter.format(date);
		}
		
		int caretPosition = dateText.getCaretPosition();
		dateText.setText(text);
		dateText.setSelection(caretPosition);
		
		updateSelection();
		fireSelection(); 
	}
	
	public synchronized void addSelectionListener(SelectionListener selectionListener) {
		if (selectionListenerList == null) {
			selectionListenerList = new ArrayList<SelectionListener>();
		}

		selectionListenerList.add(selectionListener);
	}


	public void removeModifyListener(SelectionListener selectionListener) {
		if (selectionListenerList != null) {
			selectionListenerList.remove(selectionListener);
		}
	}


	public synchronized void fireSelection() {
		if (!dontFireSelection) {
			if (selectionListenerList != null) {
				Event e = new Event();
				e.widget = this;
				e.display = getDisplay();
				SelectionEvent selectionEvent = new SelectionEvent(e);
	
				for (SelectionListener selectionListener : selectionListenerList) {
					selectionListener.widgetSelected(selectionEvent);
				}
			}
		}
	}

	private void updateSelection() {
		String text = dateText.getText();
		if (text != null && text.length() > 0) {
			cursorPosition = dateText.getCaretPosition();
			String[] splitedDate = getSplitedDate(text);
			if (isGermanyOrFrance()) {
				if (cursorPosition < splitedDate[0].length()+1) {
					selectDate();
				}
				else if (cursorPosition < splitedDate[0].length() + splitedDate[1].length()+2) {
					selectMonth();
				}
				else {
					selectYear();
				}
			}
			else {
				if (cursorPosition < splitedDate[0].length()+1) {
					selectMonth();
				}
				else if (cursorPosition < splitedDate[0].length() + splitedDate[1].length()+2) {
					selectDate();
				}
				else {
					selectYear();
				}
			}
		}			
	}


	private void selectDate() {
		int[] datePostion = getDatePosition();
		dateText.setSelection(datePostion[0], datePostion[1]);
	}

	private void selectMonth() {
		int[] monthPosition = getMonthPosition();
		dateText.setSelection(monthPosition[0], monthPosition[1]);
	}
	
	private void selectYear() {
		int[] yearPosition = getYearPosition();
		dateText.setSelection(yearPosition[0], yearPosition[1]);
	}

	private String getFullDateString() {
		String result = "";
		result = dateFormatter.format(date);
		System.out.println(result);
		return result;
	}
/*	
	private String getDateString(String date) {
		String result = "";
		String[] splitedDate = getSplitedDate(date);
		if (isGermanyOrFrance()) {
			result = splitedDate[0];
		}
		else {
			result = splitedDate[1];
		}
		return result;
	}
*/
	
	private String getMonthString(String date) {
		String result = "";
		String[] splitedDate = getSplitedDate(date);
		if (isGermanyOrFrance()) {
			result = splitedDate[1];
		}
		else {
			result = splitedDate[0];
		}
		return result;
	}
	
//	private String getYearString(String date) {
//		return getSplitedDate(date)[2];
//	}
	
	private String[] getSplitedDate(String date) {
		String[] splitedDate = null;
		if (locale.equals(Locale.GERMANY)) {
			splitedDate = date.split("[.]");
		}
		else {
			splitedDate = date.split(" ");
		}
		return splitedDate;
	}
	
	private int getSelectedDatePart() {
		int datePart = DateFormat.YEAR_FIELD;
		int caretPosition = dateText.getCaretPosition();
		String text = dateText.getText();
		if (text == null || text.length() == 0) {
			if (date == null) {
				date = new Date();
			}
			text = dateFormatter.format(date);
		}
		String[] splitedDate = getSplitedDate(text);
		if (isGermanyOrFrance()) {
			if (caretPosition < splitedDate[0].length()) {
				datePart = DateFormat.DATE_FIELD;
			}
			else if (caretPosition < splitedDate[0].length() + splitedDate[1].length()) {
				datePart = DateFormat.MONTH_FIELD;
			}
		}
		else {
			if (caretPosition < splitedDate[0].length()) {
				datePart = DateFormat.MONTH_FIELD;
			}
			else if (caretPosition < splitedDate[0].length() + splitedDate[1].length()) {
				datePart = DateFormat.DATE_FIELD;
			}
		}

		return datePart;
	}
	
	private int[] getDatePosition() {
		int[] result = new int[2];
		if (isGermanyOrFrance()) {
			result[0] = 0;
			result[1] = 2;
		}
		else {
			String text = dateText.getText();
			result[0] = text.indexOf(" ")+1;
			result[1] = result[0] + getMonthString(text).length()-1;
		}
		return result;
	}
	
	private int[] getMonthPosition() {
		int[] result = new int[2];
		String text = dateText.getText();
		if (isGermanyOrFrance()) {
			result[0] = 3;
			if (locale.equals(Locale.FRANCE)) {
				if (getMonthString(text).contains(".")) {
					result[1] = 3 + getMonthString(text).length() - 1;
				}
				else {
					result[1] = 3 + getMonthString(text).length();
				}
			}
			else {
				result[1] = 3 + getMonthString(text).length();
			}
		}
		else {
			result[0] = 0;
			result[1] = getMonthString(text).length();
		}
		return result;
	}
	
	private int[] getYearPosition() {
		int[] result = new int[2];
		String text = dateText.getText();
		result[0] = text.length()-4;
		result[1] = text.length();
		return result;
	}
	
	private boolean isGermanyOrFrance() {
		return 
			locale.equals(Locale.GERMANY) || 
			locale.equals(Locale.FRANCE);
	}
	
	public Date getDate() {
		return date;
	}
	
	public void setDate(Date date, boolean fireSelection) {
		dontFireSelection = !fireSelection;
		
		this.date = date;
		updateDateText();
		
		dontFireSelection = false;
	}

	public void setDate(Date date) {
		setDate(date, false);
	}

	public void widgetDisposed(DisposeEvent e) {
		// TODO Auto-generated method stub
		
	}

}
