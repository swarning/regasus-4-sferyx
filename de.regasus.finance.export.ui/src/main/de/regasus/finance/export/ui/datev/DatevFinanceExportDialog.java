package de.regasus.finance.export.ui.datev;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import com.lambdalogic.time.I18NDate;
import com.lambdalogic.util.FileHelper;


public class DatevFinanceExportDialog extends TitleAreaDialog {

	private DatevFinanceExportComposite financeExportComposite;


	// values
	private boolean exportInvoices;
	private boolean exportPayments;
	private I18NDate beginDate;
	private I18NDate endDate;
	private String directoryPath;


	public DatevFinanceExportDialog(Shell parentShell) {
		super(parentShell);
		setShellStyle(getShellStyle() | SWT.RESIZE );
	}


	@Override
	protected Control createDialogArea(Composite parent) {
		setTitle("DATEV-Export");
		setMessage("...");

		Composite mainComposite = (Composite) super.createDialogArea(parent);
		mainComposite.setLayout( new GridLayout(1, false) );

		financeExportComposite = new DatevFinanceExportComposite(mainComposite, SWT.NONE);
		financeExportComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		financeExportComposite.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				updateButtons();
			}
		});

		return mainComposite;
	}


	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		Button exportButton = createButton(
			parent,
			IDialogConstants.OK_ID,
			"Export",	// IDialogConstants.OK_LABEL
			true		// defaultButton
		);
		exportButton.setEnabled(isOkEnabled());

		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}


	@Override
	protected void okPressed() {
		exportInvoices = financeExportComposite.isExportInvoices();
		exportPayments = financeExportComposite.isExportPayments();
		beginDate = financeExportComposite.getBeginDate();
		endDate = financeExportComposite.getEndDate();
		directoryPath = financeExportComposite.getDirectoryPathPath();

		super.okPressed();
	}


	private boolean isOkEnabled() {
		boolean withInvoices = financeExportComposite.isExportInvoices();
		boolean withPayments = financeExportComposite.isExportPayments();
		String dirPath = financeExportComposite.getDirectoryPathPath();

		boolean okEnabled =
			(withInvoices || withPayments)
			&&
			FileHelper.dirExists(dirPath);

		return okEnabled;
	}


	private void updateButtons() {
		getButton(IDialogConstants.OK_ID).setEnabled(isOkEnabled());
	}


	public boolean isExportInvoices() {
		return exportInvoices;
	}


	public boolean isExportPayments() {
		return exportPayments;
	}


	public I18NDate getBeginDate() {
		return beginDate;
	}


	public I18NDate getEndDate() {
		return endDate;
	}


	public String getDirectoryPath() {
		return directoryPath;
	}

}
