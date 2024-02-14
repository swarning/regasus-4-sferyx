package de.regasus.core.ui.groups;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

public class OptionsGroup extends Group {

	private Button[] buttons;

	public OptionsGroup(Composite parent, int style, String title, String... options) {
		super(parent, style);

		if (title != null) {
			setText(title);
		}

		setLayout(new GridLayout(1, false));

		buttons = new Button[options.length];

		for (int i = 0; i < options.length; i++) {
			buttons[i] = new Button(this, SWT.RADIO);
			buttons[i].setText(options[i]);
			buttons[i].setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false));
		}
	}

	public int getOptionNo() {
		for(int i = 0; i < buttons.length; i++) {
			if ( buttons[i].getSelection() )
				return i;
		}
		return -1;
	}

	public void setOptionNo(int no) {
		for (int i = 0; i < buttons.length; i++) {
			buttons[i].setSelection(no == i);
		}
	}

	@Override
	protected void checkSubclass() {
		// Disables the inherited check that forbids subclassing
	}

}
