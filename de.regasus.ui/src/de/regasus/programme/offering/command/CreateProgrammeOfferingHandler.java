package de.regasus.programme.offering.command;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.event.view.ProgrammeOfferingTreeNode;
import de.regasus.event.view.ProgrammePointTreeNode;
import de.regasus.programme.offering.editor.ProgrammeOfferingEditor;
import de.regasus.programme.offering.editor.ProgrammeOfferingEditorInput;
import de.regasus.ui.Activator;

public class CreateProgrammeOfferingHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IStructuredSelection currentSelection = (IStructuredSelection) HandlerUtil.getCurrentSelection(event);
		Object object = currentSelection.getFirstElement();

		// Find out EventPK
		Long programmePointPK = null;
		Long eventPK = null;

		if (object instanceof ProgrammePointTreeNode) {
			ProgrammePointTreeNode programmePointTreeNode = (ProgrammePointTreeNode) object;
			programmePointPK = programmePointTreeNode.getProgrammePointPK();
			eventPK = programmePointTreeNode.getEventId();
		}
		else if (object instanceof ProgrammeOfferingTreeNode) {
			ProgrammeOfferingTreeNode programmeOfferingTreeNode = (ProgrammeOfferingTreeNode) object;
			programmePointPK = programmeOfferingTreeNode.getProgrammePointPK();
			eventPK = programmeOfferingTreeNode.getEventId();
		}


		if (programmePointPK != null && eventPK != null) {
			// Open editor for new ProgrammeOfferingVO
			ProgrammeOfferingEditorInput input = ProgrammeOfferingEditorInput.getCreateInstance(programmePointPK, eventPK);
			try {
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().openEditor(
					input,
					ProgrammeOfferingEditor.ID
				);
			}
			catch (PartInitException e) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			}
		}

		return null;
	}

}
