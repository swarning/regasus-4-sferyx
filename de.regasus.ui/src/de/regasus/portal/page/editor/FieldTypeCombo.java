package de.regasus.portal.page.editor;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import com.lambdalogic.util.rcp.Activator;
import de.regasus.core.error.RegasusErrorHandler;
import com.lambdalogic.util.rcp.widget.AbstractComboComposite;

import de.regasus.portal.component.Field;
import de.regasus.portal.component.FieldType;

public class FieldTypeCombo extends AbstractComboComposite<FieldType> {

	private Field field = null;


	public FieldTypeCombo(Composite parent, int style) throws Exception {
		super(parent, SWT.NONE);

		setWithEmptyElement(false);
		setKeepEntityInList(false);
	}


	@Override
	protected Object getEmptyEntity() {
		return null;
	}


	@Override
	protected LabelProvider getLabelProvider() {
		return new LabelProvider() {

			@Override
			public String getText(Object element) {
				FieldType fieldType = (FieldType) element;
				return fieldType.getString();
			}
		};
	}


	@Override
	protected Collection<FieldType> getModelData() {
		List<FieldType> fieldTypes = Collections.emptyList();
		if (field != null) {
			fieldTypes = field.getFieldTypes();
		}
		return fieldTypes;
	}


	@Override
	protected void initModel() {
		// do nothing because there is not model involved
	}


	@Override
	protected void disposeModel() {
		// do nothing because there is not model involved
	}


	@Override
	protected ViewerSorter getViewerSorter() {
		// return null to keep the original order as in the enum
		return null;
	}


	public FieldType getFieldType() {
		return entity;
	}


	public void setFieldType(FieldType fieldType) {
		setEntity(fieldType);
	}


	public Field getField() {
		return field;
	}


	public void setField(Field field) {
		FieldType oldFieldType = getFieldType();

		this.field = field;
		try {
			syncComboToModel();

			FieldType newFieldType = oldFieldType;
			if (field != null && ! field.getFieldTypes().contains(oldFieldType)) {
				newFieldType = field.getFieldTypes().get(0);
			}
			setFieldType(newFieldType);
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}

}
