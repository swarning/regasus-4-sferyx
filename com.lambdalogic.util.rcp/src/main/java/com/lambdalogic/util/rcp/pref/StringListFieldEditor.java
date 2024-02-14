package com.lambdalogic.util.rcp.pref;

import static com.lambdalogic.util.rcp.pref.AbstractPreference.DELIMITER;

import java.util.List;

import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.preference.ListEditor;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Composite;

import com.lambdalogic.util.CollectionsHelper;
import com.lambdalogic.util.TypeHelper;

/**
 * A FieldEditor for PreferencePages in which the user can enter and manage a list of Strings.
 */
public class StringListFieldEditor extends ListEditor {

	protected String inputDialogText;


	public StringListFieldEditor(String name, String labelText, String inputDialogText, Composite parent) {
		super(name, labelText, parent);
		this.inputDialogText = inputDialogText;
	}


	/**
	 * Since the preference store cannot natively store lists, but only Strings, we need to concatenate the list entries.
	 */
	@Override
	protected String createList(String[] items) {
		List<String> strList = CollectionsHelper.createArrayList(items);
		String strValue = TypeHelper.toStringFromStringColl(strList, DELIMITER);
		return strValue;
	}


	/**
	 * Since the preference store cannot natively store lists, but only Strings, we need to concatenate the list entries.
	 */
	@Override
	protected String[] parseString(String stringList) {
		String[] splitted = stringList.split(DELIMITER);
		return splitted;
	}


	/**
	 * This method is called when the user presses the button to add a new item to the list, so we open a dialog to ask
	 * the user what should be added.
	 *
	 * @return
	 */
	@Override
	protected String getNewInputObject() {
		InputDialog inputDialog = new InputDialog(getShell(), "Input", inputDialogText, null, null);
		int open = inputDialog.open();
		if (open == Window.OK) {
			return inputDialog.getValue();
		}
		return null;
	}


	public String getInputDialogText() {
		return inputDialogText;
	}


	public void setInputDialogText(String inputDialogText) {
		this.inputDialogText = inputDialogText;
	}

}
