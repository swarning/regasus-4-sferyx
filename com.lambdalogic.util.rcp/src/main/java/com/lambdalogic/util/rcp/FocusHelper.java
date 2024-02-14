package com.lambdalogic.util.rcp;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class FocusHelper {

	public static boolean containsFocusControl(Composite composite) {
		Control focusControl = findFocusControl(composite);
		return focusControl != null;
	}


	public static Control findFocusControl(Control control) {
		Control focusControl = null;

		if (control.isFocusControl()) {
			focusControl = control;
		}
		else if (control instanceof Composite) {
			focusControl = findFocusControlInComposite((Composite) control);
		}

		return focusControl;
	}


	private static Control findFocusControlInComposite(Composite composite) {
		Control focusControl = null;

		Control[] childControls = composite.getChildren();
		for (Control control : childControls) {
			focusControl = findFocusControl(control);
			if (focusControl != null) {
				break;
			}
		}

		return focusControl;
	}

}
