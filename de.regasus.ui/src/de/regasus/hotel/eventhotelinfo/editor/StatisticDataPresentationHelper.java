package de.regasus.hotel.eventhotelinfo.editor;

import static com.lambdalogic.util.HtmlHelper.*;
import static com.lambdalogic.util.StringHelper.NEW_LINE;

import java.time.format.FormatStyle;
import java.util.List;

import com.lambdalogic.messeinfo.config.parameterset.HotelConfigParameterSet;
import com.lambdalogic.messeinfo.hotel.HotelLabel;
import com.lambdalogic.time.TimeFormatter;
import com.lambdalogic.util.HtmlHelper;
import com.lambdalogic.util.rcp.UtilI18N;

/**
 * Can convert Lists of {@link StatisticData} to HTML for display in a Browser, or tab separated values
 * (similar to CSV) for copy and pasting into a spreadsheet.
 *
 * @author manfred
 *
 */
public class StatisticDataPresentationHelper {

	private static TimeFormatter timeFormatter = TimeFormatter.getDateInstance(FormatStyle.SHORT);

	/**
	 * Converts a list of {@link StatisticData} to a String containing tab separated values
	 * (similar to CSV) for copy and pasting into a spreadsheet.
	 */
	public static String convertToTabSeparatedValues(
		List<StatisticData> statisticDataList,
		String name,
		HotelConfigParameterSet hotelConfigParameterSet
	) {
		boolean showBookSize = hotelConfigParameterSet.getBookSize().isVisible();

		StringBuilder sb = new StringBuilder();

		// First row with header
		sb.append(UtilI18N.StatisticsFor);
		sb.append(name);
		sb.append(NEW_LINE);
		sb.append(NEW_LINE);


		// Second row with column headers
		sb.append(HotelLabel.RoomDefinition.getString());

		sb.append('\t');
		sb.append(UtilI18N.Date);

		sb.append('\t');
		sb.append(HotelLabel.HotelBooking_TrueSize.getString());

		if (showBookSize) {
    		sb.append('\t');
    		sb.append(HotelLabel.HotelBooking_BookSize.getString());
		}

		sb.append('\t');
		sb.append(HotelLabel.Booked.getString());

		sb.append('\t');
		sb.append(HotelLabel.Free.getString());


		// For lists of statistic data (which reflect each one particular set of room definitions...
		for (StatisticData data : statisticDataList) {
			// ...print each room definition in one row
			sb.append(NEW_LINE).append(NEW_LINE);
			sb.append("<!-- New combination of Room Definitions -->");
			sb.append(NEW_LINE);

			for(String roomDefinitionName : data.getRoomDefinitionNames()) {
				sb.append(NEW_LINE);
				sb.append(roomDefinitionName);
			}

			// ... then the actual data for each day in one row
			for (StatisticDatum datum : data.getStatisticDatumList()) {
				sb.append(NEW_LINE);

				sb.append('\t');
				sb.append(timeFormatter.format(datum.date));

				sb.append('\t');
				sb.append(datum.trueSize);

				if (showBookSize) {
    				sb.append('\t');
    				sb.append(datum.bookSize);
				}

				sb.append('\t');
				sb.append(datum.booked);

				sb.append('\t');
				sb.append(datum.free);
			}
		}
		return sb.toString();
	}


	/**
	 * Converts a list of {@link StatisticData} to a String containing HTML for display in a Browser.
	 */
	public static String convertToHtml(
		List<StatisticData> statisticDataList,
		String name,
		HotelConfigParameterSet hotelConfigParameterSet
	) {
		boolean showBookSize = hotelConfigParameterSet.getBookSize().isVisible();

		StringBuilder sb = new StringBuilder();
		sb.append(HTML);

		// Header
		sb.append("<h1>");
		sb.append(UtilI18N.StatisticsFor);
		sb.append(name);
		sb.append("</h1>");
		sb.append("<table border=\"1\" cellspacing=\"0\" cellpadding=\"5\">");

		// First row with column headers
		sb.append(TR_OPEN);
		sb.append("<td>");
		sb.append(HotelLabel.RoomDefinition.getString());
		sb.append("</td>");

		sb.append("<td>");
		sb.append(UtilI18N.Date);
		sb.append("</td>");

		sb.append("<td>");
		sb.append(HotelLabel.HotelBooking_TrueSize.getString());
		sb.append("</td>");

		if (showBookSize) {
    		sb.append("<td>");
    		sb.append(HotelLabel.HotelBooking_BookSize.getString());
    		sb.append("</td>");
		}

		sb.append("<td>");
		sb.append(HotelLabel.Booked.getString());
		sb.append("</td>");

		sb.append("<td>");
		sb.append(HotelLabel.Free.getString());
		sb.append("</td>");
		sb.append(TR_CLOSE);

		// For lists of statistic data (which reflect each one particular set of room definitions...
		for (int i = 0; i < statisticDataList.size(); i++)  {
			sb.append(NEW_LINE).append(NEW_LINE);
			sb.append("<!-- New combination of Room Definitions -->");
			sb.append(NEW_LINE);

			// Alternating highlighting of the rows
			if (i % 2 == 0) {
				sb.append(TR_OPEN);
			}
			else {
				sb.append(TR_OPEN_COLORED);
			}

			// We print the room definition names in one cell, spanning all rows with statistics data
			StatisticData statisticData = statisticDataList.get(i);
			int numberOfDates = statisticData.getStatisticDatumList().size();
			List<String> roomDefinitions = statisticData.getRoomDefinitionNames();

			sb.append("<th align=\"left\" valign=\"top\" rowspan=\"" + numberOfDates + "\">");
			int roomDefCounter = 0;
			for (String roomDefinitionVO : roomDefinitions) {
				roomDefCounter++;
				if (roomDefCounter > 1) {
    				sb.append(BR);
				}
   				sb.append(roomDefinitionVO).append(NEW_LINE);
			}
			sb.append(TH_CLOSE);
			boolean isFirstCycle = true;


			// For each date there are volumes for...
			for (StatisticDatum datum: statisticData.getStatisticDatumList()) {

				// Alternating highlighting of the rows
				if (! isFirstCycle) {
					if (i % 2 == 0) {
						sb.append(TR_OPEN);
					}
					else {
						sb.append(TR_OPEN_COLORED);
					}
				}

				// ... then the actual data for each day in one row
				sb.append("<td>");
				sb.append( timeFormatter.format(datum.date) );
				sb.append("</td>\n");

				sb.append("<td align=\"right\">");
				sb.append(datum.trueSize);
				sb.append("</td>\n");

				if (showBookSize) {
    				sb.append("<td align=\"right\">");
    				sb.append(datum.bookSize);
    				sb.append("</td>\n");
				}

				sb.append("<td align=\"right\">");
				sb.append(datum.booked);
				sb.append("</td>\n");

				sb.append("<td align=\"right\">");
				sb.append(datum.free);
				sb.append("</td>\n");
				sb.append(HtmlHelper.TR_CLOSE);
				isFirstCycle = false;
			}

		}
		sb.append("</table>");
		sb.append("</body>");
		sb.append("</html>");
		return sb.toString();
	}

}
