package de.regasus.impex.db.command;

import java.io.File;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

import de.regasus.core.error.RegasusErrorHandler;

import de.regasus.core.ui.jobs.JobDoneNotifier;
import de.regasus.impex.MasterDataExportSettings;
import de.regasus.impex.db.dialog.MasterDataExportDialog;
import de.regasus.impex.db.job.MasterDataExportJob;
import de.regasus.impex.ui.Activator;


public class MasterDataExportHandler extends AbstractHandler {
	
	public Object execute(ExecutionEvent event) throws ExecutionException {
		if (PlatformUI.getWorkbench().saveAllEditors(true)) {
			try {
				Shell shell = HandlerUtil.getActiveShell(event);
				MasterDataExportDialog dialog = new MasterDataExportDialog(shell);
				int result = dialog.open();
				
				if (result == Window.OK) {
					// do the export
					File file = dialog.getFile();
					MasterDataExportSettings settings = dialog.getMasterDataSettings();
					
					// load with progress monitor and possibility to cancel 
					MasterDataExportJob job = new MasterDataExportJob(settings, file);
					job.setUser(true);
					job.addJobChangeListener(new JobDoneNotifier());
					job.schedule();
				}
			}
			catch (Exception e) {
				RegasusErrorHandler.handleApplicationError(Activator.PLUGIN_ID, getClass().getName(), e);
			}
		}
		return null;
	}
	
}
