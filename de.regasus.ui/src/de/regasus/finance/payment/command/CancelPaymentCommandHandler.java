package de.regasus.finance.payment.command;

import java.util.Collection;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;

import com.lambdalogic.messeinfo.invoice.data.PaymentVO;
import com.lambdalogic.util.rcp.UtilI18N;

import de.regasus.I18N;
import de.regasus.finance.AccountancyModel;
import de.regasus.finance.command.AbstractFinanceCommandHandler;
import de.regasus.participant.editor.ParticipantEditor;

public class CancelPaymentCommandHandler extends AbstractFinanceCommandHandler {

	public static final String COMMAND_ID = "CancelPaymentCommand";


	@Override
	protected void execute(ExecutionEvent event, ParticipantEditor participantEditor)
	throws Exception {
		/* The FinanceSourceProvider and the plugin.xml make sure that there is precisely one uncancelled
		 * payment selected.
		 */

		Collection<PaymentVO> paymentVOs = participantEditor.getSelectedPaymentVOs();
		if (paymentVOs != null && paymentVOs.size() == 1) {
    		// extract the Payment
    		PaymentVO paymentVO = paymentVOs.iterator().next();

    		Shell shell = HandlerUtil.getActiveShell(event);

    		// check if Payment is not cancelled
    		if ( !paymentVO.isCanceled() ) {
    			// determine the right question text
    			String question = I18N.CancelPaymentQuestion;
    			if (paymentVO.getPaymentSystem() != null) {
    				// different text for electronic payments
    				question = I18N.CancelPaymentQuestionForElectronicPayents;
    			}

    			// open dialog to confirm the cancellation
    			boolean confirmed = MessageDialog.openConfirm(shell, UtilI18N.Confirm, question);
    			if (confirmed) {
    				// cancel Payment
    				AccountancyModel.getInstance().cancelPayment(paymentVO);
    			}
    		}
    		else {
    			MessageDialog.openInformation(
    				shell,
    				"Notice / Hinweis",
    				  "The payment is already cancelled."
    				+ "\nDie Zahlung ist bereits storniert."
    			);
    		}
		}
		else {
			System.err.print("Exactly one payment must be selected.");
		}
	}

}
