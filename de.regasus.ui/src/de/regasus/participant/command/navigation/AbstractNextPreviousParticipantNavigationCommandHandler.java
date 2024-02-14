package de.regasus.participant.command.navigation;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.handlers.HandlerUtil;

import com.lambdalogic.messeinfo.participant.Participant;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.participant.editor.ParticipantEditor;
import de.regasus.participant.editor.ParticipantEditorInput;
import de.regasus.ui.Activator;

abstract public class AbstractNextPreviousParticipantNavigationCommandHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			IEditorPart activeEditor = HandlerUtil.getActiveEditor(event);

			if (activeEditor instanceof ParticipantEditor) {

				// Find current participant
				ParticipantEditor participantEditor = (ParticipantEditor) activeEditor;
				Participant participant = participantEditor.getParticipant();

				// If there is no, or a new participant without a number, i cannot find the next one
				if (participant != null && participant.getID() != null) {
					// Let subclass tell the Long of participant to which we have to navigate
					Long eventPK = participant.getEventId();
					Long participantPK = participant.getID();

					Long pk = findPKOfTargetParticipant(eventPK, participantPK);

					// If we have to go to another one
					if (pk != null && !pk.equals(participantPK)) {
						ParticipantEditorInput editorInput = ParticipantEditorInput.getEditInstance(pk);

						// Close current participant editor
						IWorkbenchPage activePage = HandlerUtil.getActiveWorkbenchWindow(event).getActivePage();

						activePage.saveEditor(participantEditor, true /* ask for saving */);

						if (! participantEditor.isDirty()) {
							activePage.closeEditor(participantEditor, false /* don't ask again for saving */);
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
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
		return null;
	}


	abstract protected Long findPKOfTargetParticipant(Long eventPK, Long participantPK);

}
