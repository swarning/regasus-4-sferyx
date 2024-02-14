package de.regasus.participant.editor.finance;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.lambdalogic.messeinfo.contact.CreditCard;
import com.lambdalogic.messeinfo.contact.data.CreditCardTypeVO;
import com.lambdalogic.messeinfo.contact.data.CreditCardVO;
import com.lambdalogic.messeinfo.invoice.data.PaymentVO;
import com.lambdalogic.util.StringHelper;

import de.regasus.core.CreditCardTypeModel;
import de.regasus.finance.PaymentSystem;
import de.regasus.finance.PaymentType;

/**
 * A composite for details of payments of {@link PaymentType#CREDIT_CARD},
 * whether via {@link PaymentSystem#PAYENGINE} or one of the rarely used others.
 * <p>
 * May be shown together with the {@link PaymentDetailsPayEngineGroup}.
 */
public class PaymentDetailsCreditCardComposite extends Composite {

	private Label typeLabel;
	private Label ownerLabel;
	private Label numberLabel;
	private Label expirationDateLabel;

	private CreditCardTypeModel creditCardTypeModel = CreditCardTypeModel.getInstance();


	public PaymentDetailsCreditCardComposite(Composite parent, int style) {
		super(parent, style );

		setLayout(new GridLayout(2, false));

		// Type
		{
			Label label = new Label(this, SWT.NONE);
			label.setText(CreditCard.CREDIT_CARD_TYPE.getLabel() + ":");
			label.setLayoutData(PaymentDetailsComposite.getDefaultLabelGridData());

			typeLabel = new Label(this, SWT.NONE);
			typeLabel.setLayoutData(PaymentDetailsComposite.getDefaultTextGridData());
		}

		// Owner
		{
			Label label = new Label(this, SWT.NONE);
			label.setText(CreditCard.OWNER.getLabel() + ":");
			label.setLayoutData(PaymentDetailsComposite.getDefaultLabelGridData());

			ownerLabel = new Label(this, SWT.NONE);
			ownerLabel.setLayoutData(PaymentDetailsComposite.getDefaultTextGridData());
		}

		// Number
		{
			Label label = new Label(this, SWT.NONE);
			label.setText(CreditCard.NUMBER.getLabel() + ":");
			label.setLayoutData(PaymentDetailsComposite.getDefaultLabelGridData());

			numberLabel = new Label(this, SWT.NONE);
			numberLabel.setLayoutData(PaymentDetailsComposite.getDefaultTextGridData());
		}

		// Expiration Date
		{
			Label label = new Label(this, SWT.NONE);
			label.setText(CreditCard.EXPIRATION.getLabel() + ":");
			label.setLayoutData(PaymentDetailsComposite.getDefaultLabelGridData());

			expirationDateLabel = new Label(this, SWT.NONE);
			expirationDateLabel.setLayoutData(PaymentDetailsComposite.getDefaultTextGridData());
		}
	}


	public void setPaymentVO(PaymentVO paymentVO) {

		CreditCardVO creditCardVO = paymentVO.getCreditCardVO();

		try {
			Long creditCardTypePK = creditCardVO.getCreditCardTypePK();
			if (creditCardTypePK != null) {
				CreditCardTypeVO creditCardTypeVO = creditCardTypeModel.getCreditCardTypeVO(creditCardTypePK);
    			String creditCardTypeName = creditCardTypeVO.getName();
    			typeLabel.setText(StringHelper.avoidNull(creditCardTypeName));
			}
		}
		catch (Exception e) {
			com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
		}

		ownerLabel.setText(StringHelper.avoidNull(creditCardVO.getOwner()));
		numberLabel.setText(StringHelper.avoidNull(creditCardVO.getNoInvisible()));
		expirationDateLabel.setText(StringHelper.avoidNull(creditCardVO.getExpirationAsString()));
	}

}
