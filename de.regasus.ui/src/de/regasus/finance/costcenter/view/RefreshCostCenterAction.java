package de.regasus.finance.costcenter.view;

import org.eclipse.ui.plugin.AbstractUIPlugin;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.IImageKeys;
import de.regasus.core.ui.action.AbstractAction;
import de.regasus.finance.CostCenter1Model;
import de.regasus.finance.FinanceI18N;
import de.regasus.ui.Activator;

public class RefreshCostCenterAction extends AbstractAction {

	public static final String ID = "com.lambdalogic.mi.invoice.ui.RefreshCostCenterAction"; 

	
	public RefreshCostCenterAction() {
		super();
		setId(ID);
		setText(FinanceI18N.CostCenter_Action_Refresh_Text);
		setToolTipText(FinanceI18N.CostCenter_Action_Refresh_ToolTip);
		setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(
			de.regasus.core.ui.Activator.PLUGIN_ID,
			IImageKeys.REFRESH
		));
	}
	
	
	public void runWithBusyCursor() {
		try {
			CostCenter1Model.getInstance().refresh();
		}
		catch (Throwable t) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t);
		}
	}
	
}
