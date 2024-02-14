package de.regasus.finance.datatrans.dialog;

import org.eclipse.ui.plugin.AbstractUIPlugin;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.IImageKeys;
import de.regasus.core.ui.action.AbstractAction;
import de.regasus.finance.CostCenter2Model;
import de.regasus.finance.FinanceI18N;
import de.regasus.ui.Activator;

public class RefreshCostUnitAction extends AbstractAction {

	public static final String ID = "com.lambdalogic.mi.invoice.ui.RefreshCostUnitAction"; 

	
	public RefreshCostUnitAction() {
		super();
		setId(ID);
		setText(FinanceI18N.CostUnit_Action_Refresh_Text);
		setToolTipText(FinanceI18N.CostUnit_Action_Refresh_ToolTip);
		setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(
			de.regasus.core.ui.Activator.PLUGIN_ID,
			IImageKeys.REFRESH
		));
	}
	
	
	public void runWithBusyCursor() {
		try {
			CostCenter2Model.getInstance().refresh();
		}
		catch (Throwable t) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t);
		}
	}
	
}
