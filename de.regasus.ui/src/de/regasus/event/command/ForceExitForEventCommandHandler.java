package de.regasus.event.command;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.event.EventSelectionHelper;
import de.regasus.participant.ParticipantModel;
import de.regasus.participant.editor.ParticipantEditor;
import de.regasus.ui.Activator;

public class ForceExitForEventCommandHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			// Determine the Participants
			Long eventID = EventSelectionHelper.getEventID(event);

			if (eventID != null) {
				// try to save all ParticipantEditors
				boolean editorSaveCkeckOK = ParticipantEditor.saveEditors(ParticipantEditor.class);
				if (editorSaveCkeckOK) {
					ParticipantModel participantModel = ParticipantModel.getInstance();
					participantModel.forceExitForEvent(eventID);
				}
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}

		return null;
	}

}
