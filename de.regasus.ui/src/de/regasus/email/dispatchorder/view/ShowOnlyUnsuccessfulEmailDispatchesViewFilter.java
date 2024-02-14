package de.regasus.email.dispatchorder.view;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

import com.lambdalogic.messeinfo.email.DispatchStatus;
import com.lambdalogic.messeinfo.email.EmailDispatch;

public class ShowOnlyUnsuccessfulEmailDispatchesViewFilter extends ViewerFilter {

	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		if (element instanceof EmailDispatch) {
			EmailDispatch ed = (EmailDispatch)element;
			if (ed.getStatus() == DispatchStatus.SUCCESS) {
				return false;
			}
		} 
		return true;
	}

}
