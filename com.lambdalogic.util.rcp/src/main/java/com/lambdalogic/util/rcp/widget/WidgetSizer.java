package com.lambdalogic.util.rcp.widget;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Text;

import com.lambdalogic.util.rcp.datetime.DateComposite;
import com.lambdalogic.util.rcp.datetime.DateTimeComposite;
import com.lambdalogic.util.rcp.datetime.TimeComposite;

public class WidgetSizer {

	private final static LocalDateTime SAMPLE_DATE_TIME = LocalDateTime.of(2000, 12, 10, 22, 30);
	private final static LocalDate SAMPLE_DATE = SAMPLE_DATE_TIME.toLocalDate();
	private final static LocalTime SAMPLE_TIME = SAMPLE_DATE_TIME.toLocalTime();


	/**
	 * Set the width of the given Text widget according to the mximum possible value.
	 * The maximum value must have been previously set using setTextLimit ().
	 * Works only in the context of GridLayout.
	 * @param textWidget
	 */
	public static void setWidth(Text textWidget) {
		GridData gridData = null;
		Object layoutData = textWidget.getLayoutData();
		if (layoutData != null) {
			if (layoutData instanceof GridData) {
				gridData = (GridData) textWidget.getLayoutData();
			}
		}
		else {
			gridData = new GridData();
			textWidget.setLayoutData(gridData);
		}

		if (gridData != null) {
			int maxLength = textWidget.getTextLimit();
			StringBuilder maxText = new StringBuilder(maxLength);
			for (int i = 0; i < maxLength; i++) {
				maxText.append("M");
			}

			String textValue = textWidget.getText();
			textWidget.setText(maxText.toString());
			gridData.minimumWidth = textWidget.computeSize(SWT.DEFAULT, SWT.DEFAULT).x;
			gridData.widthHint = gridData.minimumWidth;
			textWidget.setText(textValue);
		}
		else {
			System.err.println("WidgetSizer.setWidth() called with no GridLayout!");
		}
	}

	/**
	 * Setzt die Breite des übergebenen DecimalNumberText-Widgets gemäß des maximal möglichen Wertes.
	 * Der Maximalwert muss zuvor gesetzt worden sein.
	 * Funktioniert nur im Kontext von GridLayout.
	 * @param widget
	 */
	public static void setWidth(DecimalNumberText widget) {
		GridData gridData = null;
		Object layoutData = widget.getLayoutData();
		if (layoutData != null) {
			if (layoutData instanceof GridData) {
				gridData = (GridData) widget.getLayoutData();
			}
		}
		else {
			gridData = new GridData();
			widget.setLayoutData(gridData);
		}

		if (gridData != null) {
			BigDecimal value = widget.getValue();
			widget.setValue(widget.getMaxValue());
			gridData.minimumWidth = widget.computeSize(SWT.DEFAULT, SWT.DEFAULT).x;
			gridData.widthHint = gridData.minimumWidth;
			widget.setValue(value);
		}
		else {
			System.err.println("WidgetSizer.setWidth() called with no GridLayout!");
		}
	}


	/**
	 * Setzt die Breite des übergebenen Spinners gemäß des maximal möglichen Wertes.
	 * Der Maximalwert muss zuvor gesetzt worden sein.
	 * Funktioniert nur im Kontext von GridLayout.
	 * @param spinner
	 */
	public static void setWidth(NullableSpinner spinner) {
		GridData gridData = null;
		Object layoutData = spinner.getLayoutData();
		if (layoutData != null) {
			if (layoutData instanceof GridData) {
				gridData = (GridData) spinner.getLayoutData();
			}
		}
		else {
			gridData = new GridData();
			spinner.setLayoutData(gridData);
		}

		if (gridData != null) {
			Integer value = spinner.getValueAsInteger();
			boolean nullable = spinner.isNullable();

			spinner.setValue(spinner.getMaximum());
			spinner.setLayoutData(gridData);
			gridData.minimumWidth = spinner.computeSize(SWT.DEFAULT, SWT.DEFAULT).x;
			gridData.widthHint = gridData.minimumWidth;

			spinner.setNullable(true);
			spinner.setValue(value);
			spinner.setNullable(nullable);
		}
		else {
			System.err.println("WidgetSizer.setWidth() called with no GridLayout!");
		}
	}


	public static void setWidth(DateTimeComposite dateTimeComposite) {
		GridData gridData = null;
		Object layoutData = dateTimeComposite.getLayoutData();
		if (layoutData != null) {
			if (layoutData instanceof GridData) {
				gridData = (GridData) dateTimeComposite.getLayoutData();
			}
		}
		else {
			gridData = new GridData();
			dateTimeComposite.setLayoutData(gridData);
		}

		if (gridData != null) {
			LocalDateTime value = dateTimeComposite.getLocalDateTime();
			dateTimeComposite.setLocalDateTime(SAMPLE_DATE_TIME);
			dateTimeComposite.setLayoutData(gridData);
			gridData.minimumWidth = dateTimeComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT).x;
			gridData.widthHint = gridData.minimumWidth;
			dateTimeComposite.setLocalDateTime(value);
		}
		else {
			System.err.println("WidgetSizer.setWidth() called with no GridLayout!");
		}
	}


	public static void setWidth(DateComposite dateComposite) {
		GridData gridData = null;
		Object layoutData = dateComposite.getLayoutData();
		if (layoutData != null) {
			if (layoutData instanceof GridData) {
				gridData = (GridData) dateComposite.getLayoutData();
			}
		}
		else {
			gridData = new GridData();
			dateComposite.setLayoutData(gridData);
		}

		if (gridData != null) {
			LocalDate value = dateComposite.getLocalDate();
			dateComposite.setLocalDate(SAMPLE_DATE);
			dateComposite.setLayoutData(gridData);
			gridData.minimumWidth = dateComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT).x;
			gridData.widthHint = gridData.minimumWidth;
			dateComposite.setLocalDate(value);
		}
		else {
			System.err.println("WidgetSizer.setWidth() called with no GridLayout!");
		}
	}


	public static void setWidth(TimeComposite timeComposite) {
		GridData gridData = null;
		Object layoutData = timeComposite.getLayoutData();
		if (layoutData != null) {
			if (layoutData instanceof GridData) {
				gridData = (GridData) timeComposite.getLayoutData();
			}
		}
		else {
			gridData = new GridData();
			timeComposite.setLayoutData(gridData);
		}

		if (gridData != null) {
			LocalTime value = timeComposite.getLocalTime();
			timeComposite.setLocalTime(SAMPLE_TIME);
			timeComposite.setLayoutData(gridData);
			gridData.minimumWidth = timeComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT).x;
			gridData.widthHint = gridData.minimumWidth;
			timeComposite.setLocalTime(value);
		}
		else {
			System.err.println("WidgetSizer.setWidth() called with no GridLayout!");
		}
	}



}
