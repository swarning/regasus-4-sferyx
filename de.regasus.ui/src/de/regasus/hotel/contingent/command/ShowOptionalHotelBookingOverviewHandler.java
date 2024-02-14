package de.regasus.hotel.contingent.command;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.event.view.EventHotelInfoListTreeNode;
import de.regasus.event.view.EventHotelInfoTreeNode;
import de.regasus.event.view.EventTreeNode;
import de.regasus.hotel.contingent.editor.OptionalHotelBookingOverviewEditor;
import de.regasus.hotel.contingent.editor.OptionalHotelBookingOverviewEditorInput;
import de.regasus.ui.Activator;


public class ShowOptionalHotelBookingOverviewHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IStructuredSelection currentSelection = (IStructuredSelection) HandlerUtil.getCurrentSelection(event);
		Object object = currentSelection.getFirstElement();

		// determine eventPK and hotelPK
		Long eventPK = null;
		Long hotelPK = null;

		if (object instanceof EventTreeNode) {
			eventPK = ((EventTreeNode) object).getEventId();
		}
		else if (object instanceof EventHotelInfoListTreeNode) {
			eventPK = ((EventHotelInfoListTreeNode) object).getEventId();
		}
		else if (object instanceof EventHotelInfoTreeNode) {
			eventPK = ((EventHotelInfoTreeNode) object).getEventId();
			hotelPK = ((EventHotelInfoTreeNode) object).getHotelPK();
		}

		if (eventPK != null) {
			// Open editor for new ProgrammePointVO
			OptionalHotelBookingOverviewEditorInput input = new OptionalHotelBookingOverviewEditorInput(eventPK, hotelPK);
			try {
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().openEditor(
					input,
					OptionalHotelBookingOverviewEditor.ID
				);
			}
			catch (PartInitException e) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			}
		}

		return null;
	}

}
