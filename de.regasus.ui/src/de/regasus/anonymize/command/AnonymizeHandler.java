package de.regasus.anonymize.command;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;

import de.regasus.anonymize.dialog.AnonymizeWizardDialog;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.ui.Activator;


public class AnonymizeHandler extends AbstractHandler {

	public static final String COMMAND_ID = "AnonymizeHandler";


	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			Shell shell = HandlerUtil.getActiveShellChecked(event);

			AnonymizeWizardDialog dialog = new AnonymizeWizardDialog(shell);

			dialog.create();
			dialog.open();
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}

		return null;
	}

}
