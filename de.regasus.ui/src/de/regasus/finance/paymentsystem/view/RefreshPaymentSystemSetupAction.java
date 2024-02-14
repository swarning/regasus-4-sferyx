package de.regasus.finance.paymentsystem.view;

import java.lang.invoke.MethodHandles;

import org.eclipse.ui.plugin.AbstractUIPlugin;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.IImageKeys;
import de.regasus.core.ui.action.AbstractAction;
import de.regasus.finance.FinanceI18N;
import de.regasus.finance.PaymentSystemSetupModel;
import de.regasus.ui.Activator;

public class RefreshPaymentSystemSetupAction extends AbstractAction {

	public static final String ID = MethodHandles.lookup().lookupClass().getName();


	public RefreshPaymentSystemSetupAction() {
		super();
		setId(ID);
		setText(FinanceI18N.PaymentSystemSetup_Action_Refresh_Text);
		setToolTipText(FinanceI18N.PaymentSystemSetup_Action_Refresh_ToolTip);
		setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(
			de.regasus.core.ui.Activator.PLUGIN_ID,
			IImageKeys.REFRESH
		));
	}


	@Override
	public void runWithBusyCursor() {
		try {
			PaymentSystemSetupModel.getInstance().refresh();
		}
		catch (Throwable t) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t);
		}
	}

}
