package de.regasus.profile;

import static com.lambdalogic.util.CollectionsHelper.*;
import static de.regasus.LookupService.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.lambdalogic.messeinfo.profile.ProfileCustomField;
import com.lambdalogic.messeinfo.profile.ProfileCustomFieldGroup;
import com.lambdalogic.messeinfo.profile.ProfileCustomFieldGroup_Location_Position_Comparator;
import com.lambdalogic.messeinfo.profile.ProfileCustomFieldSettings;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.model.CacheModelOperation;

import de.regasus.core.OrderPosition;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.model.MICacheModel;
import de.regasus.model.Activator;


/**
 * Model that manages {@link ProfileCustomField}s.
 * The {@link ProfileCustomField}s contain {@link ProfileCustomFieldGroup} that is loaded from
 * {@link ProfileCustomFieldGroupModel} and not from the server directly.
 * {@link ProfileCustomFieldModel} is observing {@link ProfileCustomFieldGroupModel}. So if a
 * {@link ProfileCustomFieldGroup} changes, it will be replaced in the {@link ProfileCustomField}s and
 * {@link ProfileCustomFieldModel} will fire a {@link CacheModelEvent}, too.
 */
public class ProfileCustomFieldModel extends MICacheModel<Long, ProfileCustomField> {

	private static ProfileCustomFieldModel singleton;

	private static Long NULL_FOREIGN_KEY = 0L;


	private ProfileCustomFieldGroupModel groupModel;


