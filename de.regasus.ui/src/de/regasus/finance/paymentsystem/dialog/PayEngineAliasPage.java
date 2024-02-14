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

import com.lambdalogic.messeinfo.contact.CreditCardAlias;
import com.lambdalogic.messeinfo.invoice.data.InvoiceVO;
import com.lambdalogic.messeinfo.participant.Participant;
import com.lambdalogic.messeinfo.participant.data.EventVO;
import com.lambdalogic.util.StringHelper;
import com.lambdalogic.util.rcp.BusyCursorHelper;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.I18N;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.finance.AccountancyModel;
import de.regasus.finance.payengine.PayEngineResponse;
import de.regasus.finance.payengine.PayEngineStatus;
import de.regasus.participant.ParticipantModel;
import de.regasus.ui.Activator;


public class PayEngineAliasPage extends WizardPage {

	public static final String NAME = "PayEngineAliasPage";

	// parameters
	private String currency;
	private BigDecimal amount;
	private CreditCardAlias creditCardAlias;
	private Participant participant;
	private EventVO eventVO;
	private Long emailTemplateID;
	private InvoiceVO invoiceVO;


	// Widgets
	private Button startTransactionButton;
	private Text resultText;



	public PayEngineAliasPage() {
		super(NAME);

		setTitle(I18N.PayEngineAliasPage_Title);
		setMessage(I18N.PayEngineAliasPage_Message);
	}


	public void init(
		String currency,
		BigDecimal amount,
		CreditCardAlias creditCardAlias,
		Participant participant,
		EventVO eventVO,
		Long emailTemplateID,
		InvoiceVO invoiceVO
	) {
		this.currency = currency;
		this.amount = amount;
		this.creditCardAlias = creditCardAlias;
		this.participant = participant;
		this.eventVO = eventVO;
		this.emailTemplateID = emailTemplateID;
		this.invoiceVO = invoiceVO;
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
				}
				catch (Throwable t) {
					RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t);
				}
			}
		});


		appendLine(I18N.PayEngineAliasPage_Msg_StartTransaction);


		StringBuffer resultMessage = new StringBuffer();
		PayEngineResponse payEngineResponse = null;
		try {
			payEngineResponse = getPaymentMgr().payViaPayEngineWithAlias(
				currency,
				amount,
				creditCardAlias,
				participant,
				eventVO,
				emailTemplateID,
				invoiceVO
			);

			if (payEngineResponse.getStatus() == PayEngineStatus.PAYMENT_REQUESTED) {
				resultMessage.append(I18N.PayEngineAliasPage_Msg_Success);
			}
			else {
				resultMessage.append(I18N.PayEngineAliasPage_Msg_Failure);
				String status = payEngineResponse.getStatus().getDescription() + "(" + payEngineResponse.getStatus() + ")";
				StringHelper.replace(resultMessage, "<status>", status);
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


		if (payEngineResponse.getStatus() == PayEngineStatus.PAYMENT_REQUESTED) {

			SWTHelper.syncExecDisplayThread(new Runnable() {
				@Override
				public void run() {
					try {
						// disable startTransactionButton
						startTransactionButton.setEnabled(false);

						// enable close button
						setPageComplete(true);
					}
					catch (Throwable t) {
						RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t);
					}
				}
			});


			try {
				((PayEnginePaymentWizard) getWizard()).createPaymentReceipt(payEngineResponse.getOrderID());

				// refresh ParticipantModel to show the new alias in ParticipantEditor
				ParticipantModel.getInstance().refresh(participant.getID());

				// refresh AccountancyModel to show new Payment in ParticipantEditor
				AccountancyModel.getInstance().refresh(participant.getID());
			}
			catch (Exception e) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			}

		}

	}


	private void appendLine(final String text) {
		SWTHelper.syncExecDisplayThread(new Runnable() {
			@Override
			public void run() {
				try {
					final StringBuffer sb = new StringBuffer();
					String oldText = resultText.getText();
					if (oldText.length() > 0) {
						sb.append(oldText);
						sb.append("\n");
					}

					sb.append(text);

					// set resultText
					resultText.setText(sb.toString());
				}
				catch (Throwable t) {
					RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t);
				}
			}
		});

		/* Zum Ende scrollen.
		 * Findet in eigenem Thread statt.
		 * Wird es in obigem Thread direkt nach resultText.setText() aufgerufen,
		 * wird nicht korrekt gescrollt.
		 * setTopIndex(int index) berechnet das Minimum vom Parameter index und
		 * der Anzahl der Zeilen. Letztere berechnet er mittels getLineCount().
		 * getLineCount() scheint aber noch nicht die letzte Ändereung zu
		 * berücksichtigen!
		 */
		SWTHelper.syncExecDisplayThread(new Runnable() {
			@Override
			public void run() {
				try {
					resultText.setTopIndex(Integer.MAX_VALUE);
				}
				catch (Throwable t) {
					RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t);
				}
			}
		});
	}

}
