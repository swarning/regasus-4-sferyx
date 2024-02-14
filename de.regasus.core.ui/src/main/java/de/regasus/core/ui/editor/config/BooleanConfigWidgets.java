package de.regasus.core.ui.editor.config;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.lambdalogic.util.rcp.ModifyListenerAdapter;

/**
 * Set of widgets to display the values of a Boolean that may be null.
 */
public class BooleanConfigWidgets {

	public static final int NUM_COLS = 2;

	// Widgets
	private Label label;
	private Button yesCheckbox;


	public BooleanConfigWidgets(Composite parent, String name, String toolTip) {
		label = new Label(parent, SWT.NONE);
		label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false));
		label.setText(name);
		label.setToolTipText(toolTip);

		yesCheckbox = new Button(parent, SWT.CHECK);
	}


	public BooleanConfigWidgets(Composite parent, String name) {
		this(parent, name, null);
	}


	public void addModifyListener(ModifyListener modifyListener) {
		ModifyListenerAdapter adapter = new ModifyListenerAdapter(modifyListener);

		// add this as ModifyListener to all widgets
		yesCheckbox.addSelectionListener(adapter);
	}


	public boolean getValue() {
		return yesCheckbox.getSelection();
	}


	public void setValue(boolean value) {
		yesCheckbox.setSelection(value);
	}


	public void setEnabled(boolean enabled) {
		yesCheckbox.setEnabled(enabled);
	}

}
