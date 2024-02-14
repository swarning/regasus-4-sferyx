package de.regasus.finance.payment.command;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.eclipse.core.commands.ExecutionEvent;

import com.lambdalogic.messeinfo.invoice.data.ClearingVO;

import de.regasus.finance.AccountancyModel;
import de.regasus.finance.command.AbstractFinanceCommandHandler;
import de.regasus.participant.editor.ParticipantEditor;


public class CreateClearingCommandHandler extends AbstractFinanceCommandHandler {

	@Override
	protected void execute(ExecutionEvent event, ParticipantEditor participantEditor)
	throws Exception {
		Collection<ClearingVO> candidateClearingVOs = participantEditor.getSelectedClearingCandidates();

		if (candidateClearingVOs  != null) {
			Map<Long, Collection<Long>> payment2invoicePositionsMap = new HashMap<>();
			for (ClearingVO clearingVO : candidateClearingVOs) {
				Long paymentPK = clearingVO.getPaymentPK();
				Long invoicePositionPK = clearingVO.getInvoicePositionPK();

				Collection<Long> invoicePositionPKs = payment2invoicePositionsMap.get(paymentPK);
				if (invoicePositionPKs == null) {
					invoicePositionPKs = new HashSet<>();
					payment2invoicePositionsMap.put(paymentPK, invoicePositionPKs);
				}
				invoicePositionPKs.add(invoicePositionPK);
			}

			Long participantPK = participantEditor.getParticipantPK();
			AccountancyModel.getInstance().createClearing(participantPK, payment2invoicePositionsMap);
		}
	}

}
