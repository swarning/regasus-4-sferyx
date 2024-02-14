package de.regasus.profile.role.view;

import org.eclipse.ui.plugin.AbstractUIPlugin;

import de.regasus.I18N;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.Activator;
import de.regasus.core.ui.IImageKeys;
import de.regasus.core.ui.action.AbstractAction;
import de.regasus.profile.ProfileRoleModel;

public class RefreshProfileRoleAction extends AbstractAction {
	
	public static final String ID = "de.regasus.profile.action.RefreshProfileRoleAction"; 
	
	
	public RefreshProfileRoleAction() {
		super();
		
		setId(ID);
		setText(I18N.RefreshProfileRoleAction_Text);
		setToolTipText(I18N.RefreshProfileRoleAction_ToolTip);
		setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(
			Activator.PLUGIN_ID,
			IImageKeys.REFRESH
		));
	}

	
	@Override
	public void runWithBusyCursor() {
		try {
			ProfileRoleModel.getInstance().refresh();
		}
		catch (Throwable t) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t);
		}
	}

}
