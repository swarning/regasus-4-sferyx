package de.regasus.event.customfield;

import java.text.ParseException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import com.lambdalogic.messeinfo.contact.CustomField;
import com.lambdalogic.util.TypeHelper;
import com.lambdalogic.util.rcp.widget.NullableSpinner;
import com.lambdalogic.util.rcp.widget.WidgetSizer;

public class IntegralNumberCustomFieldWidget extends AbstractCustomFieldWidget {

	private NullableSpinner numberText;


	public IntegralNumberCustomFieldWidget(Composite parent, CustomField field) {
		super(parent, field);
		
		GridLayout gridLayout = new GridLayout(1, false);
		gridLayout.horizontalSpacing = 0;
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;
		gridLayout.verticalSpacing = 0;
		setLayout(gridLayout);

		numberText = new NullableSpinner(this, SWT.NONE);
		numberText.setMaximum(field.getMax());
		numberText.setMinimum(field.getMin());

		WidgetSizer.setWidth(numberText);
	}

	

	
	@Override
	public void addModifyListener(ModifyListener modifyListener) {
		numberText.addModifyListener(modifyListener);
	}


	@Override
	Object getWidgetValue() {
		Long pkValue = numberText.getValue();
		return pkValue;
	}


	@Override
	void setWidgetValueFromCustomFieldValue() throws ParseException {
		Long value = null;
		if (customFieldValue != null) {
			String s = customFieldValue.getValue();
			value = TypeHelper.toLong(s);
		}
		numberText.setValue(value);
	}
	
}
