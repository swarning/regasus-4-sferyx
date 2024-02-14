package de.regasus.common.language.view;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import com.lambdalogic.util.model.ModelEvent;
import com.lambdalogic.util.model.ModelListener;
import com.lambdalogic.util.rcp.BusyCursorHelper;

import de.regasus.I18N;
import de.regasus.core.LanguageModel;
import de.regasus.core.ServerModel;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.Activator;
import de.regasus.core.ui.IImageKeys;

public class RefreshLanguageAction
extends Action
implements ActionFactory.IWorkbenchAction, ModelListener {

	public static final String ID = "de.regasus.core.ui.language.RefreshLanguageAction"; 

	// Models
	private ServerModel serverModel = ServerModel.getInstance();

	
	public RefreshLanguageAction() {
		super();
		setId(ID);
		setText(I18N.RefreshLanguageAction_Text);
		setToolTipText(I18N.RefreshLanguageAction_ToolTip);
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
			BusyCursorHelper.busyCursorWhile(new Runnable() {
				public void run() {
					try {
						LanguageModel.getInstance().refresh();
					}
					catch (Throwable t) {
						RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t);
					}
				}
			});
		}
		catch (Throwable t) {
			RegasusErrorHandler.handleApplicationError(Activator.PLUGIN_ID, getClass().getName(), t);
		}
	}


	public void dataChange(ModelEvent event) {
		boolean enable = serverModel.isLoggedIn();
		setEnabled(enable);
	}
	
}
