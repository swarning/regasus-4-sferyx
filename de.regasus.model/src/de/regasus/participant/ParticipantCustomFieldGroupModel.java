package de.regasus.participant;

import static de.regasus.LookupService.getParticipantCustomFieldGroupMgr;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.lambdalogic.messeinfo.participant.ParticipantCustomFieldGroup;
import com.lambdalogic.messeinfo.participant.ParticipantCustomFieldGroup_Location_Position_Comparator;
import com.lambdalogic.messeinfo.participant.ParticipantCustomFieldGroupLocation;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.model.CacheModelOperation;

import de.regasus.core.OrderPosition;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.model.MICacheModel;
import de.regasus.event.EventModel;
import de.regasus.model.Activator;

public class ParticipantCustomFieldGroupModel
extends MICacheModel<Long, ParticipantCustomFieldGroup>
implements CacheModelListener<Long> {

	private static ParticipantCustomFieldGroupModel singleton;

	private EventModel eventModel;


	private ParticipantCustomFieldGroupModel() {
	}


	public static ParticipantCustomFieldGroupModel getInstance() {
		if (singleton == null) {
			singleton = new ParticipantCustomFieldGroupModel();
			singleton.initModels();
		}
		return singleton;
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
		eventModel = EventModel.getInstance();
		eventModel.addListener(this);
	}


	@Override
	protected Long getKey(ParticipantCustomFieldGroup entity) {
		return entity.getID();
	}


	@Override
	protected ParticipantCustomFieldGroup getEntityFromServer(Long participantCustomFieldGroupID)
	throws Exception {
		ParticipantCustomFieldGroup participantCustomFieldGroup = getParticipantCustomFieldGroupMgr().getParticipantCustomFieldGroup(participantCustomFieldGroupID);
		return participantCustomFieldGroup;
	}


	public ParticipantCustomFieldGroup getParticipantCustomFieldGroup(Long participantCustomFieldGroupID)
	throws Exception {
		return super.getEntity(participantCustomFieldGroupID);
	}


	@Override
	protected List<ParticipantCustomFieldGroup> getEntitiesFromServer(Collection<Long> participantCustomFieldGroupIDs)
	throws Exception {
		List<ParticipantCustomFieldGroup> participantCustomFieldGroups = getParticipantCustomFieldGroupMgr().getParticipantCustomFieldGroups(participantCustomFieldGroupIDs);
		return participantCustomFieldGroups;
	}


	public List<ParticipantCustomFieldGroup> getParticipantCustomFieldGroups(List<Long> participantCustomFieldGroupIDs)
	throws Exception {
		return super.getEntities(participantCustomFieldGroupIDs);
	}

	@Override
	protected ParticipantCustomFieldGroup createEntityOnServer(ParticipantCustomFieldGroup customFieldGroup)
	throws Exception {
		// temporarily set the position to pass validation
		customFieldGroup.setPosition(0);
		customFieldGroup.validate();
		// remove position because it should be calculated automatically by the server
		customFieldGroup.setPosition(null);

		customFieldGroup = getParticipantCustomFieldGroupMgr().create(customFieldGroup);
		return customFieldGroup;
	}


	@Override
	public ParticipantCustomFieldGroup create(ParticipantCustomFieldGroup participantCustomFieldGroup)
	throws Exception {
		return super.create(participantCustomFieldGroup);
	}


	public ParticipantCustomFieldGroup copyParticipantCustomFieldGroup(
		Long participantCustomFieldGroupPK,
		Long targetEventPK,
		ParticipantCustomFieldGroupLocation newLocation
	)
	throws Exception {
		// let the server do the copy work
		ParticipantCustomFieldGroup customFieldGroup = getParticipantCustomFieldGroupMgr().copy(
			participantCustomFieldGroupPK,	// sourceParticipantCustomFieldGroupID
			targetEventPK,					// targetEventPK
			newLocation						// newLocation
		);

		// add new data to model
		put(customFieldGroup);

		// inform listeners
		fireCreate( Collections.singletonList(customFieldGroup.getID()) );

		return customFieldGroup;
	}


	public ParticipantCustomFieldGroup copyFromProfileCustomFieldGroup(
		Long profileCustomFieldGroupPK,
		Long targetEventPK,
		ParticipantCustomFieldGroupLocation newLocation
	)
	throws Exception {
		ParticipantCustomFieldGroup customFieldGroup = getParticipantCustomFieldGroupMgr().copyFromProfileCustomFieldGroup(
			profileCustomFieldGroupPK,
			targetEventPK,
			newLocation
		);

		put(customFieldGroup);

		fireCreate( Collections.singletonList(customFieldGroup.getID()) );

		return customFieldGroup;
	}


	@Override
	protected ParticipantCustomFieldGroup updateEntityOnServer(ParticipantCustomFieldGroup participantCustomFieldGroup)
	throws Exception {
		participantCustomFieldGroup.validate();
		participantCustomFieldGroup = getParticipantCustomFieldGroupMgr().update(participantCustomFieldGroup);
		return participantCustomFieldGroup;
	}


	@Override
	public ParticipantCustomFieldGroup update(ParticipantCustomFieldGroup participantCustomFieldGroup)
	throws Exception {
		return super.update(participantCustomFieldGroup);
	}


	public void moveToLocation(Long movedGroupId, ParticipantCustomFieldGroupLocation targetLocation)
	throws Exception {
		List<ParticipantCustomFieldGroup> groups = getParticipantCustomFieldGroupMgr().moveToLocation(
			movedGroupId,
			targetLocation
		);

		put(groups);
		fireDataChange(CacheModelOperation.UPDATE, ParticipantCustomFieldGroup.getPKs(groups));
	}


	public void move(Long sourceGroupId, OrderPosition orderPosition, Long targetGroupId)
	throws Exception {
		List<ParticipantCustomFieldGroup> groups = getParticipantCustomFieldGroupMgr().move(
			sourceGroupId,
			orderPosition,
			targetGroupId
		);

		put(groups);
		fireDataChange(CacheModelOperation.UPDATE, ParticipantCustomFieldGroup.getPKs(groups));
	}


	@Override
	protected void deleteEntityOnServer(ParticipantCustomFieldGroup participantCustomFieldGroup)
	throws Exception {
		if (participantCustomFieldGroup != null) {
			getParticipantCustomFieldGroupMgr().deleteByPK(participantCustomFieldGroup.getID());
		}
	}


	@Override
	public void delete(ParticipantCustomFieldGroup participantCustomFieldGroup)
	throws Exception {
		super.delete(participantCustomFieldGroup);
	}


	@Override
	protected boolean isForeignKeySupported() {
		return true;
	}


	@Override
	protected Object getForeignKey(ParticipantCustomFieldGroup participantCustomFieldGroup) {
		Long eventPK = null;
		if (participantCustomFieldGroup != null) {
			eventPK = participantCustomFieldGroup.getEventPK();
		}
		return eventPK;
	}


	@Override
	protected List<ParticipantCustomFieldGroup> getEntitiesByForeignKeyFromServer(Object foreignKey)
	throws Exception {
		Long eventPK = (Long) foreignKey;

		List<ParticipantCustomFieldGroup> groupList =
			getParticipantCustomFieldGroupMgr().getParticipantCustomFieldGroupsByEventPK(eventPK);

		Collections.sort(groupList, ParticipantCustomFieldGroup_Location_Position_Comparator.getInstance());

		return groupList;
	}


	public List<ParticipantCustomFieldGroup> getParticipantCustomFieldGroupsByEventPK(Long eventPK)
	throws Exception {
		return getEntityListByForeignKey(eventPK);
	}


	@Override
	public void dataChange(CacheModelEvent<Long> event) {
		if (!serverModel.isLoggedIn()) {
			return;
		}

		try {
			if (event.getSource() == eventModel && event.getOperation() == CacheModelOperation.DELETE) {

				Collection<Long> deletedPKs = new ArrayList<>(event.getKeyList().size());

				for (Long eventPK : event.getKeyList()) {
					for (ParticipantCustomFieldGroup participantCustomFieldGroup : getLoadedAndCachedEntities()) {
						if (eventPK.equals(participantCustomFieldGroup.getEventPK())) {
							deletedPKs.add(participantCustomFieldGroup.getID());
						}
					}

					/* Remove the foreign key whose entity has been deleted from the model before firing the
					 * corresponding CacheModelEvent. The entities shall exist in the model when firing the
					 * CacheModelEvent, but not the structural information about the foreign keys. If a listener gets
					 * the CacheModelEvent and consequently requests the list of all entities of the foreign key, it
					 * shall get an empty list.
					 */
					removeForeignKeyData(eventPK);
				}

				if (!deletedPKs.isEmpty()) {
					fireDelete(deletedPKs);
					removeEntities(deletedPKs);
				}
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}

}
