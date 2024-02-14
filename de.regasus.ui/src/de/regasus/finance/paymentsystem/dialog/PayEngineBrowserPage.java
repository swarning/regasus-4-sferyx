package de.regasus.finance.paymentsystem.dialog;

import static com.lambdalogic.util.CollectionsHelper.empty;
import static com.lambdalogic.util.StringHelper.isNotEmpty;
import static de.regasus.LookupService.*;

import java.math.BigDecimal;
import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.LocationEvent;
import org.eclipse.swt.browser.LocationListener;
import org.eclipse.swt.browser.ProgressEvent;
import org.eclipse.swt.browser.ProgressListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import com.lambdalogic.messeinfo.invoice.data.InvoiceVO;
import com.lambdalogic.messeinfo.invoice.payengine.PayEngineECI;
import com.lambdalogic.messeinfo.participant.Participant;
import com.lambdalogic.messeinfo.participant.data.EventVO;
import com.lambdalogic.util.exception.ErrorMessageException;
import com.lambdalogic.util.rcp.UtilI18N;
import com.lambdalogic.util.rcp.html.BrowserFactory;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.I18N;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.event.EventModel;
import de.regasus.finance.AccountancyModel;
import de.regasus.finance.payengine.PayEngineRequest;
import de.regasus.finance.payengine.PayEngineResponse;
import de.regasus.finance.payengine.PayEngineStatus;
import de.regasus.participant.ParticipantModel;
import de.regasus.ui.Activator;


public class PayEngineBrowserPage extends WizardPage implements LocationListener {

	private boolean createPaymentReceipt = true;

	public static final String NAME = "PayEngineBrowserPage";

	private static final int STATUS_INITIAL = 0;
	private static final int STATUS_IN_PROGRESS = 1;
	private static final int STATUS_SUCCESS = 2;
	private static final int STATUS_ERROR = 3;
	private static final int STATUS_CANCELED = 4;

	private int status = STATUS_INITIAL;

	private Participant participant;
	private EventVO eventVO;

	private Browser browser;
	private Label browserStatus;

	private PayEngineRequest payEngineRequest;

	private String htmlRequest;


	// For a better usability we do not ask for this parameter.
	private static final PayEngineECI ECI = PayEngineECI.MOTO_CARD_NOT_PRESENT;



	public PayEngineBrowserPage() {
		super(NAME);

		setTitle(I18N.PayEngineBrowserPage_Title);
		setDescription(I18N.PayEngineBrowserPage_Message);
	}


	@Override
	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		setControl(composite);
		composite.setLayout(new GridLayout(1, false));

		// Row 1
		browser = BrowserFactory.createBrowser(composite, SWT.BORDER);
		browser.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		// Row 2
		browserStatus = new Label(composite, SWT.LEFT);
		browserStatus.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		// text is set in ProgressListener


		browser.addLocationListener(this);


		ProgressListener progressListener = new ProgressListener() {

			@Override
			public void completed(ProgressEvent event) {
				System.out.println("Loading completed.");
				browserStatus.setText("");
			}

			@Override
			public void changed(ProgressEvent event) {
				System.out.println("Loading...");
				browserStatus.setText(UtilI18N.Loading + "...");
			}
		};
		browser.addProgressListener(progressListener);

