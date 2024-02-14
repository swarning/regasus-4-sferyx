package de.regasus.hotel.view.tree;

import org.eclipse.ui.plugin.AbstractUIPlugin;

import de.regasus.common.CountryCitiesModel;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.Activator;
import com.lambdalogic.util.rcp.UtilI18N;
import de.regasus.core.ui.IImageKeys;
import de.regasus.core.ui.action.AbstractAction;
import de.regasus.hotel.HotelCountriesModel;
import de.regasus.hotel.HotelModel;
import de.regasus.hotel.RoomDefinitionModel;

public class RefreshTreeAction extends AbstractAction {
	
	public RefreshTreeAction() {
		super();
		setId(getClass().getName());
		setText(UtilI18N.Refresh);
		setImageDescriptor(
			AbstractUIPlugin.imageDescriptorFromPlugin(
				Activator.PLUGIN_ID,
				IImageKeys.REFRESH
			)
		);
	}
	
	
	@Override
	public void runWithBusyCursor() {
		try {
			HotelCountriesModel.getInstance().refresh();
			CountryCitiesModel.getInstance().refresh();
			HotelModel.getInstance().refresh();
			RoomDefinitionModel.getInstance().refresh();
		}
		catch (Throwable t) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t);
		}
	}
	
}
