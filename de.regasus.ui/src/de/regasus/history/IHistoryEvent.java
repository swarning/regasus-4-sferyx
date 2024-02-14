package de.regasus.history;

import java.util.Date;

public interface IHistoryEvent {

	public String TABLE_OPEN = "<table border='1' cellpadding='5' cellspacing='0' width='100%'>";
	public String TABLE_CLOSE = "</table>";

	public String TR_OPEN = "<tr>";
	public String TR_CLOSE = "</tr>";

	public String TH_OPEN_COLORED = "<th bgcolor=\"#DDDDDD\" >";
	public String TH_CLOSE = "</th>";

	public String TD_OPEN =  "<td valign='top' width='25%'>";
	public String TD_CLOSE = "</td>";


	/**
	 * Marks this {@link IHistoryEvent} as the very beginning.
	 * If two {@link IHistoryEvent} with equal time are compared, the one with isFirstEvent() == true is treated as earlier.
	 * @return
	 */
	default boolean isFirstEvent() {
		return false;
	}

	/**
	 * @return
	 */
	String getType();

	/**
	 * @return
	 */
	Date getTime();

	/**
	 * @return
	 */
	String getUser();

	/**
	 * @return
	 */
	String getHtmlDescription();
}
