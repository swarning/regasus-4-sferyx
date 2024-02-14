package com.lambdalogic.util.rcp.widget;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import com.lambdalogic.util.rcp.UtilI18N;

public class BooleanRadio extends Composite {
	
	public static final String DEFAULT_TRUE_LABEL = UtilI18N.Yes;
	public static final String DEFAULT_FALSE_LABEL = UtilI18N.No;

	private Button trueButton;
	private Button falseButton;
	
	
	public BooleanRadio(Composite parent, int style) {
		super(parent, style);
		
		setLayout(new GridLayout(2, false));
		
		trueButton = new Button(this, SWT.RADIO);
		trueButton.setText(DEFAULT_TRUE_LABEL);
		trueButton.setLayoutData( new GridData(SWT.LEFT, SWT.CENTER, false, false) );
		
		falseButton = new Button(this, SWT.RADIO);
		falseButton.setText(DEFAULT_FALSE_LABEL);
		falseButton.setLayoutData( new GridData(SWT.LEFT, SWT.CENTER, false, false) );
	}
	
	
	@Override
	public void setEnabled (boolean enabled) {
		trueButton.setEnabled(enabled);
		falseButton.setEnabled(enabled);
	}
	
	
	public void setTrueLabel(String label) {
		trueButton.setText(label);
	}
	
	
	public void setFalseLabel(String label) {
		falseButton.setText(label);
	}
	
	
	public Boolean getValue() {
		Boolean value = null;
		if (trueButton.getSelection()) {
			value = Boolean.TRUE;
		}
		else if (falseButton.getSelection()) {
			value = Boolean.FALSE;
		}
		return value;
	}
	
	
	public void setValue(Boolean value) {
		trueButton.setSelection(value == Boolean.TRUE);
		falseButton.setSelection(value == Boolean.FALSE);
	}
	
	
	public void addSelectionListener(SelectionListener listener) {
		trueButton.addSelectionListener(listener);
		falseButton.addSelectionListener(listener);
	}
	
	
	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}
	
}
