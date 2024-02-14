package de.regasus.finance.payment.command;

import static de.regasus.LookupService.getPaymentMgr;

import java.util.Collection;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;

import com.lambdalogic.messeinfo.invoice.data.PaymentVO;
import com.lambdalogic.report.DocumentContainer;

import de.regasus.common.dialog.FormatDialog;
import de.regasus.finance.command.AbstractFinanceCommandHandler;
import de.regasus.participant.editor.ParticipantEditor;

public class ShowPaymentReceiptCommandHandler extends AbstractFinanceCommandHandler {

	public static final String COMMAND_ID = "ShowPaymentReceiptCommand";


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
    			FormatDialog formatDialog = new FormatDialog(HandlerUtil.getActiveShell(event));
    			int code = formatDialog.open();
    			if (Window.OK == code) {
    				/* This code is referenced by
    				 * https://lambdalogic.atlassian.net/wiki/display/REGASUS/How+to+open+and+print+documents+in+the+Regasus+RCP+client
    				 * Adapt the wiki document if this code is moved to another class or method.
    				 */

    				// save and open generated payment receipt file
    				String format = formatDialog.getFormat();
    				DocumentContainer dc = getPaymentMgr().getPaymentReceipt(paymentVO.getPK(), null, format);
    				dc.open();
    			}
    		}
    		else {
    			MessageDialog.openInformation(
    				shell,
    				"Notice / Hinweis",
    				  "A receipt cannot be generated, because the payment is cancelled."
    				+ "\nEin Beleg kann nicht generiert werden, weil die Zahlung storniert ist."
    			);
    		}
		}
		else {
			System.err.print("Exactly one payment must be selected.");
		}
	}

}
