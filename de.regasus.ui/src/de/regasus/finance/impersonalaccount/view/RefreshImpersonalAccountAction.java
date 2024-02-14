package de.regasus.finance.impersonalaccount.view;

import org.eclipse.ui.plugin.AbstractUIPlugin;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.IImageKeys;
import de.regasus.core.ui.action.AbstractAction;
import de.regasus.finance.FinanceI18N;
import de.regasus.finance.ImpersonalAccountModel;
import de.regasus.ui.Activator;

public class RefreshImpersonalAccountAction extends AbstractAction {

	public static final String ID = "com.lambdalogic.mi.invoice.ui.RefreshImpersonalAccountAction"; 

	
	public RefreshImpersonalAccountAction() {
		super();
		setId(ID);
		setText(FinanceI18N.ImpersonalAccount_Action_Refresh_Text);
		setToolTipText(FinanceI18N.ImpersonalAccount_Action_Refresh_ToolTip);
		setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(
			de.regasus.core.ui.Activator.PLUGIN_ID,
			IImageKeys.REFRESH
		));
	}
	
	
	public void run() {
		try {
			ImpersonalAccountModel.getInstance().refresh();
		}
		catch (Throwable t) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t);
		}
	}
	
}
