package com.lambdalogic.util.rcp.datetime;

import java.util.Calendar;
import java.util.Date;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.lambdalogic.time.I18NMonth;
import com.lambdalogic.util.rcp.widget.NullableSpinner;
import com.lambdalogic.util.rcp.widget.WidgetSizer;

public class MonthComposite extends Composite {

	private Label monthLabel;
	private NullableSpinner monthSpinner;
	private Label yearLabel;
	private NullableSpinner yearSpinner;


	/**
	 * Create the composite
	 * @param parent
	 * @param style
	 */
	public MonthComposite(Composite parent, int style) {
		super(parent, style);

		GridLayout gridLayout = new GridLayout();
		gridLayout.marginWidth = 0;
		gridLayout.marginHeight = 0;
		gridLayout.numColumns = 4;
		setLayout(gridLayout);

		monthLabel = new Label(this, SWT.NONE);
		final GridData gd_monthLabel = new GridData(SWT.FILL, SWT.CENTER, false, false);
		monthLabel.setLayoutData(gd_monthLabel);
		monthLabel.setText("Month");

//		final Composite monthComposite = new Composite(this, style);
//		monthComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
//		monthComposite.setLayout(new FillLayout());
//		monthSpinner = new NullableSpinner(monthComposite, CDT.BORDER | CDT.DROP_DOWN | CDT.DATE_MEDIUM);

		monthSpinner = new NullableSpinner(this, SWT.NONE);
		monthSpinner.setMinimum(1L);
		monthSpinner.setMaximum(12L);

		// calculate and set the maximum width
		WidgetSizer.setWidth(monthSpinner);

		yearLabel = new Label(this, SWT.NONE);
		final GridData gd_yearLabel = new GridData();
		gd_yearLabel.horizontalIndent = 5;
		yearLabel.setLayoutData(gd_yearLabel);
		yearLabel.setText("Year");


		yearSpinner = new NullableSpinner(this, SWT.NONE);
		yearSpinner.setMinimum(1980L);
		yearSpinner.setMaximum(2099L);
		// set startValue to current year
		Calendar cal = Calendar.getInstance();
		long currentYear = cal.get(Calendar.YEAR);
		yearSpinner.setStartValue(currentYear);

		// calculate and set the maximum width
		WidgetSizer.setWidth(yearSpinner);
	}


	@Override
	public void setEnabled(boolean enabled) {
		yearLabel.setEnabled(enabled);
		yearSpinner.setEnabled(enabled);

		monthLabel.setEnabled(enabled);
		monthSpinner.setEnabled(enabled);

		super.setEnabled(enabled);
	}


	public I18NMonth getI18NMonth() {
		I18NMonth i18nMonth = null;

		Integer month = getMonth();
		Integer year = getYear();
		if (month != null && year != null) {
			i18nMonth = I18NMonth.of(year, month);
		}

		return i18nMonth;
	}


	public Integer getMonth() {
		return monthSpinner.getValueAsInteger();
	}


	public Integer getYear() {
		return yearSpinner.getValueAsInteger();
	}


	public void setI18NMonth(I18NMonth i18nMonth) {
		if (i18nMonth != null) {
			int month = i18nMonth.getMonthValue();
			int year = i18nMonth.getYear();

			monthSpinner.setValue(month);
			yearSpinner.setValue(year);
		}
		else {
			monthSpinner.setValue((Long) null);
			yearSpinner.setValue((Long) null);
		}
	}


	@Deprecated
	public void setDate(Date date) {
		if (date != null) {
			Calendar cal = Calendar.getInstance();
			cal.setTime(date);

			int month = cal.get(Calendar.MONTH) + 1;
			int year = cal.get(Calendar.YEAR);

			monthSpinner.setValue(month);
			yearSpinner.setValue(year);
		}
		else {
			monthSpinner.setValue((Long) null);
			yearSpinner.setValue((Long) null);
		}
	}


	public String getMonthLabelText() {
		return monthLabel.getText();
	}


	public void setMonthLabelText(String text) {
		monthLabel.setText(text);
	}


	public String getYearLabelText() {
		return yearLabel.getText();
	}


	public void setYearLabelText(String text) {
		yearLabel.setText(text);
	}


	public void addModifyListener(ModifyListener listener) {
		monthSpinner.addModifyListener(listener);
		yearSpinner.addModifyListener(listener);
	}


	public void removeModifyListener(ModifyListener listener) {
		monthSpinner.removeModifyListener(listener);
		yearSpinner.removeModifyListener(listener);
	}


	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

}
