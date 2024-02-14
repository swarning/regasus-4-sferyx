package de.regasus.profile;

import static de.regasus.LookupService.*;

import java.util.Collection;
import java.util.List;

import com.lambdalogic.messeinfo.profile.ProfileRelationType;
import com.lambdalogic.messeinfo.profile.ProfileRole;

import de.regasus.core.model.MICacheModel;

public class ProfileRoleModel 
extends MICacheModel<Long, ProfileRole> {
	
	private static ProfileRoleModel instance;
	
	
	private ProfileRoleModel() {
	}
	
	
	public static ProfileRoleModel getInstance() {
		if (instance == null) {
			instance = new ProfileRoleModel();
		}
		return instance;
	}


	@Override
	protected boolean isForeignKeySupported() {
		return false;
	}
	

	@Override
	protected Long getKey(ProfileRole entity) {
		return entity.getID();
	}
	

	@Override
	protected ProfileRole getEntityFromServer(Long id) throws Exception {
		ProfileRole profileRole = getProfileRoleMgr().getProfileRole(id);
		return profileRole;
	}

	
	@Override
	protected List<ProfileRole> getEntitiesFromServer(Collection<Long> ids) throws Exception {
		List<ProfileRole> profileRelationTypes = getProfileRoleMgr().getProfileRoles(ids);
		return profileRelationTypes;
	}

	
	@Override
	protected ProfileRole createEntityOnServer(ProfileRole profileRole) 
	throws Exception {
		profileRole.validate();
		profileRole = getProfileRoleMgr().create(profileRole);
		return profileRole;
	}
	
	
	@Override
	protected ProfileRole updateEntityOnServer(ProfileRole profileRole) 
	throws Exception {
		profileRole.validate();
		profileRole = getProfileRoleMgr().update(profileRole);
		return profileRole;
	}
	
	
	@Override
	protected void deleteEntityOnServer(ProfileRole profileRole) throws Exception {
		getProfileRoleMgr().deleteByPK(profileRole.getID());
	}
	
	
	@Override
	protected void deleteEntitiesOnServer(Collection<ProfileRole> profileRoles) 
	throws Exception {
		List<Long> pkList = ProfileRelationType.getPrimaryKeyList(profileRoles);
		getProfileRoleMgr().deleteByPKs(pkList);
	}
	
	
	@Override
	protected List<ProfileRole> getAllEntitiesFromServer() throws Exception {
		List<ProfileRole> profileRoles = getProfileRoleMgr().getAllProfileRoles(false /*withSystemRole*/);
		return profileRoles;
	}
	
	
	public ProfileRole getProfileRole(Long id)
	throws Exception {
		return super.getEntity(id);
	}
	

	public List<ProfileRole> getProfileRoles(List<Long> profileRolePKs)
	throws Exception {
		return super.getEntities(profileRolePKs);
	}

	
	public Collection<ProfileRole> getAllProfileRoles()
	throws Exception {
		return getAllEntities();
	}
	
	
	public ProfileRole create(ProfileRole profileRole)
	throws Exception {
		return super.create(profileRole);
	}
	
	
	public ProfileRole update(ProfileRole profileRole)
	throws Exception {
		return super.update(profileRole);
	}
	
	
	public void delete(ProfileRole profileRole)
	throws Exception {
		super.delete(profileRole);
	}

}
