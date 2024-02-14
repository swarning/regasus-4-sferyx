package de.regasus.person;

import static de.regasus.LookupService.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.lambdalogic.messeinfo.participant.Participant;
import com.lambdalogic.messeinfo.profile.PersonLinkData;
import com.lambdalogic.messeinfo.profile.Profile;
import com.lambdalogic.util.CollectionsHelper;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.model.CacheModelOperation;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.model.MICacheModel;
import de.regasus.model.Activator;
import de.regasus.participant.ParticipantModel;
import de.regasus.profile.ProfileModel;


public class PersonLinkModel extends MICacheModel<Long, PersonLinkData> implements CacheModelListener<Long> {
	/**
	 * Size of the cache for single entities.
	 */
	public static final int ENTITY_CACHE_SIZE = 10000;

	/**
	 * Size of the foreign key cache is 0, because this model doesn't use it.
	 */
	public static final int FOREIGN_KEY_CACHE_SIZE = 0;


	private static PersonLinkModel singleton = null;


	// manager

	// models
	private ProfileModel profileModel;
	private ParticipantModel participantModel;


	public static PersonLinkModel getInstance() {
		if (singleton == null) {
			singleton = new PersonLinkModel();
			singleton.initModels();
		}
		return singleton;
	}


	private PersonLinkModel() {
		super(ENTITY_CACHE_SIZE, FOREIGN_KEY_CACHE_SIZE);

	}


	/**
	 * Initialize references to other Models.
	 * Models are initialized outside the constructor to avoid OutOfMemoryErrors when two Models
	 * reference each other.
	 * This happens because the variable is set after the constructor is finished.
	 * If the constructor calls getInstance() of another Model that calls getInstance() of this Model,
	 * the variable instance is still null. So this Model would be created again and so on.
	 * To avoid this, the constructor has to finish before calling getInstance() of another Model.
	 * The initialization of references to other Models is done in getInstance() right after
	 * the constructor has finished.
	 */
	private void initModels() {
		profileModel = ProfileModel.getInstance();
		profileModel.addListener(this);

		participantModel = ParticipantModel.getInstance();
		participantModel.addListener(this);
	}


	@Override
	protected boolean isForeignKeySupported() {
		return false;
	}


	@Override
	protected Long getKey(PersonLinkData personLinkData) {
		Long personLink = null;
		if (personLinkData != null) {
			personLink = personLinkData.getPersonLink();
		}
		return personLink;
	}


	@Override
	protected PersonLinkData getEntityFromServer(Long personLink) throws Exception {
		PersonLinkData personLinkData = getProfileMgr().getPersonLinkData(personLink);
		return personLinkData;
	}


	@Override
	protected List<PersonLinkData> getEntitiesFromServer(Collection<Long> personLinks) throws Exception {
		List<PersonLinkData> personLinkDataList = getProfileMgr().getPersonLinkData(personLinks);
		return personLinkDataList;
	}


	public PersonLinkData getPersonLinkData(Long personLink) throws Exception {
		PersonLinkData personLinkData = null;
		if (personLink != null) {
			personLinkData = super.getEntity(personLink);
		}
		return personLinkData;
	}


	public List<PersonLinkData> getPersonLinkDatas(Collection<Long> personLinkIDs) throws Exception {
		return super.getEntities(personLinkIDs);
	}





	public void link(Long profileID, Long participantID, boolean forceNewLink) throws Exception {
		Set<Long> refreshPersonLinks = CollectionsHelper.createHashSet(3);

		// get current personLink of the Profile
		Profile profile = profileModel.getProfile(profileID);
		Long profilePersonLink = profile.getPersonLink();
		if (profilePersonLink != null) {
			refreshPersonLinks.add(profilePersonLink);
		}

		// get current personLink of the Participant
		Participant participant = participantModel.getParticipant(participantID);
		Long participantPersonLink = participant.getPersonLink();
		if (participantPersonLink != null) {
			refreshPersonLinks.add(participantPersonLink);
		}

		// link Profile and Participant
		Long newPersonLink = getProfileMgr().link(profileID, participantID, forceNewLink);
		refreshPersonLinks.add(newPersonLink);

		if ( ! refreshPersonLinks.isEmpty()) {
			refresh(refreshPersonLinks);
		}
	}


	public void unlinkProfile(Long profileID) throws Exception {
		Long formerPersonLink = getProfileMgr().updatePersonLink(profileID, null);
		if (formerPersonLink != null) {
			refresh(formerPersonLink);
		}
	}


	public void unlinkParticipant(Long participantID) throws Exception {
		Participant participant = participantModel.getParticipant(participantID);
		Long oldPersonLink = participant.getPersonLink();

		getParticipantMgr().updatePersonLink(participantID, null);

		if (oldPersonLink != null) {
			refresh(oldPersonLink);
		}
	}


	@Override
	public void dataChange(CacheModelEvent<Long> event) {
		if (!serverModel.isLoggedIn()) {
			return;
		}

		try {
			// The entity of this editor was changed somehow.
			if (event.getSource() == profileModel) {
				dataChangeProfileModel(event);
			}
			else if (event.getSource() == participantModel) {
				dataChangeParticipantModel(event);
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	private void dataChangeProfileModel(CacheModelEvent<Long> event) throws Exception {
		/* CacheModelOperation.REFRESH and UPDATE can be ignored, because they have no effect on the
		 * Profile's personLink.
		 */
		if (event.getOperation() == CacheModelOperation.CREATE ||
			event.getOperation() == CacheModelOperation.DELETE
		) {
			// Get all created or deleted Profiles and collect their personLinks.
			List<Long> profileIDs = event.getKeyList();
			List<Profile> profiles = profileModel.getProfiles(profileIDs);
			List<Long> personLinks = new ArrayList<Long>(profiles.size());
			for (Profile profile : profiles) {
				Long personLink = profile.getPersonLink();
				if (personLink != null) {
					personLinks.add(personLink);
				}
			}

			// refresh data of the personLinks of all created or deleted Profiles
			refresh(personLinks);
		}
	}


	private void dataChangeParticipantModel(CacheModelEvent<Long> event) throws Exception {
		/* CacheModelOperation.REFRESH and UPDATE can be ignored, because they have no effect on the
		 * Participant's personLink.
		 */
		if (event.getOperation() == CacheModelOperation.CREATE ||
			event.getOperation() == CacheModelOperation.DELETE
		) {
			// Get all created or deleted Participants and collect their personLinks.
			List<Long> participantIDs = event.getKeyList();
			List<Participant> participants = participantModel.getParticipants(participantIDs);
			List<Long> personLinks = new ArrayList<Long>(participants.size());
			for (Participant participant : participants) {
				Long personLink = participant.getPersonLink();
				if (personLink != null) {
					personLinks.add(personLink);
				}
			}

			// remove deleted participantIDs from the loaded PersonLinkData
			if (event.getOperation() == CacheModelOperation.DELETE) {
				Collection<PersonLinkData> loadedPersonLinkDataList = getLoadedEntities();
				for (PersonLinkData personLinkData : loadedPersonLinkDataList) {
					if (personLinks.contains(personLinkData.getPersonLink())) {
						personLinkData.getParticipantIDs().removeAll(participantIDs);
					}
				}
			}

			// refresh data of the personLinks of all created or deleted Participants
			refresh(personLinks);
		}
	}

}
