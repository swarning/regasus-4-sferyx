package de.regasus.finance.command;

import java.util.Collection;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;

import com.lambdalogic.messeinfo.invoice.data.InvoiceVO;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.finance.FinanceSourceProvider;
import de.regasus.finance.invoice.command.IInvoiceCommandHandler;
import de.regasus.finance.invoice.view.InvoiceSearchView;
import de.regasus.participant.editor.ParticipantEditor;
import de.regasus.ui.Activator;

abstract public class AbstractFinanceCommandHandler extends AbstractHandler {

	abstract protected void execute(ExecutionEvent event, ParticipantEditor participantEditor)
	throws Exception;


	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			IWorkbenchPart activePart = HandlerUtil.getActivePart(event);

			// In case the active part is the InvoiceSearchView
			if (activePart instanceof InvoiceSearchView && this instanceof IInvoiceCommandHandler) {
				InvoiceSearchView invoiceSearchView = (InvoiceSearchView) activePart;
				Collection<InvoiceVO> invoiceVOs = invoiceSearchView.getSelectedInvoiceVOs();

				((IInvoiceCommandHandler) this).execute(event, invoiceVOs);
			}
			else if (activePart instanceof ParticipantEditor) {
				ParticipantEditor participantEditor = (ParticipantEditor) activePart;
				execute(event, participantEditor);
			}


			/* After execution, the set of available commands may have changed, so update
			 * the FinanceSourceProvider.
			 * This is not nice, but the FinanceSourceProvider gets its information from
			 * GUI elements in the FinanceComposite. If the FinanceSourceProvider would
			 * listen to the ParticipantModel to get informed when changes happen, it is
			 * not sure, that the FinanceComposite has already the actual data, cause
			 * it is a listener of the ParticipantModel, too, but may be informed later.
			 * So the FinanceSourceProvider would get old informations from the FinanceComposite.
			 *
			 * The same problem with the same solution exists in CreateClearingAction.
			 */
			FinanceSourceProvider.getInstance().refreshVariables();
		}

		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
		return null;
	}

}
