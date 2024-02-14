package de.regasus.profile;

import static de.regasus.LookupService.getProfileMgr;
import static com.lambdalogic.util.CollectionsHelper.createArrayList;
import static com.lambdalogic.util.CollectionsHelper.createHashSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.lambdalogic.messeinfo.contact.CustomFieldUpdateParameter;
import com.lambdalogic.messeinfo.exception.InvalidValuesException;
import com.lambdalogic.messeinfo.exception.ProfileDuplicateException;
import com.lambdalogic.messeinfo.kernel.interfaces.SQLOperator;
import com.lambdalogic.messeinfo.kernel.sql.SQLParameter;
import com.lambdalogic.messeinfo.profile.PersonLinkData;
import com.lambdalogic.messeinfo.profile.Profile;
import com.lambdalogic.messeinfo.profile.ProfileCustomField;
import com.lambdalogic.messeinfo.profile.ProfileCustomFieldValidator;
import com.lambdalogic.messeinfo.profile.ProfileRelation;
import com.lambdalogic.messeinfo.profile.interfaces.ProfileSettings;
import com.lambdalogic.messeinfo.profile.sql.ProfileSearch;
import com.lambdalogic.messeinfo.salutation.AddressLabelGenerator;
import com.lambdalogic.util.exception.ErrorMessageException;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.model.CacheModelOperation;

import de.regasus.core.ClientCountryNameProvider;
import de.regasus.core.InvitationCardGeneratorModel;
import de.regasus.core.SalutationGeneratorModel;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.model.MICacheModel;
import de.regasus.model.Activator;
import de.regasus.person.AddressLabelGeneratorModel;
import de.regasus.person.PersonLinkModel;


