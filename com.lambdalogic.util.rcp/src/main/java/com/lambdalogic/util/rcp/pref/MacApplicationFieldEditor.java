package com.lambdalogic.util.rcp.pref;

import java.io.File;

import org.eclipse.jface.preference.FileFieldEditor;
import org.eclipse.swt.widgets.Composite;


/**
 * A field editor for an application directory path type preference.
 * A standard file dialog appears when the user presses the change button.
 */
public class MacApplicationFieldEditor extends FileFieldEditor {

	public static final String[] FILE_EXTENSIONS = {"*.app"};

    /**
     * Creates a new file field editor
     */
    protected MacApplicationFieldEditor() {
    }

    /**
     * Creates a file field editor.
     *
     * @param name the name of the preference this field editor works on
     * @param labelText the label text of the field editor
     * @param parent the parent of the field editor's control
     */
    public MacApplicationFieldEditor(String name, String labelText, Composite parent) {
        super(name, labelText, false, parent);

        setFileExtensions(FILE_EXTENSIONS);
    }


    @Override
    protected boolean checkState() {

        String msg = null;

        String path = getTextControl().getText();
        if (path != null) {
			path = path.trim();
		}
        else {
			path = "";
		}

        if (path.length() == 0) {
            if ( !isEmptyStringAllowed() ) {
				msg = getErrorMessage();
			}
        }
        else {
            File file = new File(path);
            if (!file.isDirectory()) {
                msg = "The selected file is not a directory!";
            }
            else if (!file.getName().endsWith(".app")) {
                msg = "The selected directory does not end with '.app'!";
            }
        }

        if (msg != null) { // error
            showErrorMessage(msg);
            return false;
        }

        if (doCheckState()) { // OK!
	        clearErrorMessage();
	        return true;
        }
        msg = getErrorMessage(); // subclass might have changed it in the #doCheckState()
        if (msg != null) {
            showErrorMessage(msg);
        }
    	return false;
    }

}
