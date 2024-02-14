package de.regasus.event.customfield;

import java.text.ParseException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import com.lambdalogic.messeinfo.contact.CustomField;
import com.lambdalogic.util.StringHelper;


public class SingleLineTextCustomFieldWidget extends AbstractCustomFieldWidget {

	private Text text;

	
	public SingleLineTextCustomFieldWidget(Composite parent, CustomField field) {
		super(parent, field);
		
		setLayout(new FillLayout());
		
		text = new Text(this, SWT.SINGLE | SWT.BORDER);
		
		Integer max = field.getMax();
		if (max != null) {
			text.setTextLimit(max.intValue());
		}
	}
	

	@Override
	public void addModifyListener(ModifyListener modifyListener) {
		text.addModifyListener(modifyListener);
	}

	
	@Override
	Object getWidgetValue() {
		return StringHelper.trim(text.getText());
	}

	
	@Override
	void setWidgetValueFromCustomFieldValue() throws ParseException {
		String value = null;
		if (customFieldValue != null) {
			value = customFieldValue.getValue();
		}
		value = StringHelper.avoidNull(value);
		text.setText(value);
	}


	@Override
	public boolean isGrabHorizontalSpace() {
		return true;
	}

}
