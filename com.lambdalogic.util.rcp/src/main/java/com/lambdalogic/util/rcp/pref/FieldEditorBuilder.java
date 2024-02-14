package com.lambdalogic.util.rcp.pref;

import java.util.Objects;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FileFieldEditor;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.widgets.Composite;

public class FieldEditorBuilder {

	public static FieldEditor buildFieldEditor(Composite parent, PreferenceField preferenceField) {
		Objects.requireNonNull(preferenceField);

		FieldEditor fieldEditor = null;

		PreferenceType preferenceType = preferenceField.getType();
		switch (preferenceType) {
			case STRING:
				return buildStringFieldEditor(parent, preferenceField);
			case PASSWORD:
				return buildPasswordFieldEditor(parent, preferenceField);
			case FILE:
				return buildFileFieldEditor(parent, preferenceField);
			case DIRECTORY:
				return buildDirectoryFieldEditor(parent, preferenceField);
			case DATE:
				// Until now there is no need for need for a I18NDateFieldEditor, because this type is only used internally.
				return buildStringFieldEditor(parent, preferenceField);
			case BOOL:
				return buildBooleanFieldEditor(parent, preferenceField);
			case INTEGER:
				return buildIntegerFieldEditor(parent, preferenceField);
			case LONG:
				return buildLongFieldEditor(parent, preferenceField);
			case DOUBLE:
				return buildDoubleFieldEditor(parent, preferenceField);
			case RADIO:
				return buildRadioGroupFieldEditor(parent, preferenceField);
			case STRING_LIST:
				return buildStringListFieldEditor(parent, preferenceField);
			case INTEGER_LIST:
				// Until now there is no need for need for an IntegerFieldEditor, because this type is only used internally.
				return buildStringFieldEditor(parent, preferenceField);

		}

		return fieldEditor;
	}


	public static StringFieldEditor buildStringFieldEditor(Composite parent, PreferenceField preferenceField) {
		String name = preferenceField.getKey();
		String label = preferenceField.getLabelOrKey();
		StringFieldEditor fieldEditor = new StringFieldEditor(name, label, parent);

		// handle maxLength
		Integer maxLength = preferenceField.getMaxLength();
		if (maxLength != null) {
			fieldEditor.setTextLimit(maxLength);
		}

		// handle null values
		fieldEditor.setEmptyStringAllowed( preferenceField.isNullable() );

		return fieldEditor;
	}


	public static PasswordFieldEditor buildPasswordFieldEditor(Composite parent, PreferenceField preferenceField) {
		String name = preferenceField.getKey();
		String label = preferenceField.getLabelOrKey();
		PasswordFieldEditor fieldEditor = new PasswordFieldEditor(name, label, parent);

		// handle maxLength
		Integer maxLength = preferenceField.getMaxLength();
		if (maxLength != null) {
			fieldEditor.setTextLimit(maxLength);
		}

		// handle null values
		fieldEditor.setEmptyStringAllowed( preferenceField.isNullable() );

		return fieldEditor;
	}


	public static FileFieldEditor buildFileFieldEditor(Composite parent, PreferenceField preferenceField) {
		String name = preferenceField.getKey();
		String label = preferenceField.getLabelOrKey();
		FileFieldEditor fieldEditor = new FileFieldEditor(name, label, parent);

		// handle fileExtensions
		String[] fileExtensions = preferenceField.getFileExtensions();
		if (fileExtensions != null) {
			fieldEditor.setFileExtensions(fileExtensions);
		}

		// handle null values
		fieldEditor.setEmptyStringAllowed( preferenceField.isNullable() );

		return fieldEditor;
	}


	public static DirectoryFieldEditor buildDirectoryFieldEditor(Composite parent, PreferenceField preferenceField) {
		String name = preferenceField.getKey();
		String label = preferenceField.getLabelOrKey();
		DirectoryFieldEditor fieldEditor = new DirectoryFieldEditor(name, label, parent);

		// handle null values
		fieldEditor.setEmptyStringAllowed( preferenceField.isNullable() );

		return fieldEditor;
	}


