package de.regasus.email.template.editor;

import org.eclipse.jface.viewers.BaseLabelProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Image;

import de.regasus.common.BaseFile;

public class NotificationTemplateLabelProvider extends BaseLabelProvider implements ITableLabelProvider {

	@Override
	public Image getColumnImage(Object element, int columnIndex) {
		return null;
	}


	@Override
	public String getColumnText(Object element, int columnIndex) {
		if (columnIndex == 1) {
			BaseFile file = (BaseFile) element;
			String fileName = file.getExternalFileName();
			return fileName;
		}
		else {
			return null;
		}
	}

}
