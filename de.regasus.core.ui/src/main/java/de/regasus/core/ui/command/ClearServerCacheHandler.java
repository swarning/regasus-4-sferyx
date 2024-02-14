package de.regasus.core.ui.command;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.handlers.HandlerUtil;

import com.lambdalogic.util.rcp.BusyCursorHelper;

import de.regasus.core.ServerModel;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.Activator;
import de.regasus.core.ui.CoreI18N;

public class ClearServerCacheHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		boolean confirmed = MessageDialog.openConfirm(
			HandlerUtil.getActiveShellChecked(event),
			CoreI18N.ClearServerCacheConfirmationTitle,
			CoreI18N.ClearServerCacheConfirmationMessage
		);

		if (confirmed) {
    		BusyCursorHelper.busyCursorWhile(new Runnable() {
    			@Override
    			public void run() {
    				try {

						ServerModel.getInstance().clearServerCache();
    				}
    				catch (Exception e) {
    					RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
    				}
    			}
    		});
   		}

		return null;
	}

}
