package de.regasus.event.customfield;

import java.text.ParseException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Composite;

import com.lambdalogic.i18n.LanguageString;
import com.lambdalogic.messeinfo.contact.CustomField;
import com.lambdalogic.messeinfo.contact.CustomFieldType;
import com.lambdalogic.messeinfo.contact.CustomFieldValue;



/**
 * Any subclass widget needs to know
 * 
 * <ul>
 * <li>the {@link CustomField} that determines the type of the widget</li>
 * <li>a {@link CustomFieldValue} with PK from the database, or otherwise
 * <li>a {@link CustomFieldValue} with no PK, for holding default values and/or user modifications
 * </ul> 
 *
 */
public abstract class AbstractCustomFieldWidget extends Composite {

	protected CustomField customField;
	
	protected CustomFieldValue customFieldValue;
	

	public AbstractCustomFieldWidget(Composite parent, CustomField customField) {
		super(parent, SWT.NONE);

		this.customField = customField;
		
		// Do NOT set the Layout, because each implementation sets its own.
	}
	
	
	abstract Object getWidgetValue();
	
	abstract void setWidgetValueFromCustomFieldValue() throws ParseException;
	
	abstract public void addModifyListener(ModifyListener modifyListener);

	
	public CustomFieldType getType() {
		return customField.getCustomFieldType();
	}

	
	public String getFieldName() {
		return customField.getName();
	}


	@Override
	public String getToolTipText() {
		String toolTipText = null;
		
		LanguageString toolTip = customField.getToolTip();
		if (toolTip != null) {
			toolTipText = toolTip.getString();
		}
		return toolTipText;
	}
	
	
	public CustomFieldValue getCustomFieldValue() {
		return customFieldValue;
	}


	public void setCustomFieldValue(CustomFieldValue customFieldValue) {
		if (customFieldValue != null) {
			this.customFieldValue = customFieldValue;
		}
		else {
			this.customFieldValue = new CustomFieldValue(customField.getPrimaryKey(), null);
		}
	}
	

	public void syncEntityToWidget() throws ParseException {
		Object widgetValue = getWidgetValue();
		CustomFieldType type = getType();

		/* Create pcFieldValue if necessary.
		 * But don't destroy it if it exist and widgetValue is null, because it may be needed later.
		 */
		if (widgetValue != null) {
			if (customFieldValue == null) {
				customFieldValue = new CustomFieldValue(customField.getPrimaryKey(), null);
			}
			customFieldValue.setValue(widgetValue, type);
		}
		else if (customFieldValue != null) {
			customFieldValue.setValue(widgetValue, type);
		}
	}
	
	
	public void syncWidgetToEntity() throws ParseException {
		setWidgetValueFromCustomFieldValue();
	}
	
	
	public boolean isGrabHorizontalSpace() {
		return false;
	}
	
	
	public boolean isGrabVerticalSpace() {
		return false;
	}


	@Override
	public String toString() {
		return getClass().getSimpleName() + ": field=" + customField.getLabel() + ": value=" + getWidgetValue();
	}

}
