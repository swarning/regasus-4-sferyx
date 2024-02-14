package de.regasus.participant.editor.finance;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.lambdalogic.messeinfo.contact.Bank;
import com.lambdalogic.messeinfo.invoice.InvoiceLabel;
import com.lambdalogic.messeinfo.invoice.data.PaymentVO;
import com.lambdalogic.util.StringHelper;

public class PaymentDetailsChequeComposite extends Composite {

	private Label bankNameLabel;
	private Label chequeNumberLabel;


	public PaymentDetailsChequeComposite(Composite parent, int style) {
		super(parent, style );

		setLayout(new GridLayout(2, false));

		// Bank Name
		{
			Label label = new Label(this, SWT.NONE);
			label.setText(Bank.BANK_NAME.getString() + ":");
			label.setLayoutData(PaymentDetailsComposite.getDefaultLabelGridData());

			bankNameLabel = new Label(this, SWT.NONE);
			bankNameLabel.setLayoutData(PaymentDetailsComposite.getDefaultTextGridData());
		}

		// Cheque Number
		{
			Label label = new Label(this, SWT.NONE);
			label.setText(InvoiceLabel.ChequeNumber.getString() + ":");
			label.setLayoutData(PaymentDetailsComposite.getDefaultLabelGridData());

			chequeNumberLabel = new Label(this, SWT.NONE);
			chequeNumberLabel.setLayoutData(PaymentDetailsComposite.getDefaultTextGridData());
		}
	}


	public void setPaymentVO(PaymentVO paymentVO) {
		bankNameLabel.setText(StringHelper.avoidNull(paymentVO.getChequeBank()));
		chequeNumberLabel.setText(StringHelper.avoidNull(paymentVO.getChequeNo()));
	}
}
