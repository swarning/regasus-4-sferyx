package de.regasus.workflow;

import java.util.Collection;
import java.util.List;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Shell;

import com.lambdalogic.messeinfo.invoice.data.AccountancyCVO;
import com.lambdalogic.messeinfo.invoice.data.InvoiceVO;
import com.lambdalogic.messeinfo.participant.data.EventVO;
import com.lambdalogic.messeinfo.participant.data.ParticipantCVO;
import com.lambdalogic.util.rcp.CustomWizardDialog;
import com.lambdalogic.util.rcp.UtilI18N;

import de.regasus.I18N;
import de.regasus.event.EventModel;
import de.regasus.finance.payment.dialog.CreatePaymentWizard;
import de.regasus.finance.paymentsystem.dialog.PayEnginePaymentWizard;

public class PaymentWorkflowService {

	private Shell shell;


	public PaymentWorkflowService(Shell shell) {
		this.shell = shell;
	}


	public void createManual(ParticipantCVO p, String currency) throws Exception {
		List<InvoiceVO> invoiceVOs = null;

		// initialize invoiceVOs and currency
		AccountancyCVO accountancyCVO = p.getAccountancyCVO();
		if (accountancyCVO != null) {
			accountancyCVO.getInvoiceVOs();
			invoiceVOs = accountancyCVO.getInvoiceVOs();

			if (currency == null) {
    			List<String> currencyList = accountancyCVO.getCurrencyList();
    			if (!currencyList.isEmpty()) {
    				currency = currencyList.get(0);
    			}
			}
		}


		CreatePaymentWizard createPaymentWizard = new CreatePaymentWizard(
			currency,
			invoiceVOs,
			p.getParticipant(),
			false	// realCharging
		);

		CustomWizardDialog wizardDialog = new CustomWizardDialog(shell, createPaymentWizard);
		wizardDialog.setFinishButtonText(I18N.PayWizardDialog_FinishButton);
		wizardDialog.create();

		Point preferredSize = createPaymentWizard.getPreferredSize();
		wizardDialog.getShell().setSize(preferredSize.x, preferredSize.y);

		wizardDialog.open();
	}


	public void createManual(ParticipantCVO p) throws Exception {
		createManual(p, null /*currency*/);
	}


	public void createPayEngine(ParticipantCVO p, String currency) throws Exception {
		Collection<InvoiceVO> invoiceVOs = null;

		// initialize invoiceVOs and currency
		AccountancyCVO accountancyCVO = p.getAccountancyCVO();
		if (accountancyCVO != null) {
			accountancyCVO.getInvoiceVOs();
			invoiceVOs = accountancyCVO.getInvoiceVOs();

			if (currency == null) {
    			List<String> currencyList = accountancyCVO.getCurrencyList();
    			if (!currencyList.isEmpty()) {
    				currency = currencyList.get(0);
    			}
			}
		}


		EventVO eventVO = EventModel.getInstance().getEventVO(p.getEventId());

		PayEnginePaymentWizard paymentWizard = PayEnginePaymentWizard.getPayInvoicesInstance(
			currency,
			invoiceVOs,
			p.getParticipant(),
			eventVO
		);

		CustomWizardDialog wizardDialog = new CustomWizardDialog(shell, paymentWizard);
		wizardDialog.setFinishButtonText(UtilI18N.Close);

		paymentWizard.setCustomWizardDialog(wizardDialog);

		wizardDialog.create();

		Point preferredSize = paymentWizard.getPreferredSize();
		wizardDialog.getShell().setSize(preferredSize.x, preferredSize.y);
		wizardDialog.open();
	}


	public void createPayEngine(ParticipantCVO p) throws Exception {
		createPayEngine(p, null /*currency*/);
	}

}