	private CacheModelListener<Long> groupModelListener = new CacheModelListener<Long>() {
		@Override
		public void dataChange(CacheModelEvent<Long> event) {
			try {
				// update affected groups in all custom fields
				CacheModelOperation op = event.getOperation();

				if (op == CacheModelOperation.REFRESH || op == CacheModelOperation.UPDATE) {
					/* Update ProfileCustomField.group but don't fire a CacheModelEvent!
					 * This would cause a refresh of corresponding ProfileCustomFieldEditors which might lose edited data.
					 * Clients who are interested of changes of ProfileCustomFieldGroups have to observe the
					 * ProfileCustomFieldGroupModel directly.
					 */
					List<Long> refreshedKeyList = createArrayList();
					for (Long groupPK : event.getKeyList()) {
						ProfileCustomFieldGroup group = groupModel.getProfileCustomFieldGroup(groupPK);

						for (ProfileCustomField customField : getLoadedAndCachedEntities()) {
							if (customField.getGroupPK().equals(groupPK)) {
								customField.setProfileCustomFieldGroup(group);
								refreshedKeyList.add(customField.getID());
							}
						}
					}

					// don't fire CacheModelEvent (see above)
					// fireRefresh(refreshedKeyList);
				}
				else if (op == CacheModelOperation.CREATE) {
					/* Why should we handle this CacheModelOperation at all?
					 * Newly created Groups cannot contains any Custom Field!
					 */

//					// reload Custom Fields of affected Groups
//					for (Long groupPK : event.getKeyList()) {
//						refreshForeignKey(groupPK);
//					}
				}
				else if (op == CacheModelOperation.DELETE) {
					// determine Custom Fields of deleted Groups
					List<ProfileCustomField> deletedCustomFields = new ArrayList<>();
					for (Long groupPK : event.getKeyList()) {
						for (ProfileCustomField customField : getLoadedAndCachedEntities()) {
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


	private ProfileCustomFieldModel() {
	}


	public static ProfileCustomFieldModel getInstance() {
		if (singleton == null) {
			singleton = new ProfileCustomFieldModel();
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
		groupModel = ProfileCustomFieldGroupModel.getInstance();
		groupModel.addListener(groupModelListener);
	}


	/**
	 * Add ProfileCustomFieldGroup to ProfileCustomField.
	 * The ProfileCustomFieldGroup is NOT loaded from the server but from the ProfileCustomFieldGroupModel!
	 *
	 * @param customFieldList
	 * @throws Exception
	 */
	private void enrich(ProfileCustomField customField) throws Exception {
		if (customField != null) {
			// load all groups into cache
			groupModel.getAllProfileCustomFieldGroups();

			ProfileCustomFieldGroup group = groupModel.getProfileCustomFieldGroup(customField.getGroupPK());
			customField.setProfileCustomFieldGroup(group);
		}
	}


	/**
	 * Add ProfileCustomFieldGroups to List of ProfileCustomField.
	 * The ProfileCustomFieldGroups are NOT loaded from the server but from the ProfileCustomFieldGroupModel!
	 *
	 * @param customFieldList
	 * @throws Exception
	 */
	private void enrich(List<ProfileCustomField> customFieldList) throws Exception {
		if (notEmpty(customFieldList)) {
			// load all groups into cache
			groupModel.getAllProfileCustomFieldGroups();

			for (ProfileCustomField customField : customFieldList) {
				ProfileCustomFieldGroup group = groupModel.getProfileCustomFieldGroup( customField.getGroupPK() );
				customField.setProfileCustomFieldGroup(group);
			}
		}
	}


	// **************************************************************************
	// * Overridden protected methods, not for public use
	// *


	@Override
	protected Long getKey(ProfileCustomField entity) {
		return entity.getID();
	}


	@Override
	protected boolean isForeignKeySupported() {
		return true;
	}


	@Override
	protected Object getForeignKey(ProfileCustomField profileCustomField) {
		Long fk = null;
		if (profileCustomField != null) {
			fk = profileCustomField.getGroupPK();
			if (fk == null) {
				fk = NULL_FOREIGN_KEY;
			}
		}
		return fk;
	}


	public boolean existProfileCustomFieldValue(Collection<Long> customFieldPKs) throws Exception {
		boolean result = getProfileCustomFieldValueMgr().existProfileCustomFieldValue(customFieldPKs);
		return result;
	}


	@Override
	protected ProfileCustomField createEntityOnServer(ProfileCustomField customField)
	throws Exception {
		// temporarily set the position to pass validation
		customField.setPosition(0);
		customField.validate();
		// remove position because it should be calculated automatically by the server
		customField.setPosition(null);

		customField = getProfileCustomFieldMgr().create(customField);

		// add additional data
		enrich(customField);

		return customField;
	}


	@Override
	protected ProfileCustomField getEntityFromServer(Long profileCustomFieldID)
	throws Exception {
		ProfileCustomField customField = getProfileCustomFieldMgr().getProfileCustomField(profileCustomFieldID);

		// add additional data
		enrich(customField);

		return customField;
	}


	@Override
	protected List<ProfileCustomField> getEntitiesFromServer(Collection<Long> profileCustomFieldIDs)
	throws Exception {
		List<ProfileCustomField> customFields = getProfileCustomFieldMgr().getProfileCustomFields(profileCustomFieldIDs);

		// add additional data
		enrich(customFields);

		return customFields;
	}


	@Override
	protected List<ProfileCustomField> getAllEntitiesFromServer()
	throws Exception {
		/* Don't use casted null because of ambiguity with overloaded method on server
		 * Even  getProfileCustomFields((ProfileCustomFieldSettings) null)  does not work reliably!
		 */
		ProfileCustomFieldSettings settings = new ProfileCustomFieldSettings();
		List<ProfileCustomField> customFields = getProfileCustomFieldMgr().getProfileCustomFields(settings);

		// add additional data
		enrich(customFields);

		return customFields;
	}


	@Override
	protected List<ProfileCustomField> getEntitiesByForeignKeyFromServer(Object foreignKey) throws Exception {
		Long groupPK;
		if (foreignKey == NULL_FOREIGN_KEY) {
			groupPK = null;
		}
		else {
			groupPK = (Long) foreignKey;
		}

		List<ProfileCustomField> customFields = getProfileCustomFieldMgr().getProfileCustomFieldsByGroupPK(groupPK);

		// add additional data
		enrich(customFields);

		return customFields;
	}


	@Override
	protected ProfileCustomField updateEntityOnServer(ProfileCustomField customField)
	throws Exception {
		customField.validate();
		customField = getProfileCustomFieldMgr().update(customField);

		/* Enrich even here, because after update customField contains only the lazy-loading-dummy, no matter what the
		 * original parameter contained.
		 */
		enrich(customField);

		return customField;
	}


	@Override
	protected void deleteEntityOnServer(ProfileCustomField customField)
	throws Exception {
		if (customField != null) {
			getProfileCustomFieldMgr().deleteByPK(customField.getID());
		}
	}


	@Override
	protected void deleteEntitiesOnServer(Collection<ProfileCustomField> customFields)
	throws Exception {
		if (customFields != null) {
			getProfileCustomFieldMgr().deleteByPKs( ProfileCustomField.getPrimaryKeyList(customFields) );
		}
	}

    //	**************************************************************************
    //	* Public methods
    //	*

	public List<ProfileCustomField> getAllProfileCustomFields()
	throws Exception {
		return super.getAllEntities();
	}


	@Override
	public ProfileCustomField create(ProfileCustomField customField)
	throws Exception {
		return super.create(customField);
	}


	public ProfileCustomField getProfileCustomField(Long customFieldID)
	throws Exception {
		return super.getEntity(customFieldID);
	}


	public List<ProfileCustomField> getProfileCustomFields(List<Long> customFieldIDs)
	throws Exception {
		return super.getEntities(customFieldIDs);
	}


	public List<ProfileCustomField> getProfileCustomFieldsByGroup(Long groupID)
	throws Exception {
		if (groupID == null) {
			groupID = NULL_FOREIGN_KEY;
		}

		return getEntityListByForeignKey(groupID);
	}


	@Override
	public ProfileCustomField update(ProfileCustomField customField)
	throws Exception {
		return super.update(customField);
	}


	public void moveToGroup(Long movedCustomFieldId, Long targetCustomFieldGroupId)
	throws Exception {
		// the following code refers to CacheModel.update(EntityType entity)

		// determine old Group
		ProfileCustomField movedCustomField = getProfileCustomField(movedCustomFieldId);
		Long oldGroupPK = movedCustomField.getGroupPK();

		// do the update on server
		List<ProfileCustomField> customFields = getProfileCustomFieldMgr().moveToGroup(
			movedCustomFieldId,
			targetCustomFieldGroupId
		);

		// add additional data
		enrich(customFields);

		put(customFields);

		// fire CacheModelEvent for the moved Custom Field incl. its old Group PK
		fireDataChange(CacheModelOperation.UPDATE, movedCustomFieldId, Collections.singletonList(oldGroupPK));

		// fire CacheModlEvent for all updated Custom Fields
		fireDataChange(CacheModelOperation.UPDATE, ProfileCustomField.getPKs(customFields));
	}


	public void move(Long movedCustomFieldId, OrderPosition orderPosition, Long targetCustomFieldId)
	throws Exception {
		// the following code refers to CacheModel.update(EntityType entity)

		// determine old Group
		ProfileCustomField movedCustomField = getProfileCustomField(movedCustomFieldId);
		Long oldGroupPK = movedCustomField.getGroupPK();

		// do the update on server
		List<ProfileCustomField> customFields = getProfileCustomFieldMgr().move(
			movedCustomFieldId,
			orderPosition,
			targetCustomFieldId
		);

		// add additional data
		enrich(customFields);

		put(customFields);

		// fire CacheModelEvent for the moved Custom Field incl. its old Group PK
		fireDataChange(CacheModelOperation.UPDATE, movedCustomFieldId, Collections.singletonList(oldGroupPK));

		// fire CacheModlEvent for all updated Custom Fields
		fireDataChange(CacheModelOperation.UPDATE, ProfileCustomField.getPKs(customFields));
	}


	@Override
	public void delete(ProfileCustomField customField)
	throws Exception {
		super.delete(customField);
	}


	@Override
	public void delete(Collection<ProfileCustomField> customFields)
	throws Exception {
		super.delete(customFields);
	}


	public Map<ProfileCustomFieldGroup, List<ProfileCustomField>> getProfileCustomFieldsByGroupMap()
	throws Exception {
		Map<ProfileCustomFieldGroup, List<ProfileCustomField>> map = new LinkedHashMap<>() ;

		Map<Long, ProfileCustomFieldGroup> _pk2GroupMap = new LinkedHashMap<>();

		ProfileCustomFieldGroupModel groupModel = ProfileCustomFieldGroupModel.getInstance();
		List<ProfileCustomFieldGroup> allGroups = createArrayList(groupModel.getAllProfileCustomFieldGroups());
		Collections.sort(allGroups, ProfileCustomFieldGroup_Location_Position_Comparator.getInstance());

		map.put(null, new ArrayList<ProfileCustomField>());

		for (ProfileCustomFieldGroup group : allGroups) {
			_pk2GroupMap.put(group.getID(), group);
			map.put(group, new ArrayList<ProfileCustomField>());
		}

		List<ProfileCustomField> allFields = getAllEntities();

		for (ProfileCustomField field : allFields) {
			Long groupPK = field.getGroupPK();
			ProfileCustomFieldGroup group = _pk2GroupMap.get(groupPK);

			List<ProfileCustomField> groupFields = map.get(group);
			if (groupFields == null) {
				groupFields = createArrayList();
				map.put(group, groupFields);
			}
			groupFields.add(field);
		}

		return map;
	}


	public ProfileCustomField copyProfileCustomField(
		Long sourceCustomFieldID,
		Long destCustomFieldGroupID
	)
	throws Exception {
		// let the server do the copy work
		ProfileCustomField customField = getProfileCustomFieldMgr().copy(
			sourceCustomFieldID,
			destCustomFieldGroupID
		);

		// add additional data
		enrich(customField);

		// add new data to model
		put(customField);

		// inform listeners
		fireCreate( Collections.singletonList(customField.getID()) );

		return customField;
	}


	public ProfileCustomField copyFromParticipantCustomField(
		Long participantCustomFieldId,
		Long targetProfileGroupId
	)
	throws Exception {
		ProfileCustomField customField = getProfileCustomFieldMgr().copyFromParticipantCustomField(
			participantCustomFieldId,
			targetProfileGroupId
		);

		// add additional data
		enrich(customField);

		put(customField);

		fireCreate( Collections.singletonList(customField.getID()) );

		return customField;
	}

}
