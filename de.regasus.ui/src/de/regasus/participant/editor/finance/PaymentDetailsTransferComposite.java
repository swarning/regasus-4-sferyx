package de.regasus.participant.editor.finance;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.lambdalogic.messeinfo.contact.Bank;
import com.lambdalogic.messeinfo.contact.ContactLabel;
import com.lambdalogic.messeinfo.contact.data.BankVO;
import com.lambdalogic.messeinfo.invoice.data.PaymentVO;
import com.lambdalogic.util.StringHelper;

public class PaymentDetailsTransferComposite extends Composite {

	private Label bankOwnerLabel;
	private Label bankNameLabel;
	private Label bicLabel;
	private Label ibanLabel;
	private Label bankIdentifierCodeLabel;
	private Label accountNumberLabel;


	public PaymentDetailsTransferComposite(Composite parent, int style) {
		super(parent, style);

		setLayout(new GridLayout(2, false));

		// Bank Owner
		{
			Label label = new Label(this, SWT.NONE);
			label.setText(Bank.BANK_OWNER.getString() + ":");
			label.setLayoutData(PaymentDetailsComposite.getDefaultLabelGridData());

			bankOwnerLabel = new Label(this, SWT.NONE);
			bankOwnerLabel.setLayoutData(PaymentDetailsComposite.getDefaultTextGridData());
		}

		// Bank Name
		{
			Label label = new Label(this, SWT.NONE);
			label.setText(Bank.BANK_NAME.getString() + ":");
			label.setLayoutData(PaymentDetailsComposite.getDefaultLabelGridData());

			bankNameLabel = new Label(this, SWT.NONE);
			bankNameLabel.setLayoutData(PaymentDetailsComposite.getDefaultTextGridData());
		}

		// Bank Identifier Code (new, the BIC)
		{
			Label label = new Label(this, SWT.NONE);
			label.setText(Bank.BIC.getString() + ":");
			label.setLayoutData(PaymentDetailsComposite.getDefaultLabelGridData());

			bicLabel = new Label(this, SWT.NONE);
			bicLabel.setLayoutData(PaymentDetailsComposite.getDefaultTextGridData());
		}

		// IBAN
		{
			Label label = new Label(this, SWT.NONE);
			label.setText(Bank.IBAN.getString() + ":");
			label.setLayoutData(PaymentDetailsComposite.getDefaultLabelGridData());

			ibanLabel = new Label(this, SWT.NONE);
			ibanLabel.setLayoutData(PaymentDetailsComposite.getDefaultTextGridData());
		}

		// Bank Identifier Code (old, so-called "Bankleitzahl")
		{
			Label label = new Label(this, SWT.NONE);
			label.setText(ContactLabel.bankIdentifierCode_short.getString() + ":");
			label.setLayoutData(PaymentDetailsComposite.getDefaultLabelGridData());

			bankIdentifierCodeLabel = new Label(this, SWT.NONE);
			bankIdentifierCodeLabel.setLayoutData(PaymentDetailsComposite.getDefaultTextGridData());
		}

		// Bank Account Number
		{
			Label label = new Label(this, SWT.NONE);
			label.setText(ContactLabel.bankAccountNumber_short.getString() + ":");
			label.setLayoutData(PaymentDetailsComposite.getDefaultLabelGridData());

			accountNumberLabel = new Label(this, SWT.NONE);
			accountNumberLabel.setLayoutData(PaymentDetailsComposite.getDefaultTextGridData());
		}
	}


	public void setPaymentVO(PaymentVO paymentVO) {
		BankVO bankVO = paymentVO.getBankVO();
		bankOwnerLabel.setText(StringHelper.avoidNull(bankVO.getBankOwner()));
		bankNameLabel.setText(StringHelper.avoidNull(bankVO.getBankName()));
		bicLabel.setText(StringHelper.avoidNull(bankVO.getBic()));
		ibanLabel.setText(StringHelper.avoidNull(bankVO.getIban()));
		bankIdentifierCodeLabel.setText(StringHelper.avoidNull(bankVO.getBankIdentifierCode()));
		accountNumberLabel.setText(StringHelper.avoidNull(bankVO.getBankAccountNumber()));
	}

}
