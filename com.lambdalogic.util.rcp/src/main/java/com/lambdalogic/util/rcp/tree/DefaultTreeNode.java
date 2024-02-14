package com.lambdalogic.util.rcp.tree;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.graphics.Image;

public class DefaultTreeNode<T> extends TreeNode<T> {

	public DefaultTreeNode(TreeViewer treeViewer, TreeNode parent, T value, boolean addToParent) {
		super(treeViewer, parent, value, addToParent);
	}


	public DefaultTreeNode(TreeViewer treeViewer, TreeNode parent, T value) {
		super(treeViewer, parent, value);
	}


	public DefaultTreeNode(TreeViewer treeViewer, TreeNode parent) {
		super(treeViewer, parent);
	}


	public DefaultTreeNode() {
	}


	@Override
	public Class getEntityType() {
		return null;
	}

	@Override
	public Image getImage() {
		return null;
	}

	@Override
	public Object getKey() {
		return null;
	}

	@Override
	public String getText() {
		return null;
	}

	@Override
	public String getToolTipText() {
		return null;
	}

	@Override
	protected void loadChildren() {
	}

	@Override
	public void refresh() {
	}

	@Override
	public void refreshChildren() {
	}

}
