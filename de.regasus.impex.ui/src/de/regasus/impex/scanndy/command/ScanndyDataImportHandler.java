package de.regasus.impex.scanndy.command;

import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

import com.lambdalogic.util.rcp.CustomWizardDialog;
import de.regasus.core.error.RegasusErrorHandler;

import com.lambdalogic.util.rcp.UtilI18N;
import de.regasus.event.EventSelectionHelper;
import de.regasus.impex.scanndy.dialog.ScanndyDataImportWizard;
import de.regasus.impex.ui.Activator;

public class ScanndyDataImportHandler extends AbstractHandler {

	public Object execute(ExecutionEvent event) throws ExecutionException {
		if (PlatformUI.getWorkbench().saveAllEditors(true)) {
			try {
				ScanndyDataImportWizard wizard = new ScanndyDataImportWizard();
				
				// init wizard with selected event
				List<Long> eventIDs = EventSelectionHelper.getEventIDs(event);
				if (eventIDs != null && eventIDs.size() == 1) {
					wizard.setEventPK(eventIDs.get(0));
				}

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
