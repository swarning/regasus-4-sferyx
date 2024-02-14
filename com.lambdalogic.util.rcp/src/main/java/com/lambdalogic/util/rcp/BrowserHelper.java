package com.lambdalogic.util.rcp;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class BrowserHelper {

	public static void openBrowser(String url) {
		URI uri = URI.create(url);

		Desktop desktop = java.awt.Desktop.getDesktop();

		try {
			desktop.browse(uri);
		}
		catch (IOException e) {
			Shell shell = Display.getDefault().getActiveShell();
			String message = UtilI18N.ErrorOpenBrowser.replace("<url>", url);
			MessageDialog.openError(shell, UtilI18N.ErrorInfo, message);
		}
	}

}
