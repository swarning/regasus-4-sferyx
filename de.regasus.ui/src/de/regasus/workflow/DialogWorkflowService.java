package de.regasus.workflow;

import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;

import com.lambdalogic.util.TypeHelper;

import com.lambdalogic.util.rcp.UtilI18N;
import de.regasus.core.ui.CoreI18N;

public class DialogWorkflowService {
	
	private static final String[] OK_CANCEL = {UtilI18N.OK, UtilI18N.Cancel};

	private Shell shell;

	private String title;

	private IInputValidator numberValidator = new IInputValidator() {
		public String isValid(String newText) {
			if (newText.matches("[0-9]+")) {
				return null;
			}
			return "";
		}
	};
	
	public DialogWorkflowService(Shell shell, String name) {
		this.shell = shell;
		this.title = CoreI18N.Config_OnsiteWorkflow + " - " + name;
	}
	
	
	public void showOK(Object o) {
		showOK(String.valueOf(o));
	}

	
	public void showOK(String message) {
		MessageDialog.openInformation(shell, title, message);
	}
	
	
	public boolean showOKCancel(String message) {
		MessageDialog dialog = new MessageDialog(
			shell,					// parentShell
			title,					// dialogTitle
			null,					// dialogTitleImage
			message,				// dialogMessage 
			MessageDialog.QUESTION, // dialogImageType
			OK_CANCEL,				// dialogButtonLabels
			1						// defaultIndex
		);
		int open = dialog.open();
		return open == 0;
	}

	
	public void showYesNo(Object o) {
		showYesNo(String.valueOf(o));
	}

	
	public boolean showYesNo(String message) {
		return MessageDialog.openQuestion(shell, title, message);
	}

	
	public String showInput(String message) {
		return showInput(message, "");
	}
	
	
	public String showInput(String message, Object currentValue) {
		String input = null;
		InputDialog dlg = new InputDialog(shell, title, message, TypeHelper.toString(currentValue), null);
		if (dlg.open() == Window.OK) {
			input = dlg.getValue();
		}

		return input;
	}

	
	public String showNumberInput(String message) {
		return showNumberInput(message, "");
	}
	

	public String showNumberInput(String message, Object currentValue) {
		String input = null;
		InputDialog dlg = new InputDialog(shell, title, message, TypeHelper.toString(currentValue), numberValidator);
		if (dlg.open() == Window.OK) {
			input = dlg.getValue();
		}

		return input;
	}
}
