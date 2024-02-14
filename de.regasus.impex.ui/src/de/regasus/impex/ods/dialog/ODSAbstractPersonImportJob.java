package de.regasus.impex.ods.dialog;

import java.io.File;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Shell;

import com.lambdalogic.i18n.I18NString;
import com.lambdalogic.messeinfo.impex.ODSAbstractPersonImportHelper;
import com.lambdalogic.messeinfo.impex.ODSImportCallback;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.model.Activator;
import com.lambdalogic.util.rcp.UtilI18N;
import de.regasus.impex.ImpexI18N;

public class ODSAbstractPersonImportJob extends Job {

	private ODSAbstractPersonImportHelper helper;
	private int maxErrors;
	private boolean success;
	private Shell shell;
	private String entities;


	public ODSAbstractPersonImportJob(
		ODSAbstractPersonImportHelper helper,
		int maxErrors,
		Shell shell,
		String name,
		String entities
	) {
		super(name);
		this.helper = helper;
		this.maxErrors = maxErrors;
		this.shell = shell;
		this.entities = entities;
	}


	@Override
	protected IStatus run(final IProgressMonitor monitor) {
		try {
			ODSImportCallback callback = new ODSImportCallback() {
				private int totalWork = 0;
				private int workDone = 0;

				@Override
				public void beginTask(I18NString name, int totalWork) {
					this.totalWork = totalWork;

					String progressName = null;
					if (name != null) {
						progressName = name.getString();
					}

					monitor.beginTask(progressName, totalWork);
				}

				@Override
				public void worked(int work) {
					workDone += work;

					monitor.worked(work);

					monitor.subTask(
						new StringBuilder(64)
						.append(workDone)
						.append(" ")
						.append(UtilI18N._of_)
						.append(" ")
						.append(totalWork)
						.toString()
					);
				}

				@Override
				public boolean isCanceled() {
					return monitor.isCanceled();
				}
			};
			helper.setCallback(callback);


			success = helper.importData(maxErrors);


			if (monitor.isCanceled()) {
				monitor.done();
				showResultsInDisplayThread();

				return Status.CANCEL_STATUS;
			}

		}
		catch (Exception e) {
			monitor.done();
			showResultsInDisplayThread();

			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			return Status.CANCEL_STATUS;
		}

		monitor.done();
		showResultsInDisplayThread();

		return Status.OK_STATUS;
	}


	private void showResultsInDisplayThread() {
		SWTHelper.syncExecDisplayThread(new Runnable() {
			@Override
			public void run() {
				showResults();
			}
		});
	}


	private void showResults() {
		int created = helper.getCreateCount();
		int updated = helper.getUpdateCount();
		int errors = helper.getErrorCount();
		int duplicates = helper.getDuplicateCount();
		File errorFile = helper.getErrorFile();

		if (success) {
			String stats = ImpexI18N.ODSImport_Statistics;
			stats = stats.replace("<entities>", entities);
			stats = stats.replace("<created>", String.valueOf(created));
			stats = stats.replace("<updated>", String.valueOf(updated));

			String message = ImpexI18N.ODSImportAction_Completed + "\n\n" + stats;

			MessageDialog.openInformation(shell, UtilI18N.Info, message);
		}
		else if (errorFile != null) {
			/* Show the dialog with statistics only is an error file exists.
			 * Otherwise the import did not even start yet due to a basic exception like unknown field in table header.
			 */
			String stats = ImpexI18N.ODSImport_StatisticsWithErrors;
			stats = stats.replace("<entities>", entities);
			stats = stats.replace("<created>", String.valueOf(created));
			stats = stats.replace("<updated>", String.valueOf(updated));

			stats = stats.replace("<errors>", String.valueOf(errors));
			stats = stats.replace("<duplicates>", String.valueOf(duplicates));

			String errorFileName = errorFile.getName();
			stats = stats.replace("<ErrorFileName>", errorFileName);

			String message = ImpexI18N.ODSImportAction_Completed + "\n\n" + stats;

			// Show a dialog with a button to optionally open the error file
			MessageDialog dialog = new MessageDialog(shell, UtilI18N.Warning, null, message, MessageDialog.WARNING,
					new String[] { UtilI18N.OpenVerb, UtilI18N.OK }, 0);

			// Open the file if desired
			int result = dialog.open();
			if (result == 0) {
				Program.launch(errorFile.getAbsolutePath());
			}
		}
	}

}
