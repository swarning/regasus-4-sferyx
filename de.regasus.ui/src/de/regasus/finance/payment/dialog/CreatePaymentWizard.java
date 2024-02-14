package de.regasus.finance.payment.dialog;

import static com.lambdalogic.util.CollectionsHelper.notEmpty;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.graphics.Point;

import com.lambdalogic.messeinfo.contact.Bank;
import com.lambdalogic.messeinfo.contact.CreditCard;
import com.lambdalogic.messeinfo.contact.data.BankVO;
import com.lambdalogic.messeinfo.contact.data.CreditCardVO;
import com.lambdalogic.messeinfo.email.EmailTemplate;
import com.lambdalogic.messeinfo.email.EmailTemplateSystemRole;
import com.lambdalogic.messeinfo.invoice.InvoiceLabel;
import com.lambdalogic.messeinfo.invoice.data.AccountancyCVO;
import com.lambdalogic.messeinfo.invoice.data.InvoicePositionVO;
import com.lambdalogic.messeinfo.invoice.data.InvoiceVO;
import com.lambdalogic.messeinfo.invoice.data.PaymentVO;
import com.lambdalogic.messeinfo.kernel.data.AbstractVO;
import com.lambdalogic.messeinfo.participant.Participant;
import com.lambdalogic.messeinfo.participant.ParticipantLabel;
import com.lambdalogic.messeinfo.participant.data.EventVO;
import com.lambdalogic.util.CurrencyAmount;
import com.lambdalogic.util.exception.ErrorMessageException;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.email.EmailTemplateModel;
import de.regasus.event.EventModel;
import de.regasus.finance.AccountancyModel;
import de.regasus.finance.ICurrencyAmountProvider;
import de.regasus.finance.PaymentType;
import de.regasus.ui.Activator;


public class CreatePaymentWizard extends Wizard implements ICurrencyAmountProvider {

	// WizardPages
	private CreatePaymentAmountPage createPaymentAmountPage;
	private CreditCardDetailsPage creditCardDetailsPage;
	private BankDetailsPage bankDetailsPage;
	private CashDetailsPage cashDetailsPage;
	private ChequeDetailsPage chequeDetailsPage;
	private EmailTemplateSelectionPage emailTemplateSelectionPage;

	private Participant participant;
	private String currency = null;
	private BigDecimal amount = BigDecimal.ZERO;
	private String customerAccountNo = null;
	private String customerAccountSource = null;
	private Collection<InvoiceVO> invoiceVOs;
	private Collection<InvoicePositionVO> invoicePositionVOs;

	/**
	 * Switches between entering of existing payments (false) and
	 * real charging of money via WebPOS (true).
	 */
	private boolean realCharging = false;


	// **************************************************************************
	// * Constructors
	// *

