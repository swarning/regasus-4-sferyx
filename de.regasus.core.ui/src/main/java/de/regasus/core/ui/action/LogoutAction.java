package de.regasus.core.ui.action;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import com.lambdalogic.util.model.ModelEvent;
import com.lambdalogic.util.model.ModelListener;

import de.regasus.core.ServerModel;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.model.Activator;
import de.regasus.core.ui.CoreI18N;
import de.regasus.core.ui.editor.AbstractEditor;

public class LogoutAction extends Action implements ModelListener {

	public static final String ID = "de.regasus.core.ui.action.LogoutAction"; 
	
	// Models
	private static final ServerModel serverModel = ServerModel.getInstance();


	public LogoutAction() {
		super();
		setId(ID);
		setText(CoreI18N.LogoutAction_Text);
		setToolTipText(CoreI18N.LogoutAction_ToolTip);
		setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(
			de.regasus.core.ui.Activator.PLUGIN_ID, 
			de.regasus.core.ui.IImageKeys.LOGOUT));
		
		// beim ServerModel registrieren
		serverModel.addListener(this);
		setEnabled(serverModel.isLoggedIn());
	}
	
	
	public void dataChange(ModelEvent event) {
		if (event.getSource() instanceof ServerModel) {
			setEnabled(serverModel.isLoggedIn());
		}
	}

	
	public void dispose() {
		serverModel.removeListener(this);
	}
	
	
	public void run() {
		try {
			boolean notCanceled = PlatformUI.getWorkbench().saveAllEditors(true);
			if (notCanceled) {
				AbstractEditor.closeAllEditors();
				
				serverModel.logout();
			}
		}
		catch (Throwable t) {
			RegasusErrorHandler.handleApplicationError(Activator.PLUGIN_ID, getClass().getName(), t);
		}
	}

}
