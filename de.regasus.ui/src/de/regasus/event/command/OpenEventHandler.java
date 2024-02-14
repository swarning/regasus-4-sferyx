package de.regasus.event.command;

import java.util.Iterator;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;
import com.lambdalogic.messeinfo.participant.data.EventVO;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.event.EventModel;
import de.regasus.event.view.EventTreeNode;
import de.regasus.ui.Activator;

/**
 * This action deletes the currently selected event(s).
 */
public class OpenEventHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		// Iterate through whatever is currently selected
		final IStructuredSelection currentSelection = (IStructuredSelection) HandlerUtil.getCurrentSelection(event);

		if (!currentSelection.isEmpty()) {

			Iterator<?> iterator = currentSelection.iterator();
			while (iterator.hasNext()) {
				Object object = iterator.next();
				// If you can find out what Event to close, do it.
				if (object instanceof EventTreeNode) {
					EventTreeNode node = (EventTreeNode) object;
					EventVO eventVO = node.getValue();

					try {
						EventModel.getInstance().openEvent(eventVO);
					}
					catch (Exception e) {
						RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
					}

				}
			}
		}

		return null;
	}

}
