package de.regasus.core.ui.statusline;

import com.lambdalogic.util.model.ModelEvent;
import com.lambdalogic.util.model.ModelListener;
import com.lambdalogic.util.rcp.StatusLineContributionItem;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.core.ServerModel;
import de.regasus.core.ServerModelData;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.Activator;
import de.regasus.core.ui.CoreI18N;



public class ServerStatusLineContribution
extends StatusLineContributionItem
implements ModelListener {

	// Models
	private ServerModel serverModel;

	public ServerStatusLineContribution() {
		super(
			"ServerStatusLineContribution",	// id
			true,							// visible
			40								// widthInChars
		);

		try {
			serverModel = ServerModel.getInstance();
			serverModel.addListener(this);
			ServerModelData serverModelData = serverModel.getModelData();
			setServerModelData(serverModelData);
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	@Override
	public void dataChange(ModelEvent event) {
		try {
			ServerModelData serverModelData = serverModel.getModelData();
			setServerModelData(serverModelData);
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	private void setServerModelData(ServerModelData serverModelData) {
		if (serverModelData != null) {
			final StringBuilder sb = new StringBuilder(40);
			if (serverModelData.isConnected()) {
				sb.append( serverModelData.getUser() );
				sb.append("@");
				sb.append( serverModelData.getHostWithoutPath() );
			}
			else {
				sb.append(CoreI18N.ServerStatusLineContribution_NoConnection);
			}

			SWTHelper.asyncExecDisplayThread(new Runnable() {
				@Override
				public void run() {
					setText(sb.toString());
				}
			});
		}
	}

}
