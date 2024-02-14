package de.regasus.anonymize.dialog;

import static de.regasus.LookupService.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import com.lambdalogic.messeinfo.contact.Address;
import com.lambdalogic.messeinfo.contact.Person;
import com.lambdalogic.messeinfo.kernel.KernelLabel;
import com.lambdalogic.messeinfo.participant.Participant;
import com.lambdalogic.messeinfo.participant.data.ParticipantVO;
import com.lambdalogic.messeinfo.profile.Profile;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.participant.ParticipantModel;
import de.regasus.profile.ProfileModel;
import de.regasus.ui.Activator;


public abstract class AbstractSelectedProfilesAndParticipantsWizardPage extends WizardPage {

	// models
	protected ProfileModel profileModel;
	protected ParticipantModel participantModel;

	protected List<Long> profilePKs = Collections.emptyList();
	protected List<Long> participantPKs = Collections.emptyList();
	protected List<Profile> profileList = Collections.emptyList();
	protected List<Participant> participantList = Collections.emptyList();


	protected ProfileParticipantTable profileParticipantTable;

	protected boolean selectable;


	public AbstractSelectedProfilesAndParticipantsWizardPage(String name, boolean selectable) {
		super(name);

		this.selectable = selectable;

		// init models
		profileModel = ProfileModel.getInstance();
		participantModel = ParticipantModel.getInstance();
	}


	protected ProfileParticipantTable createProfileParticipantTable(Table table) {
		return new ProfileParticipantTable(table);
	}


	/**
	 * Create contents of the wizard
	 * @param parent
	 */
	@Override
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		container.setLayout(new FillLayout());
		//
		setControl(container);

		// determine style parameter for table depending on the value of selectable
		int tableStyle = SWT.BORDER;
		if (selectable) {
			tableStyle |= SWT.CHECK;
		}

		Table table = new Table(container, tableStyle);
		table.setHeaderVisible(true);

		// show only vertical lines but no horizontal ones (alternating row colors)
		// turn off lines ...
		table.setLinesVisible(true);

		// create TabbleColumns according to ProfileParticipantTable.ProfileParticipantTableColumns

		// Type
		TableColumn typeTableColumn = new TableColumn(table, SWT.NONE);
		typeTableColumn.setWidth(70);
		typeTableColumn.setText(KernelLabel.Type.getString());

		// First Name
		TableColumn firstNameTableColumn = new TableColumn(table, SWT.NONE);
		firstNameTableColumn.setWidth(150);
		firstNameTableColumn.setText(Person.FIRST_NAME.getString());

		// Last Name
		TableColumn lastNameTableColumn = new TableColumn(table, SWT.NONE);
		lastNameTableColumn.setWidth(200);
		lastNameTableColumn.setText(Person.LAST_NAME.getString());

		// City
		TableColumn cityTableColumn = new TableColumn(table, SWT.NONE);
		cityTableColumn.setWidth(100);
		cityTableColumn.setText( Address.CITY.getString() );

		// Organisation
		TableColumn organisationTableColumn = new TableColumn(table, SWT.NONE);
		organisationTableColumn.setWidth(150);
		organisationTableColumn.setText( Address.ORGANISATION.getString() );

		// Event
		TableColumn eventTableColumn = new TableColumn(table, SWT.NONE);
		eventTableColumn.setWidth(100);
		eventTableColumn.setText( Participant.EVENT.getString() );

		// Participant State
		TableColumn paStateTableColumn = new TableColumn(table, SWT.NONE);
		paStateTableColumn.setWidth(100);
		paStateTableColumn.setText( Participant.PARTICIPANT_STATE.getString() );


		profileParticipantTable = createProfileParticipantTable(table);

