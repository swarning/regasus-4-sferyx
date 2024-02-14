package de.regasus.finance.paymentsystem.dialog;

import static com.lambdalogic.util.rcp.widget.SWTHelper.*;

import java.math.BigDecimal;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import com.lambdalogic.messeinfo.invoice.InvoiceLabel;
import com.lambdalogic.messeinfo.invoice.data.PaymentVO;
import com.lambdalogic.util.CurrencyAmount;
import com.lambdalogic.util.rcp.widget.DecimalNumberText;

import de.regasus.I18N;
import com.lambdalogic.util.rcp.UtilI18N;

public class RefundAmountPage extends WizardPage {

	public static final String NAME = "RefundAmountPage";

	// Widgets
	private DecimalNumberText amountNumberText;

	// parameters
	private BigDecimal maxRefundableAmount;
	private BigDecimal refundAmount;

	private PaymentVO paymentVO;


	public RefundAmountPage(BigDecimal maxRefundableAmount, PaymentVO paymentVO) {
		super(NAME);

		setTitle(UtilI18N.Input);
		setMessage(I18N.AmountForRefund);

		this.maxRefundableAmount = maxRefundableAmount;
		this.refundAmount = maxRefundableAmount;
		this.paymentVO = paymentVO;
	}


	@Override
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		container.setLayout(new GridLayout(3, false));

		createLabel(container, InvoiceLabel.Amount.getString());

		amountNumberText = new DecimalNumberText(container, SWT.BORDER);
		amountNumberText.setFractionDigits(2);

		createLabel(container, paymentVO.getCurrencyAmount().getSymbol());

		amountNumberText.setNullAllowed(false);
		amountNumberText.setMinValue(new BigDecimal("0.00"));
		amountNumberText.setValue(maxRefundableAmount);
		amountNumberText.setMaxValue(maxRefundableAmount);
		amountNumberText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				validate();
			}
		});
		amountNumberText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));


		setControl(container);
	}


	public CurrencyAmount getRefundAmount() {
		return new CurrencyAmount(refundAmount, paymentVO.getCurrency());
	}


	private void validate() {
		BigDecimal tmpAmount = amountNumberText.getValue();
		refundAmount = null;

		// The absolute amount of a Payment must be greater or equal than the absolute amount of all its associated Clearings.
		if (tmpAmount.abs().compareTo(maxRefundableAmount) > 0) {
			setErrorMessage(I18N.AmountForRefundNotLargerThanPayment);
		}
		else {
			setErrorMessage(null);
			refundAmount = tmpAmount;
		}
		boolean canFinish = (refundAmount != null && refundAmount.signum() == 1);
		setPageComplete(canFinish);
	}

}
