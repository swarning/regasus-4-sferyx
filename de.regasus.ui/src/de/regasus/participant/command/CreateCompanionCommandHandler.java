package de.regasus.participant.command;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import com.lambdalogic.messeinfo.participant.data.IParticipant;

import de.regasus.I18N;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.participant.ParticipantSelectionHelper;
import de.regasus.participant.editor.ParticipantEditor;
import de.regasus.participant.editor.ParticipantEditorInput;
import de.regasus.ui.Activator;

public class CreateCompanionCommandHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			// Determine the main participant
			IParticipant mainParticipant = ParticipantSelectionHelper.getParticipant(event);
			Long eventPK = mainParticipant.getEventId();

			if (mainParticipant != null) {
				Long mainParticipantID = mainParticipant.getPK();

				IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
				IWorkbenchPage page = window.getActivePage();
				ParticipantEditorInput editorInput = ParticipantEditorInput.getCreateCompanionInstance(mainParticipantID, eventPK);
				try {
					page.openEditor(editorInput, ParticipantEditor.ID);
				}
				catch (PartInitException e) {
					RegasusErrorHandler.handleApplicationError(Activator.PLUGIN_ID, getClass().getName(), e, I18N.CreateProfileAction_Error);
				}
			}
		}
		catch (Throwable t) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, this.getClass().getName(), t);
		}
		return null;
	}

}
