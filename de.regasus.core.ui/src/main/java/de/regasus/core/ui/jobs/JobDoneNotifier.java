package de.regasus.core.ui.jobs;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.dialogs.MessageDialog;

import com.lambdalogic.util.rcp.UtilI18N;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.core.ui.CoreI18N;

/**
 * This class can be used to indicate to the user whether a job was in fact cancelled, or did run successfully. The code
 * appears to be more complex than needed: Normally one would expect that the common Display.asyncExec is sufficient to
 * circumvent any thread problems. However, there were problems seemingly stemming from the fact the progress monitor
 * also opens a dialog, with the result that the MessageDialog didn't appear at all.
 *
 * @author manfred
 */
public class JobDoneNotifier extends JobChangeAdapter {

	@Override
	public void done(final IJobChangeEvent event) {
		new Thread(
			new Runnable() {
				@Override
				public void run() {
					showLater(event);
				}
			}
		).start();
	}


	private void showLater(final IJobChangeEvent event) {
		try {
			Thread.sleep(100);
		}
		catch (InterruptedException e) {
			com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
		}

		SWTHelper.asyncExecDisplayThread(new Runnable() {
			@Override
			public void run() {
//				System.out.println("JobDoneNotifier.run()");
				try {
					if (event.getResult().isOK()) {
//						System.out.println("JobDoneNotifier.run() isOK");

						MessageDialog.openConfirm(null, UtilI18N.Info, CoreI18N.JobDoneSuccessfully);
					}
					else if (event.getResult().matches(IStatus.CANCEL)) {
//						System.out.println("JobDoneNotifier.run() matches CANCEL");
						MessageDialog.openInformation(null, UtilI18N.Info, CoreI18N.JobCancelled);
					}
					else if (event.getResult().matches(IStatus.WARNING)) {
//						System.out.println("JobDoneNotifier.run() matches WARNING");
						MessageDialog.openInformation(null, UtilI18N.Warning, CoreI18N.JobDoneWarning);
					}

					// Not needed, because workbench already shows an error dialog
					// else if (event.getResult().matches(IStatus.ERROR)) {
					// System.out.println("JobDoneNotifier.run() matches ERROR");
					// ErrorHandler.handleError(Activator.PLUGIN_ID, this.getClass().getName(),
					// event.getResult().getException());
					//
					// }
				}
				catch (Throwable e) {
					com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
				}
			}
		});
	}

}
