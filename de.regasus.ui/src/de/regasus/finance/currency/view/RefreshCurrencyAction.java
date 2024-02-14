package de.regasus.finance.currency.view;

import org.eclipse.ui.plugin.AbstractUIPlugin;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.IImageKeys;
import de.regasus.core.ui.action.AbstractAction;
import de.regasus.finance.CurrencyModel;
import de.regasus.finance.FinanceI18N;
import de.regasus.ui.Activator;

public class RefreshCurrencyAction extends AbstractAction {

	public static final String ID = "com.lambdalogic.mi.invoice.ui.RefreshCurrencyAction"; 

	
	public RefreshCurrencyAction() {
		super();
		setId(ID);
		setText(FinanceI18N.Currency_Action_Refresh_Text);
		setToolTipText(FinanceI18N.Currency_Action_Refresh_ToolTip);
		setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(
			de.regasus.core.ui.Activator.PLUGIN_ID,
			IImageKeys.REFRESH
		));
	}

	
	public void runWithBusyCursor() {
		try {
			CurrencyModel.getInstance().refresh();
		}
		catch (Throwable t) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t);
		}
	}
	
}
