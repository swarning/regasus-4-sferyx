package de.regasus.finance.payment.dialog;

import static com.lambdalogic.util.rcp.widget.SWTHelper.*;

import java.math.BigDecimal;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import com.lambdalogic.messeinfo.invoice.InvoiceLabel;
import com.lambdalogic.messeinfo.invoice.data.PaymentVO;
import com.lambdalogic.util.rcp.widget.DecimalNumberText;

import de.regasus.I18N;
import com.lambdalogic.util.rcp.UtilI18N;


public class ClearingAmountDialog extends TitleAreaDialog {

	private DecimalNumberText amountNumberText;
	private BigDecimal amount;
	private PaymentVO paymentVO;
	private int requiredSignum;

	public ClearingAmountDialog(Shell parentShell, PaymentVO paymentVO) {
		super(parentShell);

		this.paymentVO = paymentVO;
		requiredSignum = paymentVO.getAmount().signum();
	}


	@Override
	protected Control createDialogArea(Composite parent) {
		setTitle(UtilI18N.Input);
		setMessage(I18N.AmountForClearing);

		Composite area = (Composite) super.createDialogArea(parent);
		Composite container = new Composite(area, SWT.NONE);
		container.setLayout(new GridLayout(3, false));
		container.setLayoutData(new GridData(GridData.FILL_BOTH));

		createLabel(container, InvoiceLabel.Amount.getString());

		amountNumberText = new DecimalNumberText(container, SWT.BORDER);
		amountNumberText.setFractionDigits(2);

		createLabel(container, paymentVO.getCurrencyAmount().getSymbol());

		amountNumberText.setNullAllowed(false);

		BigDecimal minValue = null;
		BigDecimal maxValue = null;

		if (requiredSignum > 0) {
			minValue = new BigDecimal("0.01");
			maxValue = paymentVO.getOpenAmount();
			amountNumberText.setValue(maxValue);
		}
		else {
			minValue = paymentVO.getOpenAmount();
			maxValue = new BigDecimal("-0.01");
			amountNumberText.setValue(minValue);
		}

		amountNumberText.setMinValue(minValue);
		amountNumberText.setMaxValue(maxValue);

		amountNumberText.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				validate();
			}
		});
		amountNumberText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		return area;
	}


	/**
	 * Return the initial size of the dialog.
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(450, 300);
	}

	public BigDecimal getAmount() {
		return amount;
	}


	private void validate() {
		BigDecimal tmpAmount = amountNumberText.getValue();

		// C1: The sign of a Clearing must be the same as its associated Payment.
		if (tmpAmount.signum() != 0 && tmpAmount.signum() != requiredSignum) {
			setErrorMessage(I18N.AmountForClearingSameSign);
			amount = null;
		}
		// C2: The absolute amount of a Payment must be greater or equal than the absolute amount of all its associated Clearings.
		else if (tmpAmount.abs().compareTo(paymentVO.getAmount().abs()) == 1) {
			setErrorMessage(I18N.AmountForClearingNotLargerThanPayment);
			amount = null;
		}
		else {
			setErrorMessage(null);
			amount = tmpAmount;
		}
		boolean canFinish = (amount != null);
		getButton(IDialogConstants.OK_ID).setEnabled(canFinish);
	}

}
