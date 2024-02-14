package de.regasus.eventgroup.command;

import java.util.Collection;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

import com.lambdalogic.messeinfo.participant.data.EventVO;

import de.regasus.I18N;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.event.EventGroup;
import de.regasus.event.EventModel;
import de.regasus.event.dialog.EventSelectionDialog;
import de.regasus.event.view.EventGroupTreeNode;
import de.regasus.ui.Activator;

public class AssignEventGroupHandler extends AbstractHandler {

	private EventGroup eventGroup;


	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			determineSelectedEventGroup(event);

			// hide Events that are already assigned to the EventGroup
			Collection<EventVO> hideEvents = EventModel.getInstance().getEventVOsByGroup( eventGroup.getId() );
			List<Long> hideEventPKs = EventVO.getPKs(hideEvents);

			Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
			EventSelectionDialog dialog = new EventSelectionDialog(
				shell,
				hideEventPKs,
				null,	// initSelectedEventPKs
				true	// multiSelection
			);

			String groupName = eventGroup.getName().getString();
			dialog.setTitle( I18N.AssignEventGroup_Title.replace("<name>", groupName) 	);
			dialog.setMessage( I18N.AssignEventGroup_Message.replace("<name>", groupName) );


			int result = dialog.open();
			if (result == 0) {
				List<EventVO> eventList = dialog.getSelectedEvents();
				for (EventVO eventVO : eventList) {
					/* Clone entity before changing it, because we must not change its current version in the model.
					 * Otherwise the model cannot determine the "old" foreign key and won't inform the
					 * foreign-key-listeners.
					 */
					eventVO = eventVO.clone();
					eventVO.setEventGroupPK( eventGroup.getId() );

					EventModel.getInstance().update(eventVO);
				}
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}

		return null;
	}


	private void determineSelectedEventGroup(ExecutionEvent event) {
		// determine selected Event
		IStructuredSelection currentSelection = (IStructuredSelection) HandlerUtil.getCurrentSelection(event);
		Object object = currentSelection.getFirstElement();

		if (object instanceof EventGroupTreeNode) {
			eventGroup = ((EventGroupTreeNode) object).getValue();
		}
	}

}
