package de.regasus.history;

import static com.lambdalogic.util.StringHelper.trim;

import java.util.ArrayList;
import java.util.List;

import com.lambdalogic.i18n.I18NString;
import com.lambdalogic.time.I18NTemporal;
import com.lambdalogic.util.EqualsHelper;
import com.lambdalogic.util.rcp.UtilI18N;
import com.lambdalogic.util.rcp.i18n.I18NText;


public class FieldChangeGroup {

	private String groupName;

	private List<FieldChange> fieldChangeList = new ArrayList<>();


	public FieldChangeGroup(String name) {
		this.groupName = name;
	}


	@Override
	public String toString() {
		return groupName + ": " + fieldChangeList + "\n\n";
	}


	public void addIfNeeded(I18NString fieldName, boolean oldValue, boolean newValue) {
		String oldYesOrNo = oldValue ? UtilI18N.Yes : UtilI18N.No;
		String newYesOrNo = newValue ? UtilI18N.Yes : UtilI18N.No;
		addIfNeeded(I18NText.getString(fieldName), oldYesOrNo, newYesOrNo);
	}

	public void addIfNeeded(I18NString fieldName, Object oldValue, Object newValue) {
		addIfNeeded(I18NText.getString(fieldName), oldValue, newValue);
	}

	public void addIfNeeded(String fieldName, Object oldValue, Object newValue) {
		if ( ! isEqual(newValue, oldValue) ) {
			FieldChange fieldChange = new FieldChange(fieldName, oldValue, newValue);
			fieldChangeList.add(fieldChange);
		}
	}


	public void addIfNeeded(I18NString fieldName, I18NTemporal oldValue, I18NTemporal newValue) {
		if ( ! isEqual(oldValue, newValue) ) {
			FieldChange fieldChange = new FieldChange(fieldName.getString(), oldValue, newValue);
			fieldChangeList.add(fieldChange);
		}
	}


	public void add(String fieldName, Object oldValue, Object newValue) {
		fieldChangeList.add(new FieldChange(fieldName, oldValue, newValue));
	}


	public FieldChange getFieldChange(int i) {
		return fieldChangeList.get(i);
	}


	public boolean containsChanges() {
		return !fieldChangeList.isEmpty();
	}


	public int fieldChangeCount() {
		return fieldChangeList.size();
	}


	public String getGroupName() {
		return groupName;
	}


	private static boolean isEqual(Object oldValue, Object newValue) {
		if (oldValue instanceof CharSequence) {
			oldValue = trim(oldValue.toString(), false, false);
		}

		if (newValue instanceof CharSequence) {
			newValue = trim(newValue.toString(), false, false);
		}

		return EqualsHelper.isEqual(oldValue, newValue);
	}

}
