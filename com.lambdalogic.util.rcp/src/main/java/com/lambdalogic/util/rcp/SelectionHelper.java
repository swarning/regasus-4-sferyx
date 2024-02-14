package com.lambdalogic.util.rcp;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;

/**
 * @author manfred
 * 
 */
public class SelectionHelper {

	/**
	 * Returns the objects in this selection as list, parametrized with the given class is returned; so you can use this
	 * method as follows:
	 * 
	 * <pre>
	 * List&lt;Profile&gt; list = SelectionUtil.toList(selection, Profile.class);
	 * </pre>
	 * 
	 * @throws RuntimeException
	 *             when one of the elements of the selection is not an instance of the given class
	 */
	public static <T> List<T> toList(ISelection selection, Class<? extends T> clazz) throws RuntimeException {
		ArrayList<T> list = new ArrayList<T>();
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection sselection = (IStructuredSelection) selection;
			Iterator<?> iterator = sselection.iterator();
			while (iterator.hasNext()) {
				Object o = iterator.next();
				if (clazz.isInstance(o)) {
					list.add(clazz.cast(o));
				}
				else {
					throw new RuntimeException("Not an instance of " + clazz.getName() + ": " + o.getClass().getName());
				}
			}
		}
		return list;
	}


	@SuppressWarnings("unchecked")
	public static <T> List<T> toList(ISelection selection) throws RuntimeException {
		ArrayList<T> list = new ArrayList<T>();
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection sselection = (IStructuredSelection) selection;
			Iterator iterator = sselection.iterator();
			while (iterator.hasNext()) {
				Object o = iterator.next();
				list.add((T) o);
			}
		}
		return list;
	}


	/**
	 * Returns true when all elements of the selection are instances of the given class (and there is at least one of
	 * such elements).
	 */
	public static boolean isNonemptySelectionOf(ISelection selection, Class<?> clazz) {
		if (selection.isEmpty()) {
			return false;
		}
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection sselection = (IStructuredSelection) selection;
			Iterator<?> iterator = sselection.iterator();
			while (iterator.hasNext()) {
				Object o = iterator.next();
				if (!clazz.isInstance(o)) {
					return false;
				}
			}
		}
		return true;

	}

	/**
	 * Returns true when all elements of the selection are instances of the given class (and there is at least one of
	 * such elements).
	 */
	public static boolean isSingleSelectionOf(ISelection selection, Class<?> clazz) {
		if (selection.isEmpty()) {
			return false;
		}
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection sselection = (IStructuredSelection) selection;
			Object[] objects = sselection.toArray();
			return (objects.length == 1 && clazz.isInstance(objects[0]));
		}
		return false;

	}
	/**
	 * Returns the objects selected in this viewer as list, parametrized with the given class is returned; so you can
	 * use this method as follows:
	 * 
	 * <pre>
	 * List&lt;Profile&gt; list = SelectionUtil.toList(viewer, Profile.class);
	 * </pre>
	 * 
	 * @throws RuntimeException
	 *             when one of the selected elements is not an instance of the given class
	 */
	public static <T> List<T> getSelection(StructuredViewer viewer, Class<? extends T> clazz) {
		return toList(viewer.getSelection(), clazz);

	}


	/**
	 * Gets the first (and only) element of the given selection, or null if none.
	 * 
	 * @return the first and only selected object in the given (structured) selection, or <code>null</code>.
	 * @throws RuntimeException
	 *             if more than one object is selected
	 */
	public static <T> T getUniqueSelected(ISelection selection) {
		if (selection.isEmpty()) {
			return null;
		}
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection sselection = (IStructuredSelection) selection;
			if (sselection.size() > 1) {
				throw new RuntimeException("More than one object selected");
			}
			return (T) sselection.getFirstElement();
		}
		return null;
	}
	
	
	/**
	 * Gets the first (and only) element of the given selection, or null if none.
	 * 
	 * @return the first and only selected object in the given (structured) selection, or <code>null</code>.
	 * @throws RuntimeException
	 *             if more than one object is selected
	 */
	public static <T> T getUniqueSelected(StructuredViewer structuredViewer) {
		return getUniqueSelected(structuredViewer.getSelection());
	}
	
}
