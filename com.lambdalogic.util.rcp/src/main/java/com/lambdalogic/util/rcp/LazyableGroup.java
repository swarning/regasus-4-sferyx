package com.lambdalogic.util.rcp;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;

import com.lambdalogic.util.rcp.html.LazyHtmlEditor;

/**
 * A {@link Group} that forwards calls of {@link Group#setVisible(boolean)} to all of its children.
 * Use this {@link LazyableComposite} if one of its subordinated widgets is a {@link LazyComposite}
 * or {@link LazyHtmlEditor}.
 */
public class LazyableGroup extends Group {

	public LazyableGroup(Composite parent, int style) {
		super(parent, style);
	}


	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);

		// forward to children
		for (Control control : getChildren()) {
			control.setVisible(visible);
		}
	}


	@Override
	protected void checkSubclass() {
		// ignore
	}

}
