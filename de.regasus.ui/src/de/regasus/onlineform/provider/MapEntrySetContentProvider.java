package de.regasus.onlineform.provider;

import java.util.Map;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

public class MapEntrySetContentProvider implements IStructuredContentProvider {

	public void dispose() {
	}


	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}


	public Object[] getElements(Object inputElement) {
		Map<?,?> map = (Map<?,?>) inputElement;
		return map.entrySet().toArray();
	}

}
