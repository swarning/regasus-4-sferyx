package de.regasus.common.config.command;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

import com.lambdalogic.messeinfo.config.ConfigScope;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.editor.config.ConfigEditor;
import de.regasus.core.ui.editor.config.ConfigEditorInput;
import de.regasus.event.view.EventTreeNode;
import de.regasus.ui.Activator;

/**
 * This command handler finds out what event is currently in focus (based on the selection),
 * and opens the corresponding ConfigEditor.
 */
public class EditEventCustomerConfigHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IStructuredSelection currentSelection = (IStructuredSelection) HandlerUtil.getCurrentSelection(event);
		Object object = currentSelection.getFirstElement();

		// Find out EventPK
		Long eventPK = null;

		if (object instanceof EventTreeNode) {
			eventPK = ((EventTreeNode) object).getEventId();
		}

		if (eventPK != null) {
			// Open the ConfigEditor for the corresponding Config.
			ConfigEditorInput input = ConfigEditorInput.getInstance(ConfigScope.EVENT_CUSTOMER, eventPK.toString());
			try {
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().openEditor(
					input,
					ConfigEditor.ID
				);
			}
			catch (PartInitException e) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			}
		}

		return null;
	}

}
