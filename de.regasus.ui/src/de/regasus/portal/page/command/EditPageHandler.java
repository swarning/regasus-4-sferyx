package de.regasus.portal.page.command;

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
import de.regasus.event.view.PageTreeNode;
import de.regasus.portal.Page;
import de.regasus.portal.page.editor.PageEditor;
import de.regasus.portal.page.editor.PageEditorInput;

public class EditPageHandler  extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		final Page page = getSelectedPage(event);
		IWorkbenchPage workbenchPage = HandlerUtil.getActiveWorkbenchWindow(event).getActivePage();

		if (page != null) {
			openPageEditor(workbenchPage, page.getId());
		}

		return null;
	}


	private Page getSelectedPage(ExecutionEvent event) {
		Page page = null;

		ISelection currentSelection = HandlerUtil.getCurrentSelection(event);
		Object selectedObject = SelectionHelper.getUniqueSelected(currentSelection);
		if (selectedObject instanceof Page) {
			page = (Page) selectedObject;
		}
		else if (selectedObject instanceof PageTreeNode) {
			PageTreeNode node = (PageTreeNode) selectedObject;
			page = node.getValue();
		}

		return page;
	}


	public static void openPageEditor(IWorkbenchPage page, Long pageId) {
		PageEditorInput editorInput = PageEditorInput.getEditInstance(pageId);
		try {
			page.openEditor(editorInput, PageEditor.ID);
		}
		catch (PartInitException e) {
			RegasusErrorHandler.handleApplicationError(Activator.PLUGIN_ID, EditPageHandler.class.getName(), e);
		}
	}

}
