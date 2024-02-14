package de.regasus.profile.customfield.view;

import org.eclipse.ui.plugin.AbstractUIPlugin;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.Activator;
import com.lambdalogic.util.rcp.UtilI18N;
import de.regasus.core.ui.IImageKeys;
import de.regasus.core.ui.action.AbstractAction;
import de.regasus.profile.ProfileCustomFieldGroupModel;
import de.regasus.profile.ProfileCustomFieldModel;

public class RefreshTreeAction extends AbstractAction {

	
	public RefreshTreeAction() {
		setId(getClass().getName());
		setText(UtilI18N.Refresh);
		setImageDescriptor(
			AbstractUIPlugin.imageDescriptorFromPlugin(
				Activator.PLUGIN_ID,
				IImageKeys.REFRESH
			)
		);
	}
	
	
	public void run() {
		try {
			ProfileCustomFieldModel.getInstance().refresh();
			ProfileCustomFieldGroupModel.getInstance().refresh();
		}
		catch (Throwable t) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t);
		}
	}
	
}
