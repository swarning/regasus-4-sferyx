package de.regasus.hotel.offering.command;

import java.util.Collection;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

import com.lambdalogic.messeinfo.hotel.data.HotelContingentCVO;
import com.lambdalogic.util.CollectionsHelper;
import com.lambdalogic.util.rcp.UtilI18N;

import de.regasus.I18N;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.event.view.EventHotelInfoTreeNode;
import de.regasus.event.view.HotelContingentTreeNode;
import de.regasus.event.view.HotelOfferingTreeNode;
import de.regasus.hotel.HotelContingentModel;
import de.regasus.hotel.offering.editor.HotelOfferingEditor;
import de.regasus.hotel.offering.editor.HotelOfferingEditorInput;
import de.regasus.ui.Activator;

public class CreateHotelOfferingHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IStructuredSelection currentSelection = (IStructuredSelection) HandlerUtil.getCurrentSelection(event);
		Object object = currentSelection.getFirstElement();

		// Find out Event, Hotel and Contingent

		HotelContingentTreeNode hotelContingentTreeNode = null;
		if (object instanceof HotelContingentTreeNode) {
			hotelContingentTreeNode = (HotelContingentTreeNode) object;
		}
		else if (object instanceof HotelOfferingTreeNode) {
			HotelOfferingTreeNode hotelOfferingTreeNode = (HotelOfferingTreeNode) object;
			hotelContingentTreeNode = (HotelContingentTreeNode) hotelOfferingTreeNode.getParent();
		}

		if (hotelContingentTreeNode != null) {
			EventHotelInfoTreeNode eventHotelTreeNode = (EventHotelInfoTreeNode) hotelContingentTreeNode.getParent();
			Long hotelPK = eventHotelTreeNode.getValue().getID();
			Long eventPK = eventHotelTreeNode.getEventId();
			Long hotelContingentPK = hotelContingentTreeNode.getValue().getPK();

			if (hotelPK != null && eventPK != null && hotelContingentPK != null) {
				// Check whether hotel contingent has room definitions
				try {
					HotelContingentCVO hotelContingentCVO = HotelContingentModel.getInstance().getHotelContingentCVO(
						hotelContingentPK
					);
					Collection<Long> roomDefinitionPKs = hotelContingentCVO.getRoomDefinitionPKs();
					if (CollectionsHelper.empty(roomDefinitionPKs)) {
						MessageDialog.openInformation(
							HandlerUtil.getActiveShell(event),
							UtilI18N.Info,
							I18N.ContingentHasNoRoomDefinitions);
						return null;
					}
				}
				catch (Exception e1) {
					com.lambdalogic.util.rcp.error.ErrorHandler.logError(e1);
				}

				// Open editor for new HotelContingent
				HotelOfferingEditorInput input = HotelOfferingEditorInput.getCreateInstance(hotelContingentPK);
				try {
					PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().openEditor(
						input,
						HotelOfferingEditor.ID);
				}
				catch (PartInitException e) {
					RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
				}
			}
		}
		return null;
	}
}
