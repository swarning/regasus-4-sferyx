package de.regasus.history;

import static com.lambdalogic.util.HtmlHelper.*;

import java.util.Date;
import java.util.List;

import com.lambdalogic.util.FormatHelper;
import com.lambdalogic.util.HtmlHelper;
import com.lambdalogic.util.StringHelper;

import com.lambdalogic.util.rcp.UtilI18N;
import de.regasus.history.HistoryLabel;

/**
 * Converts a list of ObjectChanges in an HTML-table with the following layout:
 * <pre>
 * +-------+--------+--------+---------+-----------+-----------+
 * | Time  | User   | Area   | Field   | Old Value | New Value |
 * |       |        |        +---------+-----------+-----------+
 * |       |        |        | Field   | Old Value | New Value |
 * |       |        +--------+---------+-----------+-----------+
 * |       |        | Area   | Field   | Old Value | New Value |
 * |       |        |        +---------+-----------+-----------+
 * |       |        |        | Field   | Old Value | New Value |
 * +-------+--------+--------+---------+-----------+-----------+
 * | Time  | User   | Area   | Field   | Old Value | New Value |
 * |       |        |        +---------+-----------+-----------+
 * |       |        |        | Field   | Old Value | New Value |
 * +-------+--------+--------+---------+-----------+-----------+
 * </pre>
 * @author manfred
 *
 */
public class HistoryEventList2HmlConverter {

	public static FormatHelper formatHelper = new FormatHelper();

	public static String tdOpen;

	public static String convert(List<IHistoryEvent> historyEventList) {
		StringBuilder html = new StringBuilder();

		html.append(HtmlHelper.HTML);

		html.append("<table border='1' cellpadding='0' cellspacing='0' width='100%'>");

		html.append(TR_OPEN);

		html.append(TH_OPEN_COLORED).append(DIV_OPEN);
		html.append(HistoryLabel.Event.getString());
		html.append(DIV_CLOSE).append(TH_CLOSE);

		html.append(TH_OPEN_COLORED).append(DIV_OPEN);
		html.append(HistoryLabel.Time.getString());
		html.append(DIV_CLOSE).append(TH_CLOSE);

		html.append(TH_OPEN_COLORED).append(DIV_OPEN);
		html.append(HistoryLabel.User.getString());
		html.append(DIV_CLOSE).append(TH_CLOSE);

		html.append(TH_OPEN_COLORED).append(DIV_OPEN);
		html.append(HistoryLabel.Description.getString());
		html.append(DIV_CLOSE).append(TH_CLOSE);

		html.append(TR_CLOSE);

		String bgColor = null;
		for (IHistoryEvent historyEvent : historyEventList) {
//			if (historyEvent.getDomainChanges().isEmpty())
//				continue;


			html.append(HtmlHelper.TR_OPEN);

			if (bgColor != null) {
				tdOpen = "<td valign='top' bgcolor='" + bgColor + "'>";
			}
			else {
				tdOpen = "<td valign='top'>";
			}


			// type
			html.append(tdOpen).append(HtmlHelper.DIV_OPEN);
			html.append(historyEvent.getType());
			html.append(HtmlHelper.DIV_CLOSE).append(HtmlHelper.TD_CLOSE);

			// time
			html.append(tdOpen).append(HtmlHelper.DIV_OPEN);
			Date time = historyEvent.getTime();
			if (time != null) {
				html.append(formatHelper.format(time));
			} else {
				html.append(UtilI18N.NotAvailable);
			}
			html.append(HtmlHelper.DIV_CLOSE).append(HtmlHelper.TD_CLOSE);

			// user
			html.append(tdOpen).append(HtmlHelper.DIV_OPEN);
			if (StringHelper.isEmpty(historyEvent.getUser())) {
				html.append("&nbsp;");
			} else {
				html.append(escape(historyEvent.getUser()));
			}
			html.append(HtmlHelper.DIV_CLOSE).append(HtmlHelper.TD_CLOSE);


			// No div here, because we want the chance to habe a completely filling HTML description, like for tables.
			html.append(tdOpen);
			html.append(historyEvent.getHtmlDescription());
			html.append(HtmlHelper.TD_CLOSE);

			html.append(HtmlHelper.TR_CLOSE);


			if (bgColor == null) {
				bgColor = HtmlHelper.TD_BG_COLOR;
			}
			else {
				bgColor = null;
			}
		}

		html.append("</table>");
		html.append("</body>");
		html.append("</html>");

		return html.toString();
	}

}