public class ProfileModel
extends MICacheModel<Long, Profile>
implements CacheModelListener<Long> {

	private static ProfileModel singleton = null;

	// models
	private PersonLinkModel personLinkModel;
	private ProfileRelationModel profileRelationModel;

	private static final ProfileSettings profileSettings;

	static {
		// set ClientCountryNameProvider to take advantage of local caches
		AddressLabelGenerator.setCountryNameProvider(ClientCountryNameProvider.getInstance());

		profileSettings = new ProfileSettings();
		profileSettings.withCustomFieldValues = true;
		profileSettings.withCorrespondences = true;
	}


	public static Profile getInitialProfile() throws Exception {
		Profile profile = new Profile();
		prepareProfile(profile);
    	return profile;
	}


	public static ProfileModel getInstance() {
		if (singleton == null) {
			singleton = new ProfileModel();
			singleton.initModels();
		}
		return singleton;
	}


	private ProfileModel() {
		super();
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
		personLinkModel = PersonLinkModel.getInstance();
		personLinkModel.addListener(this);

		/* The ProfileModel has to observe the ProfileRelationModel, because deleting a
		 * ProfileRelation may change the referenced Profiles, too. If the deleted ProfileRelation
		 * has been a second person, this reference is deleted, too (in ProfileRelationManagerBean).
		 */
		profileRelationModel = ProfileRelationModel.getInstance();
		profileRelationModel.addListener(this);
	}


	@Override
	protected boolean isForeignKeySupported() {
		return false;
	}


	@Override
	protected Long getKey(Profile entity) {
		return entity.getID();
	}


	private static void prepareProfile(Profile profile) throws Exception {
		if (profile != null) {
			// set the SalutationGenerator to avoid multiple instances
			profile.setSalutationGenerator(SalutationGeneratorModel.getInstance().getSalutationGenerator());
			profile.getSettings().withSalutationGenerator = true;

			// set the InvitationCardGeneratorModel to avoid multiple instances
			profile.setInvitationCardGenerator(InvitationCardGeneratorModel.getInstance().getInvitationCardGenerator());
			profile.getSettings().withInvitationCardGenerator = true;

			// set AddressLabelGenerator
			profile.setAddressLabelGenerator(AddressLabelGeneratorModel.getInstance().getAddressLabelGenerator());
		}
	}


	@Override
	protected Profile getEntityFromServer(Long profileID) throws Exception {
		Profile profile = getProfileMgr().find(profileID);
		prepareProfile(profile);
		return profile;
	}


	@Override
	protected List<Profile> getEntitiesFromServer(Collection<Long> profileIDs) throws Exception {
		List<Profile> profileList = getProfileMgr().findByPKs(profileIDs);

		if (profileList != null) {
			for (Profile profile : profileList) {
				prepareProfile(profile);
			}
		}

		return profileList;
	}

	// **************************************************************************
	// * Extensions
	// *

	@Override
	public boolean isExtended(Profile profile) {
		// Do not check the data, but always the settings, because the data may initialized lazily.
		return
			profile != null &&
			profile.getSettings() != null &&
			profile.getSettings().withCustomFieldValues &&
			profile.getSettings().withCorrespondences;
	}


//	public Profile getExtendedEntity(Long key) throws Exception {
//		Profile profile = getEntityIfLoaded(key);
//
//		if (profile == null || !isExtended(profile)) {
//			profile = getExtendedEntityFromServer(key);
//			put(profile);
//		}
//
//		return profile;
//	}


//	public List<Profile> getExtendedEntities(List<Long> keyList) throws Exception {
//		List<Profile> resultList = new ArrayList<Profile>(keyList.size());
//
//		List<Long> missingKeyList = null;
//
//		for (Long key : keyList) {
//			Profile profile = getEntityIfLoaded(key);
//			if (profile != null && isExtended(profile)) {
//				resultList.add(profile);
//			}
//			else {
//				if (missingKeyList == null) {
//					missingKeyList = new ArrayList<Long>(keyList.size());
//				}
//				missingKeyList.add(key);
//			}
//		}
//
//		if (missingKeyList != null) {
//			List<Profile> missingEntityList = getExtendedEntitiesFromServer(missingKeyList);
//			for (Profile profile : missingEntityList) {
//				put(profile);
//			}
//			resultList.addAll(missingEntityList);
//		}
//
//		return resultList;
//	}


	@Override
	protected void copyExtendedValues(Profile fromProfile, Profile toProfile) {
		toProfile.copyTransientValuesFrom(fromProfile, false);
	}


	@Override
	protected Profile getExtendedEntityFromServer(Long profileID) throws Exception {
		Profile profile = getProfileMgr().find(profileID, profileSettings);

		if (profile == null) {
			throw new ErrorMessageException("Profile not found: " + profileID);
		}

		prepareProfile(profile);

		return profile;
	}


	@Override
	protected List<Profile> getExtendedEntitiesFromServer(List<Long> profileIDs)
	throws Exception {
		List<Profile> profileList = getProfileMgr().find(
			profileIDs,
			profileSettings
		);

		if (profileList != null) {
			for (Profile profile : profileList) {
				prepareProfile(profile);
			}
		}
		else {
			throw new ErrorMessageException("Profiles not found: " + profileIDs);
		}

		return profileList;
	}

	// *
	// * Extensions
	// **************************************************************************


	private void validate(Profile profile) throws Exception {
		InvalidValuesException ive = null;
		try {
			profile.validate();
		}
		catch (InvalidValuesException e) {
			ive = e;
		}

		// validate custom fields
		ProfileCustomFieldModel pcfModel = ProfileCustomFieldModel.getInstance();
		Collection<ProfileCustomField> customFieldList = pcfModel.getAllProfileCustomFields();
		// create a copy because customFieldList may be unmodifiable and the Validator needs a modifiable Collection
		customFieldList = createArrayList(customFieldList);

		try {
			ProfileCustomFieldValidator.validate(profile, customFieldList);
		}
		catch (InvalidValuesException e) {
			if (ive == null) {
				ive = e;
			}
			else {
				ive.add(e);
			}
		}

		if (ive != null) {
			throw ive;
		}
	}


	public Profile create(Profile profile, boolean force)
	throws ProfileDuplicateException, Exception {
		Profile newProfile = null;

		profile.validate();

		if (serverModel.isLoggedIn()) {
			// search duplicate
			List<SQLParameter> duplicateSQLParameters = null;
			if ( ! force) {
				duplicateSQLParameters = new ArrayList<SQLParameter>();

				if (profile.getFirstName() != null) {
					duplicateSQLParameters.add(ProfileSearch.FIRST_NAME.getSQLParameter(
						profile.getFirstName(),
						SQLOperator.FUZZY_REGEXP)
					);
					duplicateSQLParameters.add(ProfileSearch.FIRST_NAME.getSQLParameter(
						null,
						SQLOperator.EQUAL)
					);
				}

				if (profile.getLastName() != null) {
					duplicateSQLParameters.add(ProfileSearch.LAST_NAME.getSQLParameter(
						profile.getLastName(),
						SQLOperator.FUZZY_REGEXP)
					);
					duplicateSQLParameters.add(ProfileSearch.LAST_NAME.getSQLParameter(
						null,
						SQLOperator.EQUAL)
					);
				}
			}

			newProfile = getProfileMgr().create(profile, duplicateSQLParameters);
			prepareProfile(newProfile);
			put(newProfile);
			fireCreate(newProfile.getID());
		}

		return newProfile;
	}


	public List<Profile> createByParticipants(List<Long> participantPKs) throws Exception {
		List<Profile> profiles = null;

		if (serverModel.isLoggedIn()) {
			profiles = getProfileMgr().createProfilesByParticipants(participantPKs);
		}
		else {
			profiles = Collections.emptyList();
		}

		// assure that the cache is big enough
		int formerCacheSize = assureCacheSize(profiles.size());

		List<Long> profileIDs = new ArrayList<Long>(profiles.size());
		for (Profile profile : profiles) {
			prepareProfile(profile);
			put(profile);

			Long profileID = profile.getID();
			profileIDs.add(profileID);
		}
		try {
			fireCreate(profileIDs);
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}


		// reset the initial cache size
		setCacheSize(formerCacheSize);

		return profiles;
	}


	@Override
	protected Profile updateEntityOnServer(Profile profile) throws Exception {
		validate(profile);

		profile = getProfileMgr().update(profile);
		prepareProfile(profile);

		return profile;
	}


	@Override
	public Profile update(Profile profile) throws Exception {
		return super.update(profile);
	}


	public void updateProfileCustomFields(
		List<CustomFieldUpdateParameter> updateParameterList,
		List<Long> profilePKs
	) {
		try {
			getProfileMgr().updateProfileCustomFields(
				updateParameterList,
				profilePKs
			);

			handleUpdate(profilePKs);
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	public void addProfileRoles (
		List<Long> profileRoleIDs,
		List<Long> profilePKs
	) {
		try {
			getProfileMgr().addProfileRoles(
				profileRoleIDs,
				profilePKs
			);

			handleUpdate(profilePKs);
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	public void removeProfileRoles (
		List<Long> profileRoleIDs,
		List<Long> profilePKs
	) {
		try {
			getProfileMgr().removeProfileRoles (
				profileRoleIDs,
				profilePKs
			);

			handleUpdate(profilePKs);
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	public void setProfileRoles (
		List<Long> profileRoleIDs,
		List<Long> profilePKs
	) {
		try {
			getProfileMgr().setProfileRoles (
				profileRoleIDs,
				profilePKs
			);

			handleUpdate(profilePKs);
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	public void copyPersonDataFromParticipant(Long participantPK) throws Exception {
		if (serverModel.isLoggedIn()) {
			Profile profile = getProfileMgr().copyPersonDataFromParticipant(participantPK);
			if (profile != null) {
				prepareProfile(profile);
				put(profile);
				fireUpdate(profile.getID());
			}
		}
	}


	@Override
	protected void deleteEntityOnServer(Profile profile) throws Exception {
		if (profile != null) {
			getProfileMgr().deleteByPK(profile.getID());
		}
	}


	@Override
	public void delete(Profile profile) throws Exception {
		super.delete(profile);
	}


	@Override
	protected void deleteEntitiesOnServer(Collection<Profile> profiles) throws Exception {
		if (profiles != null) {
			List<?> profileIDs = Profile.getPrimaryKeyList(profiles);
			getProfileMgr().deleteByPKs(profileIDs);
		}
	}

	@Override
	public void delete(Collection<Profile> profiles) throws Exception {
		super.delete(profiles);
	}


	public Profile getProfile(Long profileID) throws Exception {
		return super.getEntity(profileID);
	}


	public List<Profile> getProfiles(List<Long> profileIDs) throws Exception {
		return super.getEntities(profileIDs);
	}


	public Profile getExtendedProfile(Long profileID) throws Exception {
		Profile profile = super.getExtendedEntity(profileID);

		// put Profiles indirectly loaded with ProfileRelation into cache
		if (profile != null) {
			List<ProfileRelation> profileRelationList = profile.getProfileRelationList();
			if (profileRelationList != null) {
				for (ProfileRelation profileRelation : profileRelationList) {
					Profile profile1 = profileRelation.getProfile1();
					Profile profile2 = profileRelation.getProfile2();

					// provoke LazyLoadingException
					profile1.getLastName();
					profile2.getLastName();

					prepareProfile(profile1);
					prepareProfile(profile2);

					// put Profiles into cache
					put(profile1);
					put(profile2);

					// fire refresh
					List<Long> profileIDs = new ArrayList<Long>(2);
					profileIDs.add(profile1.getID());
					profileIDs.add(profile2.getID());
					fireRefresh(profileIDs);
				}
			}
		}

		return profile;
	}


	public List<Profile> getExtendedProfiles(List<Long> profileIDs) throws Exception {
		return super.getExtendedEntities(profileIDs);
	}


	@Override
	public void dataChange(CacheModelEvent<Long> event) {
		if (!serverModel.isLoggedIn()) {
			return;
		}

		try {
			if (event.getSource() == personLinkModel) {
				dataChangePersonLinkModel(event);
			}
			else if (event.getSource() == profileRelationModel) {
				dataChangeProfileRelationModel(event);
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	private void dataChangePersonLinkModel(CacheModelEvent<Long> event) throws Exception {
		if (! isFiringDataChange()) {
			/* If isFiringDataChange() returns true, the call of this method is a result of
			 * firing a CacheModelEvent by the ParticipantModel itself and can be ignored.
			 * Especially when deleting participants refresh() must not be called, because
			 * this would remove the data of the deleted Participants from the cache.
			 * So other Listeners that are informed later could not get these Participants from
			 * cache. If they call getParticipant(), this model tries to load them anew from
			 * the server what leads to an Exception!
			 */

    		// get modified personLinks
    		List<Long> personLinks = event.getKeyList();

    		// List with profileIDs that need a refresh
    		List<Long> refreshProfileIDs = new ArrayList<Long>(personLinks.size());

    		// get loaded Profiles
    		Collection<Profile> loadedProfiles = getLoadedAndCachedEntities();

    		/* Check for each loaded Profile if it is linked with an updated personLink.
    		 * For a new link, the ID of a loaded Profile is contained in PersonLinkData.
    		 * For a deleted link, the loaded Profile has still the old personLink.
    		 */
    		for (Profile profile : loadedProfiles) {
    			Long profileID = profile.getID();
    			Long profilePersonLink = profile.getPersonLink();

    			for (Long personLink : personLinks) {
    				PersonLinkData personLinkData = personLinkModel.getPersonLinkData(personLink);
    				if (
    					/* The Profile is part of PersonLinkData but its personLink is not the same as
    					 * in PersonLinkData. So the Profile's personLink has changed.
    					 */
    					(
        					profileID.equals(personLinkData.getProfileID())
        					&&
        					( profile.getPersonLink() == null || !profile.getPersonLink().equals(personLink) )
    					)
    					||
    					/* The Profile has the personLink of the PersonLinkData but is not its Profile
    					 * (anymore). So the Profile's personLink has changed.
    					 */
    					(
        					personLinkData.getPersonLink().equals(profilePersonLink)
        					&&
        					!profileID.equals(personLinkData.getProfileID())
    					)
    				) {
    					refreshProfileIDs.add(profileID);
    				}
    			}
    		}

    		// refresh Profiles
    		if ( ! refreshProfileIDs.isEmpty()) {
    			refresh(refreshProfileIDs);
    		}
		}
	}


	private void dataChangeProfileRelationModel(CacheModelEvent<Long> event) throws Exception {
		if (! isFiringDataChange()) {
    		if (event.getOperation() == CacheModelOperation.DELETE) {
        		// List with profileIDs that need a refresh
        		Set<Long> refreshProfileIDs = createHashSet(event.getKeyList().size() * 2);

        		// get modified profileRelations
        		for (Long profileRelationID : event.getKeyList()) {
        			ProfileRelation profileRelation = profileRelationModel.getProfileRelation(profileRelationID);
        			refreshProfileIDs.add(profileRelation.getProfile1ID());
        			refreshProfileIDs.add(profileRelation.getProfile2ID());
    			}

        		// refresh Profiles
        		if ( ! refreshProfileIDs.isEmpty()) {
        			refresh(refreshProfileIDs);
        		}
    		}
		}
	}

}
