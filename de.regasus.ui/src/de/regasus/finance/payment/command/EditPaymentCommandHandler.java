package de.regasus.finance.payment.command;

import java.util.Collection;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;

import com.lambdalogic.messeinfo.invoice.data.PaymentVO;

import de.regasus.finance.command.AbstractFinanceCommandHandler;
import de.regasus.finance.payment.dialog.EditPaymentDialog;
import de.regasus.participant.editor.ParticipantEditor;

public class EditPaymentCommandHandler extends AbstractFinanceCommandHandler {

	public static final String COMMAND_ID = "EditPaymentCommand";


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
    			// open dialog to edit the Payment
    			EditPaymentDialog dialog = new EditPaymentDialog(shell, paymentVO);
    			dialog.open();
    		}
    		else {
    			MessageDialog.openInformation(
    				shell,
    				"Notice / Hinweis",
    				  "The payment is cancelled and cannot be edited."
    				+ "\nDie Zahlung ist storniert und kann nicht bearbeitet werden."
    			);
    		}
		}
		else {
			System.err.print("Exactly one payment must be selected.");
		}
	}

}
