package de.regasus.finance.payment.dialog;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import com.lambdalogic.messeinfo.contact.Bank;
import com.lambdalogic.messeinfo.invoice.InvoiceLabel;
import com.lambdalogic.util.rcp.UtilI18N;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.finance.PaymentType;

public class ChequeDetailsPage extends WizardPage {

	public static final String NAME = "ChequeDetailsPage";

	private Text bankNameText;

	private Text chequeNumberText;


	public ChequeDetailsPage() {
		super(NAME);
		setTitle(InvoiceLabel.PaymentType.getString() + " " + UtilI18N.Details);

		setMessage(PaymentType.CHEQUE.getString());
	}


	@Override
	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(2, false));

		bankNameText = SWTHelper.createLabelAndText(composite, Bank.BANK_NAME.getString());

		chequeNumberText = SWTHelper.createLabelAndText(composite, InvoiceLabel.ChequeNumber.getString());

		setControl(composite);
	}


	public String getBankName() {
		return bankNameText.getText();
	}


	public String getChequeNumber() {
		return chequeNumberText.getText();
	}


	@Override
	public IWizardPage getNextPage() {
		return getWizard().getPage(EmailTemplateSelectionPage.NAME);
	}

}
