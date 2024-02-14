package de.regasus.finance.export.ui.datev;

import static de.regasus.LookupService.getFinanceExportMgr;

import java.io.File;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import com.lambdalogic.messeinfo.invoice.FinanceExport;
import com.lambdalogic.time.I18NDate;
import com.lambdalogic.time.TimeFormatter;
import com.lambdalogic.util.FileHelper;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.model.Activator;


public class DatevFinanceExportJob extends Job {
	public static final String DATEV_FILE_PREFIX = "DTVF_";

	private static final TimeFormatter TIME_FORMATTER = TimeFormatter.getInstance("yyyy-MM-dd-HH-mm");

	private boolean exportInvoices;
	private boolean exportPayments;
	private I18NDate beginDate;
	private I18NDate endDate;
	private String dirPath;

	private File exportFile;

	private FinanceExport financeExport;


	public DatevFinanceExportJob(
		boolean exportInvoices,
		boolean exportPayments,
		I18NDate beginDate,
		I18NDate endDate,
		String dirPath
	) {
		super("DATEV-Finanzexport");

		this.exportInvoices = exportInvoices;
		this.exportPayments = exportPayments;
		this.beginDate = beginDate;
		this.endDate = endDate;
		this.dirPath = dirPath;
	}


	@Override
	protected IStatus run(IProgressMonitor monitor) {
		try {
			int totalWork = 0;
			totalWork += 1;		// create export on server
			totalWork += 1;		// load export data
			totalWork += 1;		// save export file

			monitor.beginTask("DATEV-Export", totalWork);


			// start export
			monitor.subTask("Exportiere Finanzdaten auf dem Server");
			final Integer exportNumber = getFinanceExportMgr().export(
				exportInvoices,
				exportPayments,
				beginDate,
				endDate,
				null	// invoiceNoRangePK
			);
			monitor.worked(1);


			if (exportNumber != null) {
    			monitor.subTask("Lade Finanzdaten vom Server");
    			financeExport = getFinanceExportMgr().find(exportNumber);
    			byte[] content = financeExport.getContent();
    			monitor.worked(1);


    			/***** save content *****/
    			monitor.subTask("Speicher Finanzdaten in Datei");
				// build file name
				String timestamp = TIME_FORMATTER.format(financeExport.getExportTime());
				String fileName = DATEV_FILE_PREFIX + "_Buchungsstapel_#" + exportNumber + "_" + timestamp + ".csv";

				// save content
				exportFile = new File(dirPath, fileName);
				System.out.println("Write export file: " + exportFile.getAbsolutePath());
				FileHelper.writeFile(exportFile, content);

				monitor.worked(1);
			}
			else {
				monitor.worked(2);
			}


			// show final message dialog
			SWTHelper.syncExecDisplayThread(new Runnable() {
				@Override
				public void run() {
					try {
						Shell shell = Display.getDefault().getActiveShell();

						if (financeExport != null) {
							String msg =
								"Der Finanzexport hat die Nummer " + financeExport.getExportNumber() + "." +
								"\nEs wurden " + financeExport.getInvoiceCount() + " Rechnungen und " +
									financeExport.getPaymentCount() + " Zahlungen exportiert." +
								"\nDie Exportdaten wurden in der Datei " + exportFile.getAbsolutePath() + " gespeichert." +
								"\nSoll die Exportdatei geöffnet werden?";

							boolean openFile = MessageDialog.openQuestion(shell, "DATEV-Export", msg);
							if (openFile) {
								// open export file (that has been saved before)
								Program.launch(exportFile.getAbsolutePath());
							}
						}
						else {
							MessageDialog.openInformation(shell, "DATEV-Finanzexport", "Es liegen keine zu exportierenden Daten vor.");
						}
					}
					catch (Exception e) {
						RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
					}
				}
			});
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			return Status.CANCEL_STATUS;
		}

		return Status.OK_STATUS;
	}

}
