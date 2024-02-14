package de.regasus.core.ui.command;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

import com.lambdalogic.messeinfo.config.ConfigScope;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.Activator;
import de.regasus.core.ui.editor.config.ConfigEditor;
import de.regasus.core.ui.editor.config.ConfigEditorInput;

public class EditGlobalCustomerConfigCommandHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			ConfigEditorInput input = ConfigEditorInput.getInstance(ConfigScope.GLOBAL_CUSTOMER, null /*key*/);
			IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
			window.getActivePage().openEditor(input, ConfigEditor.ID);
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}

		return null;
	}

}
