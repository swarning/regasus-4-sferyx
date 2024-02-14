package de.regasus.finance.paymentsystem.dialog;

import static de.regasus.LookupService.getPaymentMgr;

import java.math.BigDecimal;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import com.lambdalogic.messeinfo.invoice.data.PaymentVO;
import com.lambdalogic.util.StringHelper;
import com.lambdalogic.util.rcp.BusyCursorHelper;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.I18N;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.finance.AccountancyModel;
import de.regasus.finance.payengine.PayEngineResponse;
import de.regasus.finance.payengine.PayEngineStatus;
import de.regasus.ui.Activator;

public class PayEngineRefundTransactionPage extends WizardPage {

	public static final String NAME = "PayEngineRefundPage";

	// Parameters
	private BigDecimal refundAmount;
	private Long payID;
	private PaymentVO paymentVO;
	private Long emailTemplateID;


	// Widgets
	private Button startTransactionButton;
	private Text resultText;


	public PayEngineRefundTransactionPage() {
		super(NAME);

		setTitle(I18N.PayEngineRefundTransactionPage_Title);
		setMessage(I18N.PayEngineAliasPage_Message);
	}


	public void init(BigDecimal refundAmount, Long payID, PaymentVO paymentVO, Long emailTemplateID) {
		this.refundAmount = refundAmount;
		this.payID = payID;
		this.paymentVO = paymentVO;
		this.emailTemplateID = emailTemplateID;
	}


	@Override
	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		setControl(composite);
		composite.setLayout(new GridLayout(1, false));

		startTransactionButton = new Button(composite, SWT.PUSH);
		startTransactionButton.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		startTransactionButton.setText(I18N.PayEngineAliasPage_StartTransactionButton);
		startTransactionButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				BusyCursorHelper.busyCursorWhile(new Runnable() {
					@Override
					public void run() {
						try {
							startTransaction();
						}
						catch (Exception e) {
							RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
						}
					}
				});
			}
		});

		resultText = new Text(composite, SWT.BORDER | SWT.V_SCROLL);
		resultText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		Color backColor = resultText.getBackground();
		SWTHelper.disableTextWidget(resultText);
		resultText.setBackground(backColor);

		// disable close button
		setPageComplete(false);
	}


	private void startTransaction() {
		// disable close button
		SWTHelper.syncExecDisplayThread(new Runnable() {
			@Override
			public void run() {
				try {
					// disable close button
					setPageComplete(false);
					getContainer().updateButtons();
				}
				catch (Throwable t) {
					RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t);
				}
			}
		});


		appendLine(I18N.PayEngineAliasPage_Msg_StartTransaction);


		StringBuffer resultMessage = new StringBuffer();
		PayEngineResponse payEngineResponse = null;
		PayEngineStatus status = null;

		try {
			System.out.println("Asking PaymentManager to refund " + refundAmount + " " + paymentVO.getCurrency());
			payEngineResponse = getPaymentMgr().refundViaPayEngine(
				refundAmount,
				paymentVO,
				payID,
				emailTemplateID
			);

			status = payEngineResponse.getStatus();
			if (   status == PayEngineStatus.REFUND_SUCCESS_STATUS
				|| status == PayEngineStatus.REFUND_PROCESSED_STATUS
			) {
				resultMessage.append(I18N.PayEngineAliasPage_Msg_Success);
			}
			else if (status == PayEngineStatus.REFUND_PENDING_STATUS) {
				resultMessage.append(I18N.PayEngineAliasPage_Msg_RefundPending);
			}
			else {
				resultMessage.append(I18N.PayEngineAliasPage_Msg_Failure);
				String statusDescription = payEngineResponse.getStatus().getDescription() + "(" + status + ")";
				StringHelper.replace(resultMessage, "<status>", statusDescription);
				StringHelper.replace(resultMessage, "<ncError>", payEngineResponse.getNcError());
				StringHelper.replace(resultMessage, "<ncErrorPlus>", payEngineResponse.getNcErrorPlus());
			}
		}
		catch (Exception e) {
			resultMessage.append(I18N.PayEngineAliasPage_Msg_Exception);
			StringHelper.replace(resultMessage, "<message>", e.getMessage());

			com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
		}
		resultMessage.append("\n");
		appendLine(resultMessage.toString());


		if (   status == PayEngineStatus.REFUND_SUCCESS_STATUS
			|| status == PayEngineStatus.REFUND_PROCESSED_STATUS
			|| status == PayEngineStatus.REFUND_PENDING_STATUS
		) {
			SWTHelper.syncExecDisplayThread(new Runnable() {
				@Override
				public void run() {
					startTransactionButton.setEnabled(false);
					setPageComplete(true);
					getContainer().updateButtons();
				}
			});

			try {
				AccountancyModel.getInstance().refresh( paymentVO.getPayerPK() );
			}
			catch (Exception e) {
				com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
			}

			// TODO Do we need a refund receipt?
//			try {
//				((PayEnginePaymentWizard) getWizard()).createPaymentReceipt(payEngineResponse.getOrderID());
//
//				// refresh the ParticipantEditor to show the new Payment or the new alias
//				ParticipantModel.getInstance().refresh(participant.getID());
//			}
//			catch (Exception e) {
//				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
//			}

		}

	}


	private void appendLine(final String text) {
		SWTHelper.syncExecDisplayThread(new Runnable() {
			@Override
			public void run() {
				resultText.append(text + "\n");
				resultText.setTopIndex(Integer.MAX_VALUE);
			}
		});
	}


}
