package de.regasus.event.command;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.event.editor.EventEditor;
import de.regasus.event.editor.EventEditorInput;
import de.regasus.event.view.EventGroupTreeNode;
import de.regasus.event.view.EventTreeNode;
import de.regasus.ui.Activator;

public class CreateEventHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		// determine selected Event
		IStructuredSelection currentSelection = (IStructuredSelection) HandlerUtil.getCurrentSelection(event);
		Object object = currentSelection.getFirstElement();
		Long eventGroupId = null;
		if (object instanceof EventGroupTreeNode) {
			eventGroupId = ((EventGroupTreeNode) object).getEventGroupId();
		}
		else if (object instanceof EventTreeNode) {
			eventGroupId = ((EventTreeNode) object).getEventGroupId();
		}


		if (eventGroupId != null) {
    		// Open editor for new Event
    		EventEditorInput input = EventEditorInput.getCreateInstance(eventGroupId);
    		try {
    			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().openEditor(
    				input,
    				EventEditor.ID
    			);
    		}
    		catch (PartInitException e) {
    			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
    		}
		}

		return null;
	}

}
