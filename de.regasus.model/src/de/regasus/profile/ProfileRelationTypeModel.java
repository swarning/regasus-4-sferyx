package de.regasus.profile;

import static de.regasus.LookupService.*;

import java.util.Collection;
import java.util.List;

import com.lambdalogic.i18n.LanguageString;
import com.lambdalogic.messeinfo.profile.ProfileRelation;
import com.lambdalogic.messeinfo.profile.ProfileRelationType;

import de.regasus.core.model.MICacheModel;

public class ProfileRelationTypeModel 
extends MICacheModel<Long, ProfileRelationType> {
	
	private static ProfileRelationTypeModel instance;
	
	
	private ProfileRelationTypeModel() {
	}
	
	
	public static ProfileRelationTypeModel getInstance() {
		if (instance == null) {
			instance = new ProfileRelationTypeModel();
		}
		return instance;
	}


	@Override
	protected boolean isForeignKeySupported() {
		return false;
	}
	

	@Override
	protected Long getKey(ProfileRelationType entity) {
		return entity.getID();
	}
	

	@Override
	protected ProfileRelationType getEntityFromServer(Long id) throws Exception {
		ProfileRelationType profileRelationType = getProfileRelationTypeMgr().getProfileRelationType(id);
		return profileRelationType;
	}

	
	@Override
	protected List<ProfileRelationType> getEntitiesFromServer(Collection<Long> ids) throws Exception {
		List<ProfileRelationType> profileRelationTypes = getProfileRelationTypeMgr().getProfileRelationTypes(ids);
		return profileRelationTypes;
	}
	
	
	@Override
	protected ProfileRelationType createEntityOnServer(ProfileRelationType profileRelationType) 
		throws Exception {
		profileRelationType.validate();
		profileRelationType = getProfileRelationTypeMgr().create(profileRelationType);
		return profileRelationType;
	}
	
	
	@Override
	protected ProfileRelationType updateEntityOnServer(ProfileRelationType profileRelationType) 
		throws Exception {
		profileRelationType.validate();
		profileRelationType = getProfileRelationTypeMgr().update(profileRelationType);
		return profileRelationType;
	}
	
	
	@Override
	protected void deleteEntityOnServer(ProfileRelationType profileRelationType) throws Exception {
		if (profileRelationType != null) {
			getProfileRelationTypeMgr().deleteByPK(profileRelationType.getID());
		}
	}
	
	
	@Override
	protected void deleteEntitiesOnServer(Collection<ProfileRelationType> profileRelationTypes) 
		throws Exception {
		if (profileRelationTypes != null && !profileRelationTypes.isEmpty()) {
			List<Long> pkList = ProfileRelationType.getPrimaryKeyList(profileRelationTypes);
			getProfileRelationTypeMgr().deleteByPKs(pkList);
		}
	}
	
	
	@Override
	protected List<ProfileRelationType> getAllEntitiesFromServer() throws Exception {
		List<ProfileRelationType> profileRelationTypes = getProfileRelationTypeMgr().getAllProfileRelationTypes();
		return profileRelationTypes;
	}
	
	
	public ProfileRelationType getProfileRelationType(Long id)
	throws Exception {
		return super.getEntity(id);
	}
	
	
	public Collection<ProfileRelationType> getAllProfileRelationTypes()
	throws Exception {
		return getAllEntities();
	}
	
	
	public ProfileRelationType create(ProfileRelationType profileRelationType)
	throws Exception {
		return super.create(profileRelationType);
	}
	
	
	public ProfileRelationType update(ProfileRelationType profileRelationType)
	throws Exception {
		return super.update(profileRelationType);
	}
	
	
	public void delete(ProfileRelationType profileRelationType)
	throws Exception {
		super.delete(profileRelationType);
	}
	

	
	/**
	 * The role of a profile in a relation depends on its position (1 or 2) and 
	 * its according role name is known by the respective relation type.  
	 */
    public String getRole(Long profileID, ProfileRelation profileRelation) {
    	Long profileRelationTypeID = profileRelation.getProfileRelationTypeID();
    	LanguageString ls = null;

    	try {
			ProfileRelationType profileRelationType = getProfileRelationType(profileRelationTypeID);
			if (profileID != null && profileRelationType != null) {
				if (profileID.equals(profileRelation.getProfile1ID())) {
					ls = profileRelationType.getRole1();
				}
				else if (profileID.equals(profileRelation.getProfile2ID())) {
					ls = profileRelationType.getRole2();
				}
			}
		}
		catch (Exception e) {
			com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
		}
    	
    	return LanguageString.toStringAvoidNull(ls);
    }

}
