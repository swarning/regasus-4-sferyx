package de.regasus.impex.ods.command;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

import com.lambdalogic.util.rcp.CustomWizardDialog;
import de.regasus.core.error.RegasusErrorHandler;

import com.lambdalogic.util.rcp.UtilI18N;
import de.regasus.impex.ods.dialog.ODSProfileImportWizard;
import de.regasus.impex.ui.Activator;

public class ODSProfileImportHandler extends AbstractHandler {

	public Object execute(ExecutionEvent event) throws ExecutionException {
		if (PlatformUI.getWorkbench().saveAllEditors(true)) {
			try {
				ODSProfileImportWizard wizard = new ODSProfileImportWizard();
				
				CustomWizardDialog dialog = new CustomWizardDialog(HandlerUtil.getActiveShell(event), wizard);
				dialog.setFinishButtonText(UtilI18N.Import);
				dialog.open();
			}
			catch (Exception e) {
				RegasusErrorHandler.handleApplicationError(Activator.PLUGIN_ID, getClass().getName(), e);
			}
		}
		return null;
	}

}
