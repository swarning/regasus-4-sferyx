package de.regasus.finance.payment.command;

import static com.lambdalogic.util.CollectionsHelper.notEmpty;
import static com.lambdalogic.util.StringHelper.isEmpty;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.http.client.utils.URIBuilder;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

import com.lambdalogic.i18n.I18NPattern;
import com.lambdalogic.messeinfo.email.EmailDispatch;
import com.lambdalogic.messeinfo.email.EmailDispatchOrder;
import com.lambdalogic.messeinfo.email.EmailTemplate;
import com.lambdalogic.messeinfo.email.EmailTemplateSystemRole;
import com.lambdalogic.messeinfo.email.data.SmtpSettingsVO;
import com.lambdalogic.messeinfo.invoice.InvoiceMessage;
import com.lambdalogic.messeinfo.invoice.data.InvoicePositionVO;
import com.lambdalogic.messeinfo.invoice.data.InvoiceVO;
import com.lambdalogic.messeinfo.participant.data.EventVO;
import com.lambdalogic.messeinfo.participant.data.ParticipantVO;
import com.lambdalogic.messeinfo.regasus.RegistrationFormConfig;
import com.lambdalogic.util.CurrencyAmount;
import com.lambdalogic.util.NumberHelper;
import com.lambdalogic.util.TypeHelper;
import com.lambdalogic.util.Vigenere2;
import com.lambdalogic.util.rcp.UtilI18N;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.I18N;
import de.regasus.LookupService;
import de.regasus.core.ServerModel;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.email.EmailDispatchModel;
import de.regasus.email.EmailDispatchOrderModel;
import de.regasus.email.EmailTemplateModel;
import de.regasus.event.EventModel;
import de.regasus.finance.command.AbstractFinanceCommandHandler;
import de.regasus.finance.payment.dialog.SendPaymentConfirmationEmailHelper;
import de.regasus.onlineform.RegistrationFormConfigModel;
import de.regasus.participant.editor.ParticipantEditor;
import de.regasus.portal.PaymentLinkConstants;
import de.regasus.ui.Activator;


public class SendPaymentLinkEmailHandler extends AbstractFinanceCommandHandler {

	@Override
	public boolean isEnabled() {
		IEditorPart activeEditor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
		if (activeEditor instanceof ParticipantEditor) {
			ParticipantEditor participantEditor = (ParticipantEditor) activeEditor;
			CurrencyAmount openAmount = participantEditor.getSelectedOpenAmount();
			return openAmount != null && openAmount.getAmount().signum() != 0;
		}
		return false;
	}


	@Override
	protected void execute(ExecutionEvent event, ParticipantEditor participantEditor)
	throws Exception {
		CurrencyAmount openAmount = participantEditor.getSelectedOpenAmount();

		Collection<InvoiceVO> invoiceVOs = participantEditor.getSelectedInvoiceVOs();
		List<Long> invoicePKs = InvoiceVO.getPKs(invoiceVOs);

		Collection<InvoicePositionVO> invoicePositionVOs = participantEditor.getSelectedInvoicePositionVOs();
		List<Long> invoicePositionPKs = InvoicePositionVO.getPKs(invoicePositionVOs);

		if (openAmount != null && openAmount.getAmount().signum() != 0) {
			ParticipantVO participantVO = participantEditor.getParticipant().getParticipantVO();
			EmailTemplate emailTemplate = getEmailTemplate(participantVO);
			if (emailTemplate != null) {

				String paymentUrl = null;
				if (participantVO.getPortalPK() != null) {
					paymentUrl = LookupService.getPortalMgr().buildPaymentURL(
						participantVO.getPortalPK(),
						participantVO.getID(),
						openAmount,
						invoicePKs,
						invoicePositionPKs
					);
				}
				else if (participantVO.getWebID() != null) {
					paymentUrl = getPaymentUrlForOnlineForm(
						participantVO,
						openAmount,
						invoicePKs,
						invoicePositionPKs
					);
				}
				else {
					MessageDialog.openInformation(
						HandlerUtil.getActiveShell(event),
						I18N.SendPaymentLinkEmailHandler_NoPaymentUrl_title,
						I18N.SendPaymentLinkEmailHandler_NoPaymentUrl_message
					);
					return;
				}

				sendEmail(participantVO, emailTemplate.getID(), paymentUrl);
			}
			else {
				// show Dialog that says, that no EmailTemplates with the purpose EmailTemplateSystemRole.PAYMENT_LINK are available.
				MessageDialog.openInformation(
					HandlerUtil.getActiveShell(event),
					I18N.SendPaymentLinkEmailHandler_NoEmailTemplate_title,
					I18N.SendPaymentLinkEmailHandler_NoEmailTemplate_message
				);
				return;
			}
		}
	}


