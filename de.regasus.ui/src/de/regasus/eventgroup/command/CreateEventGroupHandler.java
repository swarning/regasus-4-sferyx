package de.regasus.eventgroup.command;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.eventgroup.editor.EventGroupEditor;
import de.regasus.eventgroup.editor.EventGroupEditorInput;
import de.regasus.ui.Activator;

public class CreateEventGroupHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		// Open editor for new Event Group
		EventGroupEditorInput input = new EventGroupEditorInput();
		try {
			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().openEditor(
				input,
				EventGroupEditor.ID
			);
		}
		catch (PartInitException e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}

		return null;
	}

}
