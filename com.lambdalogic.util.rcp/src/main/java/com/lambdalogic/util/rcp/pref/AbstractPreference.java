package com.lambdalogic.util.rcp.pref;

import static com.lambdalogic.util.StringHelper.isEmpty;

import java.text.ParseException;
import java.time.DateTimeException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.ui.preferences.ScopedPreferenceStore;

import com.lambdalogic.time.I18NDate;
import com.lambdalogic.util.TypeHelper;

public abstract class AbstractPreference {

	public static final String DELIMITER = String.valueOf(";");

	public abstract ScopedPreferenceStore getPreferenceStore();


	protected String getString(PreferenceField preferenceField) {
		return getPreferenceStore().getString( preferenceField.getKey() );
	}


	protected List<String> getStringList(PreferenceField preferenceField) {
		String s = getString(preferenceField);
		String[] splitted = s.split(DELIMITER);

		List<String> list = new ArrayList<>(splitted.length);
		for (String splitItem : splitted) {
			splitItem = splitItem.trim();
			list.add(splitItem);
		}

		return list;
	}


	protected I18NDate getI18NDate(PreferenceField preferenceField) {
		try {
			String strValue = getString(preferenceField);
			I18NDate date = TypeHelper.toI18NDate(strValue);
			return date;
		}
		catch (DateTimeException | ParseException e) {
			throw new IllegalArgumentException(e);
		}
	}


	protected boolean getBoolean(PreferenceField preferenceField) {
		return getPreferenceStore().getBoolean( preferenceField.getKey() );
	}


	protected Integer getInteger(PreferenceField preferenceField) {
		String key = preferenceField.getKey();

		int intValue = getPreferenceStore().getInt(key);

		// if l is the default value 0: check if the actual value is null
		if (intValue == 0) {
			String s = getPreferenceStore().getString(key);
			if ( isEmpty(s) ) {
				return null;
			}
		}

		return intValue;
	}


	protected List<Integer> getIntegerList(PreferenceField preferenceField) {
		String strValue = getString(preferenceField);
		List<Integer> integerList = TypeHelper.toIntegerList(strValue, DELIMITER);
		return integerList;
	}


	protected Long getLong(PreferenceField preferenceField) {
		String key = preferenceField.getKey();

		long longValue = getPreferenceStore().getLong(key);

		// if l is the default value 0: check if the actual value is null
		if (longValue == 0L) {
			String s = getPreferenceStore().getString(key);
			if ( isEmpty(s) ) {
				return null;
			}
		}

		return longValue;
	}


	protected Double getDouble(PreferenceField preferenceField) {
		String key = preferenceField.getKey();

		double doubleValue = getPreferenceStore().getDouble(key);

		// if l is the default value 0: check if the actual value is null
		if (doubleValue == 0.0) {
			String s = getPreferenceStore().getString(key);
			if ( isEmpty(s) ) {
				return null;
			}
		}

		return doubleValue;
	}


	protected void setValue(PreferenceField preferenceField, String value) {
		if (value == null) {
			value = "";
		}
		getPreferenceStore().setValue(preferenceField.getKey(), value);
	}


	protected void setStringListValue(PreferenceField preferenceField, List<String> value) {
		String strValue = TypeHelper.toStringFromStringColl(value, DELIMITER);
		setValue(preferenceField, strValue);
	}


	protected void setValue(PreferenceField preferenceField, I18NDate value) {
		String strValue = TypeHelper.toString(value);
		setValue(preferenceField, strValue);
	}


	protected void setValue(PreferenceField preferenceField, boolean value) {
		getPreferenceStore().setValue(preferenceField.getKey(), value);
	}


	protected void setValue(PreferenceField preferenceField, Integer value) {
		if (value == null) {
			value = 0;
		}
		getPreferenceStore().setValue(preferenceField.getKey(), value);
	}


	protected void setIntegerListValue(PreferenceField preferenceField, List<Integer> value) {
		String strValue = TypeHelper.toStringFromIntegerColl(value, DELIMITER);
		setValue(preferenceField, strValue);
	}


	protected void setValue(PreferenceField preferenceField, Long value) {
		if (value == null) {
			value = 0L;
		}
		getPreferenceStore().setValue(preferenceField.getKey(), value);
	}


	protected void setValue(PreferenceField preferenceField, Double value) {
		if (value == null) {
			value = 0.0;
		}
		getPreferenceStore().setValue(preferenceField.getKey(), value);
	}

}
