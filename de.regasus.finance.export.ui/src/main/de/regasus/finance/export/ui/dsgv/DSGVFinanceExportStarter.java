package de.regasus.finance.export.ui.dsgv;

import java.io.File;
import java.util.Collection;
import java.util.Iterator;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;

import com.lambdalogic.messeinfo.invoice.InvoiceLabel;
import com.lambdalogic.messeinfo.invoice.data.InvoiceNoRangeCVO;
import com.lambdalogic.util.CollectionsHelper;
import com.lambdalogic.util.StringHelper;

import de.regasus.core.PropertyModel;
import de.regasus.core.error.RegasusErrorHandler;
import com.lambdalogic.util.rcp.UtilI18N;
import de.regasus.finance.InvoiceNoRangeModel;
import de.regasus.finance.export.ui.Activator;
import de.regasus.finance.export.ui.IFinanceExportStarter;


public class DSGVFinanceExportStarter implements IFinanceExportStarter {

	public static final String PROPERTY_KEY_SAP_EXPORT_DIR = "sap.export.dir";
	
	
	@Override
	public void openFinanceExportDialog(Shell shell) {
		System.out.println("Starting export of unclosed invoices to a file in SAP specific format");

		try {
			// find out wether a PROPERTY_KEY_SAP_EXPORT_DIR is configures in server properties
			String exportDirName = PropertyModel.getInstance().getPropertyValue(PROPERTY_KEY_SAP_EXPORT_DIR);
			if (StringHelper.isNotEmpty(exportDirName)) {
				// if export dir does not exist, create it
				File exportDir = new File(exportDirName);
				if (! exportDir.exists()) {
					exportDir.mkdirs();
				}
				
				if (exportDir.exists()) {
    				// open specific dialog: here to choose an invoice number range
    				InvoiceNoRangeCVO invoiceNoRangeCVO = openInvoiceNoRangeSelectionDialog(shell);
    				if (invoiceNoRangeCVO != null) {
    					// start specific ExportJob
						DsgvFinanceExportJob job = new DsgvFinanceExportJob(
							invoiceNoRangeCVO.getVO(),
							exportDir
						);
						job.setUser(true);
						job.schedule();
    				}
				}
				else {
					String message = DSGVI18N.ExportDirDoesNotExistAndCouldNotBeCreated.replace("<name>", exportDirName);
					MessageDialog.openError(shell, UtilI18N.Error, message);
				}
			}
			else {
				MessageDialog.openError(shell, UtilI18N.Error, DSGVI18N.NoConfiguredExportDir);
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}

	
	private InvoiceNoRangeCVO openInvoiceNoRangeSelectionDialog(Shell shell) throws Exception {
		InvoiceNoRangeModel invoiceNoRangeModel = InvoiceNoRangeModel.getInstance();
		Collection<InvoiceNoRangeCVO> allInvoiceNoRangeCVOs = invoiceNoRangeModel.getAllInvoiceNoRangeCVOs();
		allInvoiceNoRangeCVOs = CollectionsHelper.createArrayList(allInvoiceNoRangeCVOs);
		
		// remove Invoice Number Ranges that are not exportable
		for (Iterator<InvoiceNoRangeCVO> it = allInvoiceNoRangeCVOs.iterator(); it.hasNext();) {
			InvoiceNoRangeCVO invoiceNoRangeCVO = it.next();
			if (!invoiceNoRangeCVO.getVO().isExportable()) {
				it.remove();
			}
		}

		// Prepare the dialog to select or enter a function
		ElementListSelectionDialog listDialog = new ElementListSelectionDialog(shell, new InvoiceNumberRangeCVOLabelProvider());
		listDialog.setTitle(InvoiceLabel.InvoiceNoRange.getString());
		listDialog.setMessage(InvoiceLabel.InvoiceNoRange.getString());
		listDialog.setElements(allInvoiceNoRangeCVOs.toArray());

		int code = listDialog.open();
		if (code == Window.OK) {
			// Put the selected or entered function back to the text widget
			Object object = listDialog.getFirstResult();
			return (InvoiceNoRangeCVO) object;
		}
		return null;
	}

}
