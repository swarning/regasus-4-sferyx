package com.lambdalogic.util.rcp.datetime;

import java.time.LocalTime;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;

import com.lambdalogic.time.I18NMinute;
import com.lambdalogic.util.TypeHelper;
import com.lambdalogic.util.rcp.ModifySupport;

public class TimeComposite extends Composite {

	private LTime timeWidget;

	/**
	 * current date value
	 * We need this to decide if the date value has changed in modifyTest()
	 */
	private LocalTime timeValue = null;

	private boolean ignoreSelection = false;

	private ModifySupport modifySupport = new ModifySupport(this);


	private SelectionListener selectionListener = new SelectionAdapter() {
		@Override
		public void widgetSelected(SelectionEvent e) {
			if (ignoreSelection) {
				return;
			}

			LocalTime widgetTime = getWidgetTime();
			if (widgetTime != null && ! widgetTime.equals(timeValue)
				||
				widgetTime == null && timeValue != null
			) {
				timeValue = widgetTime;
				modifySupport.fire();
			}
		}
	};


	/**
	 * Create the composite
	 * @param parent
	 * @param style
	 */
	public TimeComposite(Composite parent, int style) {
		super(parent, SWT.NONE);
		setLayout(new FillLayout());

		timeWidget = new LTime(this, SWT.NONE);

		/* We must listen to these Widgets in any case to be informed if their values changes
		 * and not only when we're observed by anyone else, because getTime() returns just
		 * timeValue.
		 */
		timeWidget.addSelectionListener(selectionListener);
	}


	public LocalTime getLocalTime() {
		return timeValue;
	}


	public I18NMinute getI18NMinute() {
		return TypeHelper.toI18NMinute(timeValue);
	}


	/**
	 * Returns the entered time value.
	 * If a time is entered, the return value is not null.
	 */
	private LocalTime getWidgetTime() {
		return timeWidget.getLocalTime();
	}


	public void setLocalTime(LocalTime time) {
		try {
			ignoreSelection = true;

			timeValue = time;

	        timeWidget.setLocalTime(time);
		}
		finally {
			ignoreSelection = false;
		}
	}


	public void setI18NMinute(I18NMinute i18nMinute) {
		LocalTime localTime = TypeHelper.toLocalTime(i18nMinute);
		setLocalTime(localTime);
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
		timeWidget.setEditable(enabled);
		super.setEnabled(enabled);
	}


	/**
	 * Makes that the text fields visually appear to be not usable
	 */
	public void setEditable(boolean editable) {
		timeWidget.setEditable(editable);
	}


	@Override
	public void setForeground(Color color) {
		timeWidget.setForeground(color);
	}


	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

}
