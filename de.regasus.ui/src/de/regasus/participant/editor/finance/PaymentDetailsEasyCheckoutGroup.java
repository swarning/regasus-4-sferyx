package de.regasus.participant.editor.finance;

import static com.lambdalogic.util.StringHelper.avoidNull;
import static de.regasus.LookupService.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
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
import com.lambdalogic.messeinfo.participant.data.EventVO;
import com.lambdalogic.util.CurrencyAmount;
import com.lambdalogic.util.rcp.UtilI18N;
import com.lambdalogic.util.rcp.dialog.BrowserDialog;

import de.regasus.I18N;
import de.regasus.core.INewTrackingEntity;
import de.regasus.core.NewTrackingEntityComparator;
import de.regasus.core.error.Activator;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.event.EventModel;
import de.regasus.finance.AccountancyModel;
import de.regasus.finance.PaymentStatus;
import de.regasus.finance.PaymentSystem;
import de.regasus.finance.easycheckout.EasyCheckoutRequest;
import de.regasus.finance.easycheckout.EasyCheckoutRequestType;
import de.regasus.finance.easycheckout.EasyCheckoutResponse;
import de.regasus.finance.paymentsystem.EasyCheckoutHistoryHtmlConverter;

public class PaymentDetailsEasyCheckoutGroup extends Group {

	private PaymentVO paymentVO;
//	private String easyCheckoutPaymentId;
//
//	private List<EasyCheckoutRequest>  refundRequestList;
//	private List<EasyCheckoutResponse> refundResponseList;

	private boolean refundHistoryPresent;

	private String refundHistory;

	// widgets
	private Label merchantIdLabel;
	private Label documentNoLabelLabel;
	private Label documentNoLabel;
	private Label paymentStatusLabel;
	private Button refundButton;
	private Button historyButton;


