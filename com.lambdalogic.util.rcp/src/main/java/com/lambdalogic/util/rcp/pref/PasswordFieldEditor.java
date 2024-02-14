package com.lambdalogic.util.rcp.pref;

import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

/**
 * A StringFieldEditor with a password style.
 */
public class PasswordFieldEditor extends StringFieldEditor {

	public PasswordFieldEditor(String name, String labelText, Composite parent) {
		super(name, labelText, parent);
	}


	@Override
	protected Text createTextWidget(Composite parent) {
		return new Text(parent, SWT.SINGLE | SWT.BORDER | SWT.PASSWORD);
	}

}