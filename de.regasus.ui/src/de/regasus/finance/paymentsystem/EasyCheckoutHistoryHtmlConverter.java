package de.regasus.finance.paymentsystem;

import static com.lambdalogic.util.HtmlHelper.*;
import java.util.List;
import java.util.Locale;

import com.lambdalogic.messeinfo.invoice.InvoiceLabel;
import com.lambdalogic.messeinfo.participant.Participant;
import com.lambdalogic.messeinfo.participant.data.EventVO;
import com.lambdalogic.time.I18NTimestamp;
import com.lambdalogic.util.CurrencyAmount;
import com.lambdalogic.util.HtmlHelper;
import com.lambdalogic.util.rcp.UtilI18N;

import de.regasus.core.INewTrackingEntity;
import de.regasus.finance.easycheckout.EasyCheckoutRequest;
import de.regasus.finance.easycheckout.EasyCheckoutResponse;
import de.regasus.history.HistoryLabel;

/**
 * Converts a list of {@link EasyCheckoutRequest} and {@link EasyCheckoutResponse} in an HTML table with
 * a "styling" similar to the Participant History
 */
public class EasyCheckoutHistoryHtmlConverter {

	public static String tdOpen;

	public static String convert(
		EventVO eventVO,
		Participant participantVO,
		List<? extends INewTrackingEntity> requestOrResponseList
	) {
		StringBuilder html = new StringBuilder();

		html.append(HtmlHelper.HTML);

		html.append("<H1>" + eventVO.getLabel().getString() + "</H1>");
		html.append("<H2>" + participantVO.getName() + "</H2>");

		convert(requestOrResponseList, html);

		html.append("</body>");
		html.append("</html>");

		return html.toString();
	}

	public static String convert(List<? extends INewTrackingEntity> requestOrResponseList) {
		StringBuilder html = new StringBuilder();
		html.append(HtmlHelper.HTML);

		convert(requestOrResponseList, html);

		html.append("</body>");
		html.append("</html>");

		return html.toString();
	}


	private static void convert(List<? extends INewTrackingEntity> requestOrResponseList, StringBuilder html) {
		html.append("<table border='1' cellpadding='0' cellspacing='0' width='100%'>");

		html.append(TR_OPEN);

		html.append(TH_OPEN_COLORED).append(DIV_OPEN);
		html.append(HistoryLabel.Time.getString());
		html.append(DIV_CLOSE).append(TH_CLOSE);

		html.append(TH_OPEN_COLORED).append(DIV_OPEN);
		html.append(HistoryLabel.User.getString());
		html.append(DIV_CLOSE).append(TH_CLOSE);

		html.append(TH_OPEN_COLORED).append(DIV_OPEN);
		html.append(InvoiceLabel.Amount.getString());
		html.append(DIV_CLOSE).append(TH_CLOSE);

		html.append(TH_OPEN_COLORED).append(DIV_OPEN);
		html.append(InvoiceLabel.PayEngine_RequestOrResponse.getString());
		html.append(DIV_CLOSE).append(TH_CLOSE);

		html.append(TH_OPEN_COLORED).append(DIV_OPEN);
		html.append(UtilI18N.Details);
		html.append(DIV_CLOSE).append(TH_CLOSE);

		html.append(TR_CLOSE);

		String previousPaymentId = null;
		String previousRefundId = null;
		for (INewTrackingEntity requestOrResponse : requestOrResponseList) {
			EasyCheckoutRequest request = null;
			EasyCheckoutResponse response = null;
			if (requestOrResponse instanceof EasyCheckoutRequest) {
				request = (EasyCheckoutRequest) requestOrResponse;

				// always show paymentId and refundId if the row is a request
				previousPaymentId = null;
				previousRefundId = null;
			}
			else {
				response = (EasyCheckoutResponse) requestOrResponse;
			}

			html.append(HtmlHelper.TR_OPEN);

			if (request != null) {
				// request
				tdOpen = "<td valign='top' bgcolor='" + HtmlHelper.TD_BG_COLOR + "'>";
			}
			else {
				// response
				tdOpen = "<td valign='top'>";
			}


			/* Time column
			 */

			// time: same for request and response
			html.append(tdOpen).append(DIV_OPEN);
			I18NTimestamp time = requestOrResponse.getNewTime();
			html.append( time.getString() );
			html.append(DIV_CLOSE).append(TD_CLOSE);


			/* User column
			 */

			// user
			html.append(tdOpen).append(DIV_OPEN);
			if (request != null) {
				// request
				html.append(request.getNewDisplayUserStr());
			}
			else {
				// response: empty
				html.append(NBSP);
			}
			html.append(DIV_CLOSE).append(TD_CLOSE);


			/* Amount column
			 */

			html.append(tdOpen).append(HtmlHelper.DIV_OPEN);
			if (request != null) {
				// request
				CurrencyAmount currencyAmount = request.getCurrencyAmount();
				if (currencyAmount != null) {
					html.append(currencyAmount.formatForHtml(Locale.getDefault()));
				}
			}
			else if (response != null){
				// response: empty
				html.append(NBSP);
			}

			html.append(DIV_CLOSE).append(TD_CLOSE);


			/* Type column
			 */

			html.append(tdOpen).append(HtmlHelper.DIV_OPEN);
			if (request != null) {
				// request
				html.append("Request: ");
				html.append( request.getRequestType() );
			}
			else {
				// response
				html.append("Webhook Notification: ");
				html.append( response.getWebhookEvent() );
			}
			html.append(HtmlHelper.DIV_CLOSE).append(HtmlHelper.TD_CLOSE);


			/* Details column
			 */

			// Show paymentId and refundId only if they have changed.
			String currentPaymentId = request != null ? request.getPaymentId() : response.getPaymentId();
			String currentRefundId = request != null ? request.getRefundId() : response.getRefundId();
			String errorMessage = response != null ? response.getErrorMessage() : null;

			// No div here, because we want the chance to have a completely filling HTML description, like for tables.
			html.append(tdOpen);

			// request & response
			StringBuilder cellContent = new StringBuilder(256);

			if (currentPaymentId != null && !currentPaymentId.equals(previousPaymentId) ) {
				cellContent.append("Payment ID: " + currentPaymentId);
			}

			if (currentRefundId != null && !currentRefundId.equals(previousRefundId) ) {
				cellContent.append("<br>");
				cellContent.append("Refund ID: " + currentRefundId);
			}

			if (errorMessage != null) {
				cellContent.append("<br>");
				cellContent.append(errorMessage);
			}

			if (cellContent.length() > 0) {
				html.append(cellContent);
			}
			else {
				html.append(NBSP);
			}

			// set current paymentId and refundId as previous ones for the next iteration
			previousPaymentId = currentPaymentId;
			previousRefundId = currentRefundId;

			html.append(HtmlHelper.TD_CLOSE);
			html.append(HtmlHelper.TR_CLOSE);
		}

		html.append("</table>");
	}

}
