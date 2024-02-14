package de.regasus.event.command;

import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.State;
import org.eclipse.jface.menus.IMenuStateIds;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.menus.UIElement;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.event.EventModel;
import de.regasus.ui.Activator;

/**
 * Command to make closed Events visible or not.
 * The state is managed in the {@link EventModel}.
 * The context menu of this command visualizes the state with a checkmark.
 */
public class ShowClosedEventsHandler extends AbstractHandler implements IElementUpdater {

	@Override
	public final Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			// toggle state of showClosedEvents in EventModel
			boolean showClosedEvents = EventModel.getInstance().toggleShowClosedEvents();


			// update toggled state in Command
			State state = event.getCommand().getState(IMenuStateIds.STYLE);
			if (state != null) {
				state.setValue(showClosedEvents);
			}
			ICommandService commandService = (ICommandService) PlatformUI.getWorkbench().getService(ICommandService.class);
			commandService.refreshElements(event.getCommand().getId(), null);
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}

		// return value is reserved for future apis
		return null;
	}


	/**
	 * Update command element with toggle state.
	 */
	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void updateElement(UIElement element, Map parameters) {
		try {
			boolean showClosedEvents = EventModel.getInstance().isShowClosedEvents();
			element.setChecked(showClosedEvents);
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}

}
