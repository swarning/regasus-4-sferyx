package com.lambdalogic.util.rcp;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import com.lambdalogic.util.rcp.html.LazyHtmlEditor;

/**
 * A {@link Composite} that forwards calls of {@link Composite#setVisible(boolean)} to all of its children.
 * Use this {@link LazyableComposite} if one of its subordinated widgets is a {@link LazyComposite}
 * or {@link LazyHtmlEditor}.
 */
public class LazyableComposite extends Composite {

	public LazyableComposite(Composite parent, int style) {
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

}
