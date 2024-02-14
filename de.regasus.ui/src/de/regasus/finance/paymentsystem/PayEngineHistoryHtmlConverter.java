package de.regasus.finance.paymentsystem;

import static com.lambdalogic.util.HtmlHelper.*;
import static com.lambdalogic.util.StringHelper.isNotEmpty;

import java.util.List;
import java.util.Locale;

import com.lambdalogic.messeinfo.invoice.InvoiceLabel;
import com.lambdalogic.messeinfo.invoice.payengine.PayEngineOperation;
import com.lambdalogic.messeinfo.participant.Participant;
import com.lambdalogic.messeinfo.participant.data.EventVO;
import com.lambdalogic.time.I18NTimestamp;
import com.lambdalogic.util.CurrencyAmount;
import com.lambdalogic.util.HtmlHelper;

import de.regasus.core.INewTrackingEntity;
import de.regasus.finance.payengine.PayEngineRequest;
import de.regasus.finance.payengine.PayEngineResponse;

import com.lambdalogic.util.rcp.UtilI18N;

import de.regasus.history.HistoryLabel;

/**
 * Converts a list of PayEngineRequests and Responses in an HTML-table with
 * a "styling" similar to the Participant History
 *
 */
public class PayEngineHistoryHtmlConverter {

	public static String tdOpen;

	public static String convert(EventVO eventVO, Participant participantVO, List<? extends INewTrackingEntity> requestOrResponseList) {
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


	// **************************************************************************
	// * Private helper methods
	// *


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

		for (INewTrackingEntity requestOrResponse : requestOrResponseList) {

			PayEngineRequest request = null;
			PayEngineResponse response = null;
			if (requestOrResponse instanceof PayEngineRequest) {
				request = (PayEngineRequest) requestOrResponse;
			}
			else {
				response = (PayEngineResponse) requestOrResponse;
			}

			html.append(HtmlHelper.TR_OPEN);

			if (request != null) {
				tdOpen = "<td valign='top' bgcolor='" + HtmlHelper.TD_BG_COLOR + "'>";
			}
			else {
				tdOpen = "<td valign='top'>";
			}

			// time
			html.append(tdOpen).append(DIV_OPEN);
			I18NTimestamp time = requestOrResponse.getNewTime();
			html.append( time.getString() );
			html.append(DIV_CLOSE).append(TD_CLOSE);

			// user
			html.append(tdOpen).append(DIV_OPEN);
			if (request != null) {
				html.append(request.getNewDisplayUserStr());
			}
			else {
				html.append(NBSP);
			}
			html.append(DIV_CLOSE).append(TD_CLOSE);

			// Amount
			html.append(tdOpen).append(HtmlHelper.DIV_OPEN);
			if (request != null) {
				CurrencyAmount currencyAmount = request.getCurrencyAmount();
				if (currencyAmount != null) {
					html.append(currencyAmount.formatForHtml(Locale.getDefault()));
				}
			}
			else if (response != null){
				CurrencyAmount currencyAmount = response.getCurrencyAmount();
				if (currencyAmount != null) {
					html.append(currencyAmount.formatForHtml(Locale.getDefault()));
				}
			}

			html.append(DIV_CLOSE).append(TD_CLOSE);


			// request
			html.append(tdOpen).append(HtmlHelper.DIV_OPEN);
			if (request != null) {
				PayEngineOperation operation = request.getOperation();
				if (operation != null) {
    				switch (operation) {
    					case RES:
    						html.append(InvoiceLabel.PayEngine_RES);
    						break;
    					case SAL:
    						html.append(InvoiceLabel.PayEngine_SAL);
    						break;
    					case RFD:
    						html.append(InvoiceLabel.PayEngine_RFD);
    						break;
    				}
				}
			}
			else if (response != null && response.getStatus() != null) {
				html.append(response.getStatus().getStatus() + " - " + response.getStatus().getDescription());
			}
			//			html.append(historyEvent.getType());
			html.append(HtmlHelper.DIV_CLOSE).append(HtmlHelper.TD_CLOSE);


			// Amount


			// No div here, because we want the chance to habe a completely filling HTML description, like for tables.
			html.append(tdOpen);
			if (request != null) {
				html.append("ORDERID: " + request.getOrderID());
			}
			else {
				boolean proceeded = false;

				if (response.getPayID() != null) {
					html.append("PAYID: " + response.getPayID());
					proceeded = true;
					if (response.getPayIDSub() != null) {
						html.append("/" + response.getPayIDSub());
					}
				}
				if (isNotEmpty(response.getNcErrorPlus())
					&& ! "0".equals(response.getNcErrorPlus())
					&& ! "!".equals(response.getNcErrorPlus())) {

					if (proceeded) {
						html.append(", ");
					}
					html.append("NCERRORPLUS: " + response.getNcErrorPlus());
				}
			}


			html.append(HtmlHelper.TD_CLOSE);

			html.append(HtmlHelper.TR_CLOSE);

		}

		html.append("</table>");
	}

}
