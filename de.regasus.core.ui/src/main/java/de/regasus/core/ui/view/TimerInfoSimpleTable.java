package de.regasus.core.ui.view;

import java.text.DateFormat;

import org.eclipse.swt.widgets.Table;

import com.lambdalogic.messeinfo.invoice.export.TimerInfo;
import com.lambdalogic.util.rcp.simpleviewer.SimpleTable;

/**
 * The getter of the SimpleTable control their behaviour by this enum, which gives them the information which column is
 * about to be shown.
 *
 * @author manfred
 *
 */
enum ETimerInfoTableColumns {
	NEXT_TIMEOUT, TIME_REMAINING, INFO;
};

public class TimerInfoSimpleTable extends SimpleTable<TimerInfo, ETimerInfoTableColumns> {

	private DateFormat dateTimeFormat = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM);


	public TimerInfoSimpleTable(Table table) {
		super(table, ETimerInfoTableColumns.class);
	}


	@Override
	public String getColumnText(TimerInfo object, ETimerInfoTableColumns column) {
		switch (column) {
		case INFO:
			return object.getInfo() == null ? "- none -" : object.getInfo().toString();
		case NEXT_TIMEOUT:
			return dateTimeFormat.format(object.getNextTimeout());
		case TIME_REMAINING:
			long seconds = object.getTimeRemaining() / 1000;
			return (seconds / 3600) + ":" + ((seconds % 3600) / 60) + ":" + (seconds % 60);
		}
		return null;
	}


	@Override
	protected Comparable<? extends Object> getColumnComparableValue(TimerInfo object, ETimerInfoTableColumns column) {
		switch (column) {
		case NEXT_TIMEOUT:
			return object.getNextTimeout();
		case TIME_REMAINING:
			return new Long(object.getTimeRemaining());
		default:
			return super.getColumnComparableValue(object, column);
		}
	}

}
