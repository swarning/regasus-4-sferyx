package de.regasus.core.ui;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;

public class NullSelectionProvider implements ISelectionProvider {

	@Override
	public void addSelectionChangedListener(ISelectionChangedListener listener) {
	}


	@Override
	public ISelection getSelection() {
		return null;
	}


	@Override
	public void removeSelectionChangedListener(ISelectionChangedListener listener) {
	}


	@Override
	public void setSelection(ISelection selection) {
	}

}
