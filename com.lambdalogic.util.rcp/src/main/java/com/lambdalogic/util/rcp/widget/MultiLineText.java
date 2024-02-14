package com.lambdalogic.util.rcp.widget;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import com.lambdalogic.util.SystemHelper;
import com.lambdalogic.util.rcp.MultiLineTextTraverseListener;

/**
 * A text widget that adapts is height suitably to always show all of the text lines it contains.
 * It is required to appear whithin a composite with {@link GridLayout}.
 */
public class MultiLineText extends Text {

	protected boolean dynamic;

	protected int minLineCount = 1;
	protected int previousLineCount = minLineCount;

	protected int layoutDepth = 1;

	protected static int modifyStyle(int style, boolean dynamic) {
		style = style | SWT.MULTI;
		if (dynamic) {
			// bei dynamische Höhenanpassung keinen vertikalen ScrollBar
			style = style & ~SWT.V_SCROLL;
		}
		else {
			// wenn keine dynamische Höhenanpassung, dann vertikalen ScrollBar
			style = style | SWT.V_SCROLL;
		}

		if ((style & SWT.H_SCROLL) != SWT.NONE) {
			// wenn horizontaler ScrollBar, dann kein automatischer Zeilenumbruch
			style = style & ~SWT.WRAP;
		}
		else if ((style & SWT.WRAP) != SWT.NONE) {
			// wenn automatischer Zeilenumbruch, dann kein horizontaler ScrollBar
			style = style & ~SWT.H_SCROLL;
		}

		if (SystemHelper.isMacOSX() && (style & SWT.WRAP) == SWT.NONE) {
			// wenn kein automatischer Zeilenumbruch, dann horizontaler ScrollBar
			style = style | SWT.H_SCROLL;
		}

		return style;
	}


	public MultiLineText(Composite parent, int style, boolean dynamic) {
		super(parent, modifyStyle(style, dynamic));
		this.dynamic = dynamic;

		if (dynamic) {
			this.addModifyListener(new ModifyListener() {
				@Override
				public void modifyText(ModifyEvent e) {
					adaptHeight();
				}
			});
		}

		this.addTraverseListener(new MultiLineTextTraverseListener(this));
	}


	public MultiLineText(Composite parent, int style) {
		this(parent, style, true);
	}


	/**
	 * Adapt the height of this Text widget to its content.
	 */
	protected void adaptHeight() {
		int requiredLineCount = getContentLineCount();

		// Show at least min lines (may be more than 1), even if there are less line breaks
		if (requiredLineCount < minLineCount) {
			requiredLineCount = minLineCount;
		}

		// Don't do anything if required line count doesn't change
		if (previousLineCount != requiredLineCount) {
    		previousLineCount = requiredLineCount;

    		// Compute the height in pixels needed to show the required lines
    		int height = SWTHelper.computeTextWidgetHeightForLineCount(this, requiredLineCount);

    		// Take the GridData associated with this widget (creating a default one if needed)
    		// and set the height in pixels
    		Object layoutData = getLayoutData();
    		GridData gridData = null;
    		if (layoutData == null) {
    			gridData = new GridData();
    			setLayoutData(gridData);
    		}
    		if (layoutData instanceof GridData) {
    			gridData = (GridData) layoutData;
    		}
    		if (gridData != null) {
    			gridData.heightHint = height;
    		}

    		// layout grand parent or parent
    		Composite parent = getParent();
    		parent.layout();
    		Composite grandParent = parent.getParent();
    		if (grandParent != null) {
    			grandParent.layout();
    		}

    		SWTHelper.refreshSuperiorScrollbar(this);
		}
	}


	/**
	 * Number of lines of the content.
	 * Attention: The method name is different from the existing method super.getLineCount().
	 * @return
	 */
	public int getContentLineCount() {
		int lineCount = 1;

		// Count the number of line breaks as fast as possible
		String text = getText();
		int i = text.length();
		while (--i >= 0) {
			if (text.charAt(i) == '\n') {
				lineCount++;
			}
		}

		return lineCount;
	}


	public int getMinLineCount() {
		return minLineCount;
	}


	public void setMinLineCount(int minLineCount) {
		this.minLineCount = minLineCount;
		// do not call  adaptHeight()  because this will initialize layoutData at the wrong time
	}


	@Override
	public String getText() {
		String text = super.getText();

		// MIRCP-1963 - Replace \r\n which is read from superclass by \n, so that the presentation is
		// correct eg for address labels when there is a multi-line organisation
		if (text != null) {
			text = text.replace("\r\n", "\n");
		}

		return text;
	}


	@Override
	public void setText(String text) {
		if (text != null) {
			text = text.replace("\r\n", "\n");
		}

		super.setText(text);
	}


	@Override
	protected void checkSubclass() {
	}

}
