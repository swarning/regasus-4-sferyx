package de.regasus.portal.command;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.handlers.HandlerUtil;

import com.lambdalogic.util.rcp.SelectionHelper;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.Activator;
import de.regasus.event.view.PortalTreeNode;
import de.regasus.portal.Portal;
import de.regasus.portal.portal.editor.PortalEditor;
import de.regasus.portal.portal.editor.PortalEditorInput;

public class EditPortalHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		final Portal portal = getSelectedPortal(event);
		IWorkbenchPage workbenchPage = HandlerUtil.getActiveWorkbenchWindow(event).getActivePage();

		if (portal != null) {
			openPortalEditor(workbenchPage, portal.getId());
		}

		return null;
	}


	private Portal getSelectedPortal(ExecutionEvent event) {
		Portal portal = null;

		ISelection currentSelection = HandlerUtil.getCurrentSelection(event);
		Object selectedObject = SelectionHelper.getUniqueSelected(currentSelection);
		if (selectedObject instanceof Portal) {
			portal = (Portal) selectedObject;
		}
		else if (selectedObject instanceof PortalTreeNode) {
			PortalTreeNode node = (PortalTreeNode) selectedObject;
			portal = node.getValue();
		}

		return portal;
	}


	public static void openPortalEditor(IWorkbenchPage page, Long portalId) {
		PortalEditorInput editorInput = PortalEditorInput.getEditInstance(portalId);
		try {
			page.openEditor(editorInput, PortalEditor.ID);
		}
		catch (PartInitException e) {
			RegasusErrorHandler.handleApplicationError(Activator.PLUGIN_ID, EditPortalHandler.class.getName(), e);
		}
	}

}
