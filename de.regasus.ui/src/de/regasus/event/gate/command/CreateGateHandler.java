/**
 * CreateGateHandler.java
 * created on 24.09.2013 13:36:59
 */
package de.regasus.event.gate.command;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.event.gate.editor.GateEditor;
import de.regasus.event.gate.editor.GateEditorInput;
import de.regasus.event.view.GateTreeNode;
import de.regasus.event.view.LocationTreeNode;
import de.regasus.ui.Activator;

public class CreateGateHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IStructuredSelection currentSelection = (IStructuredSelection) HandlerUtil.getCurrentSelection(event);
		Object object = currentSelection.getFirstElement();
		
		// find out LocationPK
		Long locationPK = null;
		
		if (object instanceof LocationTreeNode) {
			locationPK = ((LocationTreeNode) object).getLocationPK();
		}
		else if (object instanceof GateTreeNode) {
			locationPK = ((GateTreeNode) object).getLocationPK();
		}
		
		if (locationPK != null) {
			// Open editor for new GateVO
			GateEditorInput input = GateEditorInput.getCreateInstance(locationPK);
			try {
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().openEditor(
					input,
					GateEditor.ID
				);
			}
			catch (PartInitException e) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			}
		}
		return null;
	}

}
