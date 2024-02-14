package de.regasus.finance.payment.dialog;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;

import com.lambdalogic.messeinfo.contact.Bank;
import com.lambdalogic.messeinfo.invoice.InvoiceLabel;
import com.lambdalogic.util.rcp.UtilI18N;

import de.regasus.common.composite.BankGroup;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.finance.PaymentType;
import de.regasus.ui.Activator;

public class BankDetailsPage extends WizardPage {

	public static final String NAME = "BankDetailsPage";

	private BankGroup bankGroup;

	private Bank bank;


	public BankDetailsPage(Bank bank) {
		super(NAME);
		this.bank = bank;
	}


	public void setPaymentType(PaymentType paymentType) {
		setTitle(InvoiceLabel.PaymentType.getString() + " " + UtilI18N.Details);
		setMessage(paymentType.getString());
	}


	@Override
	public void createControl(Composite parent) {
		try {
			Composite composite = new Composite(parent, SWT.NONE);
			composite.setLayout(new FillLayout());

			bankGroup = new BankGroup(composite, SWT.NONE);
			bankGroup.setText("");
			bankGroup.setBank(bank);

			setControl(composite);
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}



	@Override
	public IWizardPage getNextPage() {
		return getWizard().getPage(EmailTemplateSelectionPage.NAME);
	}


	public Bank getBank() {
		bankGroup.syncEntityToWidgets();
		return bankGroup.getBank();
	}

}
