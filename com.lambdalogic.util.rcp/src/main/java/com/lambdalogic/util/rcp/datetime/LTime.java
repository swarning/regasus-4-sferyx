package com.lambdalogic.util.rcp.datetime;

import java.text.ParseException;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import com.lambdalogic.time.I18NMinute;
import com.lambdalogic.time.TimeFormatter;
import com.lambdalogic.time.TimeParser;
import com.lambdalogic.util.EqualsHelper;
import com.lambdalogic.util.StringHelper;
import com.lambdalogic.util.SystemHelper;
import com.lambdalogic.util.TypeHelper;
import com.lambdalogic.util.rcp.widget.SWTHelper;

public class LTime extends Composite {

	/**
	 * Widget to display and change the data/time value.
	 * Works like a spinner.
	 */
	private CopyAndPasteText timeText;

	private static final String TIME_PATTERN = "HH:mm";
	private static final TimeFormatter TIME_FORMATTER = TimeFormatter.getInstance(TIME_PATTERN);
	private static final TimeParser TIME_PARSER = TimeParser.getInstance(TIME_PATTERN);

	/**
	 * Current cursor position in dateText.
	 */
	private int cursorPosition;


	private LocalTime currentTime;

	/**
	 * Buffer for keyboard input.
	 */
	private String _buffer = "";


	protected List<SelectionListener> selectionListenerList = null;


	private boolean dontFireSelection = false;
	private boolean ignoreModifyEvents = false;


	private boolean editable = true;


	public LTime(final Composite parent, final int style) {
		this(parent, style, false);
	}


	public LTime(final Composite parent, final int style, boolean required) {
		super(parent, SWT.FILL);

		GridLayout gridLayout = new GridLayout();
		gridLayout.marginWidth = 0;
		gridLayout.marginHeight = 0;
		gridLayout.numColumns = 1;
		this.setLayout(gridLayout);

		timeText = new CopyAndPasteText(this, SWT.BORDER | SWT.FILL);

		// First make the font bold, then compute the width, cause bold fonts have different metrics
		if (required) {
			SWTHelper.makeBold(timeText);
		}

		GridData layoutData = new GridData(SWT.FILL, SWT.CENTER, false, false);
		// set width as necessary for 5 characters (e.g. 23:50 is 5 characters)
		layoutData.widthHint = SWTHelper.computeTextWidgetWidthForCharCount(timeText, 5);

		timeText.setLayoutData(layoutData);

		// add observers
		timeText.addCopyAndPasteListener(copyAndPasteListener);
		timeText.addListener(SWT.KeyDown, listener);
		timeText.addListener(SWT.MouseUp, listener);

		timeText.addModifyListener(modifyListener);
	}


