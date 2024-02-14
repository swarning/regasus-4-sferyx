package de.regasus.finance.invoice.command;

import java.util.Collection;
import java.util.List;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;

import com.lambdalogic.messeinfo.invoice.data.InvoiceVO;
import com.lambdalogic.util.CollectionsHelper;
import com.lambdalogic.util.rcp.UtilI18N;
import com.lambdalogic.util.rcp.chunk.ChunkExecutor;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.I18N;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.finance.AccountancyModel;
import de.regasus.participant.editor.ParticipantEditor;
import de.regasus.ui.Activator;

/**
 * Asks one or two questions for confirmation and tells the server (via a model) to close the selected invoices.
 * <p>
 * See also https://lambdalogic.atlassian.net/browse/MIRCP-136
 */
public class CloseInvoiceCommandHandler extends AbstractInvoiceCommandHandler {

	public static final String COMMAND_ID = "CloseInvoiceCommand";


	@Override
	public void execute(ExecutionEvent event, Collection<InvoiceVO> invoiceVOs) throws Exception {
		try {
			Shell shell = HandlerUtil.getActiveShell(event);

			// Check for how many invoices this action is applicable
			int selectedCount = invoiceVOs.size();
			int applicableCount = getApplicableCount(invoiceVOs);


			if (applicableCount > 0) {
				// let the user save unsaved ParticipantEditors, because they might get refreshed
				for (InvoiceVO invoiceVO : invoiceVOs) {
					Long recipientPK = invoiceVO.getRecipientPK();
					boolean editorSaveCkeckOK = ParticipantEditor.saveEditor(recipientPK);
					if (!editorSaveCkeckOK) {
						return;
					}
				}

				// Ask if it's OK to close unclosed invoices
				boolean confirmed = MessageDialog.openConfirm(
					shell,
					UtilI18N.Confirm,
					I18N.CloseInvoicesQuestion + getApplicableCountDetail(selectedCount, applicableCount)
				);
				if (confirmed) {
					List<InvoiceVO> applicableInvoiceVOs = getApplicableList(invoiceVOs);

					// Are any of the unclosed invoices zero invoices?
					boolean areZeroInvoices = false;
					for (InvoiceVO invoiceVO : applicableInvoiceVOs) {
						if (!invoiceVO.isClosed() && invoiceVO.isZeroAmount()) {
							areZeroInvoices = true;
							break;
						}
					}
					if (areZeroInvoices) {
						// Ask if it's OK to close zero amount invoices
						confirmed = MessageDialog.openConfirm(
							shell,
							UtilI18N.Confirm,
							de.regasus.finance.FinanceI18N.CloseZeroInvoicesQuestion
						);
						if (!confirmed) {
							// If not, abort
							return;
						}
					}

					executeInChunks(shell, applicableInvoiceVOs);
				}
			}
			else {
				MessageDialog.openInformation(
					shell,
					UtilI18N.Hint,
					I18N.ActionNotApplicableForAnySelectedInvoice
				);
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	public static boolean isApplicable(InvoiceVO invoiceVO) {
		boolean applicable = ! invoiceVO.isClosed();
		return applicable;
	}


	public static int getApplicableCount(Collection<InvoiceVO> invoiceVOs) {
		int applicable = 0;

		for (InvoiceVO invoiceVO : invoiceVOs) {
			if (isApplicable(invoiceVO)) {
				applicable++;
			}
		}

		return applicable;
	}


	public static List<InvoiceVO> getApplicableList(Collection<InvoiceVO> invoiceVOs) {
		List<InvoiceVO> applicableInvoiceVOs = CollectionsHelper.createArrayList(invoiceVOs.size());

		for (InvoiceVO invoiceVO : invoiceVOs) {
			if (isApplicable(invoiceVO)) {
				applicableInvoiceVOs.add(invoiceVO);
			}
		}

		return applicableInvoiceVOs;
	}


	private void executeInChunks (
		final Shell shell,
		final List<InvoiceVO> applicableInvoiceVOs
	) {
		final int[] counter = {0};

		ChunkExecutor<InvoiceVO> chunkExecutor = new ChunkExecutor<InvoiceVO>() {
			@Override
			protected void executeChunk(List<InvoiceVO> chunkList) throws Exception {
				// Now do the required closing
				AccountancyModel.getInstance().closeInvoices(chunkList);

				// add number of Invoices that have just been closed
				counter[0] = counter[0] + chunkList.size();
			}


			@Override
			protected Collection<InvoiceVO> getItems() {
				return applicableInvoiceVOs;
			}
		};

		// set operation message
		String operationMessage = I18N.CloseInvoiceCommandHandler_OperationMessage;
		operationMessage = operationMessage.replaceFirst("<count>", String.valueOf(applicableInvoiceVOs.size()));
		chunkExecutor.setOperationMessage(operationMessage);

		chunkExecutor.executeInChunks();

		SWTHelper.syncExecDisplayThread(new Runnable() {
			@Override
			public void run() {
				try{
					// Show dialog that tells the user how many Participants have been updated
					String title = I18N.CloseInvoiceCommandHandler_Title;
					String message = I18N.CloseInvoiceCommandHandler_FinalMessage;
					message = message.replaceFirst("<count>", String.valueOf(counter[0]));

					MessageDialog.openInformation(shell, title, message);
				}
				catch (Exception e) {
					RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
				}
			}
		});
	}

}
