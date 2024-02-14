package com.lambdalogic.util.rcp.pref;

import java.text.ParseException;

import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import com.lambdalogic.util.TypeHelper;

public class IntegerFieldEditor extends StringFieldEditor {
	private int minValidValue = 0;
	private int maxValidValue = Integer.MAX_VALUE;

	private static final int DEFAULT_TEXT_LIMIT = 10;

	/**
	* Creates a new field editor for Integer values (incl. null)
	*/
	protected IntegerFieldEditor() {
	}

	/**
	 * Creates an integer field editor.
	 *
	 * @param name the name of the preference this field editor works on
	 * @param labelText the label text of the field editor
	 * @param parent the parent of the field editor's control
	 */
	public IntegerFieldEditor(String name, String labelText, Composite parent) {
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
	public void setValidRange(int min, int max) {
		minValidValue = min;
		maxValidValue = max;
		setErrorMessage(
			JFaceResources.format(
				"IntegerFieldEditor.errorMessageRange", //$NON-NLS-1$
				Integer.valueOf(min),
				Integer.valueOf(max)
			)
		);
	}


	@Override
	protected boolean doCheckState() {
		// check for empty/null value already done in super class StringFieldEditor

		String numberString = getStringValue();
		try {
			Integer number = TypeHelper.toInteger(numberString);

			boolean validRange = number == null || (minValidValue <= number && number <= maxValidValue);
			return validRange;
		}
		catch (ParseException e) {
			return false;
		}
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
    			Integer integerValue = TypeHelper.toInteger(strValue);
    			getPreferenceStore().setValue(getPreferenceName(), TypeHelper.toString(integerValue));
			}
			catch (ParseException e) {
				showErrorMessage();
			}
		}
	}


	/**
	 * Returns this field editor's current value as an {@link Integer}.
	 *
	 * @return the value
	 * @throws ParseException if the <code>String</code> does not contain a parsable Integer value
	 */
	public Integer getIntegerValue() throws ParseException {
		return TypeHelper.toInteger( getStringValue() );
	}

}
