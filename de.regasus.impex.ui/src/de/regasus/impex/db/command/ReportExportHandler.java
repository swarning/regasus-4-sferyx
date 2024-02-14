package de.regasus.impex.db.command;

import java.io.File;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

import com.lambdalogic.messeinfo.report.data.UserReportDirVO;
import de.regasus.core.error.RegasusErrorHandler;

import de.regasus.core.ui.jobs.JobDoneNotifier;
import de.regasus.impex.db.dialog.ReportExportDialog;
import de.regasus.impex.db.job.ReportExportJob;
import de.regasus.impex.ui.Activator;
import de.regasus.report.ReportSelectionHelper;


public class ReportExportHandler extends AbstractHandler {
	
	public Object execute(ExecutionEvent event) throws ExecutionException {
		if (PlatformUI.getWorkbench().saveAllEditors(true)) {
			try {
				UserReportDirVO userReportDirVO = ReportSelectionHelper.getUserReportDirVO(event);
				
				Shell shell = HandlerUtil.getActiveShell(event);
				ReportExportDialog dialog = new ReportExportDialog(shell);
				dialog.setUserReportDirVO(userReportDirVO);
				int result = dialog.open();
				
				if (result == Window.OK) {
					// do the export
					File file = dialog.getFile();
					
					// load with progress monitor and possibility to cancel 
					ReportExportJob job = new ReportExportJob(userReportDirVO.getID(), file);
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
