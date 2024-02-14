package de.regasus.profile.editor.overview;

import java.util.Comparator;
import java.util.function.Function;

import com.lambdalogic.util.ObjectComparator;

public class ComparableProfileParticipantLinkComparator implements Comparator<ComparableProfileParticipantLink> {

	private static ComparableProfileParticipantLinkComparator DEFAULT_INSTANCE;

	private ObjectComparator objectComparator;
	private Comparator<ComparableProfileParticipantLink> comparator;


	public static ComparableProfileParticipantLinkComparator getInstance() {
		if (DEFAULT_INSTANCE == null) {
			DEFAULT_INSTANCE = new ComparableProfileParticipantLinkComparator();
		}
		return DEFAULT_INSTANCE;
	}


	private ComparableProfileParticipantLinkComparator() {
		objectComparator = ObjectComparator.getInstance();

		/* Participant states should be ordered by their ID, except the IDs 0, 1 and 2.
		 * They should be ordered like this:
		 * 2: Registration
		 * 1: Online
		 * 0: Prospect
		 *
		 * Therefore the values of thisState and thatState are changed:
		 * 0 --> 2
		 * 2 --> 0
		 */
		Function<ComparableProfileParticipantLink, Long> participantStateFunction = new Function<>() {
			@Override
			public Long apply(ComparableProfileParticipantLink comparableProfileParticipantLink) {
				Long stateSortValue = comparableProfileParticipantLink.getParticipant().getParticipantStatePK();

				if (stateSortValue == 0L) {
					stateSortValue = 2L;
				}
				else if (stateSortValue == 2L) {
					stateSortValue = 0L;
				}

				return stateSortValue;
			}
		};


		comparator = Comparator
			.comparing(    ComparableProfileParticipantLink::getEventBeginDate, objectComparator)
			.thenComparing(ComparableProfileParticipantLink::getEventID,        objectComparator)
			.thenComparing(participantStateFunction,                            objectComparator)
			.thenComparing(ComparableProfileParticipantLink::getParticipantID,  objectComparator);

		comparator = Comparator.nullsFirst(comparator);
	}


	@Override
	public int compare(ComparableProfileParticipantLink value1, ComparableProfileParticipantLink value2) {
		return comparator.compare(value1, value2);
	}

}
