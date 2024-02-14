package de.regasus.anonymize.dialog;

import java.util.Collection;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Table;

import com.lambdalogic.messeinfo.contact.Person;
import com.lambdalogic.messeinfo.participant.Participant;
import com.lambdalogic.messeinfo.participant.ParticipantLabel;
import com.lambdalogic.messeinfo.participant.ParticipantState;
import com.lambdalogic.messeinfo.participant.data.EventVO;
import com.lambdalogic.messeinfo.profile.Profile;
import com.lambdalogic.messeinfo.profile.ProfileLabel;
import com.lambdalogic.util.rcp.ColorHelper;
import com.lambdalogic.util.rcp.simpleviewer.SimpleTable;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.event.EventModel;
import de.regasus.participant.ParticipantStateModel;
import de.regasus.ui.Activator;

enum ProfileParticipantTableColumns {TYPE, FIRST_NAME, LAST_NAME, CITY, ORGANISATION, EVENT, PARTICIPANT_STATE}

public class ProfileParticipantTable extends SimpleTable<Person, ProfileParticipantTableColumns> {

	public static final String GROUP_KEY = "group";

	protected EventModel eventModel;
	protected ParticipantStateModel paStateModel;


	public ProfileParticipantTable(Table table) {
		super(
			table,
			ProfileParticipantTableColumns.class,
			false,	// sortable
			false	// editable
		);

		eventModel = EventModel.getInstance();
		paStateModel = ParticipantStateModel.getInstance();
	}


	@Override
	public void setInput(Collection<Person> personList) {
		initBackgroundColor(personList);

		super.setInput(personList);
	}


	protected void initBackgroundColor(Collection<Person> personList) {
		boolean color = false;
		Long lastPersonLink = null;
		for (Person person : personList) {
			Long personLink = person.getPersonLink();

			boolean changeColor = true;
			if (lastPersonLink != null) {
				changeColor = !lastPersonLink.equals(personLink);
			}
			lastPersonLink = personLink;

			if (changeColor) {
				color = !color;
			}

			person.put(ProfileParticipantTable.GROUP_KEY, color);
		}
	}


	@Override
	public String getColumnText(Person person, ProfileParticipantTableColumns column) {
		String label = null;

		switch (column) {
			case TYPE:
				if (person instanceof Profile) {
					label = ProfileLabel.Profile.toString();
				}
				else {
					label = ParticipantLabel.Participant.toString();
				}
				break;
			case FIRST_NAME:
				label = person.getFirstName();
				break;
			case LAST_NAME:
				label = person.getLastName();
				break;
			case CITY:
				label = person.getCity();
				break;
			case ORGANISATION:
				label = person.getOrganisation();
				break;
			case EVENT:
				try {
					if (person instanceof Participant) {
						Long eventPK = ((Participant) person).getEventId();
						EventVO eventVO = eventModel.getEventVO(eventPK);
						label = eventVO.getMnemonic();
					}
				}
				catch (Exception e) {
					System.err.println(e);
				}
				break;
			case PARTICIPANT_STATE:
				try {
					if (person instanceof Participant) {
						Long paStatePK = ((Participant) person).getParticipantStatePK();
						ParticipantState paState = paStateModel.getParticipantState(paStatePK);
						label = paState.getName().getString();
					}
				}
				catch (Exception e) {
					System.err.println(e);
				}
				break;

		}

		if (label == null) {
			label = "";
		}

		return label;
	}


	@Override
	public Color getBackground(Object element, int columnIndex) {
		Color color = ColorHelper.getSystemColor(SWT.COLOR_WHITE);

		try {

			Object colorValue = ((Person) element).get(GROUP_KEY);
			boolean lightColor = colorValue == Boolean.TRUE;

			if (lightColor) {
				color = ColorHelper.GRAY_1;
			}
			else {
				color = ColorHelper.GRAY_2;
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}

		return color;
	}

}
