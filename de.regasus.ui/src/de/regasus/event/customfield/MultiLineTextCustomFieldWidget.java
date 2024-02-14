package de.regasus.event.customfield;

import java.text.ParseException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import com.lambdalogic.messeinfo.contact.CustomField;
import com.lambdalogic.util.StringHelper;
import com.lambdalogic.util.rcp.widget.MultiLineText;


public class MultiLineTextCustomFieldWidget extends AbstractCustomFieldWidget {

	private Text text;

	
	public MultiLineTextCustomFieldWidget(Composite parent, CustomField field) {
		super(parent, field);
		
		GridLayout gridLayout = new GridLayout(1, false);
		gridLayout.horizontalSpacing = 0;
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;
		gridLayout.verticalSpacing = 0;
		setLayout(gridLayout);
		
//		text = new Text(this, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL);
		text = new MultiLineText(this, SWT.BORDER, true /*dynamic*/);
		
		text.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
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


	@Override
	public boolean isGrabVerticalSpace() {
		return true;
	}

}