	public CreatePaymentWizard(
		String currency,
		Collection<InvoiceVO> invoiceVOs,
		Participant participant,
		boolean realCharging
	) {
		try {
			this.currency = currency;
			this.invoiceVOs = invoiceVOs;
			this.participant = participant;
			this.realCharging = realCharging;


			// Find out the amount that is to be suggested for the payment
			for (InvoiceVO invoiceVO : invoiceVOs) {
				// Only consider invoices with the same currency as the suggested
				if (invoiceVO.getCurrency().equals(currency)) {
					BigDecimal openAmount = invoiceVO.getAmountOpen();
					amount = amount.add(openAmount);
				}
			}

			initCustomerAccount();
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	public CreatePaymentWizard(
		String currency,
		Collection<InvoicePositionVO> invoicePositionVOs,
		Participant participant,
		boolean realCharging,
		Object dummy // ToOverloadTheConstructorBecauseOfGenericCollectionsHaveSameErasure
	) {
		try {
			this.currency = currency;
			this.invoicePositionVOs = invoicePositionVOs;
			this.participant = participant;
			this.realCharging = realCharging;


			// Find out the amount that is to be suggested for the payment
			for (InvoicePositionVO invoicePositionVO : invoicePositionVOs) {
				// Only consider invoices with the same currency as the suggested
				if (invoicePositionVO.getCurrency().equals(currency)) {
					BigDecimal openAmount = invoicePositionVO.getAmountOpen();
					amount = amount.add(openAmount);
				}
			}

			initCustomerAccount();
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	private void initCustomerAccount() throws Exception {
		customerAccountNo = participant.getCustomerAccountNumber();
		if (customerAccountNo != null) {
			customerAccountSource = ParticipantLabel.Participant.getString();
		}
		else {
			// init customerAccountNo from Event
			EventVO eventVO = EventModel.getInstance().getEventVO(participant.getEventId());
			customerAccountNo = eventVO.getCustomerAccountNo();
			if (customerAccountNo != null) {
				customerAccountSource = ParticipantLabel.Event.getString();
			}
		}
	}


	// **************************************************************************
	// * Overridden Methods
	// *

	@Override
	public CurrencyAmount getCurrencyAmount() {
		BigDecimal amount = createPaymentAmountPage.getAmount();
		String currency = createPaymentAmountPage.getCurrencyCode();

		CurrencyAmount currencyAmount = new CurrencyAmount(amount, currency);
		return currencyAmount;
	}


	@Override
	public void addPages() {
		createPaymentAmountPage = new CreatePaymentAmountPage(
			currency,
			amount,
			customerAccountNo,
			customerAccountSource,
			realCharging
		);
		addPage(createPaymentAmountPage);

		// From the following pages, only one gets shown (controlled by the CreatePaymentAmountPage)
		CreditCard creditCard = new CreditCard();
		creditCard.copyFrom(participant.getCreditCard());
		creditCardDetailsPage = new CreditCardDetailsPage(creditCard);
		addPage(creditCardDetailsPage);

		Bank bank = new Bank();
		bank.copyFrom(participant.getBank());

		bankDetailsPage = new BankDetailsPage(bank);
		addPage(bankDetailsPage);

		cashDetailsPage = new CashDetailsPage();
		addPage(cashDetailsPage);

		chequeDetailsPage = new ChequeDetailsPage();
		addPage(chequeDetailsPage);


		// Add EmailTemplateSelectionPage if any EmailTemplate with EmailTemplateSystemRole.PAYMENT_RECEIVED exist
		Long eventPK = participant.getEventId();
		List<EmailTemplate> emailTemplates = null;
		try {
			// check if any EmailTemplate with EmailTemplateSystemRole.PAYMENT_RECEIVED exist
			emailTemplates = EmailTemplateModel.getInstance().getEmailTemplateSearchDataByEvent(
				eventPK,
				EmailTemplateSystemRole.PAYMENT_RECEIVED
			);

			if (notEmpty(emailTemplates)) {
				emailTemplateSelectionPage = new EmailTemplateSelectionPage(eventPK, false /*refund*/);
				addPage(emailTemplateSelectionPage);
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	@Override
	public String getWindowTitle() {
		return InvoiceLabel.Payment.getString();
	}


	@Override
	public boolean canFinish() {
		/*
		 * The basic requirement for ending the wizard is that an amount other than zero has been entered.
		 * In addition, the wizard can only be ended via the email template selection page if it exists.
		 * The only exception is if REBOOKING was selected as the payment method.
		 */
		IWizardPage currentPage = getContainer().getCurrentPage();
		return
			createPaymentAmountPage.getAmount().signum() != 0
			&&
			(
    			createPaymentAmountPage.getPaymentType() == PaymentType.REBOOKING
    			||
    			emailTemplateSelectionPage == null
        		||
        		emailTemplateSelectionPage == currentPage && emailTemplateSelectionPage.isPageComplete()
    		);
	}


	@Override
	public boolean performFinish() {
		PaymentVO paymentVO = new PaymentVO();

		paymentVO.setEventPK(participant.getEventId());
		paymentVO.setPayerPK(participant.getID());

		// Data from the CreatePaymentAmountPage
		PaymentType paymentType = createPaymentAmountPage.getPaymentType();

		paymentVO.setType(paymentType);
		paymentVO.setAmount(createPaymentAmountPage.getAmount());
		paymentVO.setOpenAmount(paymentVO.getAmount());
		paymentVO.setCurrency(createPaymentAmountPage.getCurrencyCode());
		paymentVO.setDescription(createPaymentAmountPage.getDescription());
		paymentVO.setBookingDate(createPaymentAmountPage.getBookingDate());
		paymentVO.setCreditAccountNo(createPaymentAmountPage.getImpersonalAccountNo());

		// Data from the CreditCardDetailsPage
		if (paymentType == PaymentType.CREDIT_CARD) {
			CreditCard creditCard = creditCardDetailsPage.getCreditCard();
			CreditCardVO creditCardVO = new CreditCardVO(creditCard);
			paymentVO.setCreditCardVO(creditCardVO);
		}

		// Data from the BankDetailsPage
		if (paymentType == PaymentType.TRANSFER ||
			paymentType == PaymentType.DEBIT ||
			paymentType == PaymentType.ECMAESTRO
		) {
			BankVO bankVO = new BankVO();
			bankVO.copyFrom(bankDetailsPage.getBank());
			paymentVO.setBankVO(bankVO);
		}

		// Data from the CashDetailsPage
		if (paymentType == PaymentType.CASH) {
			paymentVO.setPayerName(cashDetailsPage.getPayer());
		}

		// Data from the ChequeDetailsPage
		if (paymentType == PaymentType.CHEQUE) {
			paymentVO.setChequeBank(chequeDetailsPage.getBankName());
			paymentVO.setChequeNo(chequeDetailsPage.getChequeNumber());
		}


		try {
			AccountancyModel accModel = AccountancyModel.getInstance();

			// Persist Payment
			Long paymentPK = null;
			if (invoicePositionVOs != null) {
				List<Long> ipPKs = AbstractVO.getPKs(invoicePositionVOs);
				paymentPK = accModel.createPaymentForInvoicePositions(paymentVO, ipPKs);
			}
			else if (invoiceVOs != null) {
				List<Long> invoicePKs = AbstractVO.getPKs(invoiceVOs);
				paymentPK = accModel.createPaymentForInvoices(paymentVO, invoicePKs);
			}
			else {
				// this case should actually not happen
				paymentPK = accModel.createPayment(paymentVO);
			}

			// MIRCP-2158 - Send email confirmations upon (manual and payengine) payments
			// MIRCP-2967 - Don't send payment receipts via email if wizard has option switched off
			if (emailTemplateSelectionPage != null && emailTemplateSelectionPage.isSendEmail()) {
    			final EmailTemplate emailTemplate = emailTemplateSelectionPage.getSelectedEmailTemplate();
    			if (paymentPK != null && emailTemplate != null) {
    				// refresh data of new Payment
    				AccountancyCVO accountancyCVO = accModel.getAccountancyCVO(participant.getID());
    				paymentVO = accountancyCVO.getPaymentVO(paymentPK);

    				// MIRCP-2158 - Send email confirmations upon (manual and PayEngine) payments
   					SendPaymentConfirmationEmailHelper.sendPaymentConfirmationEmail(paymentVO, emailTemplate.getID());
    			}
			}

			return true;
		}
		catch (ErrorMessageException e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e, e.getI18NMessage().getString());
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
		return false;
	}


	public Point getPreferredSize() {
		return new Point(600, 500);
	}

}