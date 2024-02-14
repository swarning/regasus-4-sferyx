package de.regasus.users.user.dialog;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import de.regasus.users.IconRegistry;

public class RightButton extends Button {

	private Boolean value = null;
	
	public RightButton(Composite parent, int style) {
		super(parent, style);
		
		addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
				propagateState();
			};
		});
		propagateState();
		setAlignment(SWT.RIGHT);
	}

	protected void propagateState() {
		if (value == null) {
			value = Boolean.TRUE;
			setImage(IconRegistry.getImage("icons/add.gif"));
		}
		else if (value.equals(Boolean.TRUE)) {
			value = Boolean.FALSE;
			setImage(IconRegistry.getImage("icons/forbidden.gif"));
		} 
		else if (value.equals(Boolean.FALSE)) {
			value = null;
			setImage(IconRegistry.getImage("icons/undefined.png"));
		}
		
	}
	

	@Override
	protected void checkSubclass() {
	}

	public Boolean getValue() {
		return value;
	}
	
}
