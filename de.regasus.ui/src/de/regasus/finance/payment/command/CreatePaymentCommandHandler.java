package de.regasus.finance.payment.command;

import static com.lambdalogic.util.CollectionsHelper.notEmpty;

import java.util.Collection;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.handlers.HandlerUtil;

import com.lambdalogic.messeinfo.invoice.data.InvoicePositionVO;
import com.lambdalogic.messeinfo.invoice.data.InvoiceVO;
import com.lambdalogic.messeinfo.invoice.data.PaymentVO;
import com.lambdalogic.messeinfo.participant.Participant;
import com.lambdalogic.messeinfo.participant.data.EventVO;
import com.lambdalogic.util.rcp.CustomWizardDialog;

import de.regasus.I18N;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.event.EventModel;
import de.regasus.finance.command.AbstractFinanceCommandHandler;
import de.regasus.finance.payment.dialog.CreatePaymentWizard;
import de.regasus.participant.editor.ParticipantEditor;
import de.regasus.ui.Activator;


public class CreatePaymentCommandHandler extends AbstractFinanceCommandHandler {

	public static final String COMMAND_ID = "CreatePaymentCommand";

	@Override
	protected void execute(ExecutionEvent event, ParticipantEditor participantEditor)
	throws Exception {
		if ( participantEditor.isDirty() ) {
			return;
		}

		Participant participant = participantEditor.getParticipant();
		Collection<InvoiceVO> invoiceVOs = participantEditor.getSelectedInvoiceVOs();
		Collection<InvoicePositionVO> invoicePositionVOs = participantEditor.getSelectedInvoicePositionVOs();
		Collection<PaymentVO> paymentVOs = participantEditor.getSelectedPaymentVOs();

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
			// If no invoices and no payments are selected, take the Event's default currency
			try {
				EventVO eventVO = EventModel.getInstance().getEventVO(participant.getEventId());
				currency = eventVO.getProgPriceDefaultsVO().getCurrency();
			}
			catch (Exception e) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			}
		}


		CreatePaymentWizard createPaymentWizard = null;
		if (notEmpty(invoicePositionVOs)) {
			createPaymentWizard = new CreatePaymentWizard(currency, invoicePositionVOs, participant, false, null);
		}
		else {
			createPaymentWizard = new CreatePaymentWizard(currency, invoiceVOs, participant, false);
		}

		CustomWizardDialog wizardDialog = new CustomWizardDialog(HandlerUtil.getActiveShell(event), createPaymentWizard);
		wizardDialog.setFinishButtonText(I18N.PayWizardDialog_FinishButton);
		wizardDialog.create();

		Point preferredSize = createPaymentWizard.getPreferredSize();
		wizardDialog.getShell().setSize(preferredSize.x, preferredSize.y);

		wizardDialog.open();
	}

}