		if (selectable) {
			TableViewer tableViewer = profileParticipantTable.getViewer();
			if (tableViewer instanceof CheckboxTableViewer) {
				((CheckboxTableViewer) tableViewer).addCheckStateListener(new ICheckStateListener() {
					@Override
					public void checkStateChanged(CheckStateChangedEvent event) {
						setPageComplete(isPageComplete());
					}
				});
			}
		}
	}


	public void init(List<Long> profilePKs, List<Long> participantPKs) {
		try {
			if (profilePKs == null) {
				profilePKs = Collections.emptyList();
			}
			this.profilePKs = profilePKs;

			if (participantPKs == null) {
				participantPKs = Collections.emptyList();
			}
			this.participantPKs = participantPKs;


			profileList = profileModel.getProfiles(profilePKs);
			participantList = participantModel.getParticipants(participantPKs);

			Set<Long> personLinkSet = collectPersonLinks(profileList, participantList);

			// load connected Participants and Profiles
			profileList = findMissingProfiles(profileList, personLinkSet);
			participantList = findMissingParticipants(participantList, personLinkSet);

			// unite Profiles and Participants
			List<Person> persons = new ArrayList<>(profileList.size() + participantList.size());
			persons.addAll(profileList);
			persons.addAll(participantList);

			// order Persons by personLink, type (Profiles before Participants) and Event ID
			Collections.sort(persons, ProfileAndParticipantComparator.getInstance());

			profileParticipantTable.setInput(persons);

			// update global PKs of Profiles and Participants
			this.profilePKs = Profile.getPrimaryKeyList(profileList);
			this.participantPKs = Participant.getPrimaryKeyList(participantList);
		}
		catch (Throwable t) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t);
		}
	}


	/**
	 * Collect personLinks of all Profiles and Participants.
	 * @param profileList
	 * @param participantList
	 * @return personLinks of passed Profiles and Participants
	 */
	protected Set<Long> collectPersonLinks(List<Profile> profileList, List<Participant> participantList) {
		Set<Long> personLinkSet = new HashSet<>();
		for (Profile profile : profileList) {
			if (profile.getPersonLink() != null) {
				personLinkSet.add( profile.getPersonLink() );
			}
		}
		for (Participant participant : participantList) {
			if (participant.getPersonLink() != null) {
				personLinkSet.add( participant.getPersonLink() );
			}
		}
		return personLinkSet;
	}


	protected List<Profile> findMissingProfiles(List<Profile> profileList, Collection<Long> personLinks) throws Exception {
		List<Profile> newProfileList = new ArrayList<>(profileList);

		// add missing Profiles
		for (Long personLink : personLinks) {
			// check if Profile is already there (there is only 1 Profile for each personLink)
			boolean profileFound = false;
			for (Profile profile : profileList) {
				if (personLink.equals(profile.getPersonLink())) {
					profileFound = true;
					break;
				}
			}
			if (!profileFound) {
				Profile profile = getProfileMgr().findByPersonLink(personLink);
				if (profile != null) {
					newProfileList.add(profile);
				}
			}
		}

		return newProfileList;
	}


	protected List<Participant> findMissingParticipants(List<Participant> participantList, Collection<Long> personLinks)
	throws Exception {
		Set<Long> allParticipantPKs = new HashSet<>();
		allParticipantPKs.addAll(participantPKs);

		// load all Participants of all personLinks and collect their PKs
		for (Long personLink : personLinks) {
			// load Participants of current personLink from server
			List<ParticipantVO> additionalParticipantVOs = getParticipantMgr().getParticipantVOsByPersonLink(
				personLink,
				null // days
			);

			// add PKs of additional Participants
			for (ParticipantVO participantVO : additionalParticipantVOs) {
				allParticipantPKs.add(participantVO.getID());
			}
		}

		// load all Participants via Model
		return participantModel.getParticipants(allParticipantPKs);
	}


	public List<Long> getProfilePKs() {
		return profilePKs;
	}


	public List<Long> getParticipantPKs() {
		return participantPKs;
	}


	public List<Long> getCheckedProfilePKs() {
		List<Long> checkedProfilePKs = new ArrayList<>(profilePKs.size());
		TableViewer tableViewer = profileParticipantTable.getViewer();
		if (tableViewer instanceof CheckboxTableViewer) {
			Object[] checkedElements = ((CheckboxTableViewer) tableViewer).getCheckedElements();
			for (Object element : checkedElements) {
				if (element instanceof Profile) {
					checkedProfilePKs.add( ((Profile) element).getID() );
				}
			}
		}
		return checkedProfilePKs;
	}


	public List<Long> getCheckedParticipantPKs() {
		List<Long> checkedParticipantPKs = new ArrayList<>(participantPKs.size());
		TableViewer tableViewer = profileParticipantTable.getViewer();
		if (tableViewer instanceof CheckboxTableViewer) {
			Object[] checkedElements = ((CheckboxTableViewer) tableViewer).getCheckedElements();
			for (Object element : checkedElements) {
				if (element instanceof Participant) {
					checkedParticipantPKs.add( ((Participant) element).getID() );
				}
			}
		}
		return checkedParticipantPKs;
	}

}
