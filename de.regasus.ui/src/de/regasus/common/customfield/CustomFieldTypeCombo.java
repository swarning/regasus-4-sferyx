package de.regasus.common.customfield;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import com.lambdalogic.messeinfo.contact.CustomFieldType;
import com.lambdalogic.util.rcp.widget.AbstractComboComposite;

public class CustomFieldTypeCombo
extends AbstractComboComposite<CustomFieldType> {

	public CustomFieldTypeCombo(Composite parent, int style) throws Exception {
		super(parent, SWT.NONE);
	}

	
	protected CustomFieldType getEmptyEntity() {
		return null;
	}

	
	protected LabelProvider getLabelProvider() {
		return new LabelProvider() {
			
			@Override
			public String getText(Object element) {
				CustomFieldType customFieldType = (CustomFieldType) element;
				return customFieldType.getString();
			}
		};
	}
	
	
	protected Collection<CustomFieldType> getModelData() throws Exception {
		modelData= new ArrayList<CustomFieldType>(CustomFieldType.values().length);
		for (CustomFieldType customFieldType : CustomFieldType.values()) {
			modelData.add(customFieldType);
		}
		return modelData;
	}
	
	
	protected void initModel() {
	}
	
	
	protected void disposeModel() {
	}

}
