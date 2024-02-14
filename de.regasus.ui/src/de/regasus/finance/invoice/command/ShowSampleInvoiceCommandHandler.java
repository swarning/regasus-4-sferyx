package de.regasus.finance.invoice.command;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.PlatformUI;

import com.lambdalogic.messeinfo.invoice.data.InvoiceVO;
import com.lambdalogic.report.DocumentContainer;
import com.lambdalogic.report.oo.OpenOfficeConstants;
import com.lambdalogic.util.exception.ErrorMessageException;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.finance.AccountancyModel;
import de.regasus.ui.Activator;
import de.regasus.users.CurrentUserModel;

public class ShowSampleInvoiceCommandHandler extends AbstractInvoiceCommandHandler {

	@Override
	public void execute(ExecutionEvent event, Collection<InvoiceVO> invoiceVOs) throws Exception {
		Objects.requireNonNull(invoiceVOs);

		// User can select only one Invoice
		if (invoiceVOs.size() != 1) {
			throw new IllegalArgumentException("Selection should include exactly one invoice.");
		}
		InvoiceVO invoiceVO = invoiceVOs.iterator().next();

		// determine PKs of invoice recipients
		Long recipientPK = invoiceVO.getRecipientPK();

		// let the user save unsaved ParticipantEditors
		if ( ! InvoiceUtil.saveParticipantEditors( Collections.singletonList(recipientPK) )) {
			return;
		}


		try {
			// Fetch documents from server, closing invoices if needed
			DocumentContainer invoiceDocument = AccountancyModel.getInstance().getSampleInvoiceDocument(
				invoiceVO,
				determineFileFormat(invoiceVO)
			);

			// Show documents in selected format
			if (invoiceDocument == null) {
				MessageDialog.openError(
					PlatformUI.getWorkbench().getDisplay().getActiveShell(),
					de.regasus.finance.FinanceI18N.ShowInvoiceCommandHandler_ErrorTitle,
					de.regasus.finance.FinanceI18N.ShowInvoiceCommandHandler_ErrorMessage
				);
			}

			// save to temporary file and open it
			invoiceDocument.open();
		}
		catch (ErrorMessageException e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e, e.getMessage());
		}
	}


	/**
	 * Check if the file format must be PDF.
	 * This is the case if audit-proof accountancy is turned on and the current user is not "admin".
	 *
	 * @param invoiceVO
	 * @return
	 * @throws Exception
	 */
	private String determineFileFormat(InvoiceVO invoiceVO) throws Exception {
		String formatKey = OpenOfficeConstants.FORMAT_KEY_PDF;

		boolean hasAuditProofOff = InvoiceUtil.hasAuditProofOff( Collections.singletonList(invoiceVO) );
		boolean isAdmin = CurrentUserModel.getInstance().isAdmin();

		if (hasAuditProofOff || isAdmin) {
			formatKey = OpenOfficeConstants.FORMAT_KEY_ODT;
		}

		return formatKey;
	}

}
