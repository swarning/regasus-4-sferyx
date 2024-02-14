package de.regasus.participant.editor.finance;

import static com.lambdalogic.util.StringHelper.avoidNull;
import static de.regasus.LookupService.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

import com.lambdalogic.messeinfo.invoice.InvoiceLabel;
import com.lambdalogic.messeinfo.invoice.InvoiceMessage;
import com.lambdalogic.messeinfo.invoice.data.AccountancyCVO;
import com.lambdalogic.messeinfo.invoice.data.PaymentVO;
import com.lambdalogic.messeinfo.invoice.payengine.PayEngineOperation;
import com.lambdalogic.messeinfo.participant.data.EventVO;
import com.lambdalogic.util.CollectionsHelper;
import com.lambdalogic.util.StringHelper;
import com.lambdalogic.util.rcp.UtilI18N;
import com.lambdalogic.util.rcp.dialog.BrowserDialog;

import de.regasus.I18N;
import de.regasus.core.INewTrackingEntity;
import de.regasus.core.NewTrackingEntityComparator;
import de.regasus.core.error.Activator;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.event.EventModel;
import de.regasus.finance.AccountancyModel;
import de.regasus.finance.PaymentSystem;
import de.regasus.finance.PaymentType;
import de.regasus.finance.payengine.PayEngineRequest;
import de.regasus.finance.payengine.PayEngineResponse;
import de.regasus.finance.payengine.PayEngineStatus;
import de.regasus.finance.paymentsystem.PayEngineHistoryHtmlConverter;
import de.regasus.finance.paymentsystem.dialog.PayEngineRefundWizard;

/**
 * A group for details of payments via {@link PaymentSystem#PAYENGINE},
 * whether of type {@link PaymentType#CREDIT_CARD} or not.
 * <p>
 * May be shown together with the {@link PaymentDetailsCreditCardComposite}.
 */
public class PaymentDetailsPayEngineGroup extends Group {

	private PaymentVO paymentVO;
	private String orderID;
	private Long payID;

	private List<PayEngineRequest>  requestList;
	private List<PayEngineResponse> responseList;

	private boolean refundHistoryPresent;

	private String refundHistory;

	// widgets
	private Label pspidLabel;
	private Label orderidLabel;
	private Label payidLabel;
	private Button refundButton;
	private Button historyButton;


