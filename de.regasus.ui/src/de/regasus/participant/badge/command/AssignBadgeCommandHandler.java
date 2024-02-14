package de.regasus.participant.badge.command;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.handlers.HandlerUtil;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.participant.editor.ParticipantEditor;
import de.regasus.ui.Activator;

public class AssignBadgeCommandHandler  extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {

			// Since the badge table doesn't work as selection provider, we have to find the selected BadgeCVO like this
			IEditorPart activeEditor = HandlerUtil.getActiveEditor(event);

			if (activeEditor instanceof ParticipantEditor) {
				ParticipantEditor participantEditor = (ParticipantEditor) activeEditor;

				// Delegate the handling to the composite method, because there is a button
				// which also is to activate that command
				participantEditor.assignBadge();
			}
		}
		catch (Throwable e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}

		return null;
	}

}
