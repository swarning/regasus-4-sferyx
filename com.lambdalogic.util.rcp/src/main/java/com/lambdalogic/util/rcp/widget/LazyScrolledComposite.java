package com.lambdalogic.util.rcp.widget;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import com.lambdalogic.util.rcp.LazyComposite;

public class LazyScrolledComposite extends ScrolledComposite {

	public LazyScrolledComposite(Composite parent, int style) {
		super(parent, style);
	}


	@Override
	public void setVisible(boolean visible) {
		Control content = getContent();
		if (content != null) {
			initLazyComposites(content);
			refreshScrollbars();
		}

		super.setVisible(visible);
	}


	private static void initLazyComposites(Control control) {
		if (control instanceof LazyComposite) {
			((LazyComposite) control).init();
		}
		else if (control instanceof Composite) {
			Composite composite = (Composite) control;
			Control[] children = composite.getChildren();
			for (Control childControl : children) {
				initLazyComposites(childControl);
			}
		}
	}


	/**
	 * Call this method after you created the child components, and also in case the content has changed
	 * (eg child composites have been added, opened or closed).
	 * <p>
	 * Child components of type {@link MultiLineText} subclass will do this automatically.
	 */
	public void refreshScrollbars() {
		Control content = getContent();
		if (content != null) {
    		// calculate new height based on current width
    		Rectangle clientArea = getClientArea();
    		Point size = content.computeSize(clientArea.width, SWT.DEFAULT);

    		// set new height as minHeight
    		setMinHeight(size.y);
		}
	}

}
