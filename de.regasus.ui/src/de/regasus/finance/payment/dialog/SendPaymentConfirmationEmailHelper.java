package de.regasus.finance.payment.dialog;

import static de.regasus.LookupService.getEmailDispatchOrderMgr;
import static com.lambdalogic.util.CollectionsHelper.notEmpty;

import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;

import com.lambdalogic.i18n.I18NPattern;
import com.lambdalogic.messeinfo.email.EmailDispatch;
import com.lambdalogic.messeinfo.email.EmailDispatchOrder;
import com.lambdalogic.messeinfo.email.EmailMessage;
import com.lambdalogic.messeinfo.invoice.InvoiceMessage;
import com.lambdalogic.messeinfo.invoice.data.PaymentVO;
import com.lambdalogic.messeinfo.participant.Participant;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.core.error.RegasusErrorHandler;
import com.lambdalogic.util.rcp.UtilI18N;
import de.regasus.email.EmailDispatchModel;
import de.regasus.participant.ParticipantModel;
import de.regasus.ui.Activator;


public class SendPaymentConfirmationEmailHelper {

	private static EmailDispatchModel emailDispatchModel = EmailDispatchModel.getInstance();
	private static ParticipantModel participantModel = ParticipantModel.getInstance();


	/**
	 * Send an email that is based on the data of the PaymentVO and the EmailTemplate.
	 * The recipient of the email is the payer of the Payment.
	 * The PaymentVO is set as additional variable "payment".
	 *
	 * The email is sent asynchronously in a separate thread. If an error occurs, an error dialog is
	 * shown (asynchronously).
	 *
	 * @param paymentVO
	 * @param emailTemplateID
	 */
	public static void sendPaymentConfirmationEmail(final PaymentVO paymentVO, final Long emailTemplatePK) {
		// MIRCP-2158 - Send email confirmations upon (manual and payengine) payments
		if (paymentVO == null) {
			throw new IllegalArgumentException("Parameter 'paymentVO' must not be null.");
		}
		if (emailTemplatePK == null) {
			throw new IllegalArgumentException("Parameter 'emailTemplatePK' must not be null.");
		}

		if (paymentVO.getAmount().signum() == 0) {
			MessageDialog.openInformation(
				Display.getDefault().getActiveShell(),
				UtilI18N.Info,
				EmailMessage.NoEmailsForPaymentsWithZeroAmount.getString()
			);
			return;
		}

		if (paymentVO.isCanceled()) {
			MessageDialog.openInformation(
				Display.getDefault().getActiveShell(),
				UtilI18N.Info,
				EmailMessage.NoEmailsForCancelledPayments.getString()
			);
			return;
		}

		try {
			// send payment confirmation email asynchronously
			new Thread() {
				@Override
				public void run() {
					try {
						EmailDispatchOrder emailDispatchOrder = getEmailDispatchOrderMgr().createDispatchOrderAndDispatchesForPayment(
							paymentVO.getID(),
							emailTemplatePK
						);

						if (emailDispatchOrder.getSuccessCount() == 0) {
							// load failed EmailDispatch to get error message
							final StringBuilder errorMessage = new StringBuilder();
							Long emailDispatchOrderID = emailDispatchOrder.getID();
							List<EmailDispatch> emailDispatchList =
								emailDispatchModel.getEmailDispatchesByEmailDispatchOrder(emailDispatchOrderID);

							if (notEmpty(emailDispatchList)) {
								errorMessage.append(emailDispatchList.get(0).getErrorMessage());
							}

							// load Participant
							final Participant participant = participantModel.getParticipant(
								paymentVO.getPayerPK()
							);

							SWTHelper.syncExecDisplayThread(new Runnable() {
								@Override
								public void run() {
									I18NPattern message = new I18NPattern();
									message.add(InvoiceMessage.SendPaymentConfirmationErrorMessage);
									message.putReplacement("<name>", participant.getName());
									message.putReplacement("<number>", participant.getNumber());
									message.putReplacement("<error>", errorMessage.toString());

									MessageDialog.openError(
										Display.getDefault().getActiveShell(),
										UtilI18N.Error,
										message.getString()
									);
								}
							});
						}
					}
					catch (Exception e) {
						RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
					}
				}
			}.start();


		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, SendPaymentConfirmationEmailHelper.class.getName(), e);
		}
	}

}
