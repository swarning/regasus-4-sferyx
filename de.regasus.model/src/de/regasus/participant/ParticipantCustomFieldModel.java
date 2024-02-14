package de.regasus.participant;

import static com.lambdalogic.util.CollectionsHelper.*;
import static de.regasus.LookupService.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.lambdalogic.messeinfo.participant.ParticipantCustomField;
import com.lambdalogic.messeinfo.participant.ParticipantCustomFieldGroup;
import com.lambdalogic.messeinfo.participant.ParticipantCustomFieldGroup_Location_Position_Comparator;
import com.lambdalogic.messeinfo.participant.ParticipantCustomFieldSettings;
import com.lambdalogic.util.EqualsHelper;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.model.CacheModelOperation;

import de.regasus.core.OrderPosition;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.model.MICacheModel;
import de.regasus.event.EventModel;
import de.regasus.model.Activator;


/**
 * Model that manages {@link ParticipantCustomField}s.
 * The {@link ParticipantCustomField}s contain {@link ParticipantCustomFieldGroup} that is loaded from
 * {@link ParticipantCustomFieldGroupModel} and not from the server directly.
 * {@link ParticipantCustomFieldModel} is observing {@link ParticipantCustomFieldGroupModel}. So if a
 * {@link ParticipantCustomFieldGroup} changes, it will be replaced in the {@link ParticipantCustomField}s and
 * {@link ParticipantCustomFieldModel} will fire a {@link CacheModelEvent}, too.
 */
public class ParticipantCustomFieldModel extends MICacheModel<Long, ParticipantCustomField> {

	private static ParticipantCustomFieldModel singleton;

    private static final ParticipantCustomFieldSettings SETTINGS = new ParticipantCustomFieldSettings();
    static {
		SETTINGS.withParticipantCustomFieldGroup = true;
    }

	private EventModel eventModel;
	private ParticipantCustomFieldGroupModel groupModel;


