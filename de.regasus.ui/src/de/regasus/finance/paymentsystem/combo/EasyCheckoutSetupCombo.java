package de.regasus.finance.paymentsystem.combo;

import java.util.Collection;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import de.regasus.finance.PaymentSystem;
import de.regasus.finance.PaymentSystemSetup;
import de.regasus.finance.easycheckout.EasyCheckoutSetup;


public class EasyCheckoutSetupCombo extends PaymentSystemSetupCombo {


	public EasyCheckoutSetupCombo(Composite parent, int style) throws Exception {
		super(parent, SWT.NONE);

		// avoid showing PaymentSystemSetup of other PaymentSystems but EasyCheckout
		keepEntityInList = false;
	}


	@Override
	protected LabelProvider getLabelProvider() {
		return new LabelProvider() {
			@Override
			public String getText(Object element) {
				PaymentSystemSetup paymentSystemSetup = (PaymentSystemSetup) element;

				StringBuilder label = new StringBuilder();
				if (paymentSystemSetup.getName() != null) {
    				label.append( paymentSystemSetup.getName() );
    				if ( paymentSystemSetup.isTest() ) {
    					label.append(" (");
    					label.append( EasyCheckoutSetup.TEST_ENVIRONMENT.getString() );
    					label.append(")");
    				}
				}
				return label.toString();
			}
		};
	}


	@Override
	protected Collection<PaymentSystemSetup> getModelData() throws Exception {
		Collection<PaymentSystemSetup> modelData = model.getPaymentSystemSetups(PaymentSystem.EASY_CHECKOUT);
		return modelData;
	}

}
