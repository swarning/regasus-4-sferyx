package de.regasus.hotel.chain.view;

import org.eclipse.ui.plugin.AbstractUIPlugin;

import de.regasus.I18N;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.Activator;
import de.regasus.core.ui.IImageKeys;
import de.regasus.core.ui.action.AbstractAction;
import de.regasus.hotel.HotelChainModel;

public class RefreshHotelChainAction extends AbstractAction {

	public static final String ID = "com.lambdalogic.mi.masterdata.ui.hotel.RefreshHotelChainAction"; 

	
	public RefreshHotelChainAction() {
		super();
		
		setId(ID);
		setText(I18N.RefreshHotelChainAction_Text);
		setToolTipText(I18N.RefreshHotelChainAction_ToolTip);
		setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(
			de.regasus.core.ui.Activator.PLUGIN_ID,
			IImageKeys.REFRESH
		));
	}
	
	
	/* runWithBusyCursor(), because.
	 * a) Refreshing may last long.
	 * b) Refresh don't need to be run in the Display-Thread.
	 */
	@Override
	public void runWithBusyCursor() {
		try {
			HotelChainModel.getInstance().refresh();
		}
		catch (Throwable t) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t);
		}
	}
	
}

