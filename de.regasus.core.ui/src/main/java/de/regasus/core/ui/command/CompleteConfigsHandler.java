package de.regasus.core.ui.command;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import de.regasus.core.ConfigModel;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.Activator;

public class CompleteConfigsHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			ConfigModel.getInstance().completeConfigs();
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}

		return null;
	}

}
