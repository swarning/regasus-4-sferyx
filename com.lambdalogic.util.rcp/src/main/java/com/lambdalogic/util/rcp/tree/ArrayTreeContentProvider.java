package com.lambdalogic.util.rcp.tree;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;

/**
 * A content provider for a flat array or list of elements which would normally be shown in a List or Table, but is for
 * once to be shown within a Tree (typically to allow visual feedback during drag and drop operations).
 * 
 * @author manfred
 */
public class ArrayTreeContentProvider extends ArrayContentProvider implements ITreeContentProvider {

	private static final Object[] NO_ELEMENTS = new Object[0];


	public Object[] getChildren(Object parentElement) {
		return NO_ELEMENTS;
	}


	public Object getParent(Object element) {
		return null;
	}


	public boolean hasChildren(Object element) {
		return false;
	}
}
