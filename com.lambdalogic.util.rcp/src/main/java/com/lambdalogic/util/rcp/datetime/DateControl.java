package com.lambdalogic.util.rcp.datetime;

import java.time.LocalDate;
import java.time.temporal.Temporal;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

public class DateControl extends AbstractDateControl {

	private Button emptyButton;
	private DateComposite dateComposite;

	/**
	 * Create the composite
	 * @param parent
	 * @param style
	 */
	public DateControl(Composite parent, int style, String emptyLabelText) {
		super(parent, style);

		GridLayout gridLayout = new GridLayout();
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;
		gridLayout.numColumns = 3;
		setLayout(gridLayout);

		emptyButton = new Button(this, SWT.CHECK);
		// disable date/time widget if user want to search for null values
		emptyButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				dateComposite.setEnabled( ! emptyButton.getSelection());
			}
		});
		emptyButton.setText(emptyLabelText);

		emptyButton.addSelectionListener(modifySupport);


		dateComposite = new DateComposite(this, SWT.BORDER);
		dateComposite.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false));

		dateComposite.addModifyListener(modifySupport);
	}


	public DateControl(Composite parent, int style) {
		this(parent, style, "Empty");
	}


	@Override
	public boolean isWithTime() {
		return false;
	}


	@Override
	public boolean isNullable() {
		return true;
	}


	@Override
	public Temporal getTemporal() {
		Temporal temporal = null;
		if ( ! emptyButton.getSelection()) {
			temporal = dateComposite.getLocalDate();
		}
		return temporal;
	}


	@Override
	public void setTemporal(Temporal temporal) {
		emptyButton.setSelection(temporal == null);

		// Button.setSelection does not fire an event. Therefore is has to be fired manually.
		emptyButton.notifyListeners(SWT.Selection, null);

		if (temporal != null) {
			LocalDate localDate = LocalDate.from(temporal);
			dateComposite.setLocalDate(localDate);
		}
	}


	@Override
	public String getEmptyLabelText() {
		return emptyButton.getText();
	}


	@Override
	public void setEmptyLabelText(String text) {
		if (emptyButton != null) {
			emptyButton.setText(text);
		}
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

}
