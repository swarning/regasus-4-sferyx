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

public abstract class AbstractCustomFieldListWidget extends AbstractCustomFieldWidget {

	protected List<CustomFieldListValue> dataItems;

	private Map<String, Integer> id2IndexMap = new HashMap<>();

	protected int count;


	public AbstractCustomFieldListWidget(Composite parent, CustomField field) {
		super(parent, field);

		List<? extends CustomFieldListValue> customFieldValuesList = field.getCustomFieldListValues();

		if (customFieldValuesList == null) {
			dataItems = Collections.emptyList();
		}
		else {
			dataItems = new ArrayList<CustomFieldListValue>(customFieldValuesList);
		}

		if (field.isSortByName()) {
			Collections.sort(dataItems, CustomFieldListValueLabelComparator.getInstance());
		}

		count = customFieldValuesList.size();

		for (int i = 0; i < count; i++) {
			id2IndexMap.put(getValue(i), i);
		}
	}


	abstract protected List<Integer> getSelectedIndices();


	abstract protected void setIndicesToSelect(List<Integer> indicesToSelect);


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
		List<Integer> indicesToSelect = null;

		if (customFieldValue != null) {
    		List<String> valuesToSelect = customFieldValue.getValueAsList();
    		if (valuesToSelect != null && ! valuesToSelect.isEmpty()) {
        		indicesToSelect = new ArrayList<Integer>(valuesToSelect.size());

    			for (String valueToSelect : valuesToSelect) {
    				Integer index = getIndex(valueToSelect);
    				if (index != null && index.intValue() < count) {
    					indicesToSelect.add(index);
    				}
    			}
    		}
		}

		if (indicesToSelect == null) {
			indicesToSelect = Collections.emptyList();
		}

		setIndicesToSelect(indicesToSelect);
	}



	@Override
	Object getWidgetValue() {
		List<Integer> selectedIndices = getSelectedIndices();

		List<String> selectedValues = new ArrayList<>();
		for (Integer integer : selectedIndices) {
			selectedValues.add(getValue(integer));
		}
		return selectedValues;
	}

}
