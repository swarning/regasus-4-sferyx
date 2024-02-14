package de.regasus.onlineform.editor;

import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Item;

import com.lambdalogic.messeinfo.regasus.LabelBean;
import com.lambdalogic.util.StringHelper;
import com.lambdalogic.util.rcp.ModifySupport;

public class LabelBeanCellModifier implements ICellModifier {

	private String PROP = "custom";

	private TableViewer tableViewer;
	
	private ModifySupport modifySupport = new ModifySupport();
	
	private String language;
	
	public LabelBeanCellModifier(TableViewer tableViewer) {
		this.tableViewer = tableViewer;
		
		modifySupport = new ModifySupport(tableViewer.getTable());
	}
	
	
	public void setLanguage(String language) {
		this.language = language;
	}

	@Override
	public boolean canModify(Object element, String property) {
		return PROP.equals(property);
	}
	

	@Override
	public Object getValue(Object element, String property) {
		if (PROP.equals(property) && element instanceof LabelBean) {
			LabelBean labelBean = (LabelBean) element;
			String customValue = labelBean.getCustomValue(language);
			return StringHelper.avoidNull(customValue);
		}
		return null;
	}


	@Override
	public void modify(Object element, String property, Object value) {
		if (element instanceof Item)
			element = ((Item) element).getData();

		if (PROP.equals(property) && element instanceof LabelBean) {
			LabelBean labelBean = (LabelBean) element;

			String newValue = (String) value;
			labelBean.setCustumValue(language, StringHelper.trim(newValue));
			
			modifySupport.fire();
			tableViewer.refresh();
		}
	}
	
	public void addModifyListener(ModifyListener modifyListener) {
		modifySupport.addListener(modifyListener);
	}

	
	public void removeModifyListener(ModifyListener modifyListener) {
		modifySupport.removeListener(modifyListener);
	}

}
