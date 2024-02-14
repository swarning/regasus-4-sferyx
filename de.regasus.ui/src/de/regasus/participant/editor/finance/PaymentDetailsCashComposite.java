package de.regasus.participant.editor.finance;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.lambdalogic.messeinfo.invoice.InvoiceLabel;
import com.lambdalogic.messeinfo.invoice.data.PaymentVO;
import com.lambdalogic.util.StringHelper;

public class PaymentDetailsCashComposite extends Composite {

	private Label payerLabel;
	
	
	public PaymentDetailsCashComposite(Composite parent, int style) {
		super(parent, style);
		
		setLayout(new GridLayout(2, false));

		// Payer
		{
			Label label = new Label(this, SWT.NONE);
			label.setText(InvoiceLabel.Payer.getString() + ":");
			label.setLayoutData(PaymentDetailsComposite.getDefaultLabelGridData());
			
			payerLabel = new Label(this, SWT.NONE);
			payerLabel.setLayoutData(PaymentDetailsComposite.getDefaultTextGridData());
		}
	}
	
	
	public void setPaymentVO(PaymentVO paymentVO) {
		payerLabel.setText(StringHelper.avoidNull(paymentVO.getPayerName()));
	}
	
}
