package de.regasus.finance.payment.dialog;

import static com.lambdalogic.util.rcp.widget.SWTHelper.createLabel;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.lambdalogic.messeinfo.invoice.InvoiceLabel;
import com.lambdalogic.messeinfo.invoice.data.PaymentVO;
import com.lambdalogic.report.oo.OpenOfficeConstants;
import com.lambdalogic.util.StringHelper;
import com.lambdalogic.util.rcp.UtilI18N;
import com.lambdalogic.util.rcp.datetime.DateComposite;
import com.lambdalogic.util.rcp.widget.DecimalNumberText;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.I18N;
import de.regasus.common.combo.OpenOfficeFormatCombo;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.finance.PaymentType;
import de.regasus.finance.currency.combo.CurrencyCombo;
import de.regasus.finance.impersonalaccount.combo.ImpersonalAccountCombo;
import de.regasus.finance.payment.combo.PaymentTypeCombo;
import de.regasus.ui.Activator;

public class CreatePaymentAmountPage extends WizardPage {

	public static final String NAME = "CreatePaymentAmountPage";


	private boolean realCharging;


	private DecimalNumberText amountNumberText;
	private CurrencyCombo currencyCombo;
	private Text descriptionText;
	private String currency;
	private BigDecimal amount;
	private String customerAccountNo;
	private String customerAccountSource;
	private DateComposite bookingDate;
	private PaymentTypeCombo paymentTypeCombo;
	private ImpersonalAccountCombo impersonalAccountCombo;
	private OpenOfficeFormatCombo formatCombo;


	/**
	 * @param currency
	 * @param amount
	 * @param customerAccountNo
	 *  customer account number
	 * @param customerAccountSource
	 *  source where the customer account number was taken from, e.g. Participant or Event
	 * @param realCharging
	 */
	public CreatePaymentAmountPage(
		String currency,
		BigDecimal amount,
		String customerAccountNo,
		String customerAccountSource,
		boolean realCharging
	) {
		super(NAME);

		this.realCharging = realCharging;

		setTitle(I18N.CreatePaymentAmountPage_Title);

		setMessage(I18N.CreatePaymentAmountPage_Message);

		this.currency = currency;
		this.amount = amount;
		this.customerAccountNo = customerAccountNo;
		this.customerAccountSource = customerAccountSource;
	}


	@Override
	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(3, false));

		try {
			// 1st row: Amount and currency
			createLabel(composite, InvoiceLabel.Amount.getString());

			amountNumberText = new DecimalNumberText(composite, SWT.BORDER);
			amountNumberText.setFractionDigits(2);
			amountNumberText.setValue(amount);
			amountNumberText.setNullAllowed(false);

			amountNumberText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

			// observe amountNumberText
			amountNumberText.addModifyListener(new ModifyListener() {
				@Override
				public void modifyText(ModifyEvent e) {
					setPageComplete(isPageComplete());
				}
			});


			currencyCombo = new CurrencyCombo(composite, SWT.NONE);
			currencyCombo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
			currencyCombo.setCurrencyCode(currency);

			// 2nd row: Description
			createLabel(composite, UtilI18N.Description);

			descriptionText = new Text(composite, SWT.BORDER);
			descriptionText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
			descriptionText.setTextLimit(PaymentVO.MAX_LENGTH_DESCRIPTION);

			// 3rd row: Date
			createLabel(composite, InvoiceLabel.BookingDate.getString());

			bookingDate = new DateComposite(composite, SWT.BORDER);
			// Registration date should default to today
			bookingDate.setLocalDate( LocalDate.now() );
			bookingDate.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

			// 4th row: Payment type
			if (!realCharging) {
				createLabel(composite, InvoiceLabel.PaymentType.getString());

				paymentTypeCombo = new PaymentTypeCombo(composite, SWT.NONE);
				paymentTypeCombo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
				paymentTypeCombo.setEntity(PaymentType.CASH);

				// observe paymentType
				paymentTypeCombo.addModifyListener(new ModifyListener() {
					@Override
					public void modifyText(ModifyEvent e) {
						setPageComplete(isPageComplete());
					}
				});
			}


			// 5th row: Customer Account
			Label debitorLabel = createLabel(
				composite,
				InvoiceLabel.Debitor.getString() + " (" + InvoiceLabel.CustomerAccount.getString() + ")"
			);
			debitorLabel.setToolTipText(I18N.CreatePaymentAmountPage_DebitorTooltip);

			Text debitorText = new Text(composite, SWT.BORDER);
			debitorText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
			SWTHelper.disableTextWidget(debitorText);

			// set text into debitorText
			if (StringHelper.isNotEmpty(customerAccountNo)) {
				StringBuilder sb = new StringBuilder();
				sb.append(customerAccountNo);

				if (StringHelper.isNotEmpty(customerAccountSource)) {
					sb.append(" (");
					sb.append(customerAccountSource);
					sb.append(")");
				}

				debitorText.setText(sb.toString());
			}


			// 6th row: Credit/Impersonal Account
			Label creditorLabel = createLabel(
				composite,
				InvoiceLabel.Creditor.getString() + " (" + InvoiceLabel.FinanceAccount.getString() + ")"
			);
			creditorLabel.setToolTipText(I18N.CreatePaymentAmountPage_CreditorTooltip);

			impersonalAccountCombo = new ImpersonalAccountCombo(composite, SWT.NONE);
			impersonalAccountCombo.setOnlyFinanceAcounts(true);
			impersonalAccountCombo.setWithEmptyElement(true);

			impersonalAccountCombo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));


			// 7th row: format of payment receipt
			Label paymentReceiptFormatLabel = new Label(composite, SWT.NONE);
			paymentReceiptFormatLabel.setText(I18N.PaymentReceiptFormat);

			formatCombo = new OpenOfficeFormatCombo(
				composite,
				SWT.READ_ONLY,
				OpenOfficeConstants.DOC_FORMAT_ODT // DocumentFormat of templates (only ODT is supported)
			);
			formatCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}

		setControl(composite);
	}


	public BigDecimal getAmount() {
		BigDecimal amount = amountNumberText.getValue();
		return amount;
	}


	public PaymentType getPaymentType() {
		return paymentTypeCombo.getEntity();
	}


	public Date getBookingDate() {
		return bookingDate.getDate();
	}


	@Override
	public String getDescription() {
		return StringHelper.trim(descriptionText.getText());
	}


	public String getCurrencyCode() {
		return currencyCombo.getCurrencyCode();
	}


	public Integer getImpersonalAccountNo() {
		return impersonalAccountCombo.getImpersonalAccountNo();
	}


	public String getPaymentReceiptFormat() {
		return formatCombo.getFormat().getFormatKey();
	}


	@Override
	public boolean isPageComplete() {
		boolean complete = getAmount().signum() != 0;
		return complete;
	}


	@Override
	public IWizardPage getNextPage() {
		PaymentType paymentType = getPaymentType();
		switch (paymentType) {
    		case CREDIT_CARD:
    			return getWizard().getPage(CreditCardDetailsPage.NAME);

    		case TRANSFER:
    		case DEBIT:
    		case ECMAESTRO:
    			BankDetailsPage bankDetailsPage = (BankDetailsPage) getWizard().getPage(BankDetailsPage.NAME);
    			bankDetailsPage.setPaymentType(paymentType);
    			return bankDetailsPage;

    		case CASH:
    			return getWizard().getPage(CashDetailsPage.NAME);

    		case CHEQUE:
    			return getWizard().getPage(ChequeDetailsPage.NAME);

    		default:
    			return getWizard().getPage(EmailTemplateSelectionPage.NAME);
		}
	}

}