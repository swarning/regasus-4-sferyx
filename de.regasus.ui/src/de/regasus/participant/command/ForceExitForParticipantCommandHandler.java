package de.regasus.participant.command;

import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import com.lambdalogic.messeinfo.participant.Participant;
import com.lambdalogic.messeinfo.participant.data.IParticipant;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.participant.ParticipantModel;
import de.regasus.participant.ParticipantSelectionHelper;
import de.regasus.participant.editor.ParticipantEditor;
import de.regasus.ui.Activator;

public class ForceExitForParticipantCommandHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			// Determine the Participants
			List<IParticipant> participantList = ParticipantSelectionHelper.getParticipants(event);

			if (participantList != null && !participantList.isEmpty()) {
				// try to save the editors
				List<Long> participantPKs = Participant.getIParticipantPKs(participantList);
				boolean editorSaveCkeckOK = ParticipantEditor.saveEditor(participantPKs);
				if (!editorSaveCkeckOK) {
					return null;
				}


				ParticipantModel participantModel = ParticipantModel.getInstance();
				for (IParticipant participant : participantList) {
					participantModel.forceExitForParticipant(participant.getPK());
				}
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}

		return null;
	}

}
