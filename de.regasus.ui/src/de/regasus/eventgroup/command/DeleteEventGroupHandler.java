package de.regasus.eventgroup.command;

import java.util.Iterator;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;

import com.lambdalogic.util.rcp.UtilI18N;

import de.regasus.I18N;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.model.Activator;
import de.regasus.event.EventGroup;
import de.regasus.event.EventGroupModel;
import de.regasus.event.view.EventGroupTreeNode;

/**
 * Handler to delete the currently selected Event Group.
 */
public class DeleteEventGroupHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		// Iterate through whatever is currently selected
		final IStructuredSelection currentSelection = (IStructuredSelection) HandlerUtil.getCurrentSelection(event);

		// identify the selected Event Group
		EventGroup eventGroup = null;
		if (!currentSelection.isEmpty()) {
    		Iterator<?> iterator = currentSelection.iterator();
    		while (iterator.hasNext()) {
    			Object object = iterator.next();

    			if (object instanceof EventGroupTreeNode) {
    				EventGroupTreeNode node = (EventGroupTreeNode) object;
    				eventGroup = node.getValue();
    			}
    		}
		}


		if (eventGroup != null) {
			// open confirmation dialog
			String name = eventGroup.getName().getString();
			String message = I18N.DeleteEventGroupConfirmation.replace("<name>", name);

			boolean deleteOK = MessageDialog.openQuestion(
				HandlerUtil.getActiveShell(event),
				UtilI18N.Question,
				message
			);


			if (deleteOK) {
				// delete EventGroup
				try {
					EventGroupModel.getInstance().delete(eventGroup);
				}
				catch (Exception e) {
					RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
					return Status.CANCEL_STATUS;
				}
			}
		}

		return null;
	}
}
