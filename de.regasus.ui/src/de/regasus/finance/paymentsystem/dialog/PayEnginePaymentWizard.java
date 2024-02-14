package de.regasus.finance.paymentsystem.dialog;

import static de.regasus.LookupService.*;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

import org.eclipse.jface.dialogs.IPageChangedListener;
import org.eclipse.jface.dialogs.PageChangedEvent;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.graphics.Point;

import com.lambdalogic.messeinfo.contact.CreditCardAlias;
import com.lambdalogic.messeinfo.email.EmailTemplate;
import com.lambdalogic.messeinfo.exception.WarnMessageException;
import com.lambdalogic.messeinfo.invoice.InvoiceLabel;
import com.lambdalogic.messeinfo.invoice.data.InvoicePositionVO;
import com.lambdalogic.messeinfo.invoice.data.InvoiceVO;
import com.lambdalogic.messeinfo.invoice.data.PaymentVO;
import com.lambdalogic.messeinfo.participant.Participant;
import com.lambdalogic.messeinfo.participant.data.EventVO;
import com.lambdalogic.report.DocumentContainer;
import com.lambdalogic.util.CurrencyAmount;
import com.lambdalogic.util.exception.ErrorMessageException;
import com.lambdalogic.util.rcp.CustomWizardDialog;
import com.lambdalogic.util.rcp.ICustomWizard;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.I18N;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.finance.ICurrencyAmountProvider;
import de.regasus.finance.PaymentSystem;
import de.regasus.finance.PaymentSystemSetup;
import de.regasus.finance.PaymentSystemSetupModel;
import de.regasus.finance.payengine.PayEngineSetup;
import de.regasus.finance.payment.dialog.EmailTemplateSelectionPage;
import de.regasus.ui.Activator;

