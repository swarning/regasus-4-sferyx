package de.regasus.impex.db.command;

import java.io.File;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

import com.lambdalogic.messeinfo.participant.data.EventVO;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.jobs.JobDoneNotifier;
import de.regasus.event.EventSelectionHelper;
import de.regasus.impex.EventExportSettings;
import de.regasus.impex.db.dialog.EventExportDialog;
import de.regasus.impex.db.job.EventExportJob;
import de.regasus.impex.ui.Activator;


public class EventExportHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		if (PlatformUI.getWorkbench().saveAllEditors(true)) {
			try {
				Long initialEventPK = EventSelectionHelper.getEventID(event);

				Shell shell = HandlerUtil.getActiveShell(event);
				EventExportDialog dialog = new EventExportDialog(shell);
				dialog.setInitiallySelectedEvent(initialEventPK);
				int result = dialog.open();

				if (result == Window.OK) {
					// do the export
					EventVO eventVO = dialog.getEventVO();
					File file = dialog.getFile();
					EventExportSettings settings = dialog.getEventExportSettings();

					// load with progress monitor and possibility to cancel
					EventExportJob job = new EventExportJob(eventVO.getID(), settings, file);
					job.setUser(true); // show a pop-up progress dialog if the job runs for more than a few seconds
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
