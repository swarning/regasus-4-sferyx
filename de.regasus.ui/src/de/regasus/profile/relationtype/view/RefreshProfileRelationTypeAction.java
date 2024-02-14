package de.regasus.profile.relationtype.view;

import org.eclipse.ui.plugin.AbstractUIPlugin;

import de.regasus.I18N;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.Activator;
import de.regasus.core.ui.IImageKeys;
import de.regasus.core.ui.action.AbstractAction;
import de.regasus.profile.ProfileRelationTypeModel;

public class RefreshProfileRelationTypeAction extends AbstractAction {
	
	public static final String ID = "de.regasus.profile.action.RefreshProfileRelationTypeAction"; 
	
	
	public RefreshProfileRelationTypeAction() {
		super();
		
		setId(ID);
		setText(I18N.RefreshProfileRelationTypeAction_Text);
		setToolTipText(I18N.RefreshProfileRelationTypeAction_ToolTip);
		setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(
			Activator.PLUGIN_ID,
			IImageKeys.REFRESH
		));
	}

	
	@Override
	public void runWithBusyCursor() {
		try {
			ProfileRelationTypeModel.getInstance().refresh();
		}
		catch (Throwable t) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t);
		}
	}

}
