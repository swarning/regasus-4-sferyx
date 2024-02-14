package de.regasus.finance.payment.dialog;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;

import com.lambdalogic.messeinfo.contact.CreditCard;
import com.lambdalogic.messeinfo.invoice.InvoiceLabel;

import de.regasus.common.composite.CreditCardGroup;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.finance.PaymentType;

import com.lambdalogic.util.rcp.UtilI18N;
import de.regasus.ui.Activator;

public class CreditCardDetailsPage extends WizardPage {

	public static final String NAME = "CreditCardDetailsPage"; 
	
	private CreditCardGroup creditCardGroup;

	private CreditCard creditCard;
	

	public CreditCardDetailsPage(CreditCard creditCard) {
		super(NAME);
		this.creditCard = creditCard;

		setTitle(InvoiceLabel.PaymentType.getString() + " " + UtilI18N.Details);
		setMessage(PaymentType.CREDIT_CARD.getString());
	}
	

	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new FillLayout());
		
		try {
			creditCardGroup = new CreditCardGroup(composite, SWT.NONE);
			creditCardGroup.setCreditCard(creditCard);
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
		setControl(composite);
	}
	
	
	public IWizardPage getNextPage() {
		return getWizard().getPage(EmailTemplateSelectionPage.NAME);
	}
	

	public CreditCard getCreditCard() {
		creditCardGroup.syncEntityToWidgets();
		return creditCardGroup.getCreditCard();
	}

}
