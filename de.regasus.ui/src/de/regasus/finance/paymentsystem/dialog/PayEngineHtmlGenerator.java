package de.regasus.finance.paymentsystem.dialog;

import static de.regasus.LookupService.getPropertyMgr;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import com.lambdalogic.messeinfo.invoice.data.InvoiceVO;
import com.lambdalogic.messeinfo.invoice.payengine.PayEngineECI;
import com.lambdalogic.messeinfo.invoice.payengine.PayEngineHelper;
import com.lambdalogic.messeinfo.invoice.payengine.PayEngineOperation;
import com.lambdalogic.messeinfo.invoice.payengine.PayEngineRequestBuilder;
import com.lambdalogic.messeinfo.participant.Participant;
import com.lambdalogic.messeinfo.participant.data.EventVO;
import com.lambdalogic.util.StreamHelper;
import com.lambdalogic.util.StringHelper;

import de.regasus.I18N;
import de.regasus.common.Property;
import de.regasus.core.PropertyModel;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.event.EventModel;
import de.regasus.finance.payengine.PayEngineRequest;
import de.regasus.invoice.InvoicePropertyKey;
import de.regasus.ui.Activator;

public class PayEngineHtmlGenerator {

	public static final String DEFAULT_ACCEPT_URL = "https://mi2.lambdalogic.de/payEngine/payEngine-accept.html";
	public static final String DEFAULT_DECLINE_URL = "https://mi2.lambdalogic.de/payEngine/payEngine-decline.html";
	public static final String DEFAULT_EXCEPTION_URL = "https://mi2.lambdalogic.de/payEngine/payEngine-exception.html";
	public static final String DEFAULT_CANCEL_URL = "https://mi2.lambdalogic.de/payEngine/payEngine-cancel.html";

	private static String acceptUrl;
	private static String declineUrl;
	private static String exceptionUrl;
	private static String cancelUrl;

	private String html;
	private PayEngineRequest payEngineRequest;


	static {
		try {
			// get PayEngine-URLs from DB table PROPERTY
			Collection<String> keyList = new ArrayList<>(3);
			keyList.add(InvoicePropertyKey.PAY_ENGINE_SUCCESS_KEY);
			keyList.add(InvoicePropertyKey.PAY_ENGINE_DECLINE_KEY);
			keyList.add(InvoicePropertyKey.PAY_ENGINE_ERROR_KEY);
			keyList.add(InvoicePropertyKey.PAY_ENGINE_CANCEL_KEY);

			List<Property> propertyList = getPropertyMgr().read(keyList);

			for (Property property : propertyList) {
				if (property.getKey().equals(InvoicePropertyKey.PAY_ENGINE_SUCCESS_KEY)) {
					acceptUrl = property.getValue();
				}
				else if (property.getKey().equals(InvoicePropertyKey.PAY_ENGINE_DECLINE_KEY)) {
					declineUrl = property.getValue();
				}
				else if (property.getKey().equals(InvoicePropertyKey.PAY_ENGINE_ERROR_KEY)) {
					exceptionUrl = property.getValue();
				}
				else if (property.getKey().equals(InvoicePropertyKey.PAY_ENGINE_CANCEL_KEY)) {
					cancelUrl = property.getValue();
				}
			}

			// assure initialization of by default values
			if (acceptUrl == null) {
				acceptUrl = DEFAULT_ACCEPT_URL;
			}
			if (declineUrl == null) {
				declineUrl = DEFAULT_DECLINE_URL;
			}
			if (exceptionUrl == null) {
				exceptionUrl = DEFAULT_EXCEPTION_URL;
			}
			if (cancelUrl == null) {
				cancelUrl = DEFAULT_CANCEL_URL;
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, PayEngineHelper.class.getName(), e);
		}
	}


	public PayEngineHtmlGenerator() {
	}


	public String getPaymentForm(
		String currency,
		BigDecimal amount,
		Participant participant,
		EventVO eventVO,
		PayEngineECI payEngineECI,
		boolean withAlias,
		Long emailTemplateID,
		InvoiceVO invoiceVO
	)
	throws Exception {
		return getForm(
			currency,
			amount,
			participant,
			eventVO,
			payEngineECI,
			PayEngineOperation.SAL, // Anfrage für Direktbuchung (Verkauf), siehe Advanced e-Commerce 9.2
			withAlias,
			null, // no payID
			emailTemplateID,
			invoiceVO
		);
	}


	public String getAliasForm(
		Participant participant,
		EventVO eventVO,
		PayEngineECI payEngineECI
	)
	throws Exception {
		// determine currency
		// 1.: Take the event's default currency for programme offerings
		String currency = eventVO.getProgPriceDefaultsVO().getCurrency();
		if (currency == null) {
			// 2.: Take default currency from properties
			currency = PropertyModel.getInstance().getDefaultCurrency();
		}
		if (currency == null) {
			// 3.: Take EUR
			currency = "EUR";
		}

		return getForm(
			currency,
			BigDecimal.ONE,	// amount
			participant,
			eventVO,
			payEngineECI,
			PayEngineOperation.RES, // Anfrage für Autorisierung (Reservierung), siehe Advanced e-Commerce 9.2
			true,					// withAlias
			null,	// payID;
			null,	// emailTemplateID
			null
		);
	}


	private String getForm(
		String currency,
		BigDecimal amount,
		Participant participant,
		EventVO eventVO,
		PayEngineECI payEngineECI,
		PayEngineOperation operation,
		boolean withAlias,
		String payID,
		Long emailTemplateID,
		InvoiceVO invoiceVO
	)
	throws Exception {
		if (eventVO == null) {
			eventVO = EventModel.getInstance().getEventVO(participant.getEventId());
		}

		InputStream inputStream = getClass().getResourceAsStream("PayEngineStart.html");
		html = StreamHelper.getString(inputStream);
		html = StringHelper.replace(html, "%header%", I18N.PayEngine_HtmlHeader);

		PayEngineRequestBuilder requestBuilder = new PayEngineRequestBuilder();
		{
			requestBuilder.setWithAlias(withAlias);
			if (withAlias) {
				requestBuilder.setAliasUsage(I18N.PayEngine_AliasUsage);
			}

			requestBuilder.setPayID(payID);
			requestBuilder.setAmount(amount);
			requestBuilder.setCurrency(currency);
			requestBuilder.setLocale(Locale.getDefault());
			requestBuilder.setParticipant(participant);
			requestBuilder.setEventVO(eventVO);
			requestBuilder.setEmailTemplateID(emailTemplateID);
			requestBuilder.setInvoiceVO(invoiceVO);

			requestBuilder.setAcceptUrl(acceptUrl);
			requestBuilder.setDeclineUrl(declineUrl);
			requestBuilder.setExceptionUrl(exceptionUrl);
			requestBuilder.setCancelUrl(cancelUrl);

			requestBuilder.setPayEngineECI(payEngineECI);
			requestBuilder.setPayEngineOperation(operation);

			requestBuilder.setSubmitButtonText(I18N.PayEngine_SubmitButton);

		}
		String formHtml = requestBuilder.buildHtml();
		payEngineRequest = requestBuilder.getPayEngineRequest();

		html = StringHelper.replace(html, "%form%", formHtml);

		return html;
	}



	public PayEngineRequest getPayEngineRequest() {
		return payEngineRequest;
	}

}
