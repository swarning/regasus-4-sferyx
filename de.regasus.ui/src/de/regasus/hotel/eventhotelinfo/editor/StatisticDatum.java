package de.regasus.hotel.eventhotelinfo.editor;

import com.lambdalogic.time.I18NDate;

/**
 * A simple holder for the data in one row of the table of hotel event statistics
 */
public class StatisticDatum {

	public I18NDate date;
	public Integer trueSize;
	public Integer bookSize;
	public Integer booked;
	public Integer free;


	@Override
	public String toString() {
		return
			"StatisticDatum [date=" + date +
			", trueSize=" + trueSize +
			", bookSize=" + bookSize +
			", booked=" + booked +
			", free=" + free + "]";
	}

}
