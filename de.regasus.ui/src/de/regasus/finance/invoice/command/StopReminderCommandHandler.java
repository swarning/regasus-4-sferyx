package de.regasus.finance.invoice.command;

import java.util.Collection;
import java.util.List;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;

import com.lambdalogic.messeinfo.invoice.data.InvoiceVO;
import com.lambdalogic.messeinfo.invoice.data.ReminderState;
import com.lambdalogic.util.CollectionsHelper;
import com.lambdalogic.util.rcp.chunk.ChunkExecutor;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.I18N;
import de.regasus.core.error.RegasusErrorHandler;
import com.lambdalogic.util.rcp.UtilI18N;
import de.regasus.finance.AccountancyModel;
import de.regasus.participant.editor.ParticipantEditor;
import de.regasus.ui.Activator;

public class StopReminderCommandHandler extends AbstractInvoiceCommandHandler {

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

				// Ask the user whether to stop the reminder process for the selected invoices
				boolean confirmed = MessageDialog.openConfirm(
					shell,
					UtilI18N.Question,
					I18N.StopReminderQuestion + getApplicableCountDetail(selectedCount, applicableCount)
				);
	
				// If yes, tell the server (via the model) to do so 
				if (confirmed) {
					List<InvoiceVO> applicableInvoiceVOs = getApplicableList(invoiceVOs);
					
					executeInChunks(shell, applicableInvoiceVOs);
				}
			}
			else {
				// This actually shouldn't happen when the FinanceSourceProvider enables the commands correctly
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
		boolean applicable =
			invoiceVO.isInvoice() && 
			invoiceVO.getPrint() != null && 
			invoiceVO.getNextReminder() != null &&
			invoiceVO.getReminderState() != ReminderState.STOP;
		
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
				// stop the remind state
				AccountancyModel.getInstance().stopReminder(chunkList);
				
				// add number of Invoices that have just been closed
				counter[0] = counter[0] + chunkList.size();
			}
	
			
			@Override
			protected Collection<InvoiceVO> getItems() {
				return applicableInvoiceVOs;
			}
		};
		
		// set operation message
		String operationMessage = I18N.StopReminderLevelCommandHandler_OperationMessage;
		operationMessage = operationMessage.replaceFirst("<count>", String.valueOf(applicableInvoiceVOs.size()));
		chunkExecutor.setOperationMessage(operationMessage);
		
		chunkExecutor.executeInChunks();
		
		SWTHelper.syncExecDisplayThread(new Runnable() {
			public void run() {
				try{						
					// Show dialog that tells the user how many Participants have been updated
					String title = I18N.StopReminderLevelCommandHandler_Title;
					String message = I18N.StopReminderLevelCommandHandler_FinalMessage;
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