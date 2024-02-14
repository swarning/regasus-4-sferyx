package de.regasus.event.customfield;

import java.text.ParseException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import com.lambdalogic.messeinfo.contact.CustomField;
import com.lambdalogic.messeinfo.contact.CustomFieldType;
import com.lambdalogic.util.StringHelper;
import com.lambdalogic.util.rcp.ModifyListenerAdapter;

public class BooleanCustomFieldWidget extends AbstractCustomFieldWidget {


	private Button checkBox;


	public BooleanCustomFieldWidget(Composite parent,  CustomField field) {
		super(parent, field);

		setLayout(new FillLayout());

		checkBox = new Button(this, SWT.CHECK);

		if (field.getCustomFieldType() == CustomFieldType.BST) {
			String labelText = field.getLabelOrName();
			labelText = StringHelper.stripHtml(labelText);

			checkBox.setText(labelText);
		}
	}


	@Override
	public void addModifyListener(ModifyListener modifyListener) {
		SelectionListener listenerAdapter = new ModifyListenerAdapter(modifyListener);
		checkBox.addSelectionListener(listenerAdapter);
	}


	@Override
	Object getWidgetValue() {
		return checkBox.getSelection();
	}


	@Override
	void setWidgetValueFromCustomFieldValue() throws ParseException {
		boolean value = false;
		if (customFieldValue != null) {
			value = customFieldValue.getValueAsBoolean().booleanValue();
		}
		checkBox.setSelection(value);
	}


	public void setText(String text) {
		checkBox.setText( StringHelper.avoidNull(text) );
	}

}
