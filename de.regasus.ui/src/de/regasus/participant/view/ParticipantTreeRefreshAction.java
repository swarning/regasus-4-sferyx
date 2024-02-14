package de.regasus.participant.view;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import de.regasus.I18N;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.participant.ParticipantModel;
import de.regasus.ui.Activator;


public class ParticipantTreeRefreshAction
extends Action 
implements ActionFactory.IWorkbenchAction {

	public static final String ID = "de.regasus.event.action.ParticipantTreeRefreshAction"; 
	
	private ParticipantTreeView participantTreeView;
	
	
	public ParticipantTreeRefreshAction(ParticipantTreeView participantTreeView) {
		super();
		this.participantTreeView = participantTreeView;
		setId(ID);
		setText(I18N.ParticipantTreeRefreshAction_Text);
		setToolTipText(I18N.ParticipantTreeRefreshAction_ToolTip);
		setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(
			de.regasus.core.ui.Activator.PLUGIN_ID, 
			de.regasus.core.ui.IImageKeys.REFRESH
		));
	}
	
	
	public void run() {
		if (participantTreeView != null) {
			Long rootParticipantPK = participantTreeView.getRootPK();
			if (rootParticipantPK != null) {
				try {
					ParticipantModel.getInstance().refreshForeignKey(rootParticipantPK);
				}
				catch (Exception e) {
					RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
				}
			}
		}
	}


	public void dispose() {
	}

}
