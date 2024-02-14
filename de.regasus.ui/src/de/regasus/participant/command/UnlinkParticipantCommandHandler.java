package de.regasus.participant.command;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.handlers.HandlerUtil;

import com.lambdalogic.messeinfo.participant.data.IParticipant;

import de.regasus.I18N;
import de.regasus.core.error.RegasusErrorHandler;
import com.lambdalogic.util.rcp.UtilI18N;
import de.regasus.participant.ParticipantSelectionHelper;
import de.regasus.person.PersonLinkModel;
import de.regasus.ui.Activator;

public class UnlinkParticipantCommandHandler extends AbstractHandler {

	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			IParticipant participant = ParticipantSelectionHelper.getParticipant(event);
			
			if (participant != null) {
				String msg = I18N.UnlinkParticipantCommandHandler_Confirmation;
				msg = msg.replaceFirst("<name>", participant.getName());
				msg = msg.replaceFirst("<no>", String.valueOf(participant.getNumber()));
				
				boolean unlinkOK = MessageDialog.openQuestion(
					HandlerUtil.getActiveShell(event),
					UtilI18N.Question,
					msg
				);

				if (unlinkOK) {
					Long participantID = participant.getPK();
					PersonLinkModel.getInstance().unlinkParticipant(participantID);
				}
			}
		}			
		catch (Throwable t) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t);
		}
		return null;
	}

}
