package de.regasus.participant.type.view;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import com.lambdalogic.util.model.ModelEvent;
import com.lambdalogic.util.model.ModelListener;
import com.lambdalogic.util.rcp.BusyCursorHelper;

import de.regasus.I18N;
import de.regasus.core.ServerModel;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.Activator;
import de.regasus.core.ui.IImageKeys;
import de.regasus.participant.ParticipantTypeModel;

public class RefreshParticipantTypeAction
extends Action
implements ActionFactory.IWorkbenchAction, ModelListener {

	public static final String ID = "com.lambdalogic.mi.masterdata.ui.action.participantType.RefreshParticipantTypeAction"; 

	// Models
	private ServerModel serverModel = ServerModel.getInstance();

	
	public RefreshParticipantTypeAction() {
		super();
		setId(ID);
		setText(I18N.RefreshParticipantTypeAction_Text);
		setToolTipText(I18N.RefreshParticipantTypeAction_ToolTip);
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
						ParticipantTypeModel.getInstance().refresh();
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
