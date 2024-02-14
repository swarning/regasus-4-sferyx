package de.regasus.finance.datatrans.dialog;

import static de.regasus.LookupService.*;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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

import com.lambdalogic.messeinfo.contact.CreditCardAlias;
import com.lambdalogic.messeinfo.invoice.data.DatatransRequestVO;
import com.lambdalogic.messeinfo.invoice.data.DatatransResponseVO;
import com.lambdalogic.messeinfo.invoice.data.PaymentVO;
import com.lambdalogic.messeinfo.invoice.interfaces.IDatatransManager;
import com.lambdalogic.messeinfo.participant.Participant;
import com.lambdalogic.messeinfo.participant.data.EventVO;
import com.lambdalogic.report.DocumentContainer;
import com.lambdalogic.util.NumberHelper;
import com.lambdalogic.util.StreamHelper;
import com.lambdalogic.util.StringHelper;
import com.lambdalogic.util.exception.ErrorMessageException;
import com.lambdalogic.util.rcp.UtilI18N;
import com.lambdalogic.util.rcp.html.BrowserFactory;

import de.regasus.I18N;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.event.EventModel;
import de.regasus.finance.payment.dialog.CreatePaymentAmountPage;
import de.regasus.invoice.InvoicePropertyKey;
import de.regasus.participant.ParticipantModel;
import de.regasus.ui.Activator;


public class DatatransPage2 extends WizardPage implements LocationListener {

	public static final String NAME = "DatatransPage2";

	private static final int STATUS_INITIAL = 0;
	private static final int STATUS_IN_PROGRESS = 1;
	private static final int STATUS_SUCCESS = 2;
	private static final int STATUS_ERROR = 3;
	private static final int STATUS_CANCELED = 4;


	private static String datatransURL = getPropertyMgr().readValue(InvoicePropertyKey.DATATRANS_URL);


	private boolean createPaymentReceipt = true;

	private int status = STATUS_INITIAL;

	private Participant participant;
	private BigDecimal amount;
	private String currency;
	private EventVO eventVO;
	private String referenceNo;
	private String language = Locale.getDefault().getLanguage();

	private Browser browser;
	private Label browserStatus;


	public DatatransPage2() {
		super(NAME);
		setTitle(I18N.DatatransPage2_Title);
		setDescription(I18N.DatatransPage2_Description);
	}


