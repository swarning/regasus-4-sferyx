package de.regasus.onlineform.editor;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.lambdalogic.i18n.ILanguageProvider;
import com.lambdalogic.i18n.LanguageString;
import com.lambdalogic.messeinfo.config.parameterset.ConfigParameterSet;
import com.lambdalogic.messeinfo.invoice.InvoiceLabel;
import com.lambdalogic.messeinfo.participant.ParticipantLabel;
import com.lambdalogic.messeinfo.participant.data.EventVO;
import com.lambdalogic.messeinfo.regasus.RegistrationFormConfig;
import com.lambdalogic.util.StringHelper;
import com.lambdalogic.util.rcp.BrowserHelper;
import com.lambdalogic.util.rcp.ModifySupport;
import com.lambdalogic.util.rcp.i18n.I18NText;
import com.lambdalogic.util.rcp.widget.NullableSpinner;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.core.ConfigParameterSetModel;
import de.regasus.core.error.RegasusErrorHandler;
import com.lambdalogic.util.rcp.UtilI18N;
import de.regasus.core.ui.IconRegistry;
import de.regasus.core.ui.i18n.LanguageProvider;
import de.regasus.event.EventModel;
import de.regasus.finance.PaymentSystem;
import de.regasus.finance.PaymentType;
import de.regasus.onlineform.OnlineFormI18N;
import de.regasus.onlineform.RegistrationFormConfigModel;
import de.regasus.ui.Activator;

public class ConfigurationTabComposite extends Composite {

	// the entity
	private RegistrationFormConfig registrationFormConfig;

	private ModifySupport modifySupport = new ModifySupport(this);
	
	private boolean withPayEngine;
	private boolean withEasyCheckout;


	// **************************************************************************
	// * Widgets
	// *

	private Button isProConfigurationButton;

	private Button onlineFormEnabledButton;

	private Button registrationWithoutLoginEnabledButton;

	private Button loginUsernamePasswordEnabledButton;

	private Button useParticipantNumberInsteadOfVigenereCodeButton;

	private Button loginPersonalizedLinkEnabledButton;

	private Button noEmailDispatchButton;

	private Button profilePortalEnabledButton;

	private Button pricesEnabledButton;

	private Button participantTypeSelectionEnabled;

	private Button showParticipantTypeSelectionOnBottom;

	private Button sameParticipantTypeButton;

	private Button companionEnabledButton;

	private Button companionBookingsEnabledButton;

	private Button allowMultipleCompanionsButton;

	private Button travelEnabledButton;

	private Button travelAfterBookingButton;

	private Button bookingEnabledButton;

	private Button hotelEnabledButton;

	private Button confirmationReadingButton;

	private Button cancellationDisabledButton;

	private Button askForCancellationReasonButton;

	private Button saveParticipantAlsoWhenCancelButton;

	private Button alwaysShowAttendenceButtonsButton;


	// private Button confirmationMailCcHotelButton;

	private Button alwaysShowFirstPageButton;

	private Button passwordProtectedFirstPageEnableButton;

	private Button paymentTypeEnabledButton;

	private Button ccPaymentEngineTypeButton;
	
	private Button easyCheckoutTypeButton;

	private Button transferPaymentTypeButton;

	private Button cashPaymentTypeButton;

	private Button debitPaymentTypeButton;

	private Button evaluateUseInOnlineFormButton;

	private Button showWebInfoInsteadProgrammePointInfoButton;

	private Button germanAvailableButton;
	private Button englishAvailableButton;
	private Button frenchAvailableButton;
	private Button spanishAvailableButton;
	private Button russianAvailableButton;

	private Text webIdText;

	private Text defaultFromEmailAddressText;

	private Button openWebsiteButton;

	private NullableSpinner maxNoCompanionsSpinner;

	private Button showProgrammePointDetailsButton;

	private Button showShowSubtotalsButton;

	private Button showSummaryOnEndPageButton;

	private Button groupProgrammePointsByTypeButton;

	private Text gaAccountText;

	private Button gaTrackTransactionsButton;

	private Text gaConversionIdText;

	private Text gaConversionLabelText;

	private Button gaTrackConversionButton;

	private Text gaConversionId2Text;

	private Text gaConversionLabel2Text;

	private Button gaTrackConversion2Button;

	private Button showPrintButton;

	private Button repeatEmailInputButton;

	private Button wsLinkEnabledButton;

	private Text wsLinkBaseUrl;

	private I18NText wsLinkTextI18nText;

	private Button hotelPaymentEnabledButton;

	private Button noInvoiceCloseTransferPaymentButton;

	private Button useDynamicPayEngineTemplateButton;

	private Button noInvoiceClosePayEnginePaymentButton;

	private Button sendCancellationConfirmationMailButton;

	private Button emailRecommendationEnabledButton;

	private Button newsletterEnabledButton;

	private Button uploadFunctionEnabledButton;

	private Button agreementForSharedDataEnabledButton;

	private Button showCustomFieldsAfterBookingButton;

	private Button showInvoiceAddressBeforePaymentTypeButton;

	private Button defineParticipantTypeFromRegistrationPPButton;

	private Button usePODescriptionInsteadPPNameButton;

	private Button hideTaxButton;

	private Button hideNetButton;

	private Button hideGrossButton;

	private Button feedbackButton;

	private Button paymentFormButton;

	private Label useParticipantNumberInsteadOfVigenereCodeLabel;