	private CopyAndPasteListener copyAndPasteListener = new CopyAndPasteAdapter() {
		ChronoField selectedField;

		@Override
		public void beforePaste(CopyAndPasteEvent event) {
			// save selected field
			selectedField = getSelectedField();

			String clipboardText = event.getText();

			// first try to convert clipboardText to Date
			try {
				LocalTime localTime = TypeHelper.toLocalTime(clipboardText);
				setLocalTime(localTime);
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
			if (selectedField == ChronoField.HOUR_OF_DAY) {
				selectHour();
			}
			else if (selectedField == ChronoField.MINUTE_OF_HOUR) {
				selectMinute();
			}
		}


		@Override
		public void beforeCut(CopyAndPasteEvent e) {
			// copy the whole text into the clipboard
			String text = timeText.getText();
			e.setText(text);
		}


		@Override
		public void afterCut(CopyAndPasteEvent e) {
			// delete text (and indirectly the date)
			timeText.setText("");
		}


		@Override
		public void beforeCopy(CopyAndPasteEvent e) {
			// copy the whole text into the clipboard
			String text = timeText.getText();
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
        		case ':':
        		{
        			ChronoField selectedField = getSelectedField();
        			if (selectedField == ChronoField.HOUR_OF_DAY) {
        				selectMinute();
        			}
        			initBuffer();
        			break;
        		}
        		case SWT.ARROW_LEFT: {
        			ChronoField selectedField = getSelectedField();
        			if (selectedField == ChronoField.MINUTE_OF_HOUR) {
        				selectHour();
        			}
        			initBuffer();
        			break;
        		}
        		case SWT.BS:
        		case SWT.DEL: {
        			setLocalTime(null, true);
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
		String text = timeText.getText();
		if (text != null && text.length() > 0) {
			cursorPosition = timeText.getCaretPosition();

			if (cursorPosition < 3) {
				selectHour();
			}
			else {
				selectMinute();
			}
		}
	}

	private ModifyListener modifyListener = new ModifyListener() {
		@Override
		public void modifyText(ModifyEvent event) {
			if ( ! ignoreModifyEvents) {
				LocalTime modifiedTime = null;

				String text = timeText.getText();

				if (StringHelper.isNotEmpty(text)) {
					try {
						 modifiedTime = TIME_PARSER.toLocalTime(text);
						 modifiedTime = modifiedTime.truncatedTo(ChronoUnit.MINUTES);
					}
					catch (DateTimeParseException e) {
						System.out.println("no time");
					}
				}

				try {
					ignoreModifyEvents = true;

					// fire SelectionEvent if the time value has changed
					boolean fireSelection = ! EqualsHelper.isEqual(modifiedTime, currentTime);
					setLocalTime(modifiedTime, fireSelection);
				}
				finally {
					ignoreModifyEvents = false;
				}
			}
		}
	};


	private void addChar(char c) {
		ChronoField selectedField = getSelectedField();

		if (selectedField == ChronoField.HOUR_OF_DAY) {
			if (getBufferLength() == 2) {
				initBuffer();
			}
			addBuffer(c);

			int hour = Integer.parseInt( getBuffer() );
			if (hour >= 0 && hour <= 23) {
				setHour(hour);
			}
		}
		else { // selectedDatePart == Calendar.MINUTE
			if (getBufferLength() == 2) {
				initBuffer();
			}
			addBuffer(c);

			int minute = Integer.parseInt( getBuffer() );
			if (minute >= 0 && minute <= 59) {
				setMinute(minute);
			}
		}
	}


	private ChronoField getSelectedField() {
		ChronoField selectedField = ChronoField.MINUTE_OF_HOUR;
		int caretPosition = timeText.getCaretPosition();
		if (caretPosition < 3) {
			selectedField = ChronoField.HOUR_OF_DAY;
		}
		return selectedField;
	}


	private void updateByArrowUp() {
		ChronoField selectedField = getSelectedField();

		if (currentTime == null) {
			currentTime = LocalTime.now().truncatedTo(ChronoUnit.MINUTES);
		}

		if (selectedField == ChronoField.HOUR_OF_DAY) {
			currentTime = currentTime.plusHours(1);
		}
		else if (selectedField == ChronoField.MINUTE_OF_HOUR) {
			currentTime = currentTime.plusMinutes(1);
		}

		updateDateText();
	}


	private void updateByArrowDown() {
		ChronoField selectedField = getSelectedField();

		if (currentTime == null) {
			currentTime = LocalTime.now().truncatedTo(ChronoUnit.MINUTES);
		}

		if (selectedField == ChronoField.HOUR_OF_DAY) {
			currentTime = currentTime.minusHours(1);
		}
		else if (selectedField == ChronoField.MINUTE_OF_HOUR) {
			currentTime = currentTime.minusMinutes(1);
		}

		updateDateText();
	}


	private void updateDateText() {
		String text = "";
		if (currentTime != null) {
			text = TIME_FORMATTER.format(currentTime);
		}

		int caretPosition = timeText.getCaretPosition();
		timeText.setText(text);
		timeText.setSelection(caretPosition);

		updateSelection();
		fireSelection();
	}


	private void selectHour() {
		timeText.setSelection(0, 2);
	}


	private void selectMinute() {
		timeText.setSelection(3, 5);
	}


	public LocalTime getLocalTime() {
		return currentTime;
	}


	public I18NMinute getI18NMinute() {
		return TypeHelper.toI18NMinute(currentTime);
	}


	protected void setLocalTime(LocalTime localTime, boolean fireSelection) {
		try {
			dontFireSelection = !fireSelection;

			currentTime = localTime;
			if (currentTime != null) {
				currentTime = currentTime.truncatedTo(ChronoUnit.MINUTES);
			}
			updateDateText();
		}
		finally {
			dontFireSelection = false;
		}
	}


	public void setLocalTime(LocalTime localTime) {
		setLocalTime(localTime, false);
	}


	public void setI18NMinute(I18NMinute i18nMinute) {
		LocalTime localTime = TypeHelper.toLocalTime(i18nMinute);
		setLocalTime(localTime, false);
	}


	private void setHour(int hour) {
		if (currentTime == null) {
			currentTime = LocalTime.now();
		}

		currentTime = currentTime.withHour(hour);

		updateDateText();
	}


	private void setMinute(int minute) {
		if (currentTime == null) {
			currentTime = LocalTime.now();
		}

		currentTime = currentTime.withMinute(minute);

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
		SWTHelper.enableTextWidget(timeText, editable);
	}


	@Override
	public void setForeground(Color color) {
		timeText.setForeground(color);
	}


	@Override
	public void setFont(Font font) {
		timeText.setFont(font);
	}

}
