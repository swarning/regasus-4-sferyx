package com.lambdalogic.util.rcp.pref;

import java.text.ParseException;

import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import com.lambdalogic.util.TypeHelper;

public class LongFieldEditor extends StringFieldEditor {
	private long minValidValue = 0L;
	private long maxValidValue = Long.MAX_VALUE;

	private static final int DEFAULT_TEXT_LIMIT = 20;

	/**
	* Creates a new field editor for Long values (incl. null)
	*/
	protected LongFieldEditor() {
	}

	/**
	 * Creates an integer field editor.
	 *
	 * @param name the name of the preference this field editor works on
	 * @param labelText the label text of the field editor
	 * @param parent the parent of the field editor's control
	 */
	public LongFieldEditor(String name, String labelText, Composite parent) {
		init(name, labelText);
		setTextLimit(DEFAULT_TEXT_LIMIT);
		setEmptyStringAllowed(true);
		setErrorMessage(JFaceResources.getString("IntegerFieldEditor.errorMessage"));//$NON-NLS-1$
		createControl(parent);
	}


	/**
	 * Sets the range of valid values for this field.
	 *
	 * @param min the minimum allowed value (inclusive)
	 * @param max the maximum allowed value (inclusive)
	 */
	public void setValidRange(long min, long max) {
		minValidValue = min;
		maxValidValue = max;
		setErrorMessage(
			JFaceResources.format(
				"IntegerFieldEditor.errorMessageRange", //$NON-NLS-1$
				Long.valueOf(min),
				Long.valueOf(max)
			)
		);
	}


	@Override
	protected boolean checkState() {
		Text text = getTextControl();

		if (text == null) {
			return false;
		}

		String numberString = text.getText();
		try {
			Long number = TypeHelper.toLong(numberString);
			if (number == null || (minValidValue <= number && number <= maxValidValue)) {
				clearErrorMessage();
				return true;
			}

			showErrorMessage();
			return false;
		}
		catch (ParseException e) {
			showErrorMessage();
		}

		return false;
	}


	@Override
	protected void doLoad() {
		Text text = getTextControl();
		if (text != null) {
			String strValue = getPreferenceStore().getString( getPreferenceName() );
			text.setText(strValue);
			oldValue = strValue;
		}
	}


	@Override
	protected void doLoadDefault() {
		Text text = getTextControl();
		if (text != null) {
			String strValue = getPreferenceStore().getDefaultString( getPreferenceName() );
			text.setText(strValue);
		}
		valueChanged();
	}


	@Override
	protected void doStore() {
		Text text = getTextControl();
		if (text != null) {
			try {
    			String strValue = text.getText();
    			Long longValue = TypeHelper.toLong(strValue);
    			getPreferenceStore().setValue(getPreferenceName(), TypeHelper.toString(longValue));
			}
			catch (ParseException e) {
				showErrorMessage();
			}
		}
	}


	/**
	 * Returns this field editor's current value as a {@link Long}.
	 *
	 * @return the value
	 * @throws ParseException if the <code>String</code> does not contain a parsable Long value
	 */
	public Long getLongValue() throws ParseException {
		return TypeHelper.toLong( getStringValue() );
	}

}
