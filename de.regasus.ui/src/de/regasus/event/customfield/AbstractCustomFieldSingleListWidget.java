package de.regasus.event.customfield;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.widgets.Composite;

import com.lambdalogic.i18n.LanguageString;
import com.lambdalogic.messeinfo.contact.CustomField;
import com.lambdalogic.messeinfo.contact.CustomFieldListValue;
import com.lambdalogic.messeinfo.contact.CustomFieldListValueLabelComparator;
import com.lambdalogic.messeinfo.contact.CustomFieldListValuePositionComparator;


public abstract class AbstractCustomFieldSingleListWidget extends AbstractCustomFieldWidget {

	protected List<CustomFieldListValue> dataItems;

	private Map<String, Integer> id2IndexMap = new HashMap<>();

	protected int count;


	public AbstractCustomFieldSingleListWidget(Composite parent, CustomField field) {
		super(parent, field);

		List<? extends CustomFieldListValue> customFieldValuesList = field.getCustomFieldListValues();

		if (customFieldValuesList == null) {
			dataItems = Collections.emptyList();
		}
		else {
			dataItems = new ArrayList<>(customFieldValuesList);
		}

		if (field.isSortByName()) {
			Collections.sort(dataItems, CustomFieldListValueLabelComparator.getInstance());
		}
		else {
			Collections.sort(dataItems, CustomFieldListValuePositionComparator.getInstance());
		}

		count = customFieldValuesList.size();

		for (int i = 0; i < count; i++) {
			id2IndexMap.put(getValue(i), i);
		}
	}


	abstract protected Integer getSelectedIndex();


	abstract protected void setIndexToSelect(Integer indicexToSelect);


	protected Integer getIndex(String id) {
		return id2IndexMap.get(id);
	}


	protected String getLabel(int i) {
		CustomFieldListValue listValue = dataItems.get(i);
		LanguageString languageString = listValue.getLabel();
		if (languageString != null) {
			return languageString.getString();
		}
		else {
			return "";
		}
	}


	protected String getValue(int i) {
		return dataItems.get(i).getValue();
	}


	@Override
	void setWidgetValueFromCustomFieldValue() throws ParseException {
		Integer index = null;

		if (customFieldValue != null) {
			String idToSelect = customFieldValue.getValue();
			index = getIndex(idToSelect);
		}

		setIndexToSelect(index);
	}


	@Override
	Object getWidgetValue() {
		Integer selectedIndex = getSelectedIndex();
		if (selectedIndex != null) {
			return getValue(selectedIndex);
		}
		else {
			return null;
		}
	}

}
