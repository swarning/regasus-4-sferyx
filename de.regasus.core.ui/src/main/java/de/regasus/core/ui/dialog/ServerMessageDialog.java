package de.regasus.core.ui.dialog;

import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;

import com.lambdalogic.i18n.I18NPattern;
import com.lambdalogic.i18n.I18NString;
import com.lambdalogic.messeinfo.kernel.ServerMessage;

import com.lambdalogic.util.rcp.UtilI18N;

public class ServerMessageDialog extends MessageDialog {

	/**
	 * Create the dialog
	 * @param parentShell
	 */
	public ServerMessageDialog(
		Shell parentShell,
		I18NString title,
		List<ServerMessage> serverMessages
	) {
		super(
			parentShell,		// dialogTitle
			title.getString(),	// dialogTitle
			null,				// dialogTitleImage
			null,				// dialogMessage
			MessageDialog.INFORMATION,		// dialogImageType
			new String[] {UtilI18N.OK},	//  dialogButtonLabels
			0					// defaultIndex
		);
		
		if (serverMessages != null && !serverMessages.isEmpty()) {
			I18NPattern allMessages = new I18NPattern();
			int i = 0;
			for (ServerMessage serverMessage : serverMessages) {
				i++;
				if (!allMessages.isEmpty()) {
					allMessages.append('\n');
				}
				if (serverMessages.size() > 1) {
					allMessages.append(i);
					allMessages.append(". "); 
				}
				allMessages.append(serverMessage.getText());
			}

			// set ClassLoader
			ClassLoader cl = ServerMessage.class.getClassLoader();
			allMessages.setClassLoader(cl);

			message = allMessages.getString(); // message
		}
	}

	
	public static void open(
		Shell parentShell,
		String title,
		List<ServerMessage> serverMessages
	) {
		String serverMessage = getServerMessageString(serverMessages);
		if (serverMessage != null) {
			MessageDialog.openInformation(parentShell, title, serverMessage);
		}
	}

	
	public static void open(
		Shell parentShell,
		List<ServerMessage> serverMessages
	) {
		String serverMessage = getServerMessageString(serverMessages);
		if (serverMessage != null) {
			MessageDialog.openInformation(parentShell, null, serverMessage);
		}
	}

	
	protected static String getServerMessageString(List<ServerMessage> serverMessages) {
		String message = null;
		if (serverMessages != null && !serverMessages.isEmpty()) {
			I18NPattern allMessages = new I18NPattern();
			int i = 0;
			for (ServerMessage serverMessage : serverMessages) {
				i++;
				if (!allMessages.isEmpty()) {
					allMessages.append('\n');
				}
				if (serverMessages.size() > 1) {
					allMessages.append(i);
					allMessages.append(". "); 
				}
				allMessages.append(serverMessage.getText());
			}

			message = allMessages.getString(); // message
		}
		return message;
	}

	/**
	 * Return the initial size of the dialog
	 */
//	@Override
//	protected Point getInitialSize() {
//		return new Point(500, 375);
//	}

}
