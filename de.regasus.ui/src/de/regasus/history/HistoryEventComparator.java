package de.regasus.history;

import java.util.Comparator;

import com.lambdalogic.util.ObjectComparator;

public class HistoryEventComparator implements Comparator<IHistoryEvent> {

	private static HistoryEventComparator DEFAULT_INSTANCE;

	private ObjectComparator objectComparator;
	private Comparator<IHistoryEvent> comparator;


	public static HistoryEventComparator getInstance() {
		if (DEFAULT_INSTANCE == null) {
			DEFAULT_INSTANCE = new HistoryEventComparator();
		}
		return DEFAULT_INSTANCE;
	}


	private HistoryEventComparator() {
		objectComparator = ObjectComparator.getInstance();

		comparator = Comparator
			.comparing(    IHistoryEvent::isFirstEvent, objectComparator.reversed())
			.thenComparing(IHistoryEvent::getTime,      objectComparator)
			.thenComparing(IHistoryEvent::getType,      objectComparator)
			.thenComparing(IHistoryEvent::getUser,      objectComparator)
			.reversed();

		comparator = Comparator.nullsFirst(comparator);
	}


	@Override
	public int compare(IHistoryEvent value1, IHistoryEvent value2) {
		return comparator.compare(value1, value2);
	}

}
