package de.regasus.hotel.view.tree;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import com.lambdalogic.messeinfo.hotel.data.RoomDefinitionVO;
import com.lambdalogic.util.CollectionsHelper;
import com.lambdalogic.util.rcp.BusyCursorHelper;
import com.lambdalogic.util.rcp.SelectionHelper;

import de.regasus.I18N;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.Activator;
import com.lambdalogic.util.rcp.UtilI18N;
import de.regasus.core.ui.IImageKeys;
import de.regasus.core.ui.action.AbstractAction;
import de.regasus.hotel.HotelModel;
import de.regasus.hotel.RoomDefinitionModel;

public class DeleteHotelOrRoomDefinitionAction extends AbstractAction implements ISelectionListener {

	private final IWorkbenchWindow window;

	private List<HotelTreeNode> hotelTreeNodes;

	private List<RoomDefinitionTreeNode> roomDefinitionTreeNodes;


	public DeleteHotelOrRoomDefinitionAction(IWorkbenchWindow window) {
		/* disableWhenLoggedOut is not needed, because this Action enabled/disables depending on the 
		 * current selection. When the user is not logged in, no data can be selected. So the Action
		 * is disabled then.
		 */
		super(false /*disableWhenLoggedOut*/);
		
		this.window = window;
		setId(getClass().getName());
		setText(UtilI18N.Delete);
		setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, IImageKeys.DELETE));

		window.getSelectionService().addSelectionListener(this);
	}


	public void dispose() {
		window.getSelectionService().removeSelectionListener(this);
	}


	public void run() {
		if (CollectionsHelper.notEmpty(hotelTreeNodes)) {
			deleteSelectedHotels();
		}
		else if (CollectionsHelper.notEmpty(roomDefinitionTreeNodes)) {
			deleteSelectedRoomDefinitions();
		}
	}


	private void deleteSelectedHotels() {
		// Auftragsbestätigung
		boolean deleteOK = false;
		if (hotelTreeNodes.size() == 1) {
			final String title = UtilI18N.Question;
			String message = I18N.Delete_Hotel_ConfirmationMessage;
			// Im Abfragetext den Namen des zu löschenden Hotels einfügen
			HotelTreeNode hotelTreeNode = hotelTreeNodes.get(0);
			String name = hotelTreeNode.getValue().getName();
			if (name == null) {
				name = "";
			}
			message = message.replaceFirst("<name>", name); 
			// Dialog öffnen
			deleteOK = MessageDialog.openQuestion(window.getShell(), title, message);
		}
		else {
			final String title = UtilI18N.Question;
			String message = I18N.Delete_Hotels_ConfirmationMessage;
			// Open the Dialog
			deleteOK = MessageDialog.openQuestion(window.getShell(), title, message);
		}

		// If the user answered 'Yes' in the dialog
		if (deleteOK) {
			BusyCursorHelper.busyCursorWhile(new Runnable() {

 				public void run() {
 					List<HotelTreeNode> copyList = new ArrayList<HotelTreeNode>(hotelTreeNodes);
					for (HotelTreeNode hotelTreeNode : copyList) {
						try {
							HotelModel.getInstance().delete(hotelTreeNode.getValue());
						}
						catch (Throwable t) {
							String message = I18N.Delete_Hotel_ErrorMessage;
							final String name = hotelTreeNode.getValue().getName();
							message = message.replaceFirst("<name>", name);
							RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t, message);
							return;
						}
					}
				}
			});
		}
	}


	private void deleteSelectedRoomDefinitions() {
		// Auftragsbestätigung
		boolean deleteOK = false;
		if (roomDefinitionTreeNodes.size() == 1) {
			final String title = UtilI18N.Question;
			String message = I18N.Delete_RoomDefinition_ConfirmationMessage;
			// Im Abfragetext den Namen des zu löschenden Hotels einfügen
			final RoomDefinitionVO roomDefinitionVO = roomDefinitionTreeNodes.get(0).getValue();
			String name = roomDefinitionVO.getName().getString();
			if (name == null) {
				name = "";
			}
			message = message.replaceFirst("<name>", name); 
			// Dialog öffnen
			deleteOK = MessageDialog.openQuestion(window.getShell(), title, message);
		}
		else {
			final String title = UtilI18N.Question;
			String message = I18N.Delete_RoomDefinitions_ConfirmationMessage;
			// Open the Dialog
			deleteOK = MessageDialog.openQuestion(window.getShell(), title, message);
		}

		// If the user answered 'Yes' in the dialog
		if (deleteOK) {
			BusyCursorHelper.busyCursorWhile(new Runnable() {

				public void run() {
					List<RoomDefinitionTreeNode> copyList = new ArrayList<RoomDefinitionTreeNode>(roomDefinitionTreeNodes);
					for (RoomDefinitionTreeNode roomDefinitionTreeNode : copyList) {
						try {
							RoomDefinitionModel.getInstance().delete(roomDefinitionTreeNode.getValue());
						}
						catch (Throwable t) {
							String message = I18N.Delete_RoomDefinition_ErrorMessage;
							final String name = roomDefinitionTreeNode.getValue().getName().getString();
							message = message.replaceFirst("<name>", name);
							RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t, message);
							return;
						}
					}
				}
			});
		}
	}


	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		if (SelectionHelper.isNonemptySelectionOf(selection, HotelTreeNode.class)) {
			hotelTreeNodes = SelectionHelper.toList(selection);
		}
		else {
			hotelTreeNodes = null;
		}

		if (SelectionHelper.isNonemptySelectionOf(selection, RoomDefinitionTreeNode.class)) {
			roomDefinitionTreeNodes = SelectionHelper.toList(selection);
		}
		else {
			roomDefinitionTreeNodes = null;
		}

		setEnabled(CollectionsHelper.notEmpty(hotelTreeNodes) || CollectionsHelper.notEmpty(roomDefinitionTreeNodes));
	}

}
