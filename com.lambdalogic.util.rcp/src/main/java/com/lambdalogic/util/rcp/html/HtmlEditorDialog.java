package com.lambdalogic.util.rcp.html;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import com.lambdalogic.util.rcp.UtilI18N;

public class HtmlEditorDialog extends Dialog {

	private static final Point PREFERRED_SIZE = new Point(1200, 800);

	private static Point lastSize = null;
	private static Point lastLocation = null;


	private String html;

	// Widgets
	private HtmlEditor htmlEditor;


	public HtmlEditorDialog(Shell parent) {
		super(parent);
	}


	/**
	 * Create contents of the dialog
	 * @param parent
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		container.setLayout(new FillLayout());

		htmlEditor = new HtmlEditor(container, SWT.NONE);
		htmlEditor.setHtml(html);


		getShell().addDisposeListener( new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent e) {
				lastSize = getShell().getSize();
				lastLocation = getShell().getLocation();
			}
		});


		return container;
	}


	/**
	 * Create contents of the button bar
	 * @param parent
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}


	/**
	 * Return the initial size of the dialog.
	 */
	@Override
	protected Point getInitialSize() {
		Point size = lastSize;

		final int GAP = 20;

		if (size == null) {
			size = new Point(PREFERRED_SIZE.x, PREFERRED_SIZE.y);

    		Point shellSize = getShell().getSize();

    		if (shellSize.x - GAP > PREFERRED_SIZE.x) {
    			size.x = PREFERRED_SIZE.x;
    		}
    		else {
    			size.x = shellSize.x - GAP;
    		}

    		if (shellSize.y - GAP > PREFERRED_SIZE.y) {
    			size.y = PREFERRED_SIZE.y;
    		}
    		else {
    			size.y = shellSize.y - GAP;
    		}
		}

		return size;
	}


	@Override
	protected Point getInitialLocation(Point initialSize) {
		if (lastLocation != null) {
			return lastLocation;
		}

		return super.getInitialLocation(initialSize);
	}


	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(UtilI18N.HtmlEditor);
	}


	@Override
	protected boolean isResizable() {
		return true;
	}


	@Override
	protected void buttonPressed(int buttonId) {
		if (buttonId == CANCEL) {
//			postalCodeVO = null;
		}
		super.buttonPressed(buttonId);
	}


	public String getHtml() {
		if (htmlEditor != null) {
			html = htmlEditor.getHtml();
		}
		return html;
	}


	public void setHtml(String html) {
		this.html = html;
		if (htmlEditor != null) {
			htmlEditor.setHtml(html);
		}
	}

}