	public PaymentDetailsPayEngineGroup(Composite parent, int style) {
		super(parent, style );

		setText("PayEngine");

		setLayout(new GridLayout(2, false));

		// PSPID
		{
			Label label = new Label(this, SWT.NONE);
			label.setText("PSPID:");
			label.setLayoutData(PaymentDetailsComposite.getDefaultLabelGridData());

			pspidLabel = new Label(this, SWT.NONE);
			pspidLabel.setLayoutData(PaymentDetailsComposite.getDefaultTextGridData());
		}

		// ORDERID
		{
			Label label = new Label(this, SWT.NONE);
			label.setText("ORDERID:");
			label.setLayoutData(PaymentDetailsComposite.getDefaultLabelGridData());

			orderidLabel = new Label(this, SWT.NONE);
			orderidLabel.setLayoutData(PaymentDetailsComposite.getDefaultTextGridData());
		}

		// PAYID
		{
			Label label = new Label(this, SWT.NONE);
			label.setText("PAYID:");
			label.setLayoutData(PaymentDetailsComposite.getDefaultLabelGridData());

			payidLabel = new Label(this, SWT.NONE);
			payidLabel.setLayoutData(PaymentDetailsComposite.getDefaultTextGridData());
		}

		Composite buttonComposite = new Composite(this, SWT.NONE);
		buttonComposite.setLayoutData(new GridData(SWT.CENTER, SWT.FILL, true, false, 2, 1));
		buttonComposite.setLayout(new GridLayout(2, true));

		// Optional: Refund Button
		refundButton = new Button(buttonComposite, SWT.PUSH);
		refundButton.setText(InvoiceLabel.StartRefund.getString());
		refundButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		refundButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				onRefundClicked();
			}
		});

		historyButton = new Button(buttonComposite, SWT.PUSH);
		historyButton.setText(InvoiceLabel.PastRefunds.getString());
		historyButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		historyButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				onHistoryClicked();
			}
		});
	}


	protected void onHistoryClicked() {
		BrowserDialog dialog = new BrowserDialog(
			getShell(),
			InvoiceLabel.PastRefunds.getString(),
			refundHistory
		);
		dialog.create();

		// set size of Dialog
		dialog.getShell().setSize(800, 600);

		dialog.open();
	}


	protected void onRefundClicked() {
		try {
			if (paymentVO != null) {
				BigDecimal maxRefundableAmount = getMaxRefundableAmount();

				if (maxRefundableAmount.signum() > 0) {

					if (refundHistoryPresent) {
						boolean confirm = MessageDialog.openConfirm(
							getShell(),
							UtilI18N.Confirm,
							InvoiceMessage.PastRefundExistConfirmStartAnotherRefund.getString()
						);

						if (! confirm) {
							// don't open the wizard here, because it wouldn't open if no
							// refundHistoryPresent was present
							return;
						}
					}

					PayEngineRefundWizard refundWizard = new PayEngineRefundWizard(maxRefundableAmount, paymentVO, payID);
    				WizardDialog wizardDialog = new WizardDialog(getShell(), refundWizard);
    				wizardDialog.create();
    				Point preferredSize = refundWizard.getPreferredSize();
    				wizardDialog.getShell().setSize(preferredSize.x, preferredSize.y);
    				wizardDialog.open();
				}
				else {
					refundButton.setEnabled(false);

					MessageDialog.openInformation(
						getShell(),
						UtilI18N.Info,
						I18N.RefundNotPossibleBecauseAmountAlreadyRefunded
					);
				}
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	public void setPaymentVO(PaymentVO paymentVO) throws Exception {
		this.paymentVO = paymentVO;

		refundHistoryPresent = false;
		payID = null;
		orderID = paymentVO.getDocumentNo();
		if (orderID != null) {
			// No models yet
			requestList = getPayEngineRequestMgr().readByOrderID(orderID);
			responseList = getPayEngineResponseMgr().readByOrderID(orderID);

			// determine the payID
			for (PayEngineResponse payEngineResponse : responseList) {
				if (payEngineResponse.getStatus() == PayEngineStatus.PAYMENT_REQUESTED) {
					payID = payEngineResponse.getPayID();
				}
			}

			// determine if there is any PayEngineRequest for a refund
			for (PayEngineRequest payEngineRequest: requestList) {
				if (payEngineRequest.getOperation() == PayEngineOperation.RFD) {
					refundHistoryPresent = true;
					break;
				}
			}

			if (refundHistoryPresent) {
				List<INewTrackingEntity> requestsAndResponses = new ArrayList<>();
				requestsAndResponses.addAll(requestList);
				requestsAndResponses.addAll(responseList);

				Collections.sort(requestsAndResponses, NewTrackingEntityComparator.getInstance());

				refundHistory = PayEngineHistoryHtmlConverter.convert(requestsAndResponses);
			}
		}

		setRefundButtonStates(paymentVO);

		pspidLabel.setText( avoidNull(paymentVO.getPaymentSystemId()) );
		orderidLabel.setText( avoidNull(orderID) );
		payidLabel.setText( StringHelper.toString(payID) );
	}


	/**
	 * Find out whether this payment may get a refund
	 */
	private void setRefundButtonStates(PaymentVO paymentVO) throws Exception {
		boolean mayGetRefund = true;

		EventVO eventVO = EventModel.getInstance().getEventVO(paymentVO.getEventPK());
		if (eventVO.getPaymentSystem() != PaymentSystem.PAYENGINE) {
			System.out.println("No refund possible, payengine is not the configured payment system");
			mayGetRefund = false;
		}
		else if (eventVO.getPaymentSystemSetupPK() == null) {
			System.out.println("No refund possible, no payment system setup is configured");
			mayGetRefund = false;
		}
		else if (paymentVO.getAmount().signum() == -1) {
			System.out.println("No refund possible, payment already was a refund");
			mayGetRefund = false;
		}
		else if (orderID == null) {
			System.out.println("No refund possible, no orderID found in PaymentVO");
			mayGetRefund = false;
		}
		else if (paymentVO.getPaymentSystem() != PaymentSystem.PAYENGINE) {
			System.out.println("No refund possible, payment didn't come in through PayEngine");
			mayGetRefund = false;
		}
		else if (CollectionsHelper.empty(responseList)) {
			System.out.println("No refund possible, no payengine response found with orderID=" + orderID);
			mayGetRefund = false;
		}
		else {
			mayGetRefund = getMaxRefundableAmount().signum() > 0;
		}

		refundButton.setEnabled(mayGetRefund);

		historyButton.setEnabled(refundHistoryPresent);
	}


	private BigDecimal getMaxRefundableAmount() throws Exception {
   		AccountancyCVO accountancyCVO = AccountancyModel.getInstance().getAccountancyCVO( paymentVO.getPayerPK() );
   		BigDecimal maxRefundableAmount = accountancyCVO.getMaxRefundableAmount( paymentVO.getID() );
		return maxRefundableAmount;
	}


	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

}
