package de.regasus.event;

import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;

import org.eclipse.swt.widgets.Table;

import com.lambdalogic.messeinfo.participant.data.EventVO;
import com.lambdalogic.util.rcp.simpleviewer.SimpleTable;

enum EventTableColumns {LABEL, MNEMONIC, START_TIME, END_TIME}

public class EventTable extends SimpleTable<EventVO, EventTableColumns> {

	private DateTimeFormatter dateTimeFormatter;


	public EventTable(Table table) {
		super(table, EventTableColumns.class);
		dateTimeFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT);
	}



	@Override
	public String getColumnText(EventVO eventVO, EventTableColumns column) {
		String label = null;
		switch (column) {
		case LABEL:
			label = eventVO.getLabel( Locale.getDefault() );
			break;
		case MNEMONIC:
			label = eventVO.getMnemonic();
			break;
		case START_TIME:
			if (eventVO.getBeginDate() != null) {
				label = eventVO.getBeginDate().format(dateTimeFormatter);
			}
			break;
		case END_TIME:
			if (eventVO.getEndDate() != null) {
				label = eventVO.getEndDate().format(dateTimeFormatter);
			}
			break;
		}

		if (label == null) {
			label = "";
		}

		return label;
	}

	@Override
	protected Comparable<?> getColumnComparableValue(EventVO eventVO, EventTableColumns column) {
		/* Für Werte, die nicht vom Typ String sind, die Originalwerte (z.B. Date, Integer)
		 * zurückgeben.
		 * Werte vom Typ String können pauschal über super.getColumnComparableValue(eventVO, column)
		 * zurückgegeben werden, weil sie Sortierwerte den angezeigten entsprechen.
		 */
		switch (column) {
		case START_TIME:
			return eventVO.getStartTime();
		case END_TIME:
			return eventVO.getEndTime();
		default:
			return super.getColumnComparableValue(eventVO, column);
		}
	}

	@Override
	protected EventTableColumns getDefaultSortColumn() {
		EventTableColumns defaultSortColumn = null;
		if (getCurrentSortColumn() != null) {
			defaultSortColumn = getCurrentSortColumn();
		}
		else {
			defaultSortColumn = EventTableColumns.LABEL;
		}

		return defaultSortColumn;
	}

}
