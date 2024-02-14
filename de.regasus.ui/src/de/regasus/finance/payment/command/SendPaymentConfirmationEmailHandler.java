package de.regasus.finance.payment.command;

import static com.lambdalogic.util.CollectionsHelper.notEmpty;

import java.util.Collection;
import java.util.List;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;

import com.lambdalogic.messeinfo.email.EmailTemplate;
import com.lambdalogic.messeinfo.email.EmailTemplateSystemRole;
import com.lambdalogic.messeinfo.invoice.data.PaymentVO;

import de.regasus.I18N;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.email.EmailTemplateModel;
import de.regasus.finance.command.AbstractFinanceCommandHandler;
import de.regasus.finance.payment.dialog.SendPaymentConfirmationDialog;
import de.regasus.participant.editor.ParticipantEditor;
import de.regasus.ui.Activator;


public class SendPaymentConfirmationEmailHandler extends AbstractFinanceCommandHandler {

	public static final String COMMAND_ID = "SendPaymentConfirmationEmailCommand";


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
        		Long eventPK = participantEditor.getEventId();

    			List<EmailTemplate> emailTemplates = null;
    			try {
    				// check if any EmailTemplate with EmailTemplateSystemRole.PAYMENT_RECEIVED exist
    				emailTemplates = EmailTemplateModel.getInstance().getEmailTemplateSearchDataByEvent(
    					eventPK,
    					EmailTemplateSystemRole.PAYMENT_RECEIVED
    				);
    			}
    			catch (Exception e) {
    				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
    			}


    			if ( notEmpty(emailTemplates) ) {
    				// Show dialog with EmailTemplates
    				SendPaymentConfirmationDialog dialog = new SendPaymentConfirmationDialog(shell, paymentVO);
    				dialog.open();
    			}
    			else {
    				// show Dialog that says, that no EmailTemplates with the purpose "Payment Confirmation" are available.
    				MessageDialog.openInformation(
    					HandlerUtil.getActiveShell(event),
    					I18N.SendPaymentConfimationEmailHandler_NoEmailTemplate_title,
    					I18N.SendPaymentConfimationEmailHandler_NoEmailTemplate_message
    				);
    			}
    		}
    		else {
    			MessageDialog.openInformation(
    				shell,
    				"Notice / Hinweis",
      				  "An email confirmation cannot be sent, because the payment is cancelled."
      				+ "\nEine Email-bestätigung kann nicht verschickt werden, weil die Zahlung storniert ist."
    			);
    		}
		}
		else {
			System.err.print("Exactly one payment must be selected.");
		}
	}

}