	public PaymentDetailsEasyCheckoutGroup(Composite parent, int style) {
		super(parent, style );

		setText("EASY Checkout");

		setLayout(new GridLayout(2, false));

		// Merchant ID
		{
			Label label = new Label(this, SWT.NONE);
			label.setText("Merchant ID:");
			label.setLayoutData( PaymentDetailsComposite.getDefaultLabelGridData() );

			merchantIdLabel = new Label(this, SWT.NONE);
			merchantIdLabel.setLayoutData( PaymentDetailsComposite.getDefaultTextGridData() );
		}

		// Document Number (paymentId or refundId)
		{
			documentNoLabelLabel = new Label(this, SWT.NONE);
//			documentNoLabelLabel.setText("Payment ID:"); // text will be updated later but has to be set here to for the formatting
			documentNoLabelLabel.setLayoutData( PaymentDetailsComposite.getDefaultLabelGridData() );

			documentNoLabel = new Label(this, SWT.NONE);
			documentNoLabel.setLayoutData( PaymentDetailsComposite.getDefaultTextGridData() );
		}

		// PaymentStatus
		{
			Label label = new Label(this, SWT.NONE);
			label.setText(UtilI18N.Status + ":");
			label.setLayoutData( PaymentDetailsComposite.getDefaultLabelGridData() );

			paymentStatusLabel = new Label(this, SWT.NONE);
			paymentStatusLabel.setLayoutData( PaymentDetailsComposite.getDefaultTextGridData() );
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


	protected void onRefundClicked() {
		try {
			if (paymentVO != null) {
				CurrencyAmount maxRefundableAmount = getMaxRefundableAmount();

				if (maxRefundableAmount.getAmount().signum() > 0) {

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

					EasyCheckoutRefundDialog refundDialog = new EasyCheckoutRefundDialog( getShell() );
					refundDialog.setMaxAmount(maxRefundableAmount);
					int result = refundDialog.open();
					if (result == EasyCheckoutRefundDialog.OK) {
						BigDecimal refundAmount = refundDialog.getRefundAmount();

						AccountancyModel.getInstance().refundEasyCheckoutPayment(paymentVO, refundAmount);
					}
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


	protected void onHistoryClicked() {
		BrowserDialog dialog = new BrowserDialog(
			getShell(),
			InvoiceLabel.PastRefunds.getString(),
			refundHistory
		);
		dialog.create();

		// set size of Dialog
		dialog.getShell().setSize(1024, 768);

		dialog.open();
	}


	public void setPaymentVO(PaymentVO paymentVO) throws Exception {
		this.paymentVO = paymentVO;

		refundHistoryPresent = false;

		// determine the paymentId which is the documentNo of the initial Payment
		String easyPaymentId = null;
		if (paymentVO.getInitialPaymentPK() != null) {
			// load initial Payment to get the paymentId
			PaymentVO initialPaymentVO = getPaymentMgr().getPaymentVO( paymentVO.getInitialPaymentPK() );
			easyPaymentId = initialPaymentVO.getDocumentNo();
		}
		else {
			easyPaymentId = paymentVO.getDocumentNo();
		}

		// easyPaymentId is actually not null, but check anyway cause there might be an error
		if (easyPaymentId != null) {
    		List<EasyCheckoutRequest> requestList = getEasyCheckoutRequestMgr().readByPaymentId(easyPaymentId, null);
    		List<EasyCheckoutResponse> responseList = getEasyCheckoutResponseMgr().readByPaymentId(easyPaymentId);


    		// determine if there is any EasyCheckoutRequest for a refund
    		for (EasyCheckoutRequest easyCheckoutRequest: requestList) {
    			if (easyCheckoutRequest.getRequestType() == EasyCheckoutRequestType.REFUND) {
    				refundHistoryPresent = true;
    				break;
    			}
    		}


    		if (refundHistoryPresent) {
    			List<INewTrackingEntity> requestsAndResponses = new ArrayList<>();
    			requestsAndResponses.addAll(requestList);
    			requestsAndResponses.addAll(responseList);

    			Collections.sort(requestsAndResponses, NewTrackingEntityComparator.getInstance());

    			refundHistory = EasyCheckoutHistoryHtmlConverter.convert(requestsAndResponses);
    		}
		}


		setRefundButtonStates(paymentVO);

		merchantIdLabel.setText( avoidNull(paymentVO.getPaymentSystemId()) );

		if (paymentVO.isRefund()) {
			documentNoLabelLabel.setText("Refund ID:");
		}
		else {
			documentNoLabelLabel.setText("Payment ID:");
		}
		documentNoLabelLabel.getParent().layout();
		documentNoLabel.setText( avoidNull(paymentVO.getDocumentNo()) );

		PaymentStatus paymentStatus = paymentVO.getPaymentStatus();
		paymentStatusLabel.setText( paymentStatus != null ? paymentStatus.getString() : "" );
	}


	/**
	 * Find out whether this payment may get a refund
	 */
	private void setRefundButtonStates(PaymentVO paymentVO) throws Exception {
		boolean mayGetRefund = true;

		EventVO eventVO = EventModel.getInstance().getEventVO(paymentVO.getEventPK());
		if (eventVO.getPaymentSystem() != PaymentSystem.EASY_CHECKOUT) {
			System.out.println("No refund possible, Easy Checkout is not the configured payment system");
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
		else if (paymentVO.getPaymentSystem() != PaymentSystem.EASY_CHECKOUT) {
			System.out.println("No refund possible, payment didn't come in through Easy Checkout");
			mayGetRefund = false;
		}
		else if (paymentVO.getDocumentNo() == null) {
			System.out.println("No refund possible, no paymentId found in Payment");
			mayGetRefund = false;
		}
		else {
			mayGetRefund = getMaxRefundableAmount().getAmount().signum() > 0;
		}

		refundButton.setEnabled(mayGetRefund);

		historyButton.setEnabled(refundHistoryPresent);
	}


	private CurrencyAmount getMaxRefundableAmount() throws Exception {
   		AccountancyCVO accountancyCVO = AccountancyModel.getInstance().getAccountancyCVO( paymentVO.getPayerPK() );
   		BigDecimal maxRefundableAmount = accountancyCVO.getMaxRefundableAmount( paymentVO.getID() );
		return new CurrencyAmount(maxRefundableAmount, paymentVO.getCurrency());
	}


	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

}
