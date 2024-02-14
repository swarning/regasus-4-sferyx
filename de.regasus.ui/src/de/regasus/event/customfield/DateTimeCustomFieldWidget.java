package de.regasus.event.customfield;

import java.text.ParseException;
import java.time.LocalDateTime;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import com.lambdalogic.messeinfo.contact.CustomField;
import com.lambdalogic.time.I18NDateMinute;
import com.lambdalogic.time.I18NTemporal;
import com.lambdalogic.util.TypeHelper;
import com.lambdalogic.util.rcp.datetime.DateTimeComposite;
import com.lambdalogic.util.rcp.widget.WidgetSizer;

public class DateTimeCustomFieldWidget extends AbstractCustomFieldWidget {

	private DateTimeComposite dateTimeComposite;


	public DateTimeCustomFieldWidget(Composite parent, CustomField field) {
		super(parent, field);

		GridLayout gridLayout = new GridLayout(1, false);
		gridLayout.horizontalSpacing = 0;
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;
		gridLayout.verticalSpacing = 0;
		setLayout(gridLayout);

		dateTimeComposite = new DateTimeComposite(this, SWT.NONE);

		WidgetSizer.setWidth(dateTimeComposite);
	}


	@Override
	public void addModifyListener(ModifyListener modifyListener) {
		dateTimeComposite.addModifyListener(modifyListener);
	}


	@Override
	Object getWidgetValue() {
		I18NDateMinute i18nDateMinute = dateTimeComposite.getI18NDateMinute();
		return i18nDateMinute;
	}


	@Override
	void setWidgetValueFromCustomFieldValue() throws ParseException {
		LocalDateTime valueAsLocalDateTime = null;
		if (customFieldValue != null) {
			I18NTemporal valueAsI18NTemporal = customFieldValue.getValueAsTemporal();
			valueAsLocalDateTime = TypeHelper.toLocalDateTime(valueAsI18NTemporal);
		}
		dateTimeComposite.setLocalDateTime(valueAsLocalDateTime);
	}

}
