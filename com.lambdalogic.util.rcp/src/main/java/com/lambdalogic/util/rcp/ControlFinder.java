package com.lambdalogic.util.rcp;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class ControlFinder {

	public static Control findControl(Control control, Object data) {
		Control targetControl = null;

		if (control.getData() == data) {
			targetControl = control;
		}
		else if (control instanceof Composite) {
			targetControl = findFocusControlInComposite((Composite) control, data);
		}

		return targetControl;
	}


	private static Control findFocusControlInComposite(Composite composite, Object data) {
		Control targetControl = null;

		Control[] childControls = composite.getChildren();
		for (Control control : childControls) {
			targetControl = findControl(control, data);
			if (targetControl != null) {
				break;
			}
		}

		return targetControl;
	}

}