	public ConfigurationTabComposite(Composite parent, int style) throws Exception {
		super(parent, style);

		this.setLayout(new GridLayout(3, false));
		
		ConfigParameterSetModel configParameterSetModel = ConfigParameterSetModel.getInstance();
		ConfigParameterSet configParameterSet = configParameterSetModel.getConfigParameterSet();
		withPayEngine = configParameterSet.getInvoiceDetails().getPayEngine().isVisible();
		withEasyCheckout = configParameterSet.getInvoiceDetails().getEasyCheckout().isVisible();


		// ==================================================
		// Configuration
		//

		Group configurationGroup = new Group(this, SWT.NONE);
		configurationGroup.setLayout(new GridLayout(2, false));
		configurationGroup.setText(OnlineFormI18N.Configuration);
		configurationGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));

		SWTHelper.createLabel(configurationGroup, OnlineFormI18N.IsProConfiguration);
		isProConfigurationButton = new Button(configurationGroup, SWT.CHECK);
		isProConfigurationButton.setToolTipText(OnlineFormI18N.IsProConfigurationToolTip);
		isProConfigurationButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				MessageDialog.openInformation(getShell(), UtilI18N.Info, OnlineFormI18N.IsProConfigurationToolTip);
			}
		});

		SWTHelper.createLabel(configurationGroup, OnlineFormI18N.OnlineFormEnabled);
		onlineFormEnabledButton = new Button(configurationGroup, SWT.CHECK);
		onlineFormEnabledButton.setToolTipText(OnlineFormI18N.OnlineFormEnabledToolTip);

		SWTHelper.createLabel(configurationGroup, OnlineFormI18N.WebId);
		webIdText = new Text(configurationGroup, SWT.BORDER);
		webIdText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		SWTHelper.makeBold(webIdText); // bold, because mandatory
		webIdText.setToolTipText(OnlineFormI18N.WebIdToolTip);

		SWTHelper.createLabel(configurationGroup, OnlineFormI18N.RegistrationWithoutLoginEnabled);
		registrationWithoutLoginEnabledButton = new Button(configurationGroup, SWT.CHECK);

		SWTHelper.createLabel(configurationGroup, OnlineFormI18N.AlwaysShowFirstPage);
		alwaysShowFirstPageButton = new Button(configurationGroup, SWT.CHECK);

		SWTHelper.createLabel(configurationGroup, OnlineFormI18N.PasswordProtectedFirstPage);
		passwordProtectedFirstPageEnableButton = new Button(configurationGroup, SWT.CHECK);

		SWTHelper.createLabel(configurationGroup, OnlineFormI18N.LoginWithLinkEnabled);
		loginPersonalizedLinkEnabledButton = new Button(configurationGroup, SWT.CHECK);

		SWTHelper.createLabel(configurationGroup, OnlineFormI18N.LoginWithUsernamePasswordEnabled);
		loginUsernamePasswordEnabledButton = new Button(configurationGroup, SWT.CHECK);

		useParticipantNumberInsteadOfVigenereCodeLabel = SWTHelper.createLabel(configurationGroup, OnlineFormI18N.UseParticipantNumberInsteadOfVigenereCode);
		useParticipantNumberInsteadOfVigenereCodeButton = new Button(configurationGroup, SWT.CHECK);

		SWTHelper.createLabel(configurationGroup, ParticipantLabel.Event_DefaultFrom.getString(), false);
		defaultFromEmailAddressText = new Text(configurationGroup, SWT.BORDER);
		defaultFromEmailAddressText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		SWTHelper.makeBold(defaultFromEmailAddressText); // bold, because mandatory

		SWTHelper.createLabel(configurationGroup, OnlineFormI18N.NoEmailDispatch);
		noEmailDispatchButton = new Button(configurationGroup, SWT.CHECK);

		SWTHelper.createLabel(configurationGroup, OnlineFormI18N.ProfilePortal);
		profilePortalEnabledButton = new Button(configurationGroup, SWT.CHECK);

		SWTHelper.createLabel(configurationGroup, OnlineFormI18N.Feedback);
		feedbackButton = new Button(configurationGroup, SWT.CHECK);

		SWTHelper.createLabel(configurationGroup, OnlineFormI18N.PaymentForm);
		paymentFormButton = new Button(configurationGroup, SWT.CHECK);

		new Label(configurationGroup, SWT.NONE); // dummy
		openWebsiteButton = new Button(configurationGroup, SWT.PUSH);
		openWebsiteButton.setText(OnlineFormI18N.Website);
		openWebsiteButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					openWebsite();
				}
				catch (Exception ex) {
					RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), ex);
				}
			}
		});

		// ==================================================
		// Features
		//

		Group featuresGroup = new Group(this, SWT.NONE);
		featuresGroup.setLayout(new GridLayout(2, false));
		featuresGroup.setText(OnlineFormI18N.Features);
		featuresGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 3));

		SWTHelper.createLabel(featuresGroup, OnlineFormI18N.EnableBookings);
		bookingEnabledButton = new Button(featuresGroup, SWT.CHECK);

		SWTHelper.createLabel(featuresGroup, OnlineFormI18N.ShowProgrammePointDetails);
		showProgrammePointDetailsButton = new Button(featuresGroup, SWT.CHECK);

		SWTHelper.createLabel(featuresGroup, OnlineFormI18N.PricesEnabled);
		pricesEnabledButton = new Button(featuresGroup, SWT.CHECK);

		SWTHelper.createLabel(featuresGroup, OnlineFormI18N.HideTax);
		hideTaxButton = new Button(featuresGroup, SWT.CHECK);

		SWTHelper.createLabel(featuresGroup, OnlineFormI18N.HideNet);
		hideNetButton = new Button(featuresGroup, SWT.CHECK);

		SWTHelper.createLabel(featuresGroup, OnlineFormI18N.HideGross);
		hideGrossButton = new Button(featuresGroup, SWT.CHECK);

		SWTHelper.createLabel(featuresGroup, OnlineFormI18N.ShowSubtotals);
		showShowSubtotalsButton = new Button(featuresGroup, SWT.CHECK);

		SWTHelper.createLabel(featuresGroup, OnlineFormI18N.ParticipantTypeSelectionEnabled);
		participantTypeSelectionEnabled = new Button(featuresGroup, SWT.CHECK);

		SWTHelper.createLabel(featuresGroup, OnlineFormI18N.ParticipantTypeSelectionOnBottom);
		showParticipantTypeSelectionOnBottom = new Button(featuresGroup, SWT.CHECK);

		SWTHelper.createLabel(featuresGroup, OnlineFormI18N.SameParticipantType);
		sameParticipantTypeButton = new Button(featuresGroup, SWT.CHECK);

		SWTHelper.createLabel(featuresGroup, OnlineFormI18N.EnableCompanion);
		companionEnabledButton = new Button(featuresGroup, SWT.CHECK);

		SWTHelper.createLabel(featuresGroup, OnlineFormI18N.EnableCompanionBookings);
		companionBookingsEnabledButton = new Button(featuresGroup, SWT.CHECK);

		SWTHelper.createLabel(featuresGroup, OnlineFormI18N.AllowMultipleCompanions);
		allowMultipleCompanionsButton = new Button(featuresGroup, SWT.CHECK);

		SWTHelper.createLabel(featuresGroup, OnlineFormI18N.MaxNumberOfCompanions);
		maxNoCompanionsSpinner = new NullableSpinner(featuresGroup, SWT.NONE);
		GridData layoutData = new GridData(SWT.FILL, SWT.FILL, false, false);
		layoutData.widthHint = 70;
		maxNoCompanionsSpinner.setLayoutData(layoutData);
		maxNoCompanionsSpinner.setMinimum(0);

		SWTHelper.createLabel(featuresGroup, OnlineFormI18N.EnableTravel);
		travelEnabledButton = new Button(featuresGroup, SWT.CHECK);

		SWTHelper.createLabel(featuresGroup, OnlineFormI18N.TravelAfterBooking);
		travelAfterBookingButton = new Button(featuresGroup, SWT.CHECK);

		SWTHelper.createLabel(featuresGroup, OnlineFormI18N.EnableHotel);
		hotelEnabledButton = new Button(featuresGroup, SWT.CHECK);

		SWTHelper.createLabel(featuresGroup, OnlineFormI18N.ShowSummaryOnEndPage);
		showSummaryOnEndPageButton = new Button(featuresGroup, SWT.CHECK);

		// SWTHelper.createLabel(featuresGroup, OnlineFormI18N.ConfirmationMailCcHotel);
		// confirmationMailCcHotelButton = new Button(featuresGroup, SWT.CHECK);

		SWTHelper.createLabel(featuresGroup, OnlineFormI18N.ShowWebInfoInsteadProgrammePointInfo);
		showWebInfoInsteadProgrammePointInfoButton= new Button(featuresGroup, SWT.CHECK);

		SWTHelper.createLabel(featuresGroup, OnlineFormI18N.EvaluateUseInOnlineForm);
		evaluateUseInOnlineFormButton= new Button(featuresGroup, SWT.CHECK);

		SWTHelper.createLabel(featuresGroup, OnlineFormI18N.GroupProgrammePointsByType);
		groupProgrammePointsByTypeButton = new Button(featuresGroup, SWT.CHECK);


		SWTHelper.createLabel(featuresGroup, OnlineFormI18N.ShowPrintButton);
		showPrintButton = new Button(featuresGroup, SWT.CHECK);

		SWTHelper.createLabel(featuresGroup, OnlineFormI18N.RepeatEmailInput);
		repeatEmailInputButton = new Button(featuresGroup, SWT.CHECK);

		SWTHelper.createLabel(featuresGroup, OnlineFormI18N.ConfirmationEmailReading);
		confirmationReadingButton = new Button(featuresGroup, SWT.CHECK);

		SWTHelper.createLabel(featuresGroup, OnlineFormI18N.CancellationDisabled);
		cancellationDisabledButton = new Button(featuresGroup, SWT.CHECK);

		SWTHelper.createLabel(featuresGroup, OnlineFormI18N.AskForCancellationReason);
		askForCancellationReasonButton = new Button(featuresGroup, SWT.CHECK);

		SWTHelper.createLabel(featuresGroup, OnlineFormI18N.SaveParticipantAlsoWhenCancel);
		saveParticipantAlsoWhenCancelButton = new Button(featuresGroup, SWT.CHECK);

		SWTHelper.createLabel(featuresGroup, OnlineFormI18N.AlwaysShowAttendenceButtons);
		alwaysShowAttendenceButtonsButton = new Button(featuresGroup, SWT.CHECK);

		SWTHelper.createLabel(featuresGroup, OnlineFormI18N.SendCancellationConfirmationMail);
		sendCancellationConfirmationMailButton = new Button(featuresGroup, SWT.CHECK);

		SWTHelper.createLabel(featuresGroup, OnlineFormI18N.EmailRecomendationEnabled);
		emailRecommendationEnabledButton = new Button(featuresGroup, SWT.CHECK);

		SWTHelper.createLabel(featuresGroup, OnlineFormI18N.NewsletterEnabled);
		newsletterEnabledButton = new Button(featuresGroup, SWT.CHECK);

		SWTHelper.createLabel(featuresGroup, OnlineFormI18N.ShowCustomFieldsBelowBookings);
		showCustomFieldsAfterBookingButton = new Button(featuresGroup, SWT.CHECK);

		SWTHelper.createLabel(featuresGroup, OnlineFormI18N.ShowInvoiceAddressBeforePaymentType);
		showInvoiceAddressBeforePaymentTypeButton = new Button(featuresGroup, SWT.CHECK);

		SWTHelper.createLabel(featuresGroup, OnlineFormI18N.DefineParticipantTypeFromRegistrationPP);
		defineParticipantTypeFromRegistrationPPButton = new Button(featuresGroup, SWT.CHECK);

		SWTHelper.createLabel(featuresGroup, OnlineFormI18N.UsePODescriptionInsteadPPName);
		usePODescriptionInsteadPPNameButton = new Button(featuresGroup, SWT.CHECK);

		SWTHelper.createLabel(featuresGroup, OnlineFormI18N.UploadFunctionEnabled);
		uploadFunctionEnabledButton = new Button(featuresGroup, SWT.CHECK);

		SWTHelper.createLabel(featuresGroup, OnlineFormI18N.AgreementForSharedDataEnabled);
		agreementForSharedDataEnabledButton = new Button(featuresGroup, SWT.CHECK);

		// ==================================================
		// Payment Types
		//

		Group paymentTypesGroup = new Group(this, SWT.NONE);
		paymentTypesGroup.setLayout(new GridLayout(2, false));
		paymentTypesGroup.setText(InvoiceLabel.PaymentTypes.getString());
		paymentTypesGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		// only if this button is selected, the others ane enabled
		SWTHelper.createLabel(paymentTypesGroup, OnlineFormI18N.EnablePaymentType);
		paymentTypeEnabledButton = new Button(paymentTypesGroup, SWT.CHECK);

		if (withPayEngine) {
			SWTHelper.createLabel(paymentTypesGroup, PaymentSystem.PAYENGINE.getString());
			ccPaymentEngineTypeButton = new Button(paymentTypesGroup, SWT.CHECK);
		}
		
		if (withEasyCheckout) {
			SWTHelper.createLabel(paymentTypesGroup, PaymentSystem.EASY_CHECKOUT.getString());
			easyCheckoutTypeButton = new Button(paymentTypesGroup, SWT.CHECK);
		}

		SWTHelper.createLabel(paymentTypesGroup, PaymentType.DEBIT.getString());
		debitPaymentTypeButton = new Button(paymentTypesGroup, SWT.CHECK);

		SWTHelper.createLabel(paymentTypesGroup, PaymentType.CASH.getString());
		cashPaymentTypeButton = new Button(paymentTypesGroup, SWT.CHECK);

		SWTHelper.createLabel(paymentTypesGroup, PaymentType.TRANSFER.getString());
		transferPaymentTypeButton = new Button(paymentTypesGroup, SWT.CHECK);

		SWTHelper.createLabel(paymentTypesGroup, OnlineFormI18N.HotelPayment);
		hotelPaymentEnabledButton = new Button(paymentTypesGroup, SWT.CHECK);

		SWTHelper.createLabel(paymentTypesGroup, OnlineFormI18N.NoInvoiceCloseTransferPayment);
		noInvoiceCloseTransferPaymentButton = new Button(paymentTypesGroup, SWT.CHECK);

		SWTHelper.createLabel(paymentTypesGroup, OnlineFormI18N.NoInvoiceClosePayEnginePayment);
		noInvoiceClosePayEnginePaymentButton = new Button(paymentTypesGroup, SWT.CHECK);

		SWTHelper.createLabel(paymentTypesGroup, InvoiceLabel.UseDynamicPayEngineTemplate.getString());
		useDynamicPayEngineTemplateButton = new Button(paymentTypesGroup, SWT.CHECK);


		// ==================================================
		// Languages
		//

		Group languageGroup = new Group(this, SWT.NONE);
		languageGroup.setLayout(new GridLayout(1, false));
		languageGroup.setText(OnlineFormI18N.Languages);
		languageGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		ILanguageProvider languageProvider = LanguageProvider.getInstance();
		germanAvailableButton = new Button(languageGroup, SWT.CHECK);
		germanAvailableButton.setImage(IconRegistry.getImage("icons/flags/de.png"));
		germanAvailableButton.setText(languageProvider.getLanguageByCode("de").getLanguageName().getString());

		englishAvailableButton = new Button(languageGroup, SWT.CHECK);
		englishAvailableButton.setImage(IconRegistry.getImage("icons/flags/gb.png"));
		englishAvailableButton.setText(languageProvider.getLanguageByCode("en").getLanguageName().getString());

		spanishAvailableButton = new Button(languageGroup, SWT.CHECK);
		spanishAvailableButton.setImage(IconRegistry.getImage("icons/flags/es.png"));
		spanishAvailableButton.setText(languageProvider.getLanguageByCode("es").getLanguageName().getString());

		russianAvailableButton = new Button(languageGroup, SWT.CHECK);
		russianAvailableButton.setImage(IconRegistry.getImage("icons/flags/ru.png"));
		russianAvailableButton.setText(languageProvider.getLanguageByCode("ru").getLanguageName().getString());

		// Button for extended language set (only for Syboo, since no full translation coverage exists)

		frenchAvailableButton = new Button(languageGroup, SWT.CHECK);
		frenchAvailableButton.setImage(IconRegistry.getImage("icons/flags/fr.png"));
		frenchAvailableButton.setText(languageProvider.getLanguageByCode("fr").getLanguageName().getString());

		// ==================================================
		// Google Analytics
		//
		Group googleAnalyticsGroup = new Group(this, SWT.NONE);
		googleAnalyticsGroup.setLayout(new GridLayout(2, false));
		googleAnalyticsGroup.setText("Google Analytics");
		googleAnalyticsGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		SWTHelper.createLabel(googleAnalyticsGroup, OnlineFormI18N.TrackTransactions);
		gaTrackTransactionsButton = new Button(googleAnalyticsGroup, SWT.CHECK);

		SWTHelper.createLabel(googleAnalyticsGroup, OnlineFormI18N.Account);
		gaAccountText = new Text(googleAnalyticsGroup, SWT.BORDER);
		gaAccountText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		SWTHelper.createLabel(googleAnalyticsGroup, OnlineFormI18N.TrackConversions);
		gaTrackConversionButton = new Button(googleAnalyticsGroup, SWT.CHECK);

		SWTHelper.createLabel(googleAnalyticsGroup, UtilI18N.ID);
		gaConversionIdText = new Text(googleAnalyticsGroup, SWT.BORDER);
		gaConversionIdText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		SWTHelper.createLabel(googleAnalyticsGroup, "Label"); // No need for i18n
		gaConversionLabelText = new Text(googleAnalyticsGroup, SWT.BORDER);
		gaConversionLabelText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		SWTHelper.createLabel(googleAnalyticsGroup, OnlineFormI18N.TrackConversions + " (2)");
		gaTrackConversion2Button = new Button(googleAnalyticsGroup, SWT.CHECK);

		SWTHelper.createLabel(googleAnalyticsGroup, UtilI18N.ID + " (2)");
		gaConversionId2Text = new Text(googleAnalyticsGroup, SWT.BORDER);
		gaConversionId2Text.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		SWTHelper.createLabel(googleAnalyticsGroup, "Label (2)"); // No need for i18n
		gaConversionLabel2Text = new Text(googleAnalyticsGroup, SWT.BORDER);
		gaConversionLabel2Text.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));


		// ==================================================
		// Web Service / AMSYS-Link
		//
		Group wsLinkGroup = new Group(this, SWT.NONE);
		wsLinkGroup.setLayout(new GridLayout(2, false));
		wsLinkGroup.setText(OnlineFormI18N.FollowUpLinkAmsys);
		wsLinkGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		SWTHelper.createLabel(wsLinkGroup, OnlineFormI18N.Show);
		wsLinkEnabledButton = new Button(wsLinkGroup, SWT.CHECK);

		SWTHelper.createLabel(wsLinkGroup, OnlineFormI18N.FollowUpLinkBaseUrl);
		wsLinkBaseUrl = new Text(wsLinkGroup, SWT.BORDER);
		wsLinkBaseUrl.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		SWTHelper.createLabel(wsLinkGroup, OnlineFormI18N.Text);
		wsLinkTextI18nText = new I18NText(wsLinkGroup, SWT.CHECK);
		wsLinkTextI18nText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));



		// ==================================================

		isProConfigurationButton.addSelectionListener(modifySupport);
		onlineFormEnabledButton.addSelectionListener(modifySupport);
		registrationWithoutLoginEnabledButton.addSelectionListener(modifySupport);
		loginUsernamePasswordEnabledButton.addSelectionListener(modifySupport);
		useParticipantNumberInsteadOfVigenereCodeButton.addSelectionListener(modifySupport);
		loginPersonalizedLinkEnabledButton.addSelectionListener(modifySupport);
		webIdText.addModifyListener(modifySupport);
		defaultFromEmailAddressText.addModifyListener(modifySupport);
		noEmailDispatchButton.addSelectionListener(modifySupport);
		profilePortalEnabledButton.addSelectionListener(modifySupport);
		feedbackButton.addSelectionListener(modifySupport);
		paymentFormButton.addSelectionListener(modifySupport);

		alwaysShowFirstPageButton.addSelectionListener(modifySupport);
		passwordProtectedFirstPageEnableButton.addSelectionListener(modifySupport);
		bookingEnabledButton.addSelectionListener(modifySupport);
		pricesEnabledButton.addSelectionListener(modifySupport);
		hideTaxButton.addSelectionListener(modifySupport);
		hideNetButton.addSelectionListener(modifySupport);
		hideGrossButton.addSelectionListener(modifySupport);
		participantTypeSelectionEnabled.addSelectionListener(modifySupport);
		showParticipantTypeSelectionOnBottom.addSelectionListener(modifySupport);
		sameParticipantTypeButton.addSelectionListener(modifySupport);
		companionEnabledButton.addSelectionListener(modifySupport);
		companionBookingsEnabledButton.addSelectionListener(modifySupport);
		allowMultipleCompanionsButton.addSelectionListener(modifySupport);
		maxNoCompanionsSpinner.addModifyListener(modifySupport);
		travelEnabledButton.addSelectionListener(modifySupport);
		travelAfterBookingButton.addSelectionListener(modifySupport);
		hotelEnabledButton.addSelectionListener(modifySupport);
		// confirmationMailCcHotelButton.addSelectionListener(this);
		showProgrammePointDetailsButton.addSelectionListener(modifySupport);
		showShowSubtotalsButton.addSelectionListener(modifySupport);
		showSummaryOnEndPageButton.addSelectionListener(modifySupport);
		allowMultipleCompanionsButton.addSelectionListener(modifySupport);
		groupProgrammePointsByTypeButton.addSelectionListener(modifySupport);
		evaluateUseInOnlineFormButton.addSelectionListener(modifySupport);
		showWebInfoInsteadProgrammePointInfoButton.addSelectionListener(modifySupport);
		showPrintButton.addSelectionListener(modifySupport);
		repeatEmailInputButton.addSelectionListener(modifySupport);

		paymentTypeEnabledButton.addSelectionListener(modifySupport);
		
		if (ccPaymentEngineTypeButton != null) {
			ccPaymentEngineTypeButton.addSelectionListener(modifySupport);
		}
		
		if (easyCheckoutTypeButton != null) {
			easyCheckoutTypeButton.addSelectionListener(modifySupport);
		}
		
		transferPaymentTypeButton.addSelectionListener(modifySupport);
		cashPaymentTypeButton.addSelectionListener(modifySupport);
		debitPaymentTypeButton.addSelectionListener(modifySupport);
		hotelPaymentEnabledButton.addSelectionListener(modifySupport);
		noInvoiceCloseTransferPaymentButton.addSelectionListener(modifySupport);
		noInvoiceClosePayEnginePaymentButton.addSelectionListener(modifySupport);
		useDynamicPayEngineTemplateButton.addSelectionListener(modifySupport);

		germanAvailableButton.addSelectionListener(modifySupport);
		englishAvailableButton.addSelectionListener(modifySupport);
		frenchAvailableButton.addSelectionListener(modifySupport);
		spanishAvailableButton.addSelectionListener(modifySupport);
		russianAvailableButton.addSelectionListener(modifySupport);

		gaTrackTransactionsButton.addSelectionListener(modifySupport);
		gaAccountText.addModifyListener(modifySupport);
		gaTrackConversionButton.addSelectionListener(modifySupport);
		gaConversionIdText.addModifyListener(modifySupport);
		gaConversionLabelText.addModifyListener(modifySupport);
		gaTrackConversion2Button.addSelectionListener(modifySupport);
		gaConversionId2Text.addModifyListener(modifySupport);
		gaConversionLabel2Text.addModifyListener(modifySupport);

		wsLinkEnabledButton.addSelectionListener(modifySupport);
		wsLinkBaseUrl.addModifyListener(modifySupport);
		wsLinkTextI18nText.addModifyListener(modifySupport);

		confirmationReadingButton.addSelectionListener(modifySupport);
		cancellationDisabledButton.addSelectionListener(modifySupport);
		askForCancellationReasonButton.addSelectionListener(modifySupport);
		saveParticipantAlsoWhenCancelButton.addSelectionListener(modifySupport);
		alwaysShowAttendenceButtonsButton.addSelectionListener(modifySupport);
		showCustomFieldsAfterBookingButton.addSelectionListener(modifySupport);
		showInvoiceAddressBeforePaymentTypeButton.addSelectionListener(modifySupport);
		defineParticipantTypeFromRegistrationPPButton.addSelectionListener(modifySupport);
		usePODescriptionInsteadPPNameButton.addSelectionListener(modifySupport);
		sendCancellationConfirmationMailButton.addSelectionListener(modifySupport);
		emailRecommendationEnabledButton.addSelectionListener(modifySupport);
		newsletterEnabledButton.addSelectionListener(modifySupport);
		uploadFunctionEnabledButton.addSelectionListener(modifySupport);
		agreementForSharedDataEnabledButton.addSelectionListener(modifySupport);


		modifySupport.addListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				try {
					// avoid further ModifyEvents triggered by updateButtonStates()
					modifySupport.setEnabled(false);

					updateButtonStates();
				}
				finally {
					modifySupport.setEnabled(true);
				}
			}
		});
	}


	private void updateButtonStates() {

		if (registrationWithoutLoginEnabledButton.getSelection()) {
			passwordProtectedFirstPageEnableButton.setSelection(false);
			passwordProtectedFirstPageEnableButton.setEnabled(false);
			paymentFormButton.setSelection(false);
			paymentFormButton.setEnabled(false);

		}
		else if (!loginPersonalizedLinkEnabledButton.getSelection()) {
			passwordProtectedFirstPageEnableButton.setEnabled(true);
		}

		if (passwordProtectedFirstPageEnableButton.getSelection()) {
			registrationWithoutLoginEnabledButton.setSelection(false);
			registrationWithoutLoginEnabledButton.setEnabled(false);
			loginPersonalizedLinkEnabledButton.setSelection(false);
			loginPersonalizedLinkEnabledButton.setEnabled(false);
			paymentFormButton.setSelection(false);
			paymentFormButton.setEnabled(false);
		}
		else {
			registrationWithoutLoginEnabledButton.setEnabled(true);
			loginPersonalizedLinkEnabledButton.setEnabled(true);
		}

		if (loginPersonalizedLinkEnabledButton.getSelection()) {
			passwordProtectedFirstPageEnableButton.setSelection(false);
			passwordProtectedFirstPageEnableButton.setEnabled(false);
		}
		else if (!registrationWithoutLoginEnabledButton.getSelection()) {
			passwordProtectedFirstPageEnableButton.setEnabled(true);
		}

		if (alwaysShowFirstPageButton.getSelection()) {
			paymentFormButton.setSelection(false);
			paymentFormButton.setEnabled(false);
		}

		if (feedbackButton.getSelection()) {
			paymentFormButton.setSelection(false);
			paymentFormButton.setEnabled(false);
		}

		if (profilePortalEnabledButton.getSelection()) {
			paymentFormButton.setSelection(false);
			paymentFormButton.setEnabled(false);
		}

		if (!(	alwaysShowFirstPageButton.getSelection() ||
				registrationWithoutLoginEnabledButton.getSelection() ||
				passwordProtectedFirstPageEnableButton.getSelection() ||
				profilePortalEnabledButton.getSelection() ||
				feedbackButton.getSelection()
			)
		) {
			paymentFormButton.setEnabled(true);
		}

		if (paymentFormButton.getSelection()) {
			registrationWithoutLoginEnabledButton.setSelection(false);
			registrationWithoutLoginEnabledButton.setEnabled(false);

			alwaysShowFirstPageButton.setSelection(false);
			alwaysShowFirstPageButton.setEnabled(false);

			feedbackButton.setSelection(false);
			feedbackButton.setEnabled(false);

			profilePortalEnabledButton.setSelection(false);
			profilePortalEnabledButton.setEnabled(false);

			registrationWithoutLoginEnabledButton.setSelection(false);
			registrationWithoutLoginEnabledButton.setEnabled(false);
		}
		else {
			registrationWithoutLoginEnabledButton.setEnabled(true);
			alwaysShowFirstPageButton.setEnabled(true);
			feedbackButton.setEnabled(true);
			profilePortalEnabledButton.setEnabled(true);
			registrationWithoutLoginEnabledButton.setEnabled(true);
		}

		if (! isProConfigurationButton.getSelection()) {
			frenchAvailableButton.setSelection(false);
			frenchAvailableButton.setEnabled(false);
		}
		else {
			frenchAvailableButton.setEnabled(true);
		}

		if (!loginUsernamePasswordEnabledButton.getSelection()) {
			useParticipantNumberInsteadOfVigenereCodeButton.setSelection(false);
			useParticipantNumberInsteadOfVigenereCodeButton.setEnabled(false);
			useParticipantNumberInsteadOfVigenereCodeLabel.setEnabled(false);
		}
		else {
			useParticipantNumberInsteadOfVigenereCodeButton.setEnabled(true);
			useParticipantNumberInsteadOfVigenereCodeLabel.setEnabled(true);
		}

		if (!paymentTypeEnabledButton.getSelection()) {
			if (ccPaymentEngineTypeButton != null) {
				ccPaymentEngineTypeButton.setSelection(false);
			}
			if (easyCheckoutTypeButton != null) {
				easyCheckoutTypeButton.setSelection(false);
			}
			
			cashPaymentTypeButton.setSelection(false);
			debitPaymentTypeButton.setSelection(false);
			transferPaymentTypeButton.setSelection(false);
			hotelPaymentEnabledButton.setSelection(false);
			noInvoiceCloseTransferPaymentButton.setSelection(false);
			noInvoiceClosePayEnginePaymentButton.setSelection(false);
			useDynamicPayEngineTemplateButton.setSelection(false);

			if (ccPaymentEngineTypeButton != null) {
				ccPaymentEngineTypeButton.setEnabled(false);
			}
			if (easyCheckoutTypeButton != null) {
				easyCheckoutTypeButton.setEnabled(false);
			}
			
			cashPaymentTypeButton.setEnabled(false);
			debitPaymentTypeButton.setEnabled(false);
			transferPaymentTypeButton.setEnabled(false);
			hotelPaymentEnabledButton.setEnabled(false);
			noInvoiceCloseTransferPaymentButton.setEnabled(false);
			noInvoiceClosePayEnginePaymentButton.setEnabled(false);
			useDynamicPayEngineTemplateButton.setEnabled(false);
		}
		else {
			if (ccPaymentEngineTypeButton != null) {
				ccPaymentEngineTypeButton.setEnabled(true);
			}
			if (easyCheckoutTypeButton != null) {
				easyCheckoutTypeButton.setEnabled(true);
			}
			
			cashPaymentTypeButton.setEnabled(true);
			debitPaymentTypeButton.setEnabled(true);
			transferPaymentTypeButton.setEnabled(true);
			hotelPaymentEnabledButton.setEnabled(hotelEnabledButton.getSelection());
			noInvoiceCloseTransferPaymentButton.setEnabled(transferPaymentTypeButton.getSelection());
			noInvoiceClosePayEnginePaymentButton.setEnabled(
				(ccPaymentEngineTypeButton != null && ccPaymentEngineTypeButton.getSelection()) || 
				easyCheckoutTypeButton != null && easyCheckoutTypeButton.getSelection()
			);
			useDynamicPayEngineTemplateButton.setEnabled(ccPaymentEngineTypeButton.getSelection());
			
			if (ccPaymentEngineTypeButton != null && ccPaymentEngineTypeButton.getSelection()) {
				if (easyCheckoutTypeButton != null) {
					easyCheckoutTypeButton.setSelection(false);
					easyCheckoutTypeButton.setEnabled(false);
				}
			}
			else if (easyCheckoutTypeButton != null) {
				easyCheckoutTypeButton.setEnabled(true);
			}
			
			if (easyCheckoutTypeButton != null && easyCheckoutTypeButton.getSelection()) {
				if (ccPaymentEngineTypeButton != null) {
					ccPaymentEngineTypeButton.setSelection(false);
					ccPaymentEngineTypeButton.setEnabled(false);
				}
			}
			else if (ccPaymentEngineTypeButton != null) {
				ccPaymentEngineTypeButton.setEnabled(true);
			}
		}

		// Only when both booking and travel are enabled you may choose which comes first
		if (bookingEnabledButton.getSelection() && travelEnabledButton.getSelection()) {
			travelAfterBookingButton.setEnabled(true);
		}
		else {
			travelAfterBookingButton.setSelection(false);
			travelAfterBookingButton.setEnabled(false);
		}


		// Without either bookings or companions, you cannot have bookings for companions
		if (!companionEnabledButton.getSelection() | !bookingEnabledButton.getSelection()) {
			companionBookingsEnabledButton.setSelection(false);
			companionBookingsEnabledButton.setEnabled(false);
			sameParticipantTypeButton.setSelection(false);
			sameParticipantTypeButton.setEnabled(false);
		}
		else {
			companionBookingsEnabledButton.setEnabled(true);
			sameParticipantTypeButton.setEnabled(true);
		}

		// If no companions enabled, you cannot set multiple companions
		if (!companionEnabledButton.getSelection()) {
			allowMultipleCompanionsButton.setSelection(false);
			allowMultipleCompanionsButton.setEnabled(false);
		}
		else {
			allowMultipleCompanionsButton.setEnabled(true);
		}

		// If not multiple companions are enabled, you cannot set their max number
		if (! allowMultipleCompanionsButton.getSelection()) {
			maxNoCompanionsSpinner.setValue((Integer) null);
			maxNoCompanionsSpinner.setEnabled(false);
		}
		else {
			maxNoCompanionsSpinner.setEnabled(true);
		}

		// If no prices are enabled, you cannot set subtotal lines
		if (!pricesEnabledButton.getSelection()) {
			hideTaxButton.setSelection(false);
			hideTaxButton.setEnabled(false);

			hideNetButton.setSelection(false);
			hideNetButton.setEnabled(false);

			hideGrossButton.setSelection(false);
			hideGrossButton.setEnabled(false);

			showShowSubtotalsButton.setSelection(false);
			showShowSubtotalsButton.setEnabled(false);
		}
		else {
			showShowSubtotalsButton.setEnabled(true);
			hideTaxButton.setEnabled(true);
			hideNetButton.setEnabled(true);
			hideGrossButton.setEnabled(true);

			// It is only possible to hide net price or gross price
			// it is not possible to hide both prices
			if (hideNetButton.getSelection()) {
				hideGrossButton.setSelection(false);
				hideGrossButton.setEnabled(false);
			}
			else {
				hideGrossButton.setEnabled(true);
			}

			if (hideGrossButton.getSelection()) {
				hideNetButton.setSelection(false);
				hideNetButton.setEnabled(false);
			}
			else {
				hideNetButton.setEnabled(true);
			}
		}

		// If cancellation is disabled, you can neither ask for
		// cancellation reason nor save the data anyway, also
		// you cannot force them to be visible also for unknown participants
		if (cancellationDisabledButton.getSelection()) {
			askForCancellationReasonButton.setSelection(false);
			askForCancellationReasonButton.setEnabled(false);

			saveParticipantAlsoWhenCancelButton.setSelection(false);
			saveParticipantAlsoWhenCancelButton.setEnabled(false);

			alwaysShowAttendenceButtonsButton.setSelection(false);
			alwaysShowAttendenceButtonsButton.setEnabled(false);
		}
		else {
			askForCancellationReasonButton.setEnabled(true);
			saveParticipantAlsoWhenCancelButton.setEnabled(true);
			alwaysShowAttendenceButtonsButton.setEnabled(true);
		}

		if (! participantTypeSelectionEnabled.getSelection()) {
			showParticipantTypeSelectionOnBottom.setEnabled(false);
			showParticipantTypeSelectionOnBottom.setSelection(false);
		}
		else {
			showParticipantTypeSelectionOnBottom.setEnabled(true);
		}


		gaAccountText.setEnabled(gaTrackTransactionsButton.getSelection());

		gaConversionIdText.setEnabled(gaTrackConversionButton.getSelection());
		gaConversionLabelText.setEnabled(gaTrackConversionButton.getSelection());

		gaConversionId2Text.setEnabled(gaTrackConversion2Button.getSelection());
		gaConversionLabel2Text.setEnabled(gaTrackConversion2Button.getSelection());

	}


	void setEditorIsDirty(boolean dirty) {
		openWebsiteButton.setEnabled(!dirty && onlineFormEnabledButton.getSelection());
	}


	void syncWidgetsToEntity() {
		syncWidgetsToEntityInternal(registrationFormConfig, true);
	}


	private void syncWidgetsToEntityInternal(
		final RegistrationFormConfig rfConfig,
		final boolean avoidEvents) {
		if (rfConfig != null) {
			SWTHelper.syncExecDisplayThread(new Runnable() {
				@Override
				public void run() {
					try {
						if (avoidEvents) {
							modifySupport.setEnabled(false);
						}

						EventVO eventVO = EventModel.getInstance().getEventVO(rfConfig.getEventPK());

						isProConfigurationButton.setSelection(rfConfig.isProConfiguration());
						onlineFormEnabledButton.setSelection(rfConfig.isOnlineFormEnabled());
						registrationWithoutLoginEnabledButton.setSelection(rfConfig.isRegistrationWithoutLoginEnabled());
						loginUsernamePasswordEnabledButton.setSelection(rfConfig.isLoginUsernamePasswordEnabled());
						loginPersonalizedLinkEnabledButton.setSelection(rfConfig.isLoginPersonalizedLinkEnabled());
						useParticipantNumberInsteadOfVigenereCodeButton.setSelection(rfConfig.isUseParticipantNumberInsteadOfVigenereCode());

						webIdText.setText(StringHelper.avoidNull(rfConfig.getWebId()));
						defaultFromEmailAddressText.setText(StringHelper.avoidNull(rfConfig.getDefaultFromEmailAddress()));
						noEmailDispatchButton.setSelection(rfConfig.isNoEmailDispatch());
						profilePortalEnabledButton.setSelection(rfConfig.isProfilePortalEnabled());
						feedbackButton.setSelection(rfConfig.isFeedback());
						paymentFormButton.setSelection(rfConfig.isPaymentForm());

						alwaysShowFirstPageButton.setSelection(rfConfig.isAlwaysShowFirstPage());
						passwordProtectedFirstPageEnableButton.setSelection(rfConfig.isPasswordProtectedFirstPageEnable());
						bookingEnabledButton.setSelection(rfConfig.isBookingEnabled());

						pricesEnabledButton.setSelection(rfConfig.isPricesEnabled());
						hideTaxButton.setSelection(rfConfig.isHideTax());
						hideNetButton.setSelection(rfConfig.isHideNet());
						hideGrossButton.setSelection(rfConfig.isHideGross());

						participantTypeSelectionEnabled.setSelection(rfConfig.isParticipantTypeSelectable());
						showParticipantTypeSelectionOnBottom.setSelection(rfConfig.isShowParticipantTypeSelectionOnBottom());
						sameParticipantTypeButton.setSelection(rfConfig.isSameParticipantType());

						paymentTypeEnabledButton.setSelection(rfConfig.isPaymentTypePageEnabled());
						if (ccPaymentEngineTypeButton != null) {
							ccPaymentEngineTypeButton.setSelection(rfConfig.isCcPaymentEngineEnabled());
						}
						if (easyCheckoutTypeButton != null) {
							easyCheckoutTypeButton.setSelection(rfConfig.isEasyCheckoutEnabled());
						}
						transferPaymentTypeButton.setSelection(rfConfig.isTransferPaymentEnabled());
						cashPaymentTypeButton.setSelection(rfConfig.isCashPaymentEnabled());
						debitPaymentTypeButton.setSelection(rfConfig.isDebitPaymentEnabled());
						hotelPaymentEnabledButton.setSelection(rfConfig.isHotelPaymentEnabled());
						noInvoiceCloseTransferPaymentButton.setSelection(rfConfig.isNoInvoiceCloseTransferPayment());
						noInvoiceClosePayEnginePaymentButton.setSelection(rfConfig.isNoInvoiceClosePayEnginePayment());
						useDynamicPayEngineTemplateButton.setSelection(rfConfig.isUseDynamicPayEngineTemplate());

						companionEnabledButton.setSelection(rfConfig.isCompanionEnabled());
						companionBookingsEnabledButton.setSelection(rfConfig.isCompanionBookingsEnabled());

						groupProgrammePointsByTypeButton.setSelection(rfConfig.isGroupProgrammePointsByType());
						allowMultipleCompanionsButton.setSelection(rfConfig.isMultipleCompanionsAllowed());
						travelEnabledButton.setSelection(rfConfig.isTravelEnabled());
						travelAfterBookingButton.setSelection(rfConfig.isTravelAfterBooking());
						hotelEnabledButton.setSelection(rfConfig.isHotelEnabled());
						// confirmationMailCcHotelButton.setSelection(registrationFormConfig.isConfirmationMailCcHotel());

						germanAvailableButton.setSelection(rfConfig.isGermanAvailable());
						englishAvailableButton.setSelection(rfConfig.isEnglishAvailable());
						frenchAvailableButton.setSelection(rfConfig.isLanguageAvailable("fr"));
						spanishAvailableButton.setSelection(rfConfig.isLanguageAvailable("es"));
						russianAvailableButton.setSelection(rfConfig.isLanguageAvailable("ru"));

						maxNoCompanionsSpinner.setValue(rfConfig.getMaxCountCompanions());
						showProgrammePointDetailsButton.setSelection(rfConfig
							.isShowProgrammePointDetails());
						showSummaryOnEndPageButton.setSelection(rfConfig.isShowSummaryOnEndPage());
						showShowSubtotalsButton.setSelection(rfConfig.isShowSubtotals());
						evaluateUseInOnlineFormButton.setSelection(rfConfig.isEvaluateUseInOnlineForm());
						showWebInfoInsteadProgrammePointInfoButton.setSelection(rfConfig.isShowWebInfoInsteadProgrammePointInfo());
						showPrintButton.setSelection(rfConfig.isPrintSummaryButton());
						repeatEmailInputButton.setSelection(rfConfig.isRepeatEmailInput());

						gaTrackTransactionsButton.setSelection(rfConfig.isGaTrackTransactions());
						gaAccountText.setText(StringHelper.avoidNull(rfConfig.getGaAccount()));

						gaTrackConversionButton.setSelection(rfConfig.isGaTrackConversions());
						gaConversionIdText.setText(StringHelper.avoidNull(rfConfig.getGaConversionId()));
						gaConversionLabelText.setText(StringHelper.avoidNull(rfConfig.getGaConversionLabel()));
						gaTrackConversion2Button.setSelection(rfConfig.isGaTrackConversions2());
						gaConversionId2Text.setText(StringHelper.avoidNull(rfConfig.getGaConversionId2()));
						gaConversionLabel2Text.setText(StringHelper.avoidNull(rfConfig.getGaConversionLabel2()));

						wsLinkEnabledButton.setSelection(rfConfig.isWsLinkEnabled());
						wsLinkBaseUrl.setText(StringHelper.avoidNull(rfConfig.getWsLinkBaseUrl()));
						wsLinkTextI18nText.setLanguageString(new LanguageString(rfConfig.getWsLinkTextI18n()), eventVO.getLanguages());

						confirmationReadingButton.setSelection(rfConfig.isConfirmationReading());
						cancellationDisabledButton.setSelection(registrationFormConfig.isCancellationDisabled());
						askForCancellationReasonButton.setSelection(rfConfig.isAskForCancellationReason());
						saveParticipantAlsoWhenCancelButton.setSelection(rfConfig.isSaveParticipantAlsoWhenCancel());
						alwaysShowAttendenceButtonsButton.setSelection(rfConfig.isAlwaysShowAttendenceButtons());
						showCustomFieldsAfterBookingButton.setSelection(rfConfig.isShowCustomFieldsAfterBooking());
						showInvoiceAddressBeforePaymentTypeButton.setSelection(rfConfig.isShowInvoiceAddressBeforePaymentType());
						defineParticipantTypeFromRegistrationPPButton.setSelection(rfConfig.isDefineParticipantTypeFromRegistrationPP());
						usePODescriptionInsteadPPNameButton.setSelection(rfConfig.isUsePOdescriptionWithPPname());
						sendCancellationConfirmationMailButton.setSelection(rfConfig.isSendCancellationConfirmationMail());
						emailRecommendationEnabledButton.setSelection(rfConfig.isEmailRecommendationEnabled());
						newsletterEnabledButton.setSelection(rfConfig.isNewsletterEnabled());
						uploadFunctionEnabledButton.setSelection(rfConfig.isUploadFunctionEnabled());
						agreementForSharedDataEnabledButton.setSelection(rfConfig.isAgreementForSharedDataEnabled());


						updateButtonStates();
					}
					catch (Exception e) {
						RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
					}
					finally {
						if (avoidEvents) {
							modifySupport.setEnabled(true);
						}
					}
				}
			});

		}
	}


	public void addModifyListener(ModifyListener modifyListener) {
		modifySupport.addListener(modifyListener);
	}


	public void removeModifyListener(ModifyListener modifyListener) {
		modifySupport.removeListener(modifyListener);
	}


	public void syncEntityToWidgets() {

		registrationFormConfig.setRegistrationWithoutLoginEnabled(registrationWithoutLoginEnabledButton.getSelection());

		registrationFormConfig.setProConfiguration(isProConfigurationButton.getSelection());
		registrationFormConfig.setOnlineFormEnabled(onlineFormEnabledButton.getSelection());
		registrationFormConfig.setRegistrationWithoutLoginEnabled(registrationWithoutLoginEnabledButton.getSelection());
		registrationFormConfig.setLoginUsernamePasswordEnabled(loginUsernamePasswordEnabledButton.getSelection());
		registrationFormConfig.setLoginPersonalizedLinkEnabled(loginPersonalizedLinkEnabledButton.getSelection());
		registrationFormConfig.setUseParticipantNumberInsteadOfVigenereCode(useParticipantNumberInsteadOfVigenereCodeButton.getSelection());
		registrationFormConfig.setNoEmailDispatch(noEmailDispatchButton.getSelection());
		registrationFormConfig.setProfilePortalEnabled(profilePortalEnabledButton.getSelection());
		registrationFormConfig.setFeedback(feedbackButton.getSelection());
		registrationFormConfig.setPaymentForm(paymentFormButton.getSelection());

		registrationFormConfig.setWebId(StringHelper.trim(webIdText.getText()));
		registrationFormConfig.setDefaultFromEmailAddress(StringHelper.trim(defaultFromEmailAddressText.getText()));

		registrationFormConfig.setAlwaysShowFirstPage(alwaysShowFirstPageButton.getSelection());
		registrationFormConfig.setPasswordProtectedFirstPageEnable(passwordProtectedFirstPageEnableButton.getSelection());
		registrationFormConfig.setBookingEnabled(bookingEnabledButton.getSelection());

		registrationFormConfig.setPricesEnabled(pricesEnabledButton.getSelection());
		registrationFormConfig.setHideTax(hideTaxButton.getSelection());
		registrationFormConfig.setHideNet(hideNetButton.getSelection());
		registrationFormConfig.setHideGross(hideGrossButton.getSelection());
		registrationFormConfig.setParticipantTypeSelectable(participantTypeSelectionEnabled.getSelection());
		registrationFormConfig.setShowParticipantTypeSelectionOnBottom(showParticipantTypeSelectionOnBottom.getSelection());
		registrationFormConfig.setSameParticipantType(sameParticipantTypeButton.getSelection());

		registrationFormConfig.setPaymentTypePageEnabled(paymentTypeEnabledButton.getSelection());
		if (ccPaymentEngineTypeButton != null) {
			registrationFormConfig.setCcPaymentEngineEnabled(ccPaymentEngineTypeButton.getSelection());
		}
		if (easyCheckoutTypeButton != null) {
			registrationFormConfig.setEasyCheckoutEnabled(easyCheckoutTypeButton.getSelection());
		}
		registrationFormConfig.setTransferPaymentEnabled(transferPaymentTypeButton.getSelection());
		registrationFormConfig.setCashPaymentEnabled(cashPaymentTypeButton.getSelection());
		registrationFormConfig.setDebitPaymentEnabled(debitPaymentTypeButton.getSelection());
		registrationFormConfig.setHotelPaymentEnabled(hotelPaymentEnabledButton.getSelection());
		registrationFormConfig.setNoInvoiceCloseTransferPayment(noInvoiceCloseTransferPaymentButton.getSelection());
		registrationFormConfig.setNoInvoiceClosePayEnginePayment(noInvoiceClosePayEnginePaymentButton.getSelection());
		registrationFormConfig.setUseDynamicPayEngineTemplate(useDynamicPayEngineTemplateButton.getSelection());

		registrationFormConfig.setCompanionEnabled(companionEnabledButton.getSelection());
		registrationFormConfig.setCompanionBookingsEnabled(companionBookingsEnabledButton.getSelection());
		registrationFormConfig.setMultipleCompanionsAllowed(allowMultipleCompanionsButton.getSelection());
		registrationFormConfig.setGroupProgrammePointsByType(groupProgrammePointsByTypeButton.getSelection());
		registrationFormConfig.setTravelEnabled(travelEnabledButton.getSelection());
		registrationFormConfig.setTravelAfterBooking(travelAfterBookingButton.getSelection());
		registrationFormConfig.setHotelEnabled(hotelEnabledButton.getSelection());
		// registrationFormConfig.setConfirmationMailCcHotel(confirmationMailCcHotelButton.getSelection());

		registrationFormConfig.setEnglishAvailable(englishAvailableButton.getSelection());
		registrationFormConfig.setGermanAvailable(germanAvailableButton.getSelection());
		registrationFormConfig.setLanguageAvailable("fr", frenchAvailableButton.getSelection());
		registrationFormConfig.setLanguageAvailable("es", spanishAvailableButton.getSelection());
		registrationFormConfig.setLanguageAvailable("ru", russianAvailableButton.getSelection());


		registrationFormConfig.setMaxCountCompanions(maxNoCompanionsSpinner.getValueAsInteger());
		registrationFormConfig.setShowProgrammePointDetails(showProgrammePointDetailsButton.getSelection());
		registrationFormConfig.setShowSummaryOnEndPage(showSummaryOnEndPageButton.getSelection());
		registrationFormConfig.setShowSubtotals(showShowSubtotalsButton.getSelection());
		registrationFormConfig.setEvaluateUseInOnlineForm(evaluateUseInOnlineFormButton.getSelection());
		registrationFormConfig.setShowWebInfoInsteadProgrammePointInfo(showWebInfoInsteadProgrammePointInfoButton.getSelection());

		registrationFormConfig.setPrintSummaryButton(showPrintButton.getSelection());
		registrationFormConfig.setRepeatEmailInput(repeatEmailInputButton.getSelection());

		registrationFormConfig.setGaTrackTransactions(gaTrackTransactionsButton.getSelection());
		registrationFormConfig.setGaAccount(StringHelper.trim(gaAccountText.getText()));
		registrationFormConfig.setGaTrackConversions(gaTrackConversionButton.getSelection());
		registrationFormConfig.setGaConversionId(StringHelper.trim(gaConversionIdText.getText()));
		registrationFormConfig.setGaConversionLabel(StringHelper.trim(gaConversionLabelText.getText()));
		registrationFormConfig.setGaTrackConversions2(gaTrackConversion2Button.getSelection());
		registrationFormConfig.setGaConversionId2(StringHelper.trim(gaConversionId2Text.getText()));
		registrationFormConfig.setGaConversionLabel2(StringHelper.trim(gaConversionLabel2Text.getText()));

		registrationFormConfig.setWsLinkEnabled(wsLinkEnabledButton.getSelection());
		registrationFormConfig.setWsLinkBaseUrl(wsLinkBaseUrl.getText());
		registrationFormConfig.setWsLinkTextI18n(wsLinkTextI18nText.getLanguageString().getDataString());

		registrationFormConfig.setConfirmationReading(confirmationReadingButton.getSelection());
		registrationFormConfig.setCancellationDisabled(cancellationDisabledButton.getSelection());
		registrationFormConfig.setAskForCancellationReason(askForCancellationReasonButton.getSelection());
		registrationFormConfig.setSaveParticipantAlsoWhenCancel(saveParticipantAlsoWhenCancelButton.getSelection());
		registrationFormConfig.setAlwaysShowAttendenceButtons(alwaysShowAttendenceButtonsButton.getSelection());

		registrationFormConfig.setShowCustomFieldsAfterBooking(showCustomFieldsAfterBookingButton.getSelection());
		registrationFormConfig.setShowInvoiceAddressBeforePaymentType(showInvoiceAddressBeforePaymentTypeButton.getSelection());
		registrationFormConfig.setDefineParticipantTypeFromRegistrationPP(defineParticipantTypeFromRegistrationPPButton.getSelection());
		registrationFormConfig.setUsePOdescriptionWithPPname(usePODescriptionInsteadPPNameButton.getSelection());
		registrationFormConfig.setSendCancellationConfirmationMail(sendCancellationConfirmationMailButton.getSelection());
		registrationFormConfig.setEmailRecommendationEnabled(emailRecommendationEnabledButton.getSelection());
		registrationFormConfig.setNewsletterEnabled(newsletterEnabledButton.getSelection());
		registrationFormConfig.setUploadFunctionEnabled(uploadFunctionEnabledButton.getSelection());
		registrationFormConfig.setAgreementForSharedDataEnabled(agreementForSharedDataEnabledButton.getSelection());

	}


	public void setRegistrationFormConfig(RegistrationFormConfig registrationFormConfig) {
		this.registrationFormConfig = registrationFormConfig;

		syncWidgetsToEntity();
	}


	protected void openWebsite() {
		String webId = registrationFormConfig.getWebId();
		String url = RegistrationFormConfigModel.getInstance().getOnlineWebappUrl(webId);

		BrowserHelper.openBrowser(url);
	}

}