	private EmailTemplate getEmailTemplate(ParticipantVO participantVO) throws Exception {
		EmailTemplate emailTemplate = null;
		Long eventPK = participantVO.getEventId();
		List<EmailTemplate> emailTemplates = null;
		try {
			// check if any EmailTemplate with EmailTemplateSystemRole.EASY_CHECKOUT exist
			emailTemplates = EmailTemplateModel.getInstance().getEmailTemplateSearchDataByEvent(
				eventPK,
				EmailTemplateSystemRole.PAYMENT_LINK
			);
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}

		if (notEmpty(emailTemplates)) {
			String participantLanguage = participantVO.getLanguage();
			if (participantLanguage != null) {
				emailTemplate = emailTemplates.stream().filter(e -> participantLanguage.equals(e.getLanguage()))
						.findFirst().orElse(null);
			}
			if (emailTemplate == null) {
				EventVO eventVO = EventModel.getInstance().getEventVO(eventPK);
				String eventLanguage = eventVO.getLanguage();
				if (eventLanguage != null) {
					emailTemplate = emailTemplates.stream().filter(e -> eventLanguage.equals(e.getLanguage()))
							.findFirst().orElse(null);
				}
			}
			if (emailTemplate == null) {
				String defaultLanguage = Locale.getDefault().getLanguage();
				emailTemplate = emailTemplates.stream().filter(e -> defaultLanguage.equals(e.getLanguage()))
						.findFirst().orElse(null);

			}
			if (emailTemplate == null) {
				emailTemplate = emailTemplates.get(0);
			}
		}

		return emailTemplate;
	}


	private String getPaymentUrlForOnlineForm(
		ParticipantVO participantVO,
		CurrencyAmount openAmount,
		List<Long> invoicePKs,
		List<Long> invoicePositionPKs
	)
	throws Exception {
		String paymentUrl = null;

		RegistrationFormConfig formConfig = RegistrationFormConfigModel.getInstance()
			.getRegistrationFormConfigByWebId(participantVO.getWebID());

		if (formConfig != null) {
			String alternativeDomain = formConfig.getAlternativeDomain();
			if ( isEmpty(alternativeDomain) ) {
				String onlineFormURL = ServerModel.getInstance().getOnlineFormUrl();
				paymentUrl = onlineFormURL.replace("index", "checkout");
			}
			else {
				if (!alternativeDomain.endsWith("/")) {
					alternativeDomain += "/";
				}
				paymentUrl = alternativeDomain + "online/checkout";
			}
		}


		URIBuilder uriBuilder = new URIBuilder(paymentUrl);
		uriBuilder.addParameter("codev2h", Vigenere2.toVigenereCodeHex(participantVO.getId()));
		uriBuilder.addParameter("amount", String.valueOf(openAmount.getAmount().multiply(NumberHelper.BD_100).intValue()));
		uriBuilder.addParameter("cur", openAmount.getCurrency());

		if ( notEmpty(invoicePKs) ) {
			uriBuilder.addParameter("inv", TypeHelper.toStringFromLongColl(invoicePKs));
		}

		if ( notEmpty(invoicePositionPKs) ) {
			uriBuilder.addParameter("invp", TypeHelper.toStringFromLongColl(invoicePositionPKs));
		}


		return paymentUrl;
	}


	private void sendEmail(ParticipantVO participantVO, Long emailTemplateId, String paymentUrl) {
		try {
			EventVO eventVO = EventModel.getInstance().getEventVO(participantVO.getEventId());
			SmtpSettingsVO smtpSettingsVO = eventVO.getSmtpSettingsVO();

			Map<String, Object> variables = new HashMap<>();
			variables.put(PaymentLinkConstants.PAYMENT_LINK_EXPRESSION, paymentUrl);

			// send payment confirmation email asynchronously
			new Thread() {
				@Override
				public void run() {
					try {
						EmailDispatchOrder emailDispatchOrder = EmailDispatchOrderModel.getInstance().dispatchImmediatelyOnServer(
							smtpSettingsVO,
							emailTemplateId,
							Collections.singletonList(participantVO.getId()),
							variables
						);

						if (emailDispatchOrder.getSuccessCount() == 0) {
							// load failed EmailDispatch to get error message
							final StringBuilder errorMessage = new StringBuilder();
							Long emailDispatchOrderID = emailDispatchOrder.getID();
							List<EmailDispatch> emailDispatchList =
								EmailDispatchModel.getInstance().getEmailDispatchesByEmailDispatchOrder(emailDispatchOrderID);

							if (notEmpty(emailDispatchList)) {
								errorMessage.append(emailDispatchList.get(0).getErrorMessage());
							}

							SWTHelper.syncExecDisplayThread(new Runnable() {
								@Override
								public void run() {
									I18NPattern message = new I18NPattern();
									message.add(InvoiceMessage.SendPaymentConfirmationErrorMessage);
									message.putReplacement("<name>", participantVO.getName());
									message.putReplacement("<number>", participantVO.getNumber());
									message.putReplacement("<error>", errorMessage.toString());

									MessageDialog.openError(
										Display.getDefault().getActiveShell(),
										UtilI18N.Error,
										message.getString()
									);
								}
							});
						}
						else {
							SWTHelper.syncExecDisplayThread(new Runnable() {
								@Override
								public void run() {
									I18NPattern message = EmailDispatchOrder.getStatusCountMessage(Collections.singletonList(emailDispatchOrder));
									MessageDialog.openInformation(Display.getDefault().getActiveShell(), UtilI18N.Info, message.getString());
								}
							});
						}
					}
					catch (Exception e) {
						RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
					}
				}
			}.start();

		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, SendPaymentConfirmationEmailHelper.class.getName(), e);
		}
	}

}
