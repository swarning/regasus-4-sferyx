package de.regasus.impex.db.command;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

import de.regasus.core.error.RegasusErrorHandler;

import de.regasus.core.ui.jobs.JobDoneNotifier;
import de.regasus.impex.db.dialog.ImportDialog;
import de.regasus.impex.db.job.ImportJob;
import de.regasus.impex.ui.Activator;

public class ImportHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		if (PlatformUI.getWorkbench().saveAllEditors(true)) {
			try {
				Shell shell = HandlerUtil.getActiveShell(event);
				ImportDialog dialog = new ImportDialog(shell);
				int result = dialog.open();

				if (result == Window.OK) {
					// do the import
					ImportJob job = new ImportJob(
						dialog.getFile(),
						dialog.getImportMetadata(),
						dialog.getImportSettings()
					);
					job.setUser(true);
					job.addJobChangeListener( new JobDoneNotifier() );
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
