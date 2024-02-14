// REFERENCE
package de.regasus.finance.paymentsystem.editor;

import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import com.lambdalogic.util.rcp.EntityGroup;

import de.regasus.finance.FinanceI18N;
import de.regasus.finance.easycheckout.EasyCheckoutSetup;


public class EasyCheckoutSetupGroup extends EntityGroup<EasyCheckoutSetup> {

	/*
	 * Do not initialize local non-static fields here but in initialize(Object[])!
	 * this.createWidgets() is called by super constructor and therefore run before local fields are initialized.
	 */


	public EasyCheckoutSetupGroup(Composite parent, int style) throws Exception {
		super(parent, style);

		setText(FinanceI18N.EasyCheckoutSetup);
	}


	@Override
	protected void createWidgets(Composite parent) throws Exception {
		setLayout(new GridLayout(2, false));

		widgetBuilder.createDefaultWithLabel(EasyCheckoutSetup.MERCHANT_ID);
		widgetBuilder.createDefaultWithLabel(EasyCheckoutSetup.TEST_ENVIRONMENT);
		widgetBuilder.createDefaultWithLabel(EasyCheckoutSetup.SECRET_KEY);
		widgetBuilder.createDefaultWithLabel(EasyCheckoutSetup.CHECKOUT_KEY);
		widgetBuilder.createDefaultWithLabel(EasyCheckoutSetup.TERMS_URL);
		widgetBuilder.verticalSpace();
		widgetBuilder.createDefaultWithLabel(EasyCheckoutSetup.USER);
		widgetBuilder.createDefaultWithLabel(EasyCheckoutSetup.PASSWORD);
	}

}
