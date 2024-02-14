package de.regasus.common.dialog;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import com.lambdalogic.report.oo.OpenOfficeConstants;

import de.regasus.I18N;
import de.regasus.common.combo.OpenOfficeFormatCombo;
import com.lambdalogic.util.rcp.UtilI18N;

public class FormatDialog extends Dialog {

	private String dialogTitle = UtilI18N.Format;
	private String dialogMessage = I18N.FormatDialogText;
	private String format;
	private OpenOfficeFormatCombo formatCombo;


	public FormatDialog(Shell parentShell) {
		this(parentShell, null);
	}


	public FormatDialog(Shell parentShell, String dialogMessage) {
		super(parentShell);

		if (dialogMessage == null) {
			dialogMessage = I18N.FormatDialogText;
		}
		this.dialogMessage = dialogMessage;
	}


	@Override
	protected Control createDialogArea(Composite parent) {
		Composite composite = (Composite) super.createDialogArea(parent);
		formatCombo = new OpenOfficeFormatCombo(
			composite,
			SWT.READ_ONLY,
			OpenOfficeConstants.DOC_FORMAT_ODT // DocumentFormat of templates (only ODT is supported)
		);
		formatCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));

		Label label = new Label(parent, SWT.WRAP);
		label.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true));
		label.setText(dialogMessage);

		return composite;
	}


	@Override
	protected Point getInitialSize() {
		return new Point(500, 400);
	}


	@Override
	protected void okPressed() {
		format = formatCombo.getFormat().getFormatKey();
		super.okPressed();
	}


	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(dialogTitle);
	}


	public String getFormat() {
		return format;
	}


	public boolean openDialog() {
		return (open() == Window.OK);
	}

}
