package com.lambdalogic.util.rcp.datetime;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import com.lambdalogic.time.I18NDateMinute;
import com.lambdalogic.util.TypeHelper;
import com.lambdalogic.util.rcp.ModifySupport;

public class DateTimeComposite extends Composite {

	private LDate dateWidget;
	private LTime timeWidget;

	/**
	 * current date value
	 * We need this to decide if the date value has changed in modifyTest()
	 */
	private LocalDateTime dateTimeValue = null;

	private boolean ignoreSelection = false;

	private ModifySupport modifySupport = new ModifySupport(this);


	private SelectionListener selectionListener = new SelectionAdapter() {
		@Override
		public void widgetSelected(SelectionEvent e) {
			if (ignoreSelection) {
				return;
			}

			LocalDateTime widgetDateTime = getWidgetDateTime();
			if (widgetDateTime != null && ! widgetDateTime.equals(dateTimeValue)
				||
				widgetDateTime == null && dateTimeValue != null
			) {
				dateTimeValue = widgetDateTime;
				modifySupport.fire();
			}
		}
	};


	public DateTimeComposite(Composite parent, int style) {
		this(parent, style, false);
	}


	public DateTimeComposite(Composite parent, int style, boolean required) {
		super(parent, SWT.NONE);

		final GridLayout gridLayout = new GridLayout();
		gridLayout.marginWidth = 0;
		gridLayout.marginHeight = 0;
		gridLayout.numColumns = 2;
		gridLayout.makeColumnsEqualWidth = true;
		this.setLayout(gridLayout);

		dateWidget = new LDate(this, SWT.NONE, required);
		dateWidget.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));

		timeWidget = new LTime(this, SWT.NONE, required);
		timeWidget.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));

		/* We must listen to these Widgets in any case to be informed if their values changes
		 * and not only when we're observed by anyone else, because getDate() returns just
		 * dateValue.
		 */
		dateWidget.addSelectionListener(selectionListener);
		timeWidget.addSelectionListener(selectionListener);
	}


	public LocalDateTime getLocalDateTime() {
		return dateTimeValue;
	}


	public I18NDateMinute getI18NDateMinute() {
		return TypeHelper.toI18NDateMinute(dateTimeValue);
	}


	@Deprecated
	public Date getDate() {
		return TypeHelper.toDate(dateTimeValue);
	}


	/**
	 * Returns the entered date / time value.
	 * If a date is entered, the return value is not null.
	 *
	 * The entry of a time value is optional. If none has been entered, the time value will be set to midnight.
	 */
	private LocalDateTime getWidgetDateTime() {
		LocalDateTime dateTime = null;

		LocalDate dateValue = dateWidget.getLocalDate();
		LocalTime timeValue = timeWidget.getLocalTime();
		if (dateValue != null) {

			if (timeValue == null) {
				timeValue = LocalTime.of(0, 0);
			}
			else {
				timeValue = timeValue.truncatedTo(ChronoUnit.MINUTES);
			}

			dateTime = LocalDateTime.of(dateValue, timeValue);
		}

		return dateTime;
	}


	public void setLocalDateTime(LocalDateTime localDateTime) {
		try {
			ignoreSelection = true;

			dateTimeValue = localDateTime;

			LocalDate localDate = null;
			LocalTime localTime = null;
			if (localDateTime != null) {
				localDate = localDateTime.toLocalDate();
				localTime = localDateTime.toLocalTime();
			}

			dateWidget.setLocalDate(localDate);
			timeWidget.setLocalTime(localTime);
		}
		finally {
			ignoreSelection = false;
		}
	}


	public void setI18NDateMinute(I18NDateMinute i18nDateMinute) {
		LocalDateTime localDateTime = TypeHelper.toLocalDateTime(i18nDateMinute);
		setLocalDateTime(localDateTime);
	}


	@Deprecated
	public void setDate(Date date) {
		LocalDateTime localDateTime = TypeHelper.toLocalDateTime(date);
		setLocalDateTime(localDateTime);
	}


	public void addModifyListener(ModifyListener listener) {
		modifySupport.addListener(listener);
	}


	public void removeModifyListener(ModifyListener listener) {
		modifySupport.removeListener(listener);
	}


	/* We dont't provide a SelectionListener because the CDateTime
	 * does only fire a SelectionEvent if the Calendar is opened, but
	 * not when it's closed.
	 */


	@Override
	public void setEnabled(boolean enabled) {
		dateWidget.setEditable(enabled);
		timeWidget.setEditable(enabled);
		super.setEnabled(enabled);
	}


	/**
	 * Makes that the text fields visually appear to be not usable
	 */
	public void setEditable(boolean editable) {
		dateWidget.setEditable(editable);
		timeWidget.setEditable(editable);
	}

	/**
	 * Makes the date widget visible or not.
	 * @param visible
	 */
	public void setDateVisible(boolean visible) {
		dateWidget.setVisible(visible);
	}


	/**
	 * Makes the time widget visible or not.
	 *
	 * @param visible
	 */
	public void setTimeVisible(boolean visible) {
		timeWidget.setVisible(visible);
	}


	@Override
	public void setForeground(Color color) {
		dateWidget.setForeground(color);
		timeWidget.setForeground(color);
	}


	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

}
