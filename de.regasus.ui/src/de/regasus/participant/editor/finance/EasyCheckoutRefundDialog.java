package de.regasus.participant.editor.finance;

import static com.lambdalogic.util.rcp.widget.SWTHelper.createLabel;

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
import com.lambdalogic.util.CurrencyAmount;
import com.lambdalogic.util.rcp.widget.DecimalNumberText;

public class EasyCheckoutRefundDialog extends TitleAreaDialog {

	private DecimalNumberText amountNumberText;

	private CurrencyAmount maxAmount;

	private BigDecimal refundAmount;


	public EasyCheckoutRefundDialog(Shell parentShell) {
		super(parentShell);
	}


	@Override
	protected Control createDialogArea(Composite parent) {
		setTitle("Erstattung einer EASY Checkout-Zahlung");
		setMessage("Geben Sie den Betrag ein, den Sie erstatten möchten und klicken sie auf \"Erstatten\".");

		Composite dialogArea = (Composite) super.createDialogArea(parent);
		Composite composite = new Composite(dialogArea, SWT.NONE);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		composite.setLayout(new GridLayout(3, false));


		createLabel(composite, InvoiceLabel.Amount.getString());


		amountNumberText = new DecimalNumberText(composite, SWT.BORDER);
		amountNumberText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		amountNumberText.setFractionDigits(2);
		amountNumberText.setNullAllowed(false);

		BigDecimal minValue = new BigDecimal("0.01");
		BigDecimal maxValue = maxAmount.getAmount();
		amountNumberText.setMinValue(minValue);
		amountNumberText.setMaxValue(maxValue);
		amountNumberText.setValue(maxValue);

		amountNumberText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				validate();
			}
		});


		createLabel(composite, maxAmount.getSymbol());


		return dialogArea;
	}


	private void validate() {
//		BigDecimal tmpAmount = amountNumberText.getValue();
//
//		// C1: The sign of a Clearing must be the same as its associated Payment.
//		if (tmpAmount.signum() != 0 && tmpAmount.signum() != requiredSignum) {
//			setErrorMessage(I18N.AmountForClearingSameSign);
//			amount = null;
//		}
//		// C2: The absolute amount of a Payment must be greater or equal than the absolute amount of all its associated Clearings.
//		else if (tmpAmount.abs().compareTo(paymentVO.getAmount().abs()) == 1) {
//			setErrorMessage(I18N.AmountForClearingNotLargerThanPayment);
//			amount = null;
//		}
//		else {
//			setErrorMessage(null);
//			amount = tmpAmount;
//		}
//		boolean canFinish = (amount != null);
//		getButton(IDialogConstants.OK_ID).setEnabled(canFinish);

		refundAmount = amountNumberText.getValue();
	}


	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}


	/**
	 * Return the initial size of the dialog
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(800, 600);
	}


	@Override
	protected boolean isResizable() {
		return true;
	}
	
	
	@Override
	protected void okPressed() {
		refundAmount = amountNumberText.getValue();
		super.okPressed();
	}


	public void setMaxAmount(CurrencyAmount maxAmount) {
		this.maxAmount = maxAmount;
	}


	public BigDecimal getRefundAmount() {
		return refundAmount;
	}

}
