package de.regasus.portal.page.editor;

import static com.lambdalogic.util.StringHelper.avoidNull;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.rcp.Activator;
import de.regasus.core.error.RegasusErrorHandler;
import com.lambdalogic.util.rcp.widget.AbstractComboComposite;

import de.regasus.portal.component.Field;

public abstract class FieldCombo extends AbstractComboComposite<Field> {

	public abstract Long getEventID();

	public abstract void setEventID(Long eventID) throws Exception;


	public FieldCombo(Composite parent, int style) throws Exception {
		super(parent, SWT.NONE);

		setKeepEntityInList(false);
	}


	@Override
	protected Object getEmptyEntity() {
		return EMPTY_ELEMENT;
	}


	@Override
	protected LabelProvider getLabelProvider() {
		return new LabelProvider() {
			@Override
			public String getText(Object element) {
				if (element == EMPTY_ELEMENT) {
					return EMPTY_ELEMENT;
				}

				Field field = (Field) element;
    			return avoidNull( field.getComboLabel().getString() );
			}
		};
	}


	protected CacheModelListener<Field> modelListener = new CacheModelListener<Field>() {
		@Override
		public void dataChange(CacheModelEvent<Field> event) {
			try {
				handleModelChange();
			}
			catch (Exception e) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			}
		}
	};


	public Field getField() {
		return entity;
	}


	public String getFieldId() {
		if (entity != null) {
			return entity.getFieldId();
		}
		else {
			return null;
		}
	}


	public void setFieldId(String fieldId) {
		try {
			Field field = findFieldById(fieldId);
			setEntity(field);
		}
		catch (Throwable t) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t);
		}
	}


	private Field findFieldById(String fieldId) {
		for (Field field : modelData) {
			if (field.getFieldId().equals(fieldId)) {
				return field;
			}
		}
		return null;
	}

}
