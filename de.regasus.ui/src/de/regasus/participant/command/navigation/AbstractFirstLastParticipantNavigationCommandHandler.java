package de.regasus.participant.command.navigation;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.handlers.HandlerUtil;

import com.lambdalogic.messeinfo.participant.Participant;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.event.EventSelectionHelper;
import de.regasus.participant.editor.ParticipantEditor;
import de.regasus.participant.editor.ParticipantEditorInput;
import de.regasus.ui.Activator;

abstract public class AbstractFirstLastParticipantNavigationCommandHandler extends AbstractHandler {

	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			// Only if we can find out a "event context"
			Long eventID = EventSelectionHelper.getEventID(event);

			if (eventID != null) {

				// There might be an open participant, if yes, its editor might have to be closed
				IEditorPart activeEditor = HandlerUtil.getActiveEditor(event);
				ParticipantEditor participantEditor = null;
				Long participantPK = null;

				if (activeEditor instanceof ParticipantEditor) {
					participantEditor = (ParticipantEditor) activeEditor;
					Participant participant = participantEditor.getParticipant();
					Long id = participant.getID();
					if (id != null) {
						participantPK = id;
					}
				}

				Long pk = findPKOfTargetParticipant(eventID);

				// If we have to go to another one, but only if the target is not the one that is open (if there is one=
				if (pk != null && !pk.equals(participantPK)) {

					ParticipantEditorInput editorInput = ParticipantEditorInput.getEditInstance(pk);
					IWorkbenchPage activePage = HandlerUtil.getActiveWorkbenchWindow(event).getActivePage();

					
					// Close current participant editor
					if (participantEditor != null) {
						activePage.saveEditor(participantEditor, true /* ask for saving */);
						
						if (! activeEditor.isDirty()) {
							activePage.closeEditor(participantEditor, false /* don't ask again for saving */);
						}
						
					}

					// Open editor for new participant
					activePage.openEditor(editorInput, ParticipantEditor.ID);

				}
				else {
					// Signal that there is nobody to navigate to
					HandlerUtil.getActiveShell(event).getDisplay().beep();
				}
			}
			else {
				// Signal that there is nobody to navigate to
				HandlerUtil.getActiveShell(event).getDisplay().beep();
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
		return null;
	}


	abstract protected Long findPKOfTargetParticipant(Long eventPK);

}
