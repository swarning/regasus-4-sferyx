package de.regasus.onlineform.admin.dialog;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import com.lambdalogic.util.StringHelper;
import com.lambdalogic.util.rcp.widget.QRCodeComposite;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.onlineform.admin.ui.Activator;


public class QRCodeDialog extends Dialog {

	private String url;

	public QRCodeDialog(Shell parentShell, String url) {
		super(parentShell);
		this.url = url;
	}


	@Override
	protected Control createDialogArea(Composite parent) {
		Composite c = (Composite) super.createDialogArea(parent);

		QRCodeComposite qrCodeComposite = new QRCodeComposite(c, SWT.NONE);
		try {
			qrCodeComposite.setContent(url);
			Point size = qrCodeComposite.getSize();
			qrCodeComposite.setLayoutData(new GridData(size.x, size.y));
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
		
		return c;
	}
	

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
	}


	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		
		// Show URL in window title 
		newShell.setText(StringHelper.avoidNull(url));
	}
}