	public static BooleanFieldEditor buildBooleanFieldEditor(Composite parent, PreferenceField preferenceField) {
		String name = preferenceField.getKey();
		String label = preferenceField.getLabelOrKey();
		return new BooleanFieldEditor(name, label, parent);
	}


	public static IntegerFieldEditor buildIntegerFieldEditor(Composite parent, PreferenceField preferenceField) {
		String name = preferenceField.getKey();
		String label = preferenceField.getLabelOrKey();

		IntegerFieldEditor fieldEditor = new IntegerFieldEditor(name, label, parent);

		// handle min and max
		Long min = preferenceField.getMin();
		Long max = preferenceField.getMax();
		if (min != null || max != null) {
			if (min == null || min < Integer.MIN_VALUE) {
				min = Long.valueOf(Integer.MIN_VALUE);
			}

			if (max == null || max > Integer.MAX_VALUE) {
				max = Long.valueOf(Integer.MAX_VALUE);
			}

			fieldEditor.setValidRange(min.intValue(), max.intValue());
		}

		// handle null values
		fieldEditor.setEmptyStringAllowed( preferenceField.isNullable() );

		return fieldEditor;
	}


	public static LongFieldEditor buildLongFieldEditor(Composite parent, PreferenceField preferenceField) {
		String name = preferenceField.getKey();
		String label = preferenceField.getLabelOrKey();

		LongFieldEditor fieldEditor = new LongFieldEditor(name, label, parent);

		// handle min and max
		Long min = preferenceField.getMin();
		Long max = preferenceField.getMax();
		if (min != null || max != null) {
			if (min == null) {
				min = Long.MIN_VALUE;
			}

			if (max == null) {
				max = Long.MAX_VALUE;
			}

			fieldEditor.setValidRange(min, max);
		}

		// handle null values
		fieldEditor.setEmptyStringAllowed( preferenceField.isNullable() );

		return fieldEditor;
	}


	public static DoubleFieldEditor buildDoubleFieldEditor(Composite parent, PreferenceField preferenceField) {
		String name = preferenceField.getKey();
		String label = preferenceField.getLabelOrKey();

		DoubleFieldEditor fieldEditor = new DoubleFieldEditor(name, label, parent);

		// handle min and max
		Long min = preferenceField.getMin();
		Long max = preferenceField.getMax();
		if (min != null || max != null) {
			if (min == null || min < Double.MIN_VALUE) {
				min = Math.round(Double.MIN_VALUE);
			}

			if (max == null || max > Double.MAX_VALUE) {
				max = Math.round(Double.MAX_VALUE);
			}

			fieldEditor.setValidRange(min.doubleValue(), max.doubleValue());
		}

		// handle null values
		fieldEditor.setEmptyStringAllowed( preferenceField.isNullable() );

		return fieldEditor;
	}


	public static RadioGroupFieldEditor buildRadioGroupFieldEditor(Composite parent, PreferenceField preferenceField) {
		String name = preferenceField.getKey();
		String label = preferenceField.getLabelOrKey();
		String[][] labelsAndValues = preferenceField.getLabelsAndValues();
		int numColumns = 1;
		boolean useGroup = true;
		RadioGroupFieldEditor fieldEditor = new RadioGroupFieldEditor(
			name,
			label,
			numColumns,
			labelsAndValues,
			parent,
			useGroup
		);

		return fieldEditor;
	}


	public static StringListFieldEditor buildStringListFieldEditor(Composite parent, PreferenceField preferenceField) {
		String name = preferenceField.getKey();
		String label = preferenceField.getLabelOrKey();
		String inputDialogText = preferenceField.getInputDialogText();

		StringListFieldEditor fieldEditor = new StringListFieldEditor(
			name,
			label,
			inputDialogText,
			parent
		);

		return fieldEditor;
	}

}
