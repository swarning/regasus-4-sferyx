package de.regasus.finance.payment.command;

import java.util.Collection;

import org.eclipse.core.commands.ExecutionEvent;

import com.lambdalogic.messeinfo.invoice.data.InvoicePositionVO;
import com.lambdalogic.messeinfo.participant.Participant;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.finance.AccountancyModel;
import de.regasus.finance.command.AbstractFinanceCommandHandler;
import de.regasus.participant.editor.ParticipantEditor;
import de.regasus.ui.Activator;


public class SetInvoicePositionsOffAgainstEachOtherCommandHandler extends AbstractFinanceCommandHandler {

	@Override
	protected void execute(ExecutionEvent event, ParticipantEditor participantEditor)
	throws Exception {
		try {
			Participant participant = participantEditor.getParticipant();
			Collection<InvoicePositionVO> invoicePositionVOs = participantEditor.getSelectedInvoicePositionVOs();

			AccountancyModel.getInstance().setInvoicePositionsOffAgainstEachOther(
				participant,
				invoicePositionVOs
			);
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}

}
