package de.regasus.core.ui.command;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.handlers.HandlerUtil;

import de.regasus.core.ServerModel;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.Activator;

public class RunDatabaseUpdateBeansHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		boolean confirmed = MessageDialog.openConfirm(
			HandlerUtil.getActiveShellChecked(event),
			"Procedural Database Updates",
			  "This command triggers the procedural database updates."
			+ "\nIt should only be executed if a procedural database update fails after a server has been updated."
			+ "\nIf this happens, do the following:"
			+ "\nSet the field OK of the VERSION record of the affected update to TRUE."
			+ "\nStart the server (which does not run the update code, because OK is TRUE)."
			+ "\nSet the field OK of the VERSION record of the affected update to FALSE."
			+ "\nCall this command."
		);

		if (confirmed) {
    		try {
    			ServerModel.getInstance().runDatabaseUpdateBeans();
    		}
    		catch (Exception e) {
    			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
    		}

    		MessageDialog.openInformation(
    			HandlerUtil.getActiveShellChecked(event),
    			"Procedural Database Updates",
    			"Finished!"
    		);
		}

		return null;
	}

}
