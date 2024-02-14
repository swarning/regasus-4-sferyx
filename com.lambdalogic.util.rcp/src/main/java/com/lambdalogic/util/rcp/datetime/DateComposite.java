package com.lambdalogic.util.rcp.datetime;

import java.time.LocalDate;
import java.util.Date;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;

import com.lambdalogic.time.I18NDate;
import com.lambdalogic.util.TypeHelper;
import com.lambdalogic.util.rcp.ModifySupport;


public class DateComposite extends Composite {

	private LDate dateWidget;

	/**
	 * current date value
	 * We need this to decide if the date value has changed in modifyTest()
	 */
	private LocalDate dateValue = null;

	private boolean ignoreSelection = false;

	private ModifySupport modifySupport = new ModifySupport(this);


	private SelectionListener selectionListener = new SelectionAdapter() {
		@Override
		public void widgetSelected(SelectionEvent e) {
			if (ignoreSelection) {
				return;
			}

			LocalDate widgetDate = getWidgetDate();
			if (widgetDate != null && ! widgetDate.equals(dateValue)
				||
				widgetDate == null && dateValue != null
			) {
				dateValue = widgetDate;
				modifySupport.fire();
			}
		}
	};


	public DateComposite(Composite parent, int style) {
		this(parent, style, false);
	}


	/**
	 * Create the composite
	 * @param parent
	 * @param style
	 */
	public DateComposite(Composite parent, int style, boolean required) {
		super(parent, SWT.NONE);
		setLayout(new FillLayout());

		dateWidget = new LDate(this, SWT.NONE, required);

		/* We must listen to these Widgets in any case to be informed if their values changes
		 * and not only when we're observed by anyone else, because getDate() returns just
		 * dateValue.
		 */
		dateWidget.addSelectionListener(selectionListener);
	}


	public LocalDate getLocalDate() {
		return dateValue;
	}


	public I18NDate getI18NDate() {
		return TypeHelper.toI18NDate(dateValue);
	}


	@Deprecated
	public Date getDate() {
		return TypeHelper.toDate(dateValue);
	}


	/**
	 * Returns the entered date value.
	 * If a date is entered, the return value is not null.
	 */
	private LocalDate getWidgetDate() {
		return dateWidget.getLocalDate();
	}


	public void setLocalDate(LocalDate date) {
		try {
			ignoreSelection = true;

			dateValue = date;

			dateWidget.setLocalDate(date);
		}
		finally {
			ignoreSelection = false;
		}
	}


	public void setI18NDate(I18NDate i18nDate) {
		LocalDate localDate = TypeHelper.toLocalDate(i18nDate);
		setLocalDate(localDate);
	}


	@Deprecated
	public void setDate(Date date) {
		LocalDate localDate = TypeHelper.toLocalDate(date);
		setLocalDate(localDate);
	}


	public void addModifyListener(ModifyListener listener) {
		modifySupport.addListener(listener);
	}


	public void removeModifyListener(ModifyListener listener) {
		modifySupport.removeListener(listener);
	}


	/* We don't provide a SelectionListener because the CDateTime
	 * does only fire a SelectionEvent if the Calendar is opened, but
	 * not when it's closed.
	 */


	@Override
	public void setEnabled(boolean enabled) {
		dateWidget.setEditable(enabled);
		super.setEnabled(enabled);
	}


	/**
	 * Makes that the text fields visually appear to be not usable
	 */
	public void setEditable(boolean editable) {
		dateWidget.setEditable(editable);
	}


	@Override
	public void setForeground(Color color) {
		dateWidget.setForeground(color);
	}


	@Override
	public void setFont(Font font) {
		dateWidget.setFont(font);
	}


	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

}
