package de.regasus.finance.invoice.command;

import org.eclipse.core.commands.ExecutionEvent;

import de.regasus.I18N;
import de.regasus.finance.command.AbstractFinanceCommandHandler;
import de.regasus.participant.editor.ParticipantEditor;

/**
 * Subclasses hereof may be called from when invoices are selected in the invoice search view and when
 * lots of things are selected in the participant editor's finance composite. In the latter case, only
 * the event and the selected invoices are handed to the actual execute method.
 */
abstract public class AbstractInvoiceCommandHandler
extends AbstractFinanceCommandHandler
implements IInvoiceCommandHandler{

	@Override
	protected void execute(ExecutionEvent event, ParticipantEditor participantEditor)
	throws Exception {
		execute(event, participantEditor.getSelectedInvoiceVOs());
	}


	protected String getApplicableCountDetail(int selectedCount, int applicableCount) {
		if (selectedCount == applicableCount || selectedCount == 1) {
			return "";
		}

		String message = I18N.ActionIsApplicableForMofNSelectedInvoices;
		message = message.replace("<m>", String.valueOf(applicableCount));
		message = message.replace("<n>", String.valueOf(selectedCount));

		return "\n\n" + message;
	}

}
