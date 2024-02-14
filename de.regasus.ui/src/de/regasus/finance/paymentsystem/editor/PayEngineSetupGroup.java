package de.regasus.finance.paymentsystem.editor;

import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import com.lambdalogic.util.rcp.EntityGroup;

import de.regasus.finance.FinanceI18N;
import de.regasus.finance.payengine.PayEngineSetup;


public class PayEngineSetupGroup extends EntityGroup<PayEngineSetup> {

	/*
	 * Do not initialize local non-static fields here but in initialize(Object[])!
	 * this.createWidgets() is called by super constructor and therefore run before local fields are initialized.
	 */


	public PayEngineSetupGroup(Composite parent, int style) throws Exception {
		super(parent, style);

		setText(FinanceI18N.PayEngineSetup);
	}


	@Override
	protected void createWidgets(Composite parent) throws Exception {
		setLayout(new GridLayout(2, false));

		widgetBuilder.createDefaultWithLabel(PayEngineSetup.PSPID);
		widgetBuilder.createDefaultWithLabel(PayEngineSetup.TEST_ONLY);
		widgetBuilder.createDefaultWithLabel(PayEngineSetup.IN_PASSPHRASE_ECOMMERCE);
		widgetBuilder.createDefaultWithLabel(PayEngineSetup.IN_PASSPHRASE_DIRECT_LINK);
		widgetBuilder.createDefaultWithLabel(PayEngineSetup.OUT_PASSPHRASE);
		widgetBuilder.createDefaultWithLabel(PayEngineSetup.API_USER);
		widgetBuilder.createDefaultWithLabel(PayEngineSetup.API_PASSWORD);
	}

}