	@Override
	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(1, false));

		// Row 1
		browser =  BrowserFactory.createBrowser(composite, SWT.BORDER);
		browser.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		// Row 2
		browserStatus = new Label(composite, SWT.LEFT);
		browserStatus.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		// text is set in ProgressListener

		setControl(composite);

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
	}


	/* (non-Javadoc)
	 * @see org.eclipse.swt.browser.LocationListener#changed(org.eclipse.swt.browser.LocationEvent)
	 */
	@Override
	public void changed(LocationEvent event) {
		try {
			String url = browser.getUrl();

			System.out.println("Browser changed URL: " + url);

			/* Nur auf diese URLs reagieren, weil nur dann die Verarbeitung auf der Seite von
			 * Datatrans beendet ist.
			 * Alle anderen (Zwischen-)URLs ignorieren.
			 */
			if (url.startsWith(datatransURL)) {
				status = STATUS_IN_PROGRESS;

				DatatransRequestVO datatransRequestVO = new DatatransRequestVO(
					null,						// newTime
					null,						// newUser
					referenceNo,				// referenceNo
					participant.getID(), // payerPK
					eventVO.getID(),			// eventPK
					amount,						// amount
					currency,					// currency
					"debit",					// transactionType
					eventVO.getDatatransMerchantId().toString(), // merchantId
					language
				);
				getDatatransMgr().createDatatransRequestVO(
					datatransRequestVO,
					eventVO.getMnemonic(),
					participant.getNumber()
				);

				// disable the wizard's previous button
				getWizard().getContainer().updateButtons();
			}
			else if (url.startsWith(DatatransHelper.DEFAULT_SUCCESS_URL) ||
				url.startsWith(DatatransHelper.DEFAULT_ERROR_URL) ||
				url.startsWith(DatatransHelper.DEFAULT_CANCEL_URL)
			) {
				// DatatransResponse holen und auswerten
				IDatatransManager datatransManager = getDatatransMgr();
				int count = 0;
				DatatransResponseVO datatransResponseVO = null;
				while (datatransResponseVO == null && count < 20) {
					count++;
					datatransResponseVO = datatransManager.getDatatransResponseVO(referenceNo);
					if (datatransResponseVO == null) {
						System.out.println("Waiting for Datatrans-Response (" + count + ")");
						Thread.sleep(1000);
					}
				}
				if (datatransResponseVO == null) {
					throw new ErrorMessageException(I18N.DatatransErrorNoPost);
				}


				if ("success".equals(datatransResponseVO.getStatus())) {
					status = STATUS_SUCCESS;

					// search the newest payment
					List<PaymentVO> paymentVOs = getPaymentMgr().getPaymentVOsByPersonPKAndDocumentNo(
						participant.getID(),
						referenceNo
					);

					if (createPaymentReceipt) {
						if (paymentVOs != null) {
							Date newTime = null;
							Long paymentPK = null;
							for (PaymentVO paymentVO : paymentVOs) {
								if (paymentPK == null || newTime.before(paymentVO.getNewTime())) {
									paymentPK = paymentVO.getID();
									newTime = paymentVO.getNewTime();
								}
							}
							if (paymentPK != null) {
								// create and open payment receipt
								String format = getPaymentReceiptFormat();
								DocumentContainer paymentReceipt = getPaymentMgr().getPaymentReceipt(
									paymentPK,
									null,	// languageCode
									format
								);

								/* save and open generated payment receipt file
								 * This code is referenced by
								 * https://lambdalogic.atlassian.net/wiki/pages/createpage.action?spaceKey=REGASUS&fromPageId=21987353
								 * Adapt the wiki document if this code is moved to another class or method.
								 */
								paymentReceipt.open();
							}
						}
						else {
							throw new ErrorMessageException(
								"Obwohl Datatrans den erfolgreichen Abschluss der Transaktion\n" +
								"meldete wurde kein Zahlungseingang erzeugt."
							);
						}
					}

					// refresh the ParticipantEditor to show the new Payment or the new alias
					ParticipantModel.getInstance().refresh(participant.getID());
				}
				else if ("error".equals(datatransResponseVO.getStatus())) {
					status = STATUS_ERROR;

					throw new ErrorMessageException(
						"Datatrans meldete folgenden Fehler:\n" +
						"Error-Code: " + datatransResponseVO.getErrorCode() + "\n" +
						"Error-Message: " + datatransResponseVO.getErrorMessage() + "\n" +
						"Error-Detail: " + datatransResponseVO.getErrorDetail()
					);
				}
				else if ("cancel".equals(datatransResponseVO.getStatus())) {
					status = STATUS_CANCELED;

					Shell shell = Display.getDefault().getActiveShell();
					MessageDialog.openInformation(shell, "Datatrans", "Die Transaktion wurde abgebrochen.");
				}
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	/* (non-Javadoc)
	 * @see org.eclipse.swt.browser.LocationListener#changing(org.eclipse.swt.browser.LocationEvent)
	 */
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
		PaymentVO paymentVO,
		CreditCardAlias creditCardAlias,
		Participant participant,
		EventVO eventVO
	) {
		if (status == STATUS_INITIAL) {
			this.amount = paymentVO.getAmount();
			this.currency = paymentVO.getCurrency();
			this.participant = participant;
			this.eventVO = eventVO;

			try {
				if (eventVO == null) {
					eventVO = EventModel.getInstance().getEventVO(participant.getEventId());
				}

				referenceNo = String.valueOf(getPKGenerator().getValue());

				String html = null;

				if (creditCardAlias != null && creditCardAlias.isCompleteForDatatrans()) {
					InputStream inputStream = getClass().getResourceAsStream("DatatransStartWithAlias.html");
					html = StreamHelper.getString(inputStream);

					html = StringHelper.replace(html, "%header%", I18N.DatatransPage2_HeaderWithAlias);

					String paymentMethod = DatatransHelper.creditCardTypePK2paymentMethod(creditCardAlias.getCreditCardTypePK());
					html = StringHelper.replace(html, "%paymentmethod%", paymentMethod);
					html = StringHelper.replace(html, "%aliasCC%", creditCardAlias.getAlias());
					html = StringHelper.replace(html, "%expm%", creditCardAlias.getExpirationMonthAsString());
					html = StringHelper.replace(html, "%expy%", creditCardAlias.getExpirationYearAsString());
				}
				else {
					InputStream inputStream = getClass().getResourceAsStream("DatatransStart.html");
					html = StreamHelper.getString(inputStream);

					html = StringHelper.replace(html, "%header%", I18N.DatatransPage2_Header);
				}

				html = StringHelper.replace(html, "%datatransUrl%", datatransURL);

				html = StringHelper.replace(html, "%language%", language);
				html = StringHelper.replace(html, "%successUrl%", DatatransHelper.getSuccessUrl());
				html = StringHelper.replace(html, "%errorUrl%", DatatransHelper.getErrorUrl());
				html = StringHelper.replace(html, "%cancelUrl%", DatatransHelper.getCancelUrl());
				html = StringHelper.replace(html, "%merchantID%", eventVO.getDatatransMerchantId().toString());
				html = StringHelper.replace(html, "%amount%", DatatransHelper.amountToString(paymentVO.getAmount()));
				html = StringHelper.replace(html, "%currency%", paymentVO.getCurrency());
				html = StringHelper.replace(html, "%refno%", referenceNo);

				html = StringHelper.replace(html, "%submit%", I18N.DatatransPage2_SubmitButton);

				browser.setText(html);

			}
			catch (Exception e) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			}
		}
	}


	public void startAlias(
		String currency,
		Participant participant,
		EventVO eventVO
	) {
		if (status == STATUS_INITIAL) {
			this.participant = participant;
			this.eventVO = eventVO;

			this.currency = currency;
			this.amount = BigDecimal.ONE;

			/* Dies bewirkt, dass in changed() kein Zahlungsbeleg erzeugt und angezeigt wird
			 */
			createPaymentReceipt = false;

			try {
				if (eventVO == null) {
					eventVO = EventModel.getInstance().getEventVO(participant.getEventId());
				}

				referenceNo = String.valueOf(getPKGenerator().getValue());

				InputStream inputStream = getClass().getResourceAsStream("DatatransAlias.html");
				String html = StreamHelper.getString(inputStream);

				html = StringHelper.replace(html, "%header%", I18N.DatatransPage2_HeaderForAliasRequest);

				html = StringHelper.replace(html, "%datatransUrl%", datatransURL);
				html = StringHelper.replace(html, "%language%", language);
				html = StringHelper.replace(html, "%successUrl%", DatatransHelper.getSuccessUrl());
				html = StringHelper.replace(html, "%errorUrl%", DatatransHelper.getErrorUrl());
				html = StringHelper.replace(html, "%cancelUrl%", DatatransHelper.getCancelUrl());
				html = StringHelper.replace(html, "%merchantID%", eventVO.getDatatransMerchantId().toString());
				html = StringHelper.replace(html, "%amount%", DatatransHelper.amountToString(NumberHelper.BD_100));
				html = StringHelper.replace(html, "%currency%", currency);
				html = StringHelper.replace(html, "%refno%", referenceNo);

				html = StringHelper.replace(html, "%submit%", I18N.DatatransPage2_SubmitButton);

				browser.setText(html);
			}
			catch (Exception e) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			}
		}
	}


	private String getPaymentReceiptFormat() {
		CreatePaymentAmountPage createPaymentAmountPage = (CreatePaymentAmountPage) getWizard().getPage(CreatePaymentAmountPage.NAME);
		String format = createPaymentAmountPage.getPaymentReceiptFormat();
		return format;
	}

}
