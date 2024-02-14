package de.regasus.core.ui.command;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.Activator;
import de.regasus.core.ui.dialog.CurrentlyUsedLicencesDialog;

public class ViewCurrentlyUsedLicencesCommandHandler extends AbstractHandler implements IHandler {

	public Object execute(ExecutionEvent event) throws ExecutionException {
		
		try {
			Shell shell = HandlerUtil.getActiveShell(event);

			CurrentlyUsedLicencesDialog dialog = new CurrentlyUsedLicencesDialog(shell);
			dialog.create();
			dialog.getShell().setSize(800, 600);
			dialog.open();
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
		
		return null;
	}

}
