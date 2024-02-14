package de.regasus.profile;

import static de.regasus.LookupService.getProfileRelationMgr;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.lambdalogic.messeinfo.profile.ProfileRelation;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.model.CacheModelOperation;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.model.MICacheModel;
import de.regasus.model.Activator;

/**
 * The ProfileRelationnModel manages the access to the entity ProfileRelation.
 * ProfileRelations are loaded and updated without connected entities like Profile and 
 * ProfileRelationType.
 */
public class ProfileRelationModel 
extends MICacheModel<Long, ProfileRelation> 
implements CacheModelListener<Long> {
	
	private static ProfileRelationModel instance;
	
	private ProfileModel profileModel;
	
	private ProfileRelationTypeModel profileRelationTypeModel;
	
	
	private ProfileRelationModel() {
	}
	
	
	public static ProfileRelationModel getInstance() {
		if (instance == null) {
			instance = new ProfileRelationModel();
			instance.initModels();
		}
		
		return instance;
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
		
		profileRelationTypeModel = ProfileRelationTypeModel.getInstance();
		profileRelationTypeModel.addListener(this);
	}

	
	@Override
	public void dataChange(CacheModelEvent<Long> event) {
		if (!serverModel.isLoggedIn()) {
			return;
		}
		
		try {
			Collection<Long> deletedProfileRelationIDs = new ArrayList<Long>();
			
			// Remove ProfileRelation if one of its Profiles was deleted.
			if (event.getSource() == profileModel && event.getOperation() == CacheModelOperation.DELETE) {
				for (Long profileID : event.getKeyList()) {
					for (ProfileRelation profileRelation : getLoadedAndCachedEntities()) {
						if (profileID.equals(profileRelation.getProfile1ID()) || 
							profileID.equals(profileRelation.getProfile2ID())
						) {
							deletedProfileRelationIDs.add(profileRelation.getID());
						}
					}
				}
			}
			
			// Remove ProfileRelation if its ProfileRelationType was deleted.
			if (event.getSource() == profileRelationTypeModel && event.getOperation() == CacheModelOperation.DELETE) {
				for (Long profileRelationTypeID : event.getKeyList()) {
					for (ProfileRelation profileRelation : getLoadedAndCachedEntities()) {
						if (profileRelationTypeID.equals(profileRelation.getProfileRelationTypeID())) {
							deletedProfileRelationIDs.add(profileRelation.getID());
						}
					}
				}
			}
			
			// inform listeners about deleted entities
			if (!deletedProfileRelationIDs.isEmpty()) {
				fireDelete(deletedProfileRelationIDs);
				removeEntities(deletedProfileRelationIDs);
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}

	
	@Override
	protected Long getKey(ProfileRelation entity) {
		return entity.getID();
	}


	@Override
	protected boolean isForeignKeySupported() {
		return true;
	}

	
	@Override
	protected List<Long> getForeignKeyList(ProfileRelation profileRelation) {
		List<Long> foreignKeyList = null;
		
		if (profileRelation != null) {
			foreignKeyList = new ArrayList<Long>(2);
			
			Long profile1ID = profileRelation.getProfile1ID();
			if (profile1ID != null) {
				foreignKeyList.add(profile1ID);
			}
			
			Long profile2ID = profileRelation.getProfile2ID();
			if (profile2ID != null) {
				foreignKeyList.add(profile2ID);
			}
		}
		
		return foreignKeyList;
	}
	
	
	@Override
	protected ProfileRelation getEntityFromServer(Long id) throws Exception {
		ProfileRelation profileRelation = getProfileRelationMgr().getProfileRelation(id);
		return profileRelation;
	}


	@Override
	protected List<ProfileRelation> getEntitiesFromServer(Collection<Long> ids) throws Exception {
		List<ProfileRelation> profileRelations = getProfileRelationMgr().getProfileRelations(ids);
		return profileRelations;
	}


	@Override
	protected ProfileRelation createEntityOnServer(ProfileRelation profileRelation) throws Exception {
		profileRelation.validate();
		profileRelation = getProfileRelationMgr().create(profileRelation);
		return profileRelation;
	}

	
	@Override
	protected ProfileRelation updateEntityOnServer(ProfileRelation profileRelation) throws Exception {
		profileRelation.validate();
		profileRelation = getProfileRelationMgr().update(profileRelation);
		return profileRelation;
	}
	
	
	@Override
	protected void deleteEntityOnServer(ProfileRelation profileRelation) throws Exception {
		if (profileRelation != null && profileRelation.getID() != null) {
			getProfileRelationMgr().deleteByPK(profileRelation.getID());
		}
	}
	
	
	@Override
	protected void deleteEntitiesOnServer(Collection<ProfileRelation> profileRelations) throws Exception {
		if (profileRelations != null && !profileRelations.isEmpty()) {
			getProfileRelationMgr().deleteByPKs( ProfileRelation.getPrimaryKeyList(profileRelations) );
		}
	}


	@Override
	protected List<ProfileRelation> getEntitiesByForeignKeyFromServer(Object foreignKey) throws Exception {
		Long profileID = (Long) foreignKey;
		List<ProfileRelation> profileRelationList = getProfileRelationMgr().getProfileRelationsByProfileID(profileID);
		return profileRelationList;
	}
	
	
	public ProfileRelation getProfileRelation(Long profileRelationID) throws Exception {
		return super.getEntity(profileRelationID);
	}
	
	
	public List<ProfileRelation> getProfileRelationsByProfile(Long profileID) throws Exception {
		return getEntityListByForeignKey(profileID);
	}

	
	public void createProfileRelation(Long profile1ID, Long profile2ID, Long profileRelationTypeID)
	throws Exception {
		ProfileRelation profileRelation = new ProfileRelation(
			profile1ID, 
			profile2ID, 
			profileRelationTypeID
		);
		
		super.create(profileRelation);
	}
	
	
	public void delete(Collection<ProfileRelation> profileRelations) throws Exception {
		super.delete(profileRelations);
	}

}
