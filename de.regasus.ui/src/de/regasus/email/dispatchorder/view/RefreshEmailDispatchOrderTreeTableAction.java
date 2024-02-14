package de.regasus.email.dispatchorder.view;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import com.lambdalogic.util.model.ModelEvent;
import com.lambdalogic.util.model.ModelListener;

import de.regasus.core.ServerModel;
import de.regasus.core.error.RegasusErrorHandler;
import com.lambdalogic.util.rcp.UtilI18N;
import de.regasus.email.EmailDispatchModel;
import de.regasus.email.EmailDispatchOrderModel;
import de.regasus.email.EmailTemplateModel;
import de.regasus.email.IImageKeys;
import de.regasus.ui.Activator;

public class RefreshEmailDispatchOrderTreeTableAction
extends Action
implements ActionFactory.IWorkbenchAction, ModelListener {

	public static final String ID = "de.regasus.email.action.RefreshEmailDispatchOrderTreeTableAction"; 

	// Models
	private ServerModel serverModel = ServerModel.getInstance();

	
	public RefreshEmailDispatchOrderTreeTableAction() {
		super();
		setId(ID);
		setText(UtilI18N.Refresh);
		setToolTipText(UtilI18N.Refresh);
		setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(
			Activator.PLUGIN_ID,
			IImageKeys.REFRESH));
		

		serverModel.addListener(this);
		this.dataChange(null);
	}
	
	
	public void dispose() {
		serverModel.removeListener(this);
	}
	
	
	public void run() {
		try {
			EmailTemplateModel.getInstance().refresh();
			EmailDispatchOrderModel.getInstance().refresh();
			EmailDispatchModel.getInstance().refresh();
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	public void dataChange(ModelEvent event) {
		boolean enable = serverModel.isLoggedIn();
		setEnabled(enable);
	}
	
}
