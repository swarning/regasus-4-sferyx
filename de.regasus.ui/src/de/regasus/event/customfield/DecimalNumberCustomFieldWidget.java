package de.regasus.event.customfield;

import java.math.BigDecimal;
import java.text.ParseException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import com.lambdalogic.messeinfo.contact.CustomField;
import com.lambdalogic.util.TypeHelper;
import com.lambdalogic.util.rcp.widget.DecimalNumberText;
import com.lambdalogic.util.rcp.widget.WidgetSizer;

public class DecimalNumberCustomFieldWidget extends AbstractCustomFieldWidget {

	private DecimalNumberText decimalNumberText;

	
	public DecimalNumberCustomFieldWidget(Composite parent, CustomField field) {
		super(parent, field);
	
		GridLayout gridLayout = new GridLayout(1, false);
		gridLayout.horizontalSpacing = 0;
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;
		gridLayout.verticalSpacing = 0;
		setLayout(gridLayout);

		decimalNumberText = new DecimalNumberText(this, SWT.BORDER);
		decimalNumberText.setNullAllowed(true);		
		
		// Joys of unboxing: if max or min is null, and you don't check, NPE happens
		if (field.getMax() != null) {
			decimalNumberText.setMaxValue(field.getMax());
		}
		
		/* Do not set the min value, because DecimalNumberText validates after every input.
		 * Therefore the user could not input any value if minimum has at least 2 digits.
		 * When typing the first digit it is rejected because the value is too small!
		 */
//		if (field.getMin() != null) {
//			decimalNumberText.setMinValue(field.getMin());
//		}
		
		if (field.getPrecision() != null) {
			decimalNumberText.setFractionDigits(field.getPrecision());
		}
		
		WidgetSizer.setWidth(decimalNumberText);
	}


	@Override
	public void addModifyListener(ModifyListener modifyListener) {
		decimalNumberText.addModifyListener(modifyListener);
	}


	@Override
	Object getWidgetValue() {
		BigDecimal bigDecimal = decimalNumberText.getValue();
		return bigDecimal;
	}


	@Override
	void setWidgetValueFromCustomFieldValue() throws ParseException {
		BigDecimal value = null;
		if (customFieldValue != null) {
			String s = customFieldValue.getValue();
			value = TypeHelper.toBigDecimal(s);
		}
		decimalNumberText.setValue(value);
	}
	
}
