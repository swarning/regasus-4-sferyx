package de.regasus.finance.datatrans.dialog;

import java.math.BigDecimal;
import java.util.Collection;

import org.eclipse.jface.dialogs.IPageChangedListener;
import org.eclipse.jface.dialogs.PageChangedEvent;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.graphics.Point;

import com.lambdalogic.messeinfo.contact.CreditCardAlias;
import com.lambdalogic.messeinfo.invoice.InvoiceLabel;
import com.lambdalogic.messeinfo.invoice.data.InvoicePositionVO;
import com.lambdalogic.messeinfo.invoice.data.InvoiceVO;
import com.lambdalogic.messeinfo.invoice.data.PaymentVO;
import com.lambdalogic.messeinfo.participant.Participant;
import com.lambdalogic.messeinfo.participant.ParticipantLabel;
import com.lambdalogic.messeinfo.participant.data.EventVO;
import com.lambdalogic.util.rcp.CustomWizardDialog;
import com.lambdalogic.util.rcp.ICustomWizard;

import de.regasus.finance.PaymentSystem;
import de.regasus.finance.PaymentType;
import de.regasus.finance.payment.dialog.CreatePaymentAmountPage;

public class DatatransPaymentWizard extends Wizard implements ICustomWizard, IPageChangedListener {

	private CustomWizardDialog customWizardDialog;

	// WizardPages
	private CreatePaymentAmountPage createPaymentAmountPage;
	private DatatransPage1 datatransPage1;
	private DatatransPage2 datatransPage2;


	private Participant participant;
	private EventVO eventVO;
	private String currency = null;
	private BigDecimal amount = BigDecimal.ZERO;
	private String customerAccountNo = null;
	private String customerAccountSource = null;


	// **************************************************************************
	// * Constructors
	// *

	protected DatatransPaymentWizard() {
	}


	public static DatatransPaymentWizard getPayInvoicesInstance(
		String currency,
		Collection<InvoiceVO> invoiceVOs,
		Participant participant,
		EventVO eventVO
	) {
		DatatransPaymentWizard datatransPaymentWizard = new DatatransPaymentWizard();
		datatransPaymentWizard.currency = currency;
		datatransPaymentWizard.participant = participant;
		datatransPaymentWizard.eventVO = eventVO;

		// Find out the amount that is to be suggested for the payment
		for (InvoiceVO invoiceVO : invoiceVOs) {
			// Only consider invoices with the same currency as the suggested
			if (invoiceVO.getCurrency().equals(currency)) {
				BigDecimal openAmount = invoiceVO.getAmountOpen();
				datatransPaymentWizard.amount = datatransPaymentWizard.amount.add(openAmount);
			}
		}

		initCustomerAccoutNo(datatransPaymentWizard);

		return datatransPaymentWizard;
	}


	public static DatatransPaymentWizard getPayInvoicePositionsInstance(
		String currency,
		Collection<InvoicePositionVO> invoicePositionVOs,
		Participant participant,
		EventVO eventVO
	) {
		DatatransPaymentWizard datatransPaymentWizard = new DatatransPaymentWizard();
		datatransPaymentWizard.currency = currency;
		datatransPaymentWizard.participant = participant;
		datatransPaymentWizard.eventVO = eventVO;

		// Find out the amount that is to be suggested for the payment
		for (InvoicePositionVO invoicePositionVO : invoicePositionVOs) {
			// Only consider invoices with the same currency as the suggested
			if (invoicePositionVO.getCurrency().equals(currency)) {
				BigDecimal openAmount = invoicePositionVO.getAmountOpen();
				datatransPaymentWizard.amount = datatransPaymentWizard.amount.add(openAmount);
			}
		}

		initCustomerAccoutNo(datatransPaymentWizard);

		return datatransPaymentWizard;
	}


	private static void initCustomerAccoutNo(DatatransPaymentWizard datatransPaymentWizard) {
		if (datatransPaymentWizard.participant != null) {
			datatransPaymentWizard.customerAccountNo = datatransPaymentWizard.participant.getCustomerAccountNumber();

			if (datatransPaymentWizard.customerAccountNo != null) {
				datatransPaymentWizard.customerAccountSource = ParticipantLabel.Participant.getString();
			}
		}

		if (datatransPaymentWizard.customerAccountNo == null && datatransPaymentWizard.eventVO != null) {
			// init customerAccountNo from Event
			datatransPaymentWizard.customerAccountNo = datatransPaymentWizard.eventVO.getCustomerAccountNo();

			if (datatransPaymentWizard.customerAccountNo != null) {
				datatransPaymentWizard.customerAccountSource = ParticipantLabel.Event.getString();
			}
		}
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

	// **************************************************************************
	// * Overridden Methods
	// *

	@Override
	public void addPages() {
		// CreatePaymentAmountPage
		createPaymentAmountPage = new CreatePaymentAmountPage(
			currency,
			amount,
			customerAccountNo,
			customerAccountSource,
			true	// realCharging
		);
		addPage(createPaymentAmountPage);

		// DatatransPage1
		CreditCardAlias creditCardAlias = new CreditCardAlias();
		creditCardAlias.copyFrom(participant.getCreditCardAlias());
		datatransPage1 = new DatatransPage1(creditCardAlias);
		addPage(datatransPage1);

		// DatatransPage2
		datatransPage2 = new DatatransPage2();
		addPage(datatransPage2);
	}


	@Override
	public IWizardPage getNextPage(IWizardPage page) {
		if (page == createPaymentAmountPage) {
			if (participant.getCreditCardAlias().isCompleteForDatatrans()) {
				return datatransPage1;
			}
			else {
				return datatransPage2;
			}
		}

		return super.getNextPage(page);
	}


	@Override
	public String getWindowTitle() {
		return InvoiceLabel.Payment.getString();
	}


	/**
	 * Can only finish if the second page is shown, so that the user is reminded to enter the additional data. In case
	 * of WebPOS payments however, the page showing the hint on real monetary transactions need to be confirmed.
	 */
	@Override
	public boolean canFinish() {
		IWizardPage currentPage = getContainer().getCurrentPage();
		return (currentPage == datatransPage2);
	}


	@Override
	public boolean performFinish() {
		return true;
	}


	@Override
	public Point getPreferredSize() {
		return new Point(900, 700);
	}


	@Override
	public void pageChanged(PageChangedEvent event) {
		Object selectedPage = event.getSelectedPage();
		if (selectedPage == datatransPage2) {
			startPayment();
		}
	}


	private void startPayment() {
		PaymentVO paymentVO = new PaymentVO();

		paymentVO.setEventPK(participant.getEventId());
		paymentVO.setPayerPK(participant.getPK());

		// Data from the CreatePaymentAmountPage
		paymentVO.setType(PaymentType.CREDIT_CARD);
		paymentVO.setPaymentSystem(PaymentSystem.DATATRANS);
		paymentVO.setAmount(createPaymentAmountPage.getAmount());
		paymentVO.setOpenAmount(paymentVO.getAmount());
		paymentVO.setCurrency(createPaymentAmountPage.getCurrencyCode());
		paymentVO.setDescription(createPaymentAmountPage.getDescription());
		paymentVO.setBookingDate(createPaymentAmountPage.getBookingDate());

		// Data from the DatatransPage1
		CreditCardAlias creditCardAlias = null;
		if (datatransPage1.isUseAlias()) {
			creditCardAlias = datatransPage1.getCreditCardAlias();
		}

		datatransPage2.startPayment(
			paymentVO,
			creditCardAlias,
			participant,
			eventVO
		);
	}

}
