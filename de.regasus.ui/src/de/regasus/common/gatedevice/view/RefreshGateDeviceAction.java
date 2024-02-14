/**
 * RefreshGateDeviceAction.java
 * created on 25.09.2013 14:48:06
 */
package de.regasus.common.gatedevice.view;

import org.eclipse.ui.plugin.AbstractUIPlugin;

import de.regasus.I18N;
import de.regasus.common.GateDeviceModel;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.Activator;
import de.regasus.core.ui.IImageKeys;
import de.regasus.core.ui.action.AbstractAction;

public class RefreshGateDeviceAction extends AbstractAction {

	public static final String ID = "de.regasus.event.gatedevice.RefreshGateDeviceAction";
	
	
	public RefreshGateDeviceAction() {
		super();
		
		setId(ID);
		setText(I18N.RefreshGateDeviceAction_Text);
		setToolTipText(I18N.RefreshGateDeviceAction_ToolTip);
		setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(
			Activator.PLUGIN_ID,
			IImageKeys.REFRESH
		));
	}
	
	
	@Override
	public void runWithBusyCursor() {
		try {
			GateDeviceModel.getInstance().refresh();
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}
	
}
