package de.regasus.finance.export.ui.datev;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.widgets.Shell;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.finance.export.ui.Activator;
import de.regasus.finance.export.ui.IFinanceExportStarter;


public class DatevFinanceExportStarter implements IFinanceExportStarter {

	public static final String DATEV_FILE_PREFIX = "DTVF_";

	@Override
	public void openFinanceExportDialog(Shell shell) {
		try {
			// open specific dialog
			DatevFinanceExportDialog dialog = new DatevFinanceExportDialog(shell);
			int result = dialog.open();

			// start specific ExportJob
			if (result == IDialogConstants.OK_ID) {
				// start export
				DatevFinanceExportJob job = new DatevFinanceExportJob(
					dialog.isExportInvoices(),
					dialog.isExportPayments(),
					dialog.getBeginDate(),
					dialog.getEndDate(),
					dialog.getDirectoryPath()
				);
				job.setUser(true);
				job.schedule();
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}

}
