/**
 * RefreshParticipantStateAction.java
 * Created on 16.04.2012
 */
package de.regasus.participant.state.view;

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
import de.regasus.participant.ParticipantStateModel;

/**
 * @author huuloi
 *
 */
public class RefreshParticipantStateAction 
extends Action
implements ActionFactory.IWorkbenchAction, ModelListener {
	
	public static final String ID = "com.lambdalogic.mi.masterdata.ui.action.participantState.RefreshParticipantStateAction";
	
	// Models 
	private ServerModel serverModel = ServerModel.getInstance();
	
	public RefreshParticipantStateAction() {
		super();
		setId(ID);
		setText(I18N.RefreshParticipantStateAction_Text);
		setToolTipText(I18N.RefreshParticipantStateAction_ToolTip);
		setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(
			Activator.PLUGIN_ID,
			IImageKeys.REFRESH));
		
		serverModel.addListener(this);
		this.dataChange(null);
	}

	@Override
	public void dispose() {
		serverModel.removeListener(this);
	}
	
	@Override
	public void run() {
		try {
			BusyCursorHelper.busyCursorWhile(new Runnable() {
				@Override
				public void run() {
					try {
						ParticipantStateModel.getInstance().refresh();
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

	@Override
	public void dataChange(ModelEvent event) {
		boolean enable = serverModel.isLoggedIn();
		setEnabled(enable);
	}
	
}
