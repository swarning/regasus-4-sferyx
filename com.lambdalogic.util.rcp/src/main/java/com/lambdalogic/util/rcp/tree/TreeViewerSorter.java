package com.lambdalogic.util.rcp.tree;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;

/**
 * This class can be set as sorter in TreeViewers if the TreeNodes should 
 * be sorted according to their own ordering. 
 */
public class TreeViewerSorter extends ViewerSorter {

	@Override
	public int compare(Viewer viewer, Object o1, Object o2) {
		if (o1 != null && o1 instanceof TreeNode && 
			o2 != null && o2 instanceof TreeNode
		) {
			TreeNode<?> treeNode1 = (TreeNode<?>) o1;
			TreeNode<?> treeNode2 = (TreeNode<?>) o2;

			return treeNode1.compareTo(treeNode2);
		}
		return super.compare(viewer, o1, o2);
	}

}