		// disable close button
		setPageComplete(false);
	}


	/* (non-Javadoc)
	 * @see org.eclipse.swt.browser.LocationListener#changed(org.eclipse.swt.browser.LocationEvent)
	 */
	@Override
	public void changed(LocationEvent event) {
		try {
			String currentURL = browser.getUrl();

			System.out.println("Browser changed URL: " + currentURL + ", status: " + status);

			/* Only react to these URLs, because only then is the processing on the PayEngine side finished.
			 * Ignore all other URLs that may be called occasionally.
			 */

			/* In some cases the payment form shows some images where the user has to choose VISA or MasterCard.
			 * Clicking on one of the images leads to another LocationEvent which however
			 * must NOT lead to a second creation of a PayEngineRequest record.
			 *
			 * payEngineRequest.getUrl() should be
			 * https://secure.ogone.com/ncol/prod/orderstandard_utf8.asp
			 *
			 * For unknown reasons, sometimes the URL is
			 * https://secure.ogone.com/ncol/prod/orderstandard_UTF8.asp !!
			 *
			 * Therefore the URL upper/lower case has to be ignored when comparing the URLs.
			 */
			if (   currentURL.equalsIgnoreCase( payEngineRequest.getUrl() )
				&& status != STATUS_IN_PROGRESS
			) {
				status = STATUS_IN_PROGRESS;

				// avoid to create multiple PayEngineRequests in any case
				if (payEngineRequest.getId() == null) {
					payEngineRequest = getPayEngineRequestMgr().create(payEngineRequest);
				}

				// disable the wizard's previous button
				getWizard().getContainer().updateButtons();
			}
			else if (currentURL.startsWith(PayEngineHtmlGenerator.DEFAULT_ACCEPT_URL) ||
				currentURL.startsWith(PayEngineHtmlGenerator.DEFAULT_DECLINE_URL) ||
				currentURL.startsWith(PayEngineHtmlGenerator.DEFAULT_EXCEPTION_URL) ||
				currentURL.startsWith(PayEngineHtmlGenerator.DEFAULT_CANCEL_URL)
			) {
				/* Read and evaluate PayEngineResponse.
				 * If there is no PayEngineRequest, a received PayEngineResponse cannot be handled by the PayEngineServlet!
				 * In this case the PayEngineResponse won't be persisted!
				 */

				if (payEngineRequest.getId() == null) {
					MessageDialog.openError(getShell(), "Fehler", "Beim Verarbeiten der Transaktion ist ein Fehler aufgetreten."
						+ "\nKontaktieren Sie bitte LambdaLogic. Schließen Sie Regasus vorher NICHT, damit wertvolle Informationen zu diesem Fehler nicht verloren gehen."
						+ "\nFalls Sie bereits wissen wie es geht, kopieren Sie die Ausgabe der Ansicht Console und schicken diese an s.warning@lambdalogic.de."
					);
				}

				int count = 0;
				List<PayEngineResponse> payEngineResponseList = null;
				while ( empty(payEngineResponseList) && count < 20) {
					count++;
					payEngineResponseList = getPayEngineResponseMgr().readByOrderID( payEngineRequest.getOrderID() );

					if ( empty(payEngineResponseList) ) {
						System.out.println("Waiting for PayEngine-Response (" + count + ")");
						Thread.sleep(1000);
					}
				}

				PayEngineResponse payEngineResponse = null;
				if ( empty(payEngineResponseList) ) {
					throw new ErrorMessageException(I18N.PayEngineErrorNoPost);
				}
				else if (payEngineResponseList.size() > 1) {
					throw new ErrorMessageException(I18N.PayEngineErrorManyResponses);
				}
				else {
					payEngineResponse = payEngineResponseList.get(0);
				}

				if (payEngineResponse.getAlert() != null) {
					String msg = I18N.PayEngineErrorAlert;
					msg = msg.replaceAll("<alert>", payEngineResponse.getAlert());
					throw new ErrorMessageException(msg);
				}




				if (   payEngineResponse.getStatus() == PayEngineStatus.PAYMENT_REQUESTED
					|| payEngineResponse.getStatus() == PayEngineStatus.AUTHORIZED_STATUS
				) {
					status = STATUS_SUCCESS;

					if (createPaymentReceipt) {
						((PayEnginePaymentWizard) getWizard()).createPaymentReceipt(payEngineRequest.getOrderID());
					}

					// refresh ParticipantModel to show the new alias in ParticipantEditor
					ParticipantModel.getInstance().refresh(participant.getID());

					// refresh AccountancyModel to show new Payment in ParticipantEditor
					AccountancyModel.getInstance().refresh(participant.getID());
				}
				else if (payEngineResponse.getStatus() == PayEngineStatus.CANCELLED_BY_CLIENT) {
					status = STATUS_CANCELED;

					Shell shell = Display.getDefault().getActiveShell();
					MessageDialog.openInformation(shell, "Datatrans", "Die Transaktion wurde abgebrochen.");
				}
				else {
					status = STATUS_ERROR;

					String ncError = payEngineResponse.getNcError();
					String ncErrorPlus = payEngineResponse.getNcErrorPlus();

					StringBuilder sb = new StringBuilder(512);
					sb.append("Die PayEngine meldete folgenden Fehler:\n");

					sb.append("Status: ").append( payEngineResponse.getStatus() );
					sb.append(" (").append( payEngineResponse.getStatus().getDescription() ).append(")");

					if ( isNotEmpty(ncError) ) {
						sb.append("Error-Code: ").append(ncError);
					}

					if ( isNotEmpty(ncErrorPlus) ) {
						sb.append("\n");
						sb.append("Error-Message: ");
						sb.append(ncErrorPlus);
					}

					throw new ErrorMessageException(sb.toString());
				}


				SWTHelper.syncExecDisplayThread(new Runnable() {
					@Override
					public void run() {
						try {
							// enable close button
							setPageComplete(true);
						}
						catch (Throwable t) {
							RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t);
						}
					}
				});

			}
			else {

			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			// close wizard
			getWizard().getContainer().getShell().close();
		}
	}


	@Override
	public void changing(LocationEvent event) {
		String url = browser.getUrl();
		System.out.println("Browser is changing URL: " + url);
	}


	@Override
	public IWizardPage getPreviousPage() {
		if (status == STATUS_INITIAL) {
			return super.getPreviousPage();
		}
		else {
			return null;
		}
	}


	@Override
	public IWizardPage getNextPage() {
		return null;
	}


	public void setURL(String url) {
		browser.setUrl(url);
	}


	public void setText(String text) {
		browser.setText(text);
	}


	public void startPayment(
		String currency,
		BigDecimal amount,
		Participant participant,
		Long emailTemplateID,
		InvoiceVO invoiceVO
	) {
		if (status == STATUS_INITIAL) {
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


			try {
				this.participant = participant;

				eventVO = EventModel.getInstance().getEventVO(participant.getEventId());

				PayEngineHtmlGenerator htmlGenerator = new PayEngineHtmlGenerator();
				htmlRequest = htmlGenerator.getPaymentForm(
					currency,
					amount,
					participant,
					eventVO,
					ECI,
					true,	// withAlias
					emailTemplateID,
					invoiceVO
				);

				payEngineRequest = htmlGenerator.getPayEngineRequest();

				System.out.println();
				System.out.println("Set HTML to browser:");
				System.out.println(htmlRequest);
				System.out.println();

				browser.setText(htmlRequest);
			}
			catch (Exception e) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			}

		}
	}


	public void startAlias(Participant participant) {
		if (status == STATUS_INITIAL) {

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


			try {
				this.participant = participant;

				eventVO = EventModel.getInstance().getEventVO(participant.getEventId());

				PayEngineHtmlGenerator htmlGenerator = new PayEngineHtmlGenerator();
				htmlRequest = htmlGenerator.getAliasForm(
					participant,
					eventVO,
					ECI
				);

				payEngineRequest = htmlGenerator.getPayEngineRequest();

				createPaymentReceipt = false;

				browser.setText(htmlRequest);
			}
			catch (Exception e) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			}
		}
	}

}
