package com.lambdalogic.util.rcp.pref;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;
import java.util.Objects;

import org.eclipse.ui.preferences.ScopedPreferenceStore;

import com.lambdalogic.util.BeanHelper;
import com.lambdalogic.util.TypeHelper;

public class PreferenceInitializerHelper {

	/**
	 * Initialize preference values with their default values.
	 * @param preferenceStore
	 * @param preferenceClass
	 */
	public static void initializePreferences(ScopedPreferenceStore preferenceStore, Class<?> preferenceClass) {
		Objects.requireNonNull(preferenceStore);
		Objects.requireNonNull(preferenceClass);

		List<PreferenceField> preferenceFieldList = BeanHelper.getStaticFieldMembers(preferenceClass, PreferenceField.class);
		for (PreferenceField preferenceField : preferenceFieldList) {
			preferenceStore.setToDefault( preferenceField.getKey() );
		}

		try {
			preferenceStore.save();
		}
		catch (IOException e) {
			com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
		}
	}


	/**
	 * Initialize the default values of all preference values.
	 *
	 * @param preferenceStore
	 * @param preferenceClass
	 */
	public static void initializeDefaultPreferences(ScopedPreferenceStore preferenceStore, Class<?> preferenceClass) {
		Objects.requireNonNull(preferenceStore);
		Objects.requireNonNull(preferenceClass);

		List<PreferenceField> preferenceFieldList = BeanHelper.getStaticFieldMembers(preferenceClass, PreferenceField.class);
		for (PreferenceField preferenceField : preferenceFieldList) {
			setDefault(preferenceStore, preferenceField);
		}
	}


	/**
	 * Initialize the default value of a preference value.
	 * @param preferenceStore
	 * @param field
	 */
	private static void setDefault(ScopedPreferenceStore preferenceStore, PreferenceField field) {
		Object defaultValue = getDefault(field);
		if (defaultValue != null) {
			PreferenceType type = field.getType();
			switch (type) {
				case STRING:
				case PASSWORD:
				case FILE:
				case DIRECTORY:
				case DATE:
				case RADIO:
				case STRING_LIST:
				case INTEGER_LIST:
					String strDefault = TypeHelper.toString(defaultValue);
					preferenceStore.setDefault(field.getKey(), strDefault);
					break;

				case BOOL:
					boolean boolDefault = TypeHelper.toBoolean(defaultValue, false);
					preferenceStore.setDefault(field.getKey(), boolDefault);
					break;

				case INTEGER:
					try {
    					Integer intDefault = TypeHelper.toInteger(defaultValue);
    					preferenceStore.setDefault(field.getKey(), intDefault);
					}
					catch (ParseException e) {
						e.printStackTrace();
					}
   					break;

				case LONG:
					try {
						Long longDefault = TypeHelper.toLong(defaultValue);
						preferenceStore.setDefault(field.getKey(), longDefault);
					}
					catch (ParseException e) {
						e.printStackTrace();
					}
					break;

				case DOUBLE:
					try {
						Double doubleDefault = TypeHelper.toDouble(defaultValue);
						preferenceStore.setDefault(field.getKey(), doubleDefault);
					}
					catch (ParseException e) {
						e.printStackTrace();
					}
					break;

			}
		}
	}


	private static Object getDefault(PreferenceField field) {
		Object defaultValue = System.getenv().get(field.getQualifier() + "." + field.getKey());
		if (defaultValue == null) {
			defaultValue = field.getDefaultValue();
		}

		return defaultValue;
	}

}
