package de.regasus.person;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.lambdalogic.messeinfo.contact.AbstractPerson;
import com.lambdalogic.messeinfo.contact.data.Person_LastName_FirstName_Comparator;
import com.lambdalogic.messeinfo.kernel.PersonType;
import com.lambdalogic.messeinfo.participant.Participant;
import com.lambdalogic.util.CollectionsHelper;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.model.CacheModelOperation;
import com.lambdalogic.util.model.EntityNotFoundException;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.model.MICacheModel;
import de.regasus.model.Activator;
import de.regasus.participant.ParticipantModel;

public class PersonTreeModel
extends MICacheModel<Long, AbstractPerson>
implements CacheModelListener<Long> {

	private static PersonTreeModel singleton;

	private ParticipantModel participantModel;


	private PersonTreeModel() {
		participantModel = ParticipantModel.getInstance();
		participantModel.addListener(this);
	}


	public static PersonTreeModel getInstance() {
		if (singleton == null) {
			singleton = new PersonTreeModel();
		}
		return singleton;
	}


	@Override
	protected AbstractPerson getEntityFromServer(Long key) throws Exception {
		AbstractPerson abstractPerson = null;

		try {
			if (key != null) {
				PersonType personType = PersonType.getPersonTypeByPK(key);
				if (personType == PersonType.PARTICIPANT) {
					abstractPerson = participantModel.getParticipant(key);
				}
			}
		}
		catch (EntityNotFoundException e) {
			// ignore
		}

		return abstractPerson;
	}


	@Override
	protected List<AbstractPerson> getEntitiesFromServer(Collection<Long> participantIDs) throws Exception {
		List<AbstractPerson> abstractPersonList = CollectionsHelper.createArrayList( participantIDs.size() );

		try {
			List<Participant> participants = participantModel.getParticipants(participantIDs);
			abstractPersonList.addAll(participants);
		}
		catch (EntityNotFoundException e) {
			// ignore
		}

		return abstractPersonList;
	}


	@Override
	protected Long getKey(AbstractPerson entity) {
		return entity.getID();
	}


	@Override
	public void dataChange(CacheModelEvent<Long> event) {
		System.out.println("PersonTreeModel.dataChange() " + event);
		if (!serverModel.isLoggedIn()) {
			return;
		}

		try {
			if (event.getOperation() == CacheModelOperation.DELETE) {
				/* Load available entities and delete them.
				 * Both delete...OnServer() methods are overwritten but do nothing.
				 * So delete removes the deleted entities from the model and fires events as if the
				 * entities were deleted here.
				 */
				List<AbstractPerson> availablePersons = getEntitiesIfAvailable(event.getKeyList());
				delete(availablePersons);
			}
			else {
				refresh(event.getKeyList());
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	@Override
	protected boolean isForeignKeySupported() {
		return true;
	}


	@Override
	public Object getForeignKey(AbstractPerson abstractPerson) {
		/* Foreign key is the root of the tree.
		 * This is the group manager of a group or the main participant of a companion if they don't
		 * belong to a group.
		 */

		Long rootPK = null;

		if (abstractPerson != null) {
			if (abstractPerson instanceof Participant) {
				Participant participant = (Participant) abstractPerson;
				rootPK = participant.getRootPK();
			}
		}

		return rootPK;
	}


	@Override
	protected List<AbstractPerson> getEntitiesByForeignKeyFromServer(Object foreignKey) throws Exception {
		// cast the foreignKey
		Long rootPK = (Long) foreignKey;

		List<Participant> participantList = participantModel.getParticipantTreeByRootPK(rootPK);

		// determine the real root
		if (participantList != null && !participantList.isEmpty()) {
			Participant participant = participantList.get(0);
			rootPK = participant.getRootPK();
		}

		List<AbstractPerson> personTreeList = new ArrayList<AbstractPerson>(participantList.size() + 1);

		personTreeList.addAll(participantList);

		return personTreeList;
	}


	public List<AbstractPerson> getTreeData(Long rootPK) throws Exception {
		return getEntityListByForeignKey(rootPK);
	}


	public AbstractPerson getAbstractPerson(Long pk) throws Exception {
		AbstractPerson abstractPerson = null;

		try {
			abstractPerson = getEntity(pk);
		}
		catch (EntityNotFoundException e) {
			// ignore
		}

		return abstractPerson;
	}


	public List<Participant> getCompanions(Long participantPK) throws Exception {
		List<Participant> companionList = new ArrayList<Participant>();

		if (serverModel.isLoggedIn()) {
    		// get participant
    		Participant participant = participantModel.getParticipant(participantPK);
    		Long rootPK = participant.getRootPK();

    		// get the whole participant tree of participantPK
    		List<AbstractPerson> personTree = getTreeData(rootPK);

    		// collect the companions of participantPK
    		for (AbstractPerson abstractPerson : personTree) {
    			if (abstractPerson instanceof Participant) {
    				participant = (Participant) abstractPerson;
    				if (participant.isCompanion() && participant.getCompanionOfPK().equals(participantPK)) {
    					companionList.add(participant);
    				}
    			}
    		}

    		// sort companions
    		Collections.sort(companionList, Person_LastName_FirstName_Comparator.getInstance());
		}

		return companionList;
	}


	@Override
	protected void deleteEntityOnServer(AbstractPerson entity) throws Exception {
		// do nothing
	}


	@Override
	protected void deleteEntitiesOnServer(Collection<AbstractPerson> entityList) throws Exception {
		// do nothing
	}

}
