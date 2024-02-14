package com.lambdalogic.util.rcp.tree;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.handlers.HandlerUtil;

import com.lambdalogic.util.rcp.SelectionHelper;

public class RefreshTreeNodeHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		ISelection currentSelection = HandlerUtil.getCurrentSelection(event);
		Object selectedObject = SelectionHelper.getUniqueSelected(currentSelection);
		if (selectedObject instanceof TreeNode) {
			TreeNode treeNode = (TreeNode) selectedObject;
			treeNode.refresh();
		}

		return null;
	}

}
