/**
 * CreateLocationHandler.java
 * created on 23.09.2013 13:42:25
 */
package de.regasus.event.location.command;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.event.location.editor.LocationEditor;
import de.regasus.event.location.editor.LocationEditorInput;
import de.regasus.event.view.EventTreeNode;
import de.regasus.event.view.LocationListTreeNode;
import de.regasus.event.view.LocationTreeNode;
import de.regasus.ui.Activator;

public class CreateLocationHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IStructuredSelection currentSelection = (IStructuredSelection) HandlerUtil.getCurrentSelection(event);
		Object object = currentSelection.getFirstElement();

		// Find out EventPK
		Long eventPK = null;

		if (object instanceof EventTreeNode) {
			eventPK = ((EventTreeNode) object).getEventId();
		}
		else if (object instanceof LocationListTreeNode) {
			eventPK = ((LocationListTreeNode) object).getEventId();
		}
		else if (object instanceof LocationTreeNode) {
			eventPK = ((LocationTreeNode) object).getEventId();
		}

		if (eventPK != null) {
			// Open editor for new LocationVO
			LocationEditorInput input = LocationEditorInput.getCreateInstance(eventPK);
			try {
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().openEditor(
					input,
					LocationEditor.ID
				);
			}
			catch (PartInitException e) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			}
		}

		return null;
	}

}