public class PayEnginePaymentWizard extends Wizard
implements ICustomWizard, IPageChangedListener, ICurrencyAmountProvider {

	private CustomWizardDialog customWizardDialog;

	// WizardPages
	private PayEngineStartPage payEngineStartPage;
	private EmailTemplateSelectionPage emailTemplateSelectionPage;
	private PayEngineAliasPage payEngineAliasPage;
	private PayEngineBrowserPage payEngineBrowserPage;


	private Participant participant;
	private EventVO eventVO;
	private String currency = null;
	private BigDecimal amount = BigDecimal.ZERO;
	private InvoiceVO invoiceVO;



	// **************************************************************************
	// * Constructors
	// *

	protected PayEnginePaymentWizard() {
	}


	public static PayEnginePaymentWizard getPayInvoicesInstance(
		String currency,
		Collection<InvoiceVO> invoiceVOs,
		Participant participant,
		EventVO eventVO
	)
	throws Exception {
		Long paymentSystemSetupPK = eventVO.getPaymentSystemSetupPK();
		if (paymentSystemSetupPK == null) {
			throw new WarnMessageException(I18N.PayEngine_NoSetupMessage);
		}

		PaymentSystemSetup paymentSystemSetup = PaymentSystemSetupModel.getInstance().getPaymentSystemSetup(paymentSystemSetupPK);
		if (paymentSystemSetup.getPaymentSystem() != PaymentSystem.PAYENGINE) {
			throw new WarnMessageException("The Payment System Setup does not define a PayEngine Setup!");
		}

		PayEngineSetup payEngineSetup = paymentSystemSetup.getPayEngineSetup();
		if ( !payEngineSetup.isComplete() ) {
			throw new WarnMessageException(I18N.PayEngine_IncompleteSetupMessage);
		}


		PayEnginePaymentWizard payEnginePaymentWizard = new PayEnginePaymentWizard();
		payEnginePaymentWizard.currency = currency;
		payEnginePaymentWizard.participant = participant;
		payEnginePaymentWizard.eventVO = eventVO;


		// Find out the amount that is to be suggested for the payment
		for (InvoiceVO invoiceVO : invoiceVOs) {
			// Only consider invoices with the same currency as the suggested
			if (invoiceVO.getCurrency().equals(currency)) {
				BigDecimal openAmount = invoiceVO.getAmountOpen();
				payEnginePaymentWizard.amount = payEnginePaymentWizard.amount.add(openAmount);

				if (payEnginePaymentWizard.invoiceVO == null) {
					payEnginePaymentWizard.invoiceVO = invoiceVO;
				}
			}
		}

		return payEnginePaymentWizard;
	}


	public static PayEnginePaymentWizard getPayInvoicePositionsInstance(
		String currency,
		Collection<InvoicePositionVO> invoicePositionVOs,
		Participant participant,
		EventVO eventVO
	)
	throws Exception {
		Long paymentSystemSetupPK = eventVO.getPaymentSystemSetupPK();
		if (paymentSystemSetupPK == null) {
			throw new WarnMessageException(I18N.PayEngine_NoSetupMessage);
		}

		PaymentSystemSetup paymentSystemSetup = PaymentSystemSetupModel.getInstance().getPaymentSystemSetup(paymentSystemSetupPK);
		if (paymentSystemSetup.getPaymentSystem() != PaymentSystem.PAYENGINE) {
			throw new WarnMessageException("The Payment System Setup does not define a PayEngine Setup!");
		}

		PayEngineSetup payEngineSetup = paymentSystemSetup.getPayEngineSetup();
		if ( !payEngineSetup.isComplete() ) {
			throw new WarnMessageException(I18N.PayEngine_IncompleteSetupMessage);
		}


		PayEnginePaymentWizard payEnginePaymentWizard = new PayEnginePaymentWizard();
		payEnginePaymentWizard.currency = currency;
		payEnginePaymentWizard.participant = participant;
		payEnginePaymentWizard.eventVO = eventVO;

		// Find out the amount that is to be suggested for the payment
		for (InvoicePositionVO invoicePositionVO : invoicePositionVOs) {
			// Only consider invoices with the same currency as the suggested
			if (invoicePositionVO.getCurrency().equals(currency)) {
				BigDecimal openAmount = invoicePositionVO.getAmountOpen();
				payEnginePaymentWizard.amount = payEnginePaymentWizard.amount.add(openAmount);

				if (payEnginePaymentWizard.invoiceVO == null) {
					payEnginePaymentWizard.invoiceVO = getInvoiceMgr().getInvoiceVO(invoicePositionVO.getInvoicePK(), false);
				}
			}
		}

		return payEnginePaymentWizard;
	}


	@Override
	public void setCustomWizardDialog(CustomWizardDialog customWizardDialog) {
		if (this.customWizardDialog != null) {
			this.customWizardDialog.removePageChangedListener(this);
		}

		this.customWizardDialog = customWizardDialog;
		if (this.customWizardDialog != null) {
			this.customWizardDialog.addPageChangedListener(this);
		}
	}


	@Override
	public CurrencyAmount getCurrencyAmount() {
		BigDecimal amount = payEngineStartPage.getAmount();
		String currency = payEngineStartPage.getCurrencyCode();

		CurrencyAmount currencyAmount = new CurrencyAmount(amount, currency);
		return currencyAmount;
	}


	@Override
	public void addPages() {
		// PayEngineStartPage
		payEngineStartPage = new PayEngineStartPage(currency, amount, participant);
		addPage(payEngineStartPage);

		emailTemplateSelectionPage = new EmailTemplateSelectionPage(participant.getEventId(), false /*refund*/);
		addPage(emailTemplateSelectionPage);

		// PayEngineAliasPage
		payEngineAliasPage = new PayEngineAliasPage();
		addPage(payEngineAliasPage);

		// PayEngineBrowserPage
		payEngineBrowserPage = new PayEngineBrowserPage();
		addPage(payEngineBrowserPage);
	}


	@Override
	public IWizardPage getNextPage(IWizardPage page) {
		if (page == payEngineStartPage) {
			return emailTemplateSelectionPage;
		}
		else if (page == emailTemplateSelectionPage) {
			CreditCardAlias usedCreditCardAlias = payEngineStartPage.getUsedCreditCardAlias();

			if (usedCreditCardAlias != null) {
				return payEngineAliasPage;
			}
			else {
				return payEngineBrowserPage;
			}
		}

		return null;
	}


	@Override
	public void pageChanged(PageChangedEvent event) {
		// determine ID of EmailTemplate
		Long emailTemplateID = null;
		EmailTemplate emailTemplate = emailTemplateSelectionPage.getSelectedEmailTemplate();
		if (emailTemplate != null) {
			emailTemplateID = emailTemplate.getID();
		}

		Object selectedPage = event.getSelectedPage();
		if (selectedPage == payEngineBrowserPage) {
			payEngineBrowserPage.startPayment(
				payEngineStartPage.getCurrencyCode(),
				payEngineStartPage.getAmount(),
				participant,
				emailTemplateID,
				invoiceVO
			);
		}
		else if (selectedPage == payEngineAliasPage) {
			CreditCardAlias usedCreditCardAlias = payEngineStartPage.getUsedCreditCardAlias();

			payEngineAliasPage.init(
				payEngineStartPage.getCurrencyCode(),
				payEngineStartPage.getAmount(),
				usedCreditCardAlias,
				participant,
				eventVO,
				emailTemplateID,
				invoiceVO
			);
		}
	}


	@Override
	public String getWindowTitle() {
		return InvoiceLabel.Payment.getString();
	}


	/**
	 * Can only finish if the second page is shown, so that the user is reminded to enter the additional data.
	 */
	@Override
	public boolean canFinish() {
		IWizardPage currentPage = getContainer().getCurrentPage();
		return
			(currentPage == payEngineBrowserPage && payEngineBrowserPage.isPageComplete())
			||
			(currentPage == payEngineAliasPage && payEngineAliasPage.isPageComplete());
	}


	@Override
	public boolean performFinish() {
		return true;
	}


	@Override
	public Point getPreferredSize() {
		return new Point(900, 760);
	}


	public void createPaymentReceipt(String orderID) throws Exception {

		// get the new payment
		List<PaymentVO> paymentVOs = getPaymentMgr().getPaymentVOsByPersonPKAndDocumentNo(participant.getID(), orderID);

		// eigentlich sollte paymentVOs nur ein PaymentVO enthalten

		if (paymentVOs != null && paymentVOs.size() == 1) {
			final Long paymentPK = paymentVOs.get(0).getID();

			SWTHelper.syncExecDisplayThread(new Runnable() {
				@Override
				public void run() {
					try {
						String format = payEngineStartPage.getPaymentReceiptFormat();
						DocumentContainer paymentReceipt = getPaymentMgr().getPaymentReceipt(paymentPK, null, format);

						/* save and open generated payment receipt file
						 * This code is referenced by
						 * https://lambdalogic.atlassian.net/wiki/pages/createpage.action?spaceKey=REGASUS&fromPageId=21987353
						 * Adapt the wiki document if this code is moved to another class or method.
						 */
						paymentReceipt.open();
					}
					catch (Throwable t) {
						RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t);
					}
				}
			});
		}
		else if (paymentVOs == null || paymentVOs.isEmpty()) {
			throw new ErrorMessageException(I18N.PayEnginePaymentWizard_Error_NoPayment);
		}
		else {
			throw new ErrorMessageException(I18N.PayEnginePaymentWizard_Error_MoreThanOnePayment);
		}
	}

}
