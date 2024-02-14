package de.regasus.core.ui.command;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import com.lambdalogic.util.rcp.BusyCursorHelper;

import de.regasus.core.ServerModel;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.Activator;

/**
 * This handler just calls refreshAll in the ServerModel, so that all registered models update their data.
 */
public class RefreshAllHandler extends AbstractHandler {

	public Object execute(ExecutionEvent event) throws ExecutionException {
		BusyCursorHelper.busyCursorWhile(new Runnable() {
			public void run() {
				try {
					ServerModel.getInstance().refreshAll();
				}
				catch (Exception e) {
					RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
				}
			}
		});
		return null;
	}

}
