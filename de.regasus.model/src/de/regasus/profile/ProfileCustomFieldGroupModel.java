package de.regasus.profile;

import static de.regasus.LookupService.getProfileCustomFieldGroupMgr;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.lambdalogic.messeinfo.profile.ProfileCustomFieldGroup;
import com.lambdalogic.messeinfo.profile.ProfileCustomFieldGroup_Location_Position_Comparator;
import com.lambdalogic.messeinfo.profile.ProfileCustomFieldGroupLocation;
import com.lambdalogic.util.model.CacheModelOperation;

import de.regasus.core.OrderPosition;
import de.regasus.core.model.MICacheModel;

public class ProfileCustomFieldGroupModel extends MICacheModel<Long, ProfileCustomFieldGroup> {

	private static ProfileCustomFieldGroupModel singleton;


	private ProfileCustomFieldGroupModel() {
	}


	public static ProfileCustomFieldGroupModel getInstance() {
		if (singleton == null) {
			singleton = new ProfileCustomFieldGroupModel();
		}
		return singleton;
	}


	@Override
	protected ProfileCustomFieldGroup createEntityOnServer(ProfileCustomFieldGroup customFieldGroup)
	throws Exception {
		// temporarily set the position to pass validation
		customFieldGroup.setPosition(0);
		customFieldGroup.validate();
		// remove position because it should be calculated automatically by the server
		customFieldGroup.setPosition(null);

		customFieldGroup = getProfileCustomFieldGroupMgr().create(customFieldGroup);
		return customFieldGroup;
	}


	@Override
	protected boolean isForeignKeySupported() {
		return false;
	}


	@Override
	protected Long getKey(ProfileCustomFieldGroup entity) {
		return entity.getID();
	}


	@Override
	protected List<ProfileCustomFieldGroup> getAllEntitiesFromServer()
	throws Exception {
		List<ProfileCustomFieldGroup> groupList = getProfileCustomFieldGroupMgr().findAll();
		Collections.sort(groupList, ProfileCustomFieldGroup_Location_Position_Comparator.getInstance());
		return groupList;
	}


	@Override
	protected ProfileCustomFieldGroup getEntityFromServer(Long profileCustomFieldGroupID)
	throws Exception {
		ProfileCustomFieldGroup profileCustomFieldGroup = getProfileCustomFieldGroupMgr().getProfileCustomFieldGroup(profileCustomFieldGroupID);
		return profileCustomFieldGroup;
	}


	@Override
	protected List<ProfileCustomFieldGroup> getEntitiesFromServer(Collection<Long> profileCustomFieldGroupIDs)
	throws Exception {
		List<ProfileCustomFieldGroup> profileCustomFieldGroups = getProfileCustomFieldGroupMgr().getProfileCustomFieldGroups(profileCustomFieldGroupIDs);
		return profileCustomFieldGroups;
	}


	@Override
	protected ProfileCustomFieldGroup updateEntityOnServer(ProfileCustomFieldGroup profileCustomFieldGroup)
	throws Exception {
		profileCustomFieldGroup.validate();
		profileCustomFieldGroup = getProfileCustomFieldGroupMgr().update(profileCustomFieldGroup);
		return profileCustomFieldGroup;
	}


	@Override
	protected void deleteEntityOnServer(ProfileCustomFieldGroup profileCustomFieldGroup)
	throws Exception {
		if (profileCustomFieldGroup != null) {
			getProfileCustomFieldGroupMgr().deleteByPK(profileCustomFieldGroup.getID());
		}
	}


	@Override
	protected void deleteEntitiesOnServer(Collection<ProfileCustomFieldGroup> profileCustomFieldGroups)
	throws Exception {
		if (profileCustomFieldGroups != null) {
			getProfileCustomFieldGroupMgr().deleteByPKs( ProfileCustomFieldGroup.getPrimaryKeyList(profileCustomFieldGroups) );
		}
	}


    //	**************************************************************************
    //	* Public methods
    //	*

	@Override
	public ProfileCustomFieldGroup create(ProfileCustomFieldGroup profileCustomFieldGroup)
	throws Exception {
		return super.create(profileCustomFieldGroup);
	}


	public List<ProfileCustomFieldGroup> getAllProfileCustomFieldGroups()
	throws Exception {
		return super.getAllEntities();
	}


	public List<ProfileCustomFieldGroup> getProfileCustomFieldGroups(Collection<Long> groupIDs)
	throws Exception {
		List<ProfileCustomFieldGroup> customFieldGroups = super.getEntities(groupIDs);
		return customFieldGroups;
	}


	public ProfileCustomFieldGroup getProfileCustomFieldGroup(Long profileCustomFieldGroupID)
	throws Exception {
		return super.getEntity(profileCustomFieldGroupID);
	}


	@Override
	public ProfileCustomFieldGroup update(ProfileCustomFieldGroup profileCustomFieldGroup)
	throws Exception {
		return super.update(profileCustomFieldGroup);
	}


	public void moveToLocation(Long movedGroupId, ProfileCustomFieldGroupLocation targetLocation)
	throws Exception {
		List<ProfileCustomFieldGroup> groups = getProfileCustomFieldGroupMgr().moveToLocation(
			movedGroupId,
			targetLocation
		);

		put(groups);
		fireDataChange(CacheModelOperation.UPDATE, ProfileCustomFieldGroup.getPKs(groups));
	}


	public void move(Long sourceGroupId, OrderPosition orderPosition, Long targetGroupId)
	throws Exception {
		List<ProfileCustomFieldGroup> groups = getProfileCustomFieldGroupMgr().move(
			sourceGroupId,
			orderPosition,
			targetGroupId
		);

		put(groups);
		fireDataChange(CacheModelOperation.UPDATE, ProfileCustomFieldGroup.getPKs(groups));
	}


	@Override
	public void delete(ProfileCustomFieldGroup profileCustomFieldGroup)
	throws Exception {
		super.delete(profileCustomFieldGroup);
	}


	@Override
	public void delete(Collection<ProfileCustomFieldGroup> profileCustomFieldGroups)
	throws Exception {
		super.delete(profileCustomFieldGroups);
	}


	public ProfileCustomFieldGroup copyProfileCustomFieldGroup(Long groupID, ProfileCustomFieldGroupLocation location)
	throws Exception {
		// let the server do the copy work
		ProfileCustomFieldGroup customFieldGroup = getProfileCustomFieldGroupMgr().copy(groupID, location);

		// add new data to model
		put(customFieldGroup);

		// inform listeners
		fireCreate( Collections.singletonList(customFieldGroup.getID()) );

		return customFieldGroup;
	}


	public ProfileCustomFieldGroup copyFromParticipantCustomFieldGroup(
		Long participantCustomFieldGroupId,
		ProfileCustomFieldGroupLocation location
	)
	throws Exception {
		ProfileCustomFieldGroup customFieldGroup = getProfileCustomFieldGroupMgr().copyFromParticipantCustomFieldGroup(
			participantCustomFieldGroupId,
			location
		);

		put(customFieldGroup);

		fireCreate( Collections.singletonList(customFieldGroup.getID()) );

		return customFieldGroup;
	}

}
