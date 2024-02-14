package de.regasus.programme.programmepoint.command;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.event.view.EventTreeNode;
import de.regasus.event.view.ProgrammePointListTreeNode;
import de.regasus.event.view.ProgrammePointTreeNode;
import de.regasus.programme.programmepoint.editor.ProgrammePointEditor;
import de.regasus.programme.programmepoint.editor.ProgrammePointEditorInput;
import de.regasus.ui.Activator;

public class CreateProgrammePointHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		// determine selected Event
		IStructuredSelection currentSelection = (IStructuredSelection) HandlerUtil.getCurrentSelection(event);
		Object object = currentSelection.getFirstElement();

		Long eventPK = null;

		if (object instanceof EventTreeNode) {
			eventPK = ((EventTreeNode) object).getEventId();
		}
		else if (object instanceof ProgrammePointListTreeNode) {
			eventPK = ((ProgrammePointListTreeNode) object).getEventId();
		}
		else if (object instanceof ProgrammePointTreeNode) {
			eventPK = ((ProgrammePointTreeNode) object).getEventId();
		}


		if (eventPK != null) {
			// Open editor for new ProgrammePointVO
			ProgrammePointEditorInput input = ProgrammePointEditorInput.getCreateInstance(eventPK);
			try {
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().openEditor(
					input,
					ProgrammePointEditor.ID
				);
			}
			catch (PartInitException e) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			}
		}

		return null;
	}

}
