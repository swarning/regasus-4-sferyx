package de.regasus.report.view;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import com.lambdalogic.util.model.ModelEvent;
import com.lambdalogic.util.model.ModelListener;
import com.lambdalogic.util.rcp.BusyCursorHelper;

import de.regasus.core.ServerModel;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.model.Activator;
import de.regasus.report.ReportI18N;
import de.regasus.report.model.UserReportDirListModel;
import de.regasus.report.model.UserReportListModel;

public class RefreshUserReportListAction
extends Action
implements ActionFactory.IWorkbenchAction, ModelListener {

	public static final String ID = "com.lambdalogic.mi.reporting.ui.action.RefreshUserReportListAction"; 

	// Models
	private ServerModel serverModel = ServerModel.getInstance();

	
	public RefreshUserReportListAction() {
		super();
		setId(ID);
		setText(ReportI18N.RefreshUserReportListAction_Text);
		setToolTipText(ReportI18N.RefreshUserReportListAction_ToolTip);
		setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(
			de.regasus.core.ui.Activator.PLUGIN_ID, 
			de.regasus.core.ui.IImageKeys.REFRESH));
		

		serverModel.addListener(this);
		setEnabled(serverModel.isLoggedIn());
	}
	
	
	public void dispose() {
		serverModel.removeListener(this);
	}
	
	
	public void run() {
		try {
			BusyCursorHelper.busyCursorWhile(new Runnable() {
				public void run() {
					try {
						UserReportListModel.getInstance().refresh();
						UserReportDirListModel.getInstance().refresh();
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
