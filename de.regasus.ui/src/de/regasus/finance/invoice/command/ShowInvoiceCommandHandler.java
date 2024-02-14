package de.regasus.finance.invoice.command;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

import com.lambdalogic.messeinfo.invoice.data.InvoiceVO;
import com.lambdalogic.report.DocumentContainer;
import com.lambdalogic.util.FileHelper;
import com.lambdalogic.util.exception.ErrorMessageException;
import com.lambdalogic.util.rcp.UtilI18N;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.finance.AccountancyModel;
import de.regasus.finance.FinanceI18N;
import de.regasus.finance.invoice.dialog.GenerateInvoiceDocumentsDialog;
import de.regasus.ui.Activator;

/**
 * Performs the needed actions when the user wants to print (or rather: open) one or more invoice documents.
 * <p>
 * See also https://lambdalogic.atlassian.net/browse/MIRCP-136
 */
public class ShowInvoiceCommandHandler extends AbstractInvoiceCommandHandler {

	public static final String COMMAND_ID = "ShowInvoiceCommand";


	@Override
	public void execute(ExecutionEvent event, Collection<InvoiceVO> invoiceVOs) throws Exception {

		/*
		 * The only supported formats for invoice templates are ODT and DOC.
		 * ODG is not supported yet.
		 * Reason:
		 * If the templates of the selected invoices have different formats, e.g. ODT and ODG,
		 * it is difficult to determine the available target formats.
		 * We would need the format of the templates to offer either the intersection of the
		 * supported target formats or ask the user for the target format of each template format.
		 */

		Shell parentShell = HandlerUtil.getActiveShell(event);

		// determine PKs of invoice recipients
		Set<Long> recipientPKs = InvoiceUtil.getRecipientPKs(invoiceVOs);


		// let the user save unsaved ParticipantEditors
		if ( ! InvoiceUtil.saveParticipantEditors(recipientPKs)) {
			return;
		}


		// Are there Invoices with an amount of zero?
		boolean zeroInvoices = false;
		for (InvoiceVO invoiceVO : invoiceVOs) {
			if (!invoiceVO.isClosed() && invoiceVO.isZeroAmount()) {
				zeroInvoices = true;
				break;
			}
		}
		if (zeroInvoices) {
			// Ask if it's OK to close zero amount invoices
			boolean confirmed = MessageDialog.openConfirm(
				parentShell,
				UtilI18N.Confirm,
				FinanceI18N.CloseZeroInvoicesQuestion
			);

			if (!confirmed) {
				// If not, abort
				return;
			}
		}


		// find out if at least 1 Invoice No Range has audit-proof accountancy turned off
		boolean showFormat = InvoiceUtil.hasAuditProofOff(invoiceVOs);
		GenerateInvoiceDocumentsDialog dialog = new GenerateInvoiceDocumentsDialog(parentShell, showFormat, invoiceVOs.size());
		boolean confirmed = dialog.open() == Window.OK;

		if (confirmed) {
			try {
				// Fetch documents from server, closing invoices if needed
				List<DocumentContainer> invoiceDocuments = AccountancyModel.getInstance().getInvoiceDocuments(
					invoiceVOs,
					dialog.getFormat(),
					dialog.isMerge()
				);

				// replace file name
				if (invoiceVOs.size() > 1 && dialog.isMerge() && invoiceDocuments.size() == 1) {
					String fileNamePattern = FinanceI18N.MultipleInvoiceDocumentFileNamePattern;
					String fileName = fileNamePattern.replace("<n>", String.valueOf(invoiceVOs.size()));
					invoiceDocuments.get(0).setBaseFileName(fileName);
				}

				// Show documents in selected format
				if (invoiceDocuments == null || invoiceDocuments.isEmpty()) {
					MessageDialog.openError(
						PlatformUI.getWorkbench().getDisplay().getActiveShell(),
						FinanceI18N.ShowInvoiceCommandHandler_ErrorTitle,
						FinanceI18N.ShowInvoiceCommandHandler_ErrorMessage
					);
				}
				else if (dialog.isSave()) {
					// save documents
					File directory = dialog.getFile();
					for (DocumentContainer invoiceDocument : invoiceDocuments) {
						File file = invoiceDocument.saveTo(directory);

						if (dialog.isShow()) {
							FileHelper.open(file);
						}

						if (dialog.isPrint()) {
							FileHelper.print(file, null /*printer*/);
						}
					}
				}
				else {
					if (dialog.isShow()) {
						for (DocumentContainer documentContainer : invoiceDocuments) {
							// save to temporary file and open it
							documentContainer.open();
						}
					}


					if (dialog.isPrint()) {
						for (DocumentContainer documentContainer : invoiceDocuments) {
							// save to temporary file and print it
							documentContainer.print();
						}
					}
				}

			}
			catch (ErrorMessageException e) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e, e.getMessage());
			}
		}
	}
}
