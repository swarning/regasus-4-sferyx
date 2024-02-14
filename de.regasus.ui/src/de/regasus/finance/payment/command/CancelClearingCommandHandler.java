package de.regasus.finance.payment.command;

import java.util.ArrayList;
import java.util.Collection;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.handlers.HandlerUtil;

import com.lambdalogic.messeinfo.invoice.data.ClearingVO;

import de.regasus.I18N;
import com.lambdalogic.util.rcp.UtilI18N;
import de.regasus.finance.AccountancyModel;
import de.regasus.finance.command.AbstractFinanceCommandHandler;
import de.regasus.participant.editor.ParticipantEditor;

public class CancelClearingCommandHandler extends AbstractFinanceCommandHandler {

	@Override
	protected void execute(ExecutionEvent event, ParticipantEditor participantEditor)
	throws Exception {
		Collection<ClearingVO> paymentClearingVOs = participantEditor.getSelectedPaymentClearingVOs();
		Collection<ClearingVO> ipClearingVOs = participantEditor.getSelectedInvoicePositionClearingVOs();
		Collection<ClearingVO> clearingVOs = new ArrayList<>(paymentClearingVOs.size() + ipClearingVOs.size());
		clearingVOs.addAll(paymentClearingVOs);
		clearingVOs.addAll(ipClearingVOs);

		if (! clearingVOs.isEmpty()) {
			boolean confirm = MessageDialog.openConfirm(
				HandlerUtil.getActiveShell(event),
				UtilI18N.Confirm,
				I18N.CancelClearingsQuestion
			);

			if (confirm) {
				Long participantPK = participantEditor.getParticipantPK();
				AccountancyModel.getInstance().deleteClearings(participantPK, clearingVOs);
			}
		}
	}

}
