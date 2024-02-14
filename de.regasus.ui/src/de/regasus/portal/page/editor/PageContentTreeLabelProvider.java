package de.regasus.portal.page.editor;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;

import de.regasus.portal.Section;
import de.regasus.portal.component.Component;


public class PageContentTreeLabelProvider implements ILabelProvider {

	@Override
	public String getText(Object element) {
		String text = "";

		if (element instanceof Section) {
			Section section = (Section) element;
			text = section.getDisplayLabel();
		}
		else if (element instanceof Component) {
			Component component = (Component) element;
			text = component.getDisplayLabel();
		}

		return text;
	}


	@Override
	public Image getImage(Object element) {
		return null;
	}


	@Override
	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	@Override
	public void addListener(ILabelProviderListener listener) {
	}

	@Override
	public void removeListener(ILabelProviderListener listener) {
	}

	@Override
	public void dispose() {
	}

}
