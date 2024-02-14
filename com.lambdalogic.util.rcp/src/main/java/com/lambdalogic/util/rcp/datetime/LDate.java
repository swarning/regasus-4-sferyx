package com.lambdalogic.util.rcp.datetime;

import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.PlatformUI;

import com.lambdalogic.time.I18NDate;
import com.lambdalogic.time.TimeFormatter;
import com.lambdalogic.time.TimeParser;
import com.lambdalogic.util.DateHelper;
import com.lambdalogic.util.EqualsHelper;
import com.lambdalogic.util.StringHelper;
import com.lambdalogic.util.SystemHelper;
import com.lambdalogic.util.TypeHelper;
import com.lambdalogic.util.rcp.Images;
import com.lambdalogic.util.rcp.widget.SWTHelper;


public class LDate extends Composite {

	/**
	 * Widget to display and change the data/time value.
	 * Works like a spinner.
	 */
	private CopyAndPasteText dateText;

	/**
	 * Label to open the calendar.
	 * Works like a button.
	 */
	private Label dateChooseLabel;

	private Image calendarImage = null;

	private static final String DATE_PATTERN = "dd.MM.yyyy";
	private static final TimeFormatter DATE_FORMATTER = TimeFormatter.getInstance(DATE_PATTERN);
	private static final TimeParser DATE_PARSER = TimeParser.getInstance(DATE_PATTERN);

	/**
	 * Current cursor position in dateText.
	 */
	private int cursorPosition;


	private LocalDate currentDate;

	/**
	 * Buffer for keyboard input.
	 */
	private String _buffer = "";


	protected List<SelectionListener> selectionListenerList = null;


	private boolean dontFireSelection = false;
	private boolean ignoreModifyEvents = false;


	private boolean editable = true;



	public LDate(final Composite parent, final int style) {
		this(parent, style, false);
	}


