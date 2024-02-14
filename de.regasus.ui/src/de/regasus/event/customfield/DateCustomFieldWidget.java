package de.regasus.event.customfield;

import java.text.ParseException;
import java.time.LocalDate;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import com.lambdalogic.messeinfo.contact.CustomField;
import com.lambdalogic.time.I18NDate;
import com.lambdalogic.time.I18NTemporal;
import com.lambdalogic.util.TypeHelper;
import com.lambdalogic.util.rcp.datetime.DateComposite;
import com.lambdalogic.util.rcp.widget.WidgetSizer;

public class DateCustomFieldWidget extends AbstractCustomFieldWidget {

	private DateComposite dateComposite;


	public DateCustomFieldWidget(Composite parent, CustomField field) {
		super(parent, field);

		GridLayout gridLayout = new GridLayout(1, false);
		gridLayout.horizontalSpacing = 0;
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;
		gridLayout.verticalSpacing = 0;
		setLayout(gridLayout);

		dateComposite = new DateComposite(this, SWT.NONE);
		WidgetSizer.setWidth(dateComposite);
	}


	@Override
	public void addModifyListener(ModifyListener modifyListener) {
		dateComposite.addModifyListener(modifyListener);
	}


	@Override
	Object getWidgetValue() {
		I18NDate i18nDate = dateComposite.getI18NDate();
		return i18nDate;
	}


	@Override
	void setWidgetValueFromCustomFieldValue() throws ParseException {
		LocalDate valueAsLocalDate = null;
		if (customFieldValue != null) {
			I18NTemporal valueAsI18NTemporal = customFieldValue.getValueAsTemporal();
			valueAsLocalDate = TypeHelper.toLocalDate(valueAsI18NTemporal);
		}
		dateComposite.setLocalDate(valueAsLocalDate);
	}

}
