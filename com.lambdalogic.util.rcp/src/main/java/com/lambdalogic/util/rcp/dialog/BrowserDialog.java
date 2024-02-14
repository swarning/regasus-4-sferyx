package com.lambdalogic.util.rcp.dialog;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import com.lambdalogic.util.StringHelper;
import com.lambdalogic.util.rcp.UtilI18N;
import com.lambdalogic.util.rcp.html.BrowserFactory;

public class BrowserDialog extends Dialog {

	private String browserString;
	private String title;
	private Browser browser;


	public BrowserDialog(Shell parentShell, String title, String browserString) {
		super(parentShell);
		this.title = title;
		this.browserString = browserString;
	}


	@Override
	public void create() {
		setShellStyle(SWT.RESIZE | SWT.SHELL_TRIM);
		super.create();
	}


	@Override
	protected Control createDialogArea(Composite parent) {
		Composite composite = (Composite) super.createDialogArea(parent);

		browser = BrowserFactory.createBrowser(composite, SWT.BORDER);
		browser.setText(browserString);
		browser.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		return composite;
	}


	@Override
	protected void createButtonsForButtonBar(Composite parent) {

		createButton(parent, Dialog.OK, IDialogConstants.OK_LABEL, true);
		createButton(parent, Dialog.CANCEL, UtilI18N.Print, false);
	}


	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(StringHelper.avoidNull(title));
	}


	@Override
	public void okPressed() {
		close();
	}


	@Override
	public void cancelPressed() {
		browser.execute("print()");
	}

}
