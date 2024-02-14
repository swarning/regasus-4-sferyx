package de.regasus.hotel.view.tree;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import de.regasus.I18N;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.action.AbstractAction;
import de.regasus.hotel.roomdefinition.editor.RoomDefinitionEditor;
import de.regasus.hotel.roomdefinition.editor.RoomDefinitionEditorInput;
import de.regasus.ui.Activator;

import com.lambdalogic.util.rcp.SelectionHelper;

public class CreateRoomDefinitionAction extends AbstractAction  implements ISelectionListener {

	private final IWorkbenchWindow window;

	private Long hotelPK;

	public CreateRoomDefinitionAction(IWorkbenchWindow window) {
		/* disableWhenLoggedOut is not needed, because this Action enabled/disables depending on the
		 * current selection. When the user is not logged in, no data can be selected. So the Action
		 * is disabled then.
		 */
		super(false /*disableWhenLoggedOut*/);

		this.window = window;
		setId(getClass().getName());
		setText(I18N.CreateRoomDefinition);
		setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(
			Activator.PLUGIN_ID,
			"icons/create_room_definition.png"
		));
		window.getSelectionService().addSelectionListener(this);
	}


	@Override
	public void dispose() {
		window.getSelectionService().removeSelectionListener(this);
	}


	@Override
	public void run() {
		IWorkbenchPage page = window.getActivePage();

		RoomDefinitionEditorInput editorInput = new RoomDefinitionEditorInput(hotelPK);

		try {
			page.openEditor(editorInput, RoomDefinitionEditor.ID);
		}
		catch (PartInitException e) {
			RegasusErrorHandler.handleApplicationError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		hotelPK = null;
		if (SelectionHelper.isSingleSelectionOf(selection, HotelTreeNode.class)) {
			HotelTreeNode hotelTreeNode = SelectionHelper.getUniqueSelected(selection);
			hotelPK = hotelTreeNode.getValue().getID();
		}
		else if (SelectionHelper.isSingleSelectionOf(selection, RoomDefinitionTreeNode.class)) {
			RoomDefinitionTreeNode roomDefinitionTreeNode = SelectionHelper.getUniqueSelected(selection);
			hotelPK = roomDefinitionTreeNode.getValue().getHotelPK();
		}
		setEnabled(hotelPK != null);
	}

}
