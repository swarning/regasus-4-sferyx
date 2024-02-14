package de.regasus.anonymize.dialog;

import java.util.Comparator;
import java.util.function.Function;

import com.lambdalogic.messeinfo.contact.Person;
import com.lambdalogic.messeinfo.participant.Participant;
import com.lambdalogic.messeinfo.profile.Profile;
import com.lambdalogic.util.ObjectComparator;

/**
 * Compare {@link Person} entities according to the requirements of {@link SelectedProfilesAndParticipantsReportWizardPage}.
 */
class ProfileAndParticipantComparator implements Comparator<Person> {

	private static ProfileAndParticipantComparator DEFAULT_INSTANCE;

	private ObjectComparator objectComparator;
	private Comparator<Person> comparator;


	public static ProfileAndParticipantComparator getInstance() {
		if (DEFAULT_INSTANCE == null) {
			DEFAULT_INSTANCE = new ProfileAndParticipantComparator();
		}
		return DEFAULT_INSTANCE;
	}


	private ProfileAndParticipantComparator() {
		objectComparator = ObjectComparator.getInstance();


		Function<Person, Long> personLinkFunction = new Function<Person, Long>() {
			@Override
			public Long apply(Person person) {
				// personLink: no personLink at the end (replace null with Long.MAX_VALUE)
				Long personLink = person.getPersonLink();
				if (personLink == null) {
					personLink = Long.MAX_VALUE;
				}
				return personLink;
			}
		};

		Function<Person, Integer> typeFunction = new Function<Person, Integer>() {
			@Override
			public Integer apply(Person person) {
				// type: Profile before Participant
				return person instanceof Profile ? 0 : 1;
			}
		};

		Function<Person, Long> eventFunction = new Function<Person, Long>() {
			@Override
			public Long apply(Person person) {
				return person instanceof Participant ? ((Participant) person).getEventId() : 0;
			}
		};

		comparator = Comparator
			.comparing(    personLinkFunction, objectComparator)
			.thenComparing(typeFunction,       objectComparator)
			.thenComparing(eventFunction,      objectComparator);

		comparator = Comparator.nullsFirst(comparator);
	}


	@Override
	public int compare(Person value1, Person value2) {
		return comparator.compare(value1, value2);
	}

}