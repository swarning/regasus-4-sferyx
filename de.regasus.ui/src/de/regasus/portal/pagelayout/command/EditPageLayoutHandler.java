package de.regasus.portal.pagelayout.command;

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
import de.regasus.event.view.PageLayoutTreeNode;
import de.regasus.portal.PageLayout;
import de.regasus.portal.pagelayout.editor.PageLayoutEditor;
import de.regasus.portal.pagelayout.editor.PageLayoutEditorInput;

public class EditPageLayoutHandler  extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		final PageLayout pageLayout = getSelectedPageLayout(event);
		IWorkbenchPage workbenchPage = HandlerUtil.getActiveWorkbenchWindow(event).getActivePage();

		if (pageLayout != null) {
			openPageLayoutEditor(workbenchPage, pageLayout.getId());
		}

		return null;
	}


	private PageLayout getSelectedPageLayout(ExecutionEvent event) {
		PageLayout pageLayout = null;

		ISelection currentSelection = HandlerUtil.getCurrentSelection(event);
		Object selectedObject = SelectionHelper.getUniqueSelected(currentSelection);
		if (selectedObject instanceof PageLayout) {
			pageLayout = (PageLayout) selectedObject;
		}
		else if (selectedObject instanceof PageLayoutTreeNode) {
			PageLayoutTreeNode node = (PageLayoutTreeNode) selectedObject;
			pageLayout = node.getValue();
		}

		return pageLayout;
	}


	public static void openPageLayoutEditor(IWorkbenchPage page, Long pageLayoutId) {
		PageLayoutEditorInput editorInput = PageLayoutEditorInput.getEditInstance(pageLayoutId);
		try {
			page.openEditor(editorInput, PageLayoutEditor.ID);
		}
		catch (PartInitException e) {
			RegasusErrorHandler.handleApplicationError(Activator.PLUGIN_ID, EditPageLayoutHandler.class.getName(), e);
		}
	}

}
