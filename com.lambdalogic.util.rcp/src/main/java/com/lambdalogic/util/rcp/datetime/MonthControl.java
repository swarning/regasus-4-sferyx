package com.lambdalogic.util.rcp.datetime;

import java.time.LocalDate;
import java.time.temporal.ChronoField;
import java.time.temporal.Temporal;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import com.lambdalogic.time.I18NMonth;
import com.lambdalogic.util.rcp.widget.NullableSpinner;

public class MonthControl extends AbstractDateControl {

	private Button emptyButton;
	private NullableSpinner yearControl;
	private NullableSpinner monthControl;


	/**
	 * Create the composite
	 * @param parent
	 * @param style
	 */
	public MonthControl(Composite parent, int style, String emptyLabelText) {
		super(parent, style);

		GridLayout gridLayout = new GridLayout();
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;
		gridLayout.numColumns = 3;
		setLayout(gridLayout);

		emptyButton = new Button(this, SWT.CHECK);
		// deactivate widgets for month and year if searching for NULL
		emptyButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				yearControl.setEnabled( ! emptyButton.getSelection());
				monthControl.setEnabled(!emptyButton.getSelection());
			}
		});
		emptyButton.setText(emptyLabelText);

		emptyButton.addSelectionListener(modifySupport);


		monthControl = new NullableSpinner(this, SWT.BORDER);
		monthControl.setMinimum(1L);
		monthControl.setMaximum(12L);

		monthControl.addModifyListener(modifySupport);


		yearControl = new NullableSpinner(this, SWT.BORDER);
		yearControl.setMinimum(1980L);
		yearControl.setMaximum(9999L);

		yearControl.addModifyListener(modifySupport);


		// initialize with current year
		int year = LocalDate.now().getYear();
		yearControl.setValue(year);
	}


	public MonthControl(Composite parent, int style) {
		this(parent, style, "Empty");
	}


	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}


	@Override
	public Temporal getTemporal() {
		Temporal temporal = null;
		if ( ! emptyButton.getSelection()) {
			Integer year = yearControl.getValueAsInteger();
			Integer month = monthControl.getValueAsInteger();
			temporal = I18NMonth.of(year, month);
		}
		return temporal;
	}


	@Override
	public void setTemporal(Temporal value) {
		emptyButton.setSelection(value == null);
		/* Button.setSelection does not fire an event.
		 * Therefore is has to be fired manually.
		 */
		emptyButton.notifyListeners(SWT.Selection, null);

		if (value != null) {
			int month = value.get(ChronoField.MONTH_OF_YEAR);
			int year = value.get(ChronoField.YEAR);

			monthControl.setValue(month);
			yearControl.setValue(year);
		}
	}


	@Override
	public String getEmptyLabelText() {
		return emptyButton.getText();
	}


	@Override
	public void setEmptyLabelText(String text) {
		emptyButton.setText(text);
	}


	// **************************************************************************
	// * Modifying
	// *

	@Override
	public void addModifyListener(ModifyListener modifyListener) {
		modifySupport.addListener(modifyListener);
	}


	@Override
	public void removeModifyListener(ModifyListener modifyListener) {
		modifySupport.removeListener(modifyListener);
	}

	// *
	// * Modifying
	// **************************************************************************


	@Override
	public boolean isNullable() {
		return true;
	}


	@Override
	public boolean isWithTime() {
		return false;
	}

}
