package de.regasus.portal.pagelayout.command;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.event.view.PageLayoutListTreeNode;
import de.regasus.event.view.PortalTreeNode;
import de.regasus.portal.pagelayout.editor.PageLayoutEditor;
import de.regasus.portal.pagelayout.editor.PageLayoutEditorInput;
import de.regasus.ui.Activator;

public class CreatePageLayoutHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		Long portalPK = getSelectedPortalId(event);
		if (portalPK != null) {
			// Open editor for new PageLayout
			PageLayoutEditorInput input = PageLayoutEditorInput.getCreateInstance(portalPK);
			try {
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().openEditor(
					input,
					PageLayoutEditor.ID
				);
			}
			catch (PartInitException e) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			}
		}

		return null;
	}


	private Long getSelectedPortalId(ExecutionEvent event) {
		IStructuredSelection currentSelection = (IStructuredSelection) HandlerUtil.getCurrentSelection(event);
		Object object = currentSelection.getFirstElement();

		Long portalId = null;

		if (object instanceof PortalTreeNode) {
			portalId = ((PortalTreeNode) object).getPortalId();
		}
		else if (object instanceof PageLayoutListTreeNode) {
			portalId = ((PageLayoutListTreeNode) object).getPortalPK();
		}

		return portalId;
	}

}
