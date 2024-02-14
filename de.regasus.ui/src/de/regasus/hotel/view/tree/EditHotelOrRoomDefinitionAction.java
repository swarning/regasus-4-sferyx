package de.regasus.hotel.view.tree;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import com.lambdalogic.messeinfo.hotel.Hotel;
import com.lambdalogic.messeinfo.hotel.data.RoomDefinitionVO;
import com.lambdalogic.util.rcp.SelectionHelper;
import de.regasus.core.error.RegasusErrorHandler;
import com.lambdalogic.util.rcp.tree.TreeNode;

import de.regasus.core.ui.Activator;
import com.lambdalogic.util.rcp.UtilI18N;
import de.regasus.core.ui.IImageKeys;
import de.regasus.core.ui.action.AbstractAction;
import de.regasus.hotel.editor.HotelEditor;
import de.regasus.hotel.editor.HotelEditorInput;
import de.regasus.hotel.roomdefinition.editor.RoomDefinitionEditor;
import de.regasus.hotel.roomdefinition.editor.RoomDefinitionEditorInput;

public class EditHotelOrRoomDefinitionAction extends AbstractAction implements ISelectionListener {

	private final IWorkbenchWindow window;

	@SuppressWarnings("rawtypes")
	private TreeNode node;

	private Hotel hotel;
	private RoomDefinitionVO roomDefinitionVO;


	public EditHotelOrRoomDefinitionAction(IWorkbenchWindow window) {
		/* disableWhenLoggedOut is not needed, because this Action enabled/disables depending on the
		 * current selection. When the user is not logged in, no data can be selected. So the Action
		 * is disabled then.
		 */
		super(false /*disableWhenLoggedOut*/);

		this.window = window;
		setId(getClass().getName());
		setText(UtilI18N.Edit);
		setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(
			Activator.PLUGIN_ID,
			IImageKeys.EDIT
		));

		window.getSelectionService().addSelectionListener(this);
	}


	@Override
	public void dispose() {
		window.getSelectionService().removeSelectionListener(this);
	}


	@Override
	public void run() {
		try {
			IWorkbenchPage page = window.getActivePage();
			if (node instanceof HotelTreeNode) {
				hotel = (Hotel) node.getValue();
				HotelEditorInput editorInput = new HotelEditorInput(hotel.getID());
				page.openEditor(editorInput, HotelEditor.ID);
			}
			else if (node instanceof RoomDefinitionTreeNode) {
				roomDefinitionVO = (RoomDefinitionVO) node.getValue();
				RoomDefinitionEditorInput editorInput = new RoomDefinitionEditorInput(roomDefinitionVO);
				page.openEditor(editorInput, RoomDefinitionEditor.ID);
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		if (part instanceof HotelTreeView) {
			node = SelectionHelper.getUniqueSelected(selection);
			setEnabled(node != null);
		}
	}

}
