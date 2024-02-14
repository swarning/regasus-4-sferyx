package de.regasus.finance.payment.command;

import static com.lambdalogic.util.CollectionsHelper.notEmpty;

import java.util.Collection;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;

import com.lambdalogic.messeinfo.invoice.data.InvoicePositionVO;
import com.lambdalogic.messeinfo.invoice.data.InvoiceVO;
import com.lambdalogic.messeinfo.invoice.data.PaymentVO;
import com.lambdalogic.messeinfo.participant.Participant;
import com.lambdalogic.messeinfo.participant.data.EventVO;
import com.lambdalogic.util.rcp.CustomWizardDialog;
import com.lambdalogic.util.rcp.UtilI18N;

import de.regasus.I18N;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.editor.AbstractEditor;
import de.regasus.event.EventModel;
import de.regasus.finance.PaymentSystem;
import de.regasus.finance.command.AbstractFinanceCommandHandler;
import de.regasus.finance.datatrans.dialog.DatatransPaymentWizard;
import de.regasus.finance.paymentsystem.dialog.PayEnginePaymentWizard;
import de.regasus.participant.editor.ParticipantEditor;
import de.regasus.ui.Activator;


public class ChargePaymentCommandHandler extends AbstractFinanceCommandHandler {

	public static final String COMMAND_ID = "ChargePaymentCommand";


	@Override
	protected void execute(ExecutionEvent event, ParticipantEditor participantEditor)
	throws Exception {
		if (!AbstractEditor.saveActiveEditor()) {
			return;
		}

		Collection<InvoiceVO> invoiceVOs = participantEditor.getSelectedInvoiceVOs();
		Collection<InvoicePositionVO> invoicePositionVOs = participantEditor.getSelectedInvoicePositionVOs();
		Collection<PaymentVO> paymentVOs = participantEditor.getSelectedPaymentVOs();
		Participant participant = participantEditor.getParticipant();
		Long eventPK = participantEditor.getEventId();

		// get Event to decide which PaymentSystem to use
		EventVO eventVO = EventModel.getInstance().getEventVO(eventPK);
		PaymentSystem paymentSystem = eventVO.getPaymentSystem();

		if (paymentSystem != null) {
			// Find out the currency that is to be suggested for the payment
			String currency = null;
			if (notEmpty(invoiceVOs)) {
				// If several invoices are selected, take the first one's currency
				InvoiceVO invoiceVO = invoiceVOs.iterator().next();
				currency = invoiceVO.getCurrency();
			}
			else if (notEmpty(paymentVOs)) {
				// If several payments are selected, take the first one's currency
				PaymentVO paymentVO = paymentVOs.iterator().next();
				currency = paymentVO.getCurrency();
			}
			else {
				// If no invoices and np payments are selected, take the event's default currency
				try {
					currency = eventVO.getProgPriceDefaultsVO().getCurrency();
				}
				catch (Exception e) {
					RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
				}
			}


			CustomWizardDialog wizardDialog = null;

			if (paymentSystem == PaymentSystem.DATATRANS) {
				DatatransPaymentWizard paymentWizard = null;
				if (notEmpty(invoicePositionVOs)) {
					paymentWizard = DatatransPaymentWizard.getPayInvoicePositionsInstance(
						currency,
						invoicePositionVOs,
						participant,
						eventVO
					);
				}
				else {
					paymentWizard = DatatransPaymentWizard.getPayInvoicesInstance(
						currency,
						invoiceVOs,
						participant,
						eventVO
					);
				}

				wizardDialog = new CustomWizardDialog(HandlerUtil.getActiveShell(event), paymentWizard);
				wizardDialog.setFinishButtonText(UtilI18N.Close);

				paymentWizard.setCustomWizardDialog(wizardDialog);

				wizardDialog.create();

				Point preferredSize = paymentWizard.getPreferredSize();
				wizardDialog.getShell().setSize(preferredSize.x, preferredSize.y);
			}
			else if (paymentSystem == PaymentSystem.PAYENGINE) {
				PayEnginePaymentWizard paymentWizard = null;
				if (notEmpty(invoicePositionVOs)) {
					paymentWizard = PayEnginePaymentWizard.getPayInvoicePositionsInstance(
						currency,
						invoicePositionVOs,
						participant,
						eventVO
					);
				}
				else {
					paymentWizard = PayEnginePaymentWizard.getPayInvoicesInstance(
						currency,
						invoiceVOs,
						participant,
						eventVO
					);
				}

				wizardDialog = new CustomWizardDialog(HandlerUtil.getActiveShell(event), paymentWizard);
				wizardDialog.setFinishButtonText(UtilI18N.Close);

				paymentWizard.setCustomWizardDialog(wizardDialog);

				wizardDialog.create();

				Point preferredSize = paymentWizard.getPreferredSize();
				wizardDialog.getShell().setSize(preferredSize.x, preferredSize.y);
			}
			else {
				Shell shell = HandlerUtil.getActiveWorkbenchWindow(event).getShell();
				MessageDialog.openError(shell, "Error", "Unknown payment system: " + paymentSystem);
			}

			wizardDialog.open();
		}
		else {
			Shell shell = HandlerUtil.getActiveWorkbenchWindow(event).getShell();
			MessageDialog.openError(shell, I18N.NoPaymentSystem_Title, I18N.NoPaymentSystem_Message);
		}
	}

}
