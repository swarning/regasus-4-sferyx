package de.regasus.onlineform.editor;

import org.eclipse.jface.viewers.BaseLabelProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Image;

import com.lambdalogic.messeinfo.regasus.LabelBean;

public class LabelBeanTableLabelProvider extends BaseLabelProvider implements ITableLabelProvider {

	private String language;
	
	public void setLanguage(String language) {
		this.language = language;
	}
	
	@Override
	public Image getColumnImage(Object element, int columnIndex) {
		return null;
	}


	@Override
	public String getColumnText(Object element, int columnIndex) {
		if (element instanceof LabelBean) {
			LabelBean labelBean = (LabelBean) element;
			if (columnIndex == 0) {
				return labelBean.getDefaultValue(language);
			}
			else if (columnIndex == 1) {
				return labelBean.getCustomValue(language);
			}
		}
		return null;
	}

}
