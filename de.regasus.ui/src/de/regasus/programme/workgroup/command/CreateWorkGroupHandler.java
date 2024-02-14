package de.regasus.programme.workgroup.command;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.event.view.ProgrammePointTreeNode;
import de.regasus.event.view.WorkGroupTreeNode;
import de.regasus.programme.workgroup.editor.WorkGroupEditor;
import de.regasus.programme.workgroup.editor.WorkGroupEditorInput;
import de.regasus.ui.Activator;

public class CreateWorkGroupHandler extends AbstractHandler {

	public Object execute(ExecutionEvent event) throws ExecutionException {
		IStructuredSelection currentSelection = (IStructuredSelection) HandlerUtil.getCurrentSelection(event);
		Object object = currentSelection.getFirstElement();

		Long programmePointPK = null;

		if (object instanceof ProgrammePointTreeNode) {
			programmePointPK = ((ProgrammePointTreeNode) object).getProgrammePointPK();
		}
		else if (object instanceof WorkGroupTreeNode) {
			programmePointPK = ((WorkGroupTreeNode) object).getValue().getProgrammePointPK();
		}
			
		if (programmePointPK != null) {
		
			WorkGroupEditorInput input = WorkGroupEditorInput.getCreateInstance(programmePointPK);
			try {
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().openEditor(
					input,
					WorkGroupEditor.ID
				);
			}
			catch (PartInitException e) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			}
		}
		
		return null;
	}

}
