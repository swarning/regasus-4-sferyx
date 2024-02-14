package de.regasus.email.template.search.view;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import com.lambdalogic.util.model.ModelEvent;
import com.lambdalogic.util.model.ModelListener;

import de.regasus.core.ServerModel;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.email.EmailI18N;
import de.regasus.email.EmailTemplateModel;
import de.regasus.email.IImageKeys;
import de.regasus.ui.Activator;

public class RefreshEmailTemplateTableAction
extends Action
implements ActionFactory.IWorkbenchAction, ModelListener {

	public static final String ID = "de.regasus.email.action.RefreshEmailTemplateTableAction"; 

	// Models
	private ServerModel serverModel = ServerModel.getInstance();

	
	public RefreshEmailTemplateTableAction() {
		super();
		setId(ID);
		setText(EmailI18N.RefreshEmailTemplate_Text);
		setToolTipText(EmailI18N.RefreshEmailTemplate_ToolTip);
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
