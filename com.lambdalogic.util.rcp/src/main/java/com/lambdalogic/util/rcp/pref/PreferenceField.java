package com.lambdalogic.util.rcp.pref;

import static com.lambdalogic.util.StringHelper.isNotEmpty;

import java.util.Objects;

import com.lambdalogic.i18n.I18NString;

public class PreferenceField {

	private String qualifier;
	private String key;
	private String label;
	private PreferenceType type;
	private boolean nullable;
	private Object defaultValue;
	private Integer maxLength;
	private Long min;
	private Long max;
	private String[] fileExtensions;
	private String[][] labelsAndValues;
	private String inputDialogText;


	public PreferenceField(String qualifier, String key, PreferenceType type) {
		Objects.requireNonNull(qualifier);
		Objects.requireNonNull(key);
		Objects.requireNonNull(type);

		this.qualifier = qualifier;
		this.key = key;
		this.type = type;

		nullable = true;
	}


	public PreferenceField(String qualifier, String key, PreferenceType type, Object defaultValue) {
		this(qualifier, key, type);
		setDefaultValue(defaultValue);
	}


	public String getQualifier() {
		return qualifier;
	}


	public String getKey() {
		return key;
	}


	public PreferenceType getType() {
		return type;
	}


	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	public void setLabel(I18NString label) {
		if (label != null) {
			this.label = label.getString();
		}
		else {
			this.label = null;
		}
	}
	public PreferenceField label(String label) {
		setLabel(label);
		return this;
	}
	public PreferenceField label(I18NString label) {
		setLabel(label);
		return this;
	}


	public boolean isNullable() {
		return nullable;
	}
	public void setNullable(boolean nullable) {
		if (type == PreferenceType.BOOL || type == PreferenceType.RADIO) {
			throw new IllegalAccessError("Setting nullable is not allowed for PreferenceFields with PreferenceType BOOL and RADIO");
		}
		this.nullable = nullable;
	}
	public PreferenceField nullable(boolean nullable) {
		setNullable(nullable);
		return this;
	}


	public Object getDefaultValue() {
		return defaultValue;
	}
	public void setDefaultValue(Object defaultValue) {
		this.defaultValue = defaultValue;
	}
	public PreferenceField defaultValue(Object defaultValue) {
		setDefaultValue(defaultValue);
		return this;
	}


	public Integer getMaxLength() {
		return maxLength;
	}
	public void setMaxLength(Integer maxLength) {
		if (type != PreferenceType.STRING && type != PreferenceType.PASSWORD) {
			throw new IllegalAccessError("Setting maxLength is only allowed for PreferenceFields with PreferenceType STRING");
		}
		this.maxLength = maxLength;
	}
	public PreferenceField maxLength(Integer maxLength) {
		setMaxLength(maxLength);
		return this;
	}


	public Long getMin() {
		return min;
	}
	public void setMin(Long min) {
		if (type != PreferenceType.INTEGER && type != PreferenceType.LONG && type != PreferenceType.DOUBLE) {
			throw new IllegalAccessError("Setting min is only allowed for PreferenceFields with numerical types");
		}
		this.min = min;
	}
	public PreferenceField min(long min) {
		setMin( Long.valueOf(min) );
		return this;
	}
	public PreferenceField min(int min) {
		setMin( Long.valueOf(min) );
		return this;
	}


	public Long getMax() {
		return max;
	}
	public void setMax(Long max) {
		if (type != PreferenceType.INTEGER && type != PreferenceType.LONG && type != PreferenceType.DOUBLE) {
			throw new IllegalAccessError("Setting max is only allowed for PreferenceFields with PreferenceType INT, LONG or DOUBLE");
		}
		this.max = max;
	}
	public PreferenceField max(long max) {
		setMax( Long.valueOf(max) );
		return this;
	}
	public PreferenceField max(int max) {
		setMax( Long.valueOf(max) );
		return this;
	}


	public String[] getFileExtensions() {
		return fileExtensions;
	}
	public void setFileExtensions(String[] fileExtensions) {
		if (type != PreferenceType.FILE) {
			throw new IllegalAccessError("Setting max is only allowed for PreferenceFields with PreferenceType FILE");
		}
		this.fileExtensions = fileExtensions;
	}
	public PreferenceField fileExtensions(String[] fileExtensions) {
		setFileExtensions(fileExtensions);
		return this;
	}


	public String[][] getLabelsAndValues() {
		return labelsAndValues;
	}
	public void setLabelsAndValues(String[][] labelsAndValues) {
		if (type != PreferenceType.RADIO) {
			throw new IllegalAccessError("Setting labelsAndValues is only allowed for PreferenceFields with PreferenceType RADIO");
		}
		this.labelsAndValues = labelsAndValues;
	}
	public PreferenceField labelsAndValues(String[][] labelsAndValues) {
		setLabelsAndValues(labelsAndValues);
		return this;
	}


	public String getInputDialogText() {
		return inputDialogText;
	}
	public void setInputDialogText(String inputDialogText) {
		if (type != PreferenceType.STRING_LIST) {
			throw new IllegalAccessError("Setting inputDialogText is only allowed for PreferenceFields with PreferenceType STRING_LIST");
		}
		this.inputDialogText = inputDialogText;
	}
	public PreferenceField inputDialogText(String inputDialogText) {
		setInputDialogText(inputDialogText);
		return this;
	}


	/**
	 * Label if it is not empty or alternatively the key.
	 * @return
	 */
	public String getLabelOrKey() {
		if ( isNotEmpty(label) ) {
			return label;
		}
		return key;
	}


	@Override
	public String toString() {
		return new StringBuilder(128)
			.append( getClass().getSimpleName() )
			.append("[")
			.append(qualifier).append(".").append(key)
			.append("]")
			.toString();
	}

}