	public LDate(final Composite parent, final int style, boolean required) {
		super(parent, SWT.FILL);

		addDisposeListener( new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent e) {
				if (calendarImage != null) {
					calendarImage.dispose();
				}
			}
		});

		GridLayout gridLayout = new GridLayout();
		gridLayout.marginWidth = 0;
		gridLayout.marginHeight = 0;
		gridLayout.numColumns = 2;
		this.setLayout(gridLayout);

		dateText = new CopyAndPasteText(this, SWT.BORDER | SWT.FILL);

		// First make the font bold, then compute the width, cause bold fonts have different metrics
		if (required) {
			SWTHelper.makeBold(dateText);
		}

		GridData layoutData = new GridData(SWT.FILL, SWT.CENTER, false, false);
		// set width as necessary for 10 characters (e.g. 31.12.2015 is 10 characters)
		layoutData.widthHint = SWTHelper.computeTextWidgetWidthForCharCount(dateText, 10);

		dateText.setLayoutData(layoutData);

		dateChooseLabel = new Label(this, SWT.NONE);

		// load the calendar image
		Image image = getImage();
		dateChooseLabel.setImage(image);

		// open/close the calendar if the user clicks on the calendar image
		dateChooseLabel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				openCalendar();
			}
		});

		// add observers
		dateText.addCopyAndPasteListener(copyAndPasteListener);
		dateText.addListener(SWT.KeyDown, listener);
		dateText.addListener(SWT.MouseUp, listener);

		dateText.addModifyListener(modifyListener);
	}


	private CopyAndPasteListener copyAndPasteListener = new CopyAndPasteAdapter() {
		ChronoField selectedField;

		@Override
		public void beforePaste(CopyAndPasteEvent event) {
			// save selected date part
			selectedField = getSelectedField();

			String clipboardText = event.getText();

			// first try to convert clipboardText to Date
			try {
				LocalDate localDate = TypeHelper.toLocalDate(clipboardText);
				setLocalDate(localDate);
				event.cancel();
				return;
			}
			catch (ParseException e) {
				// ignore
			}


			// TODO: what happens with the pasted integer?
			// otherwise convert clipboardText to integer
			try {
				TypeHelper.toInteger(clipboardText);
			}
			catch (ParseException e) {
				// abort paste if pasted text is no integer
				event.cancel();
			}
		}


		@Override
		public void afterPaste(CopyAndPasteEvent event) {
			// restore selected date part
			if (selectedField == ChronoField.DAY_OF_MONTH) {
				selectDay();
			}
			else if (selectedField == ChronoField.MONTH_OF_YEAR) {
				selectMonth();
			}
			else if (selectedField == ChronoField.YEAR) {
				selectYear();
			}
		}


		@Override
		public void beforeCut(CopyAndPasteEvent e) {
			// copy the whole text into the clipboard
			String text = dateText.getText();
			e.setText(text);
		}


		@Override
		public void afterCut(CopyAndPasteEvent e) {
			// delete text (and indirectly the date)
			dateText.setText("");
		}


		@Override
		public void beforeCopy(CopyAndPasteEvent e) {
			// copy the whole text into the clipboard
			String text = dateText.getText();
			e.setText(text);
		}
	};


	private Listener listener = new Listener() {
		@Override
		public void handleEvent(Event event) {
			switch (event.type) {
				case SWT.KeyDown:
					handleKeyDown(event);
					break;

				case SWT.MouseUp:
					updateSelection();
					break;

				default:
					break;
			}
		}
	};


	protected void handleKeyDown(Event event) {
		if (isEditable()) {
			switch (event.keyCode) {
        		case SWT.ARROW_DOWN:
        			updateByArrowDown();
        			break;

        		case SWT.ARROW_UP:
        			updateByArrowUp();
        			break;

        		case SWT.ARROW_RIGHT:
        		case '.':
        		{
        			ChronoField selectedField = getSelectedField();
        			if (selectedField == ChronoField.DAY_OF_MONTH) {
        				selectMonth();
        			}
        			else if (selectedField == ChronoField.MONTH_OF_YEAR) {
        				selectYear();
        			}
        			initBuffer();
        			break;
        		}
        		case SWT.ARROW_LEFT: {
        			ChronoField selectedField = getSelectedField();
        			if (selectedField == ChronoField.YEAR) {
        				selectMonth();
        			}
        			else if (selectedField == ChronoField.MONTH_OF_YEAR) {
        				selectDay();
        			}
        			initBuffer();
        			break;
        		}
        		case SWT.BS:
        		case SWT.DEL: {
        			setLocalDate(null, true);
        			break;
        		}

        		default:
        			if (Character.isDigit(event.character)) {
        				addChar(event.character);
        			}
        			break;
			}
		}


		/* Catch every key but never COMMAND on a Mac or CTRL on any other system
		 * Neither when it is a stateMask nor the keyCode.
		 */
		boolean isCOMMAND = (event.stateMask & SWT.COMMAND) == SWT.COMMAND || event.keyCode == SWT.COMMAND;
		boolean isCTRL = (event.stateMask & SWT.CTRL) == SWT.CTRL || event.keyCode == SWT.CTRL;

		event.doit = SystemHelper.isMacOSX() && isCOMMAND || SystemHelper.isMacOSX() && isCTRL;
	}


	private void updateSelection() {
		String text = dateText.getText();
		if (text != null && text.length() > 0) {
			cursorPosition = dateText.getCaretPosition();

			if (cursorPosition < 3) {
				selectDay();
			}
			else if (cursorPosition < 6) {
				selectMonth();
			}
			else {
				selectYear();
			}
		}
	}


	private ModifyListener modifyListener = new ModifyListener() {
		@Override
		public void modifyText(ModifyEvent event) {
			if ( ! ignoreModifyEvents) {
				LocalDate modifiedDate = null;

				String text = dateText.getText();

				if (StringHelper.isNotEmpty(text)) {
					try {
						modifiedDate = DATE_PARSER.toLocalDate(text);
					}
					catch (DateTimeParseException e) {
						System.out.println("no date");
					}
				}

				try {
					ignoreModifyEvents = true;

					// fire SelectionEvent if the date value has changed
					boolean fireSelection = ! EqualsHelper.isEqual(modifiedDate, currentDate);
					setLocalDate(modifiedDate, fireSelection);
				}
				finally {
					ignoreModifyEvents = false;
				}
			}
		}
	};


	private void addChar(char c) {
		ChronoField selectedField = getSelectedField();

		if (selectedField == ChronoField.DAY_OF_MONTH) {
			if (getBufferLength() == 2) {
				initBuffer();
			}
			addBuffer(c);

			int day = Integer.parseInt( getBuffer() );
			if (day >= 1 && day <= 31) {
				setDay(day);
			}
		}
		else if (selectedField == ChronoField.MONTH_OF_YEAR) {
			if (getBufferLength() == 2) {
				initBuffer();
			}
			addBuffer(c);

			int month = Integer.parseInt( getBuffer() );
			if (month >= 1 && month <= 12) {
				setMonth(month);
			}
		}
		else { // selectedDatePart == ChronoField.YEAR
			if (getBufferLength() == 4) {
				initBuffer();
			}
			addBuffer(c);

			int year = Integer.parseInt( getBuffer() );
			if (year >= 0 && year <= 9999) {
				setYear(year);
			}
		}
	}


	private ChronoField getSelectedField() {
		ChronoField selectedField = ChronoField.YEAR;
		int caretPosition = dateText.getCaretPosition();
		if (caretPosition < 3) {
			selectedField = ChronoField.DAY_OF_MONTH;
		}
		else if (caretPosition < 6) {
			selectedField = ChronoField.MONTH_OF_YEAR;
		}
		return selectedField;
	}


	private void updateByArrowUp() {
		ChronoField selectedField = getSelectedField();

		if (currentDate == null) {
			currentDate = LocalDate.now();
		}

		if (selectedField == ChronoField.DAY_OF_MONTH) {
			currentDate = currentDate.plusDays(1);
		}
		else if (selectedField == ChronoField.MONTH_OF_YEAR) {
			currentDate = currentDate.plusMonths(1);
		}
		else if (selectedField == ChronoField.YEAR) {
			currentDate = currentDate.plusYears(1);
		}

		currentDate = DateHelper.ensureAD(currentDate);

		updateDateText();
	}


	private void updateByArrowDown() {
		ChronoField selectedField = getSelectedField();

		if (currentDate == null) {
			currentDate = LocalDate.now();
		}

		if (selectedField == ChronoField.DAY_OF_MONTH) {
			currentDate = currentDate.minusDays(1);
		}
		else if (selectedField == ChronoField.MONTH_OF_YEAR) {
			currentDate = currentDate.minusMonths(1);
		}
		else if (selectedField == ChronoField.YEAR) {
			currentDate = currentDate.minusYears(1);
		}

		currentDate = DateHelper.ensureAD(currentDate);

		updateDateText();
	}


	private void updateDateText() {
		String text = "";
		if (currentDate != null) {
			text = DATE_FORMATTER.format(currentDate);
		}

		int caretPosition = dateText.getCaretPosition();
		dateText.setText(text);
		dateText.setSelection(caretPosition);

		updateSelection();
		fireSelection();
	}


	private void selectDay() {
		dateText.setSelection(0, 2);
	}


	private void selectMonth() {
		dateText.setSelection(3, 5);
	}


	private void selectYear() {
		dateText.setSelection(6, 10);
	}


	public LocalDate getLocalDate() {
		return currentDate;
	}


	public I18NDate getI18NDate() {
		return TypeHelper.toI18NDate(currentDate);
	}


	protected void setLocalDate(LocalDate localDate, boolean fireSelection) {
		try {
			dontFireSelection = !fireSelection;

			currentDate = localDate;
			updateDateText();
		}
		finally {
			dontFireSelection = false;
		}
	}


	public void setLocalDate(LocalDate localDate) {
		setLocalDate(localDate, false);
	}


	public void setI18NDate(I18NDate i18nDate) {
		LocalDate localDate = TypeHelper.toLocalDate(i18nDate);
		setLocalDate(localDate, false);
	}


	@Deprecated
	public void setDate(Date date) {
		LocalDate localDate = TypeHelper.toLocalDate(date);
		setLocalDate(localDate, false);
	}


	private void setDay(int dayOfMonth) {
		if (currentDate == null) {
			currentDate = LocalDate.now();
		}

		currentDate = currentDate.withDayOfMonth(dayOfMonth);

		updateDateText();
	}


	private void setMonth(int month) {
		if (currentDate == null) {
			currentDate = LocalDate.now();
		}

		currentDate = currentDate.withMonth(month);

		updateDateText();
	}


	private void setYear(int year) {
		if (currentDate == null) {
			currentDate = LocalDate.now();
		}

		if (year < 0) {
			year = 0;
		}

		currentDate = currentDate.withYear(year);

		updateDateText();
	}


	private void initBuffer() {
		_buffer = "";
	}


	private void addBuffer(char c) {
		_buffer += c;
	}


	private int getBufferLength() {
		return _buffer.length();
	}


	private String getBuffer() {
		return _buffer;
	}


	private Image getImage() {
		// load the calendar image
		Image image = null;
		if (PlatformUI.isWorkbenchRunning()) {
			image = Images.get(Images.CALENDAR);
		}
		else {
			image = new Image(getParent().getDisplay(), "icons/" + Images.CALENDAR);

			// save the image to dispose it later
			calendarImage = image;
		}
		return image;
	}


	public void openCalendar() {
		Point size = dateChooseLabel.getSize();

		Point location = dateChooseLabel.getLocation();

		Point rightUnder = new Point(location.x + size.x, location.y + size.y);
		rightUnder = toDisplay(rightUnder);

		LocalDate newDate = DateDialog.openDateDialog(getShell(), currentDate, rightUnder);
		if (newDate != null) {
			setLocalDate(newDate, true);
		}
	}


	// **************************************************************************
	// * Modifying
	// *

	public synchronized void addSelectionListener(SelectionListener selectionListener) {
		if (selectionListenerList == null) {
			selectionListenerList = new ArrayList<>();
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

	// *
	// * Modifying
	// **************************************************************************


	public boolean isEditable() {
		return editable;
	}


	public void setEditable(boolean editable) {
		this.editable = editable;
		SWTHelper.enableTextWidget(dateText, editable);
		dateChooseLabel.setEnabled(editable);
	}


	@Override
	public void setForeground(Color color) {
		dateText.setForeground(color);
	}


	@Override
	public void setFont(Font font) {
		dateText.setFont(font);
	}

}
