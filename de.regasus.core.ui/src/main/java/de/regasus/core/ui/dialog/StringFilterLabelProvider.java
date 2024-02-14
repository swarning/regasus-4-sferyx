package de.regasus.core.ui.dialog;

import org.eclipse.jface.viewers.BaseLabelProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Image;

public class StringFilterLabelProvider extends BaseLabelProvider implements ITableLabelProvider {

	/**
	 * Show no images
	 */
	public Image getColumnImage(Object element, int columnIndex) {
		return null;
	}


	/**
	 * The second column (index=1) shall show programme point name
	 */
	public String getColumnText(Object element, int columnIndex) {
		if (columnIndex == 1) {
			return element.toString();
		}
		return null;
	}

}
