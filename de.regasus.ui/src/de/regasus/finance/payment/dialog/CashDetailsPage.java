package de.regasus.finance.payment.dialog;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import com.lambdalogic.messeinfo.invoice.InvoiceLabel;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.finance.PaymentType;

import com.lambdalogic.util.rcp.UtilI18N;

public class CashDetailsPage extends WizardPage {

	public static final String NAME = "CashDetailsPage"; 

	private Text payerText;
	

	public CashDetailsPage() {
		super(NAME);
		setTitle(InvoiceLabel.PaymentType.getString() + " " + UtilI18N.Details);
		setMessage(PaymentType.CASH.getString());
	}
	
	
	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(2, false));
		
		payerText = SWTHelper.createLabelAndText(composite, InvoiceLabel.Payer.getString());
		
		setControl(composite);
	}
	
	public String getPayer() {
		return payerText.getText();
	}
	
	
	public IWizardPage getNextPage() {
		return getWizard().getPage(EmailTemplateSelectionPage.NAME);
	}

}
