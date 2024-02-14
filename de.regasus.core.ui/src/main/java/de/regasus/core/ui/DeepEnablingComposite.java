package de.regasus.core.ui;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * A composite that enables or disables all of it's children together 
 * when itself gets enabled or disabled. If you disable a normal composite,
 * the children ain't usable, but have an appearance as if they were!
 * 
 * @author manfred
 *
 */
public class DeepEnablingComposite extends Composite {

	public DeepEnablingComposite(Composite parent, int style) {
		super(parent, style);
	}

	@Override
	public void setEnabled(boolean enabled) {
		for ( Control control : this.getChildren()) {
			control.setEnabled(enabled);
		}
		super.setEnabled(enabled);
	}
}
