package com.lambdalogic.util.rcp.tree;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

public class TreeNodeContentProvider implements ITreeContentProvider {

	@Override
	public Object[] getChildren(Object parentElement) {
		Object[] children = null;
		if (parentElement != null && parentElement instanceof TreeNode) {
			TreeNode parentTreeNode = (TreeNode) parentElement;
			children = parentTreeNode.getChildrenAsArray();
		}
		
		return children;
	}
	

	@Override
	public Object getParent(Object element) {
		Object parent = null;
		if (element instanceof TreeNode) {
			TreeNode treeNode = (TreeNode) element;
			parent = treeNode.getParent();
		}
		return parent;
	}

	
	@Override
	public boolean hasChildren(Object element) {
		boolean hasChildren = false;
		if (element instanceof TreeNode) {
			TreeNode treeNode = (TreeNode) element;
			hasChildren = treeNode.hasChildrenMaybe();
		}
		return hasChildren;
	}

	
	@Override
	public Object[] getElements(Object inputElement) {
		return getChildren(inputElement);
	}

	
	@Override
	public void dispose() {
	}

	
	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}

}
