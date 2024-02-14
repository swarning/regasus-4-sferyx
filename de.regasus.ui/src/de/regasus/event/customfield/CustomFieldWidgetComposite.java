package de.regasus.event.customfield;

import java.text.ParseException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;

import com.lambdalogic.messeinfo.contact.CustomField;
import com.lambdalogic.messeinfo.contact.CustomFieldType;
import com.lambdalogic.messeinfo.contact.CustomFieldValue;
import com.lambdalogic.messeinfo.profile.ProfileCustomField;
import com.lambdalogic.util.rcp.ModifySupport;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.ui.Activator;


public class CustomFieldWidgetComposite extends Composite {

	// Widget for entering values
	private AbstractCustomFieldWidget customFieldWidget;
	private int preferredWidth = -1;

	private ModifySupport modifySupport;


	/**
	 * Create the composite
	 * @param parent
	 * @param style
	 */
	public CustomFieldWidgetComposite(Composite parent, int style) {
		super(parent, style);
		setLayout(new FillLayout());

		setCustomField(null);
	}


	public void setCustomField(CustomField customField) {
		if (customFieldWidget != null) {
			customFieldWidget.dispose();
		}

		if (customField != null) {
			customFieldWidget = CustomFieldWidgetFactory.create(this, customField);

			// remove label right of the checkbox that appears if the CustomFieldType is BST
			if (customFieldWidget instanceof BooleanCustomFieldWidget) {
				((BooleanCustomFieldWidget) customFieldWidget).setText("");
			}

			setEnabled(true);

			if (modifySupport != null) {
				customFieldWidget.addModifyListener(modifySupport);
			}
		}
		else {
			customField = new ProfileCustomField();
			customField.setCustomFieldType(CustomFieldType.SLT);
			customFieldWidget = CustomFieldWidgetFactory.create(this, customField);
			setEnabled(false);
		}

		/* Build GridData from based on the properties grabHorizontalSpace and grabVerticalSpace
		 * of the current customFieldWidget and set it as LayoutData for this
		 * CustomFieldWidgetComposite. It is NOT set as LayoutData for the customFieldWidget,
		 * because the parent Composite of customFieldWidget is this CustomFieldWidgetComposite
		 * and it has a FillLayout.
		 * But the GridData affects the customFieldWidget indirectly.
		 */
		boolean grabExcessHorizontalSpace = customFieldWidget.isGrabHorizontalSpace();
		boolean grabExcessVerticalSpace = customFieldWidget.isGrabVerticalSpace();

		int horizontalAlignment = SWT.LEFT;
		if (grabExcessHorizontalSpace) {
			horizontalAlignment = SWT.FILL;
		}

		int verticalAlignment = SWT.CENTER;
		if (grabExcessVerticalSpace) {
			verticalAlignment = SWT.FILL;
		}

		GridData gridData = new GridData(
			horizontalAlignment,
			verticalAlignment,
			grabExcessHorizontalSpace,
			grabExcessVerticalSpace
		);
		setLayoutData(gridData);
	}


	public CustomFieldValue getValue() {
		CustomFieldValue value = null;
		if (isEnabled()) {
			try {
				customFieldWidget.syncEntityToWidget();
			}
			catch (ParseException e) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			}
			value = customFieldWidget.getCustomFieldValue();
		}
		return value;
	}


	public int getPreferredWidth() {
		return preferredWidth;
	}


	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		customFieldWidget.setEnabled(enabled);
	}


	private ModifySupport getModifySupport() {
		if (modifySupport == null) {
			modifySupport = new ModifySupport();
			customFieldWidget.addModifyListener(modifySupport);
		}
		return modifySupport;
	}


	public void addModifyListener(ModifyListener listener) {
		getModifySupport().addListener(listener);
	}


	public void removeModifyListener(ModifyListener listener) {
		if (modifySupport != null) {
			modifySupport.removeListener(listener);
		}
	}


//	public boolean isGrabHorizontalSpace() {
//		return customFieldWidget.isGrabHorizontalSpace();
//	}
//
//
//	public boolean isGrabVerticalSpace() {
//		return customFieldWidget.isGrabVerticalSpace();
//	}


	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

}
