package de.regasus.participant.command;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

import de.regasus.participant.editor.ParticipantEditor;
import de.regasus.profile.editor.ProfileEditor;

public class AutoCorrectionCommandHandler extends AbstractHandler {

	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();

		// Try to find active editor in a possibly opened Editor
		IEditorPart editor = activePage.getActiveEditor();
		if (editor != null) {
			if (editor instanceof ParticipantEditor) {
				((ParticipantEditor) editor).autoCorrection();
			}
			else if (editor instanceof ProfileEditor) {
				((ProfileEditor) editor).autoCorrection();
			}
		}
		
		return null;
	}

}