	private CacheModelListener<Long> groupModelListener = new CacheModelListener<Long>() {
		@Override
		public void dataChange(CacheModelEvent<Long> event) {
			try {
				// update affected groups in all custom fields
				CacheModelOperation op = event.getOperation();

				if (op == CacheModelOperation.REFRESH || op == CacheModelOperation.UPDATE) {
					/* Update PartcipantCustomField.group but don't fire a CacheModelEvent!
					 * This would cause a refresh of corresponding PartcipantCustomFieldEditors which might lose edited data.
					 * Clients who are interested of changes of PartcipantCustomFieldGroups have to observe the
					 * PartcipantCustomFieldGroupModel directly.
					 */
					List<Long> refreshedKeyList = createArrayList();
					for (Long groupPK : event.getKeyList()) {
						ParticipantCustomFieldGroup group = groupModel.getParticipantCustomFieldGroup(groupPK);

						for (ParticipantCustomField customField : getLoadedAndCachedEntities()) {
							if (customField.getGroupPK().equals(groupPK)) {
								customField.setParticipantCustomFieldGroup(group);
								refreshedKeyList.add(customField.getID());
							}
						}
					}

					// don't fire CacheModelEvent (see above)
					// fireRefresh(refreshedKeyList);
				}
				else if (op == CacheModelOperation.CREATE) {
					/* Why should we handle this CacheModelOperation at all?
					 * The out-commented code causes a refresh of all PartcipantCustomFieldEditors of the same Event
					 * which might loose edited data!
					 */

//					// determine Events of created Groups
//					Set<Long> eventPKs = new HashSet<>();
//					for (Long groupPK : event.getKeyList()) {
//						ParticipantCustomFieldGroup group = groupModel.getParticipantCustomFieldGroup(groupPK);
//						eventPKs.add(group.getEventPK());
//					}
//
//					// reload Custom Fields of affected Events
//					for (Long eventPK : eventPKs) {
//						refreshForeignKey(eventPK);
//					}
				}
				else if (op == CacheModelOperation.DELETE) {
					// determine Custom Fields of deleted Groups
					List<ParticipantCustomField> deletedCustomFields = new ArrayList<>();
					for (Long groupPK : event.getKeyList()) {
						for (ParticipantCustomField customField : getLoadedAndCachedEntities()) {
							if (customField.getGroupPK().equals(groupPK)) {
								deletedCustomFields.add(customField);
							}
						}
					}

					handleDelete(deletedCustomFields, true /*fireCoModelEvent*/);
				}
			}
			catch (Exception e) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			}
		}
	};


	private CacheModelListener<Long> eventModelListener = new CacheModelListener<Long>() {
		@Override
		public void dataChange(CacheModelEvent<Long> event) {
			if (!serverModel.isLoggedIn()) {
				return;
			}

			try {
				if (event.getOperation() == CacheModelOperation.DELETE) {

					Collection<Long> deletedPKs = new ArrayList<>(event.getKeyList().size());

					for (Long eventPK : event.getKeyList()) {
						for (ParticipantCustomField participantCustomField : getLoadedAndCachedEntities()) {
							if (eventPK.equals(participantCustomField.getEventPK())) {
								deletedPKs.add(participantCustomField.getID());
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
	};


	private ParticipantCustomFieldModel() {
	}


	public static ParticipantCustomFieldModel getInstance() {
		if (singleton == null) {
			singleton = new ParticipantCustomFieldModel();
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
		eventModel.addListener(eventModelListener);

		groupModel = ParticipantCustomFieldGroupModel.getInstance();

		/* observe ParticipantCustomFieldGroupModel
		 * If a ParticipantCustomFieldGroup is refreshed or updated, replace it in every ParticipantCustomField.
		 */
		groupModel.addListener(groupModelListener);
	}


	/**
	 * Add ParticipantCustomFieldGroup to ParticipantCustomField.
	 * The ParticipantCustomFieldGroup is NOT loaded from the server but from the ParticipantCustomFieldGroupModel!
	 *
	 * @param customFieldList
	 * @throws Exception
	 */
	private void enrich(ParticipantCustomField customField) throws Exception {
		if (customField != null) {
			// load all groups into cache
			groupModel.getParticipantCustomFieldGroupsByEventPK( customField.getEventPK() );

			customField.setParticipantCustomFieldSettings(SETTINGS);

			ParticipantCustomFieldGroup group = groupModel.getParticipantCustomFieldGroup( customField.getGroupPK() );
			customField.setParticipantCustomFieldGroup(group);
		}
	}


	/**
	 * Add ParticipantCustomFieldGroups to List of ParticipantCustomField.
	 * The ParticipantCustomFieldGroups are NOT loaded from the server but from the ParticipantCustomFieldGroupModel!
	 *
	 * @param customFieldList
	 * @throws Exception
	 */
	private void enrich(List<ParticipantCustomField> customFieldList) throws Exception {
		if (notEmpty(customFieldList)) {
			// load all groups into cache
			groupModel.getParticipantCustomFieldGroupsByEventPK( customFieldList.get(0).getEventPK() );

			for (ParticipantCustomField customField : customFieldList) {
				customField.setParticipantCustomFieldSettings(SETTINGS);

				ParticipantCustomFieldGroup group = groupModel.getParticipantCustomFieldGroup(customField.getGroupPK());
				customField.setParticipantCustomFieldGroup(group);
			}
		}
	}


	@Override
	protected Long getKey(ParticipantCustomField entity) {
		return entity.getID();
	}


	@Override
	protected ParticipantCustomField getEntityFromServer(Long customFieldID)
	throws Exception {
		ParticipantCustomField customField = getParticipantCustomFieldMgr().getParticipantCustomField(
			customFieldID
		);

		enrich(customField);

		return customField;
	}


	public ParticipantCustomField getParticipantCustomField(Long customFieldID)
	throws Exception {
		return super.getEntity(customFieldID);
	}


	@Override
	protected List<ParticipantCustomField> getEntitiesFromServer(Collection<Long> customFieldIDs)
	throws Exception {
		List<ParticipantCustomField> participantCustomFields = getParticipantCustomFieldMgr().getParticipantCustomFields(
			customFieldIDs
		);

		enrich(participantCustomFields);

		return participantCustomFields;
	}


	public List<ParticipantCustomField> getParticipantCustomFields(List<Long> customFieldIDs)
	throws Exception {
		return super.getEntities(customFieldIDs);
	}


	public boolean existParticipantCustomFieldValue(Collection<Long> customFieldPKs) throws Exception {
		boolean result = getParticipantCustomFieldValueMgr().existParticipantCustomFieldValue(customFieldPKs);
		return result;
	}


	@Override
	protected ParticipantCustomField createEntityOnServer(ParticipantCustomField customField)
	throws Exception {
		// temporarily set the position to pass validation
		customField.setPosition(0);
		customField.validate();
		// remove position because it should be calculated automatically by the server
		customField.setPosition(null);

		customField = getParticipantCustomFieldMgr().create(customField);

		// add additional data
		enrich(customField);

		return customField;
	}


	@Override
	public ParticipantCustomField create(ParticipantCustomField participantCustomField)
	throws Exception {
		return super.create(participantCustomField);
	}


	public ParticipantCustomField copyParticipantCustomField(
		Long customFieldID,
		Long eventID,
		Long customFieldGroupID
	)
	throws Exception {
		// let the server do the copy work
		ParticipantCustomField customField = getParticipantCustomFieldMgr().copy(
			customFieldID,		// sourceParticipantCustomFieldID
			customFieldGroupID	// destParticipantCustomFieldGroupID
		);

		enrich(customField);

		// add new data to model
		put(customField);

		// inform listeners
		fireCreate( Collections.singletonList(customField.getID()) );

		return customField;
	}


	public ParticipantCustomField copyFromProfileCustomField(
		Long profileCustomFieldId,
		Long targetParticipantGroupId
	)
	throws Exception {
		ParticipantCustomField customField = getParticipantCustomFieldMgr().copyFromProfileCustomField(
			profileCustomFieldId,
			targetParticipantGroupId
		);

		enrich(customField);

		put(customField);

		fireCreate( Collections.singletonList(customField.getID()) );

		return customField;
	}


	@Override
	protected ParticipantCustomField updateEntityOnServer(ParticipantCustomField customField)
	throws Exception {
		customField.validate();
		customField = getParticipantCustomFieldMgr().update(customField);

		/* Enrich even here, because after update customField contains only the lazy-loading-dummy, no matter what the
		 * original parameter contained.
		 */
		enrich(customField);

		return customField;
	}


	@Override
	public ParticipantCustomField update(ParticipantCustomField participantCustomField)
	throws Exception {
		return super.update(participantCustomField);
	}


	public void moveToGroup(Long movedCustomFieldId, Long targetCustomFieldGroupId)
	throws Exception {
		List<ParticipantCustomField> customFields = getParticipantCustomFieldMgr().moveToGroup(
			movedCustomFieldId,
			targetCustomFieldGroupId
		);

		enrich(customFields);
		put(customFields);
		fireDataChange(CacheModelOperation.UPDATE, ParticipantCustomField.getPKs(customFields));
	}


	public void move(Long movedCustomFieldId, OrderPosition orderPosition, Long targetCustomFieldId)
	throws Exception {
		List<ParticipantCustomField> customFields = getParticipantCustomFieldMgr().move(
			movedCustomFieldId,
			orderPosition,
			targetCustomFieldId
		);

		enrich(customFields);
		put(customFields);
		fireDataChange(CacheModelOperation.UPDATE, ParticipantCustomField.getPKs(customFields));
	}


	@Override
	protected void deleteEntityOnServer(ParticipantCustomField customField)
	throws Exception {
		if (customField != null) {
			getParticipantCustomFieldMgr().deleteByPK(customField.getID());
		}
	}


	@Override
	public void delete(ParticipantCustomField customField)
	throws Exception {
		super.delete(customField);
	}


	@Override
	protected boolean isForeignKeySupported() {
		return true;
	}


	@Override
	protected Object getForeignKey(ParticipantCustomField customField) {
		Long eventPK = null;
		if (customField != null) {
			eventPK = customField.getEventPK();
		}
		return eventPK;
	}


	@Override
	protected List<ParticipantCustomField> getEntitiesByForeignKeyFromServer(Object foreignKey)
	throws Exception {
		Long eventPK = (Long) foreignKey;

		List<ParticipantCustomField> customFields = getParticipantCustomFieldMgr().getParticipantCustomFieldsByEventPK(
			eventPK
		);

		enrich(customFields);

		return customFields;
	}


	public List<ParticipantCustomField> getParticipantCustomFieldsByEventPK(Long eventPK)
	throws Exception {
		return getEntityListByForeignKey(eventPK);
	}


	public boolean isParticipantCustomFieldsDefinedForEventPK(Long eventPK)
	throws Exception {
		List<ParticipantCustomField> list = getEntityListByForeignKey(eventPK);
		return notEmpty(list);
	}


	/**
	 * Returns all custom fields of an event that belong to a certain group (if groupID != null) or
	 * to no group (if groupID == null).
	 *
	 * @param eventPK
	 * @param groupID
	 * @return
	 * @throws Exception
	 */
	public List<ParticipantCustomField> getParticipantCustomFieldsByGroup(Long eventPK, Long groupID)
	throws Exception {
		List<ParticipantCustomField> allCustomFields = getEntityListByForeignKey(eventPK);
		List<ParticipantCustomField> customFieldsByGroup = new ArrayList<>(allCustomFields.size());
		for (ParticipantCustomField customField : allCustomFields) {
			if (EqualsHelper.isEqual(groupID, customField.getGroupPK())) {
				customFieldsByGroup.add(customField);
			}
		}
		return customFieldsByGroup;
	}


	public Map<ParticipantCustomFieldGroup, List<ParticipantCustomField>> getParticipantCustomFieldsByGroupMap(Long eventID)
	throws Exception {
		// load all CustomFieldGroups of Event
		List<ParticipantCustomFieldGroup> customFieldGroupList = ParticipantCustomFieldGroupModel.getInstance().getParticipantCustomFieldGroupsByEventPK(eventID);
		Map<Long, ParticipantCustomFieldGroup> eventGroupsMap = ParticipantCustomFieldGroup.abstractEntities2Map(customFieldGroupList);

		// copy and sort eventGroupsList
		customFieldGroupList = createArrayList(customFieldGroupList);
		Collections.sort(customFieldGroupList, ParticipantCustomFieldGroup_Location_Position_Comparator.getInstance());


		// create result Map
		Map<ParticipantCustomFieldGroup, List<ParticipantCustomField>> group2CustomFieldsMap = new LinkedHashMap<>() ;
		// init result Map with empty ArrayLists
		for (ParticipantCustomFieldGroup group : customFieldGroupList) {
			group2CustomFieldsMap.put(group, new ArrayList<>());
		}

		// load Custom Fields of Event
		List<ParticipantCustomField> eventCustomFields = getEntityListByForeignKey(eventID);

		// put Custom Fields to result map
		for (ParticipantCustomField field : eventCustomFields) {
			// determine Group of current CustomField
			Long groupPK = field.getGroupPK();
			ParticipantCustomFieldGroup group = eventGroupsMap.get(groupPK);

			// add Custom Field to the list of the Group
			List<ParticipantCustomField> customFieldList = group2CustomFieldsMap.get(group);
			/* The Group is null if it just has been deleted.
			 * It might happen that a deleted doesn't exist in the ParticipantCustomFieldGroupModel but its Custom Fields
			 * still exist in the ParticipantCustomFieldModel. These Custom Fields of course reference the deleted Group.
			 */
			if (customFieldList != null) {
				customFieldList.add(field);
			}
		}

		return group2CustomFieldsMap;
	}

}
