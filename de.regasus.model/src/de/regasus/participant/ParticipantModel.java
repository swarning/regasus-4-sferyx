package de.regasus.participant;

import static com.lambdalogic.util.CollectionsHelper.*;
import static de.regasus.LookupService.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.lambdalogic.messeinfo.contact.CustomFieldUpdateParameter;
import com.lambdalogic.messeinfo.exception.InvalidValuesException;
import com.lambdalogic.messeinfo.exception.ParticipantDuplicateException;
import com.lambdalogic.messeinfo.hotel.data.HotelBookingCVO;
import com.lambdalogic.messeinfo.kernel.AbstractEntity2;
import com.lambdalogic.messeinfo.kernel.data.AbstractCVO;
import com.lambdalogic.messeinfo.kernel.interfaces.SQLOperator;
import com.lambdalogic.messeinfo.kernel.sql.SQLParameter;
import com.lambdalogic.messeinfo.participant.Participant;
import com.lambdalogic.messeinfo.participant.ParticipantCustomField;
import com.lambdalogic.messeinfo.participant.ParticipantCustomFieldValidator;
import com.lambdalogic.messeinfo.participant.ParticipantCustomFieldValue;
import com.lambdalogic.messeinfo.participant.data.BadgeCVO;
import com.lambdalogic.messeinfo.participant.data.BadgeDocumentCVO;
import com.lambdalogic.messeinfo.participant.data.BadgeVO;
import com.lambdalogic.messeinfo.participant.data.IParticipant;
import com.lambdalogic.messeinfo.participant.data.IParticipantDeleteComparator;
import com.lambdalogic.messeinfo.participant.data.ParticipantCVO;
import com.lambdalogic.messeinfo.participant.data.ParticipantCVOSettings;
import com.lambdalogic.messeinfo.participant.data.ParticipantVO;
import com.lambdalogic.messeinfo.participant.data.ProgrammeBookingCVO;
import com.lambdalogic.messeinfo.participant.interfaces.CertificateResult;
import com.lambdalogic.messeinfo.participant.interfaces.OldCustomFieldUpdateParameter;
import com.lambdalogic.messeinfo.participant.interfaces.ParticipantSettings;
import com.lambdalogic.messeinfo.participant.sql.ParticipantSearch;
import com.lambdalogic.messeinfo.profile.PersonLinkData;
import com.lambdalogic.messeinfo.profile.ProfileRelationTypeRole;
import com.lambdalogic.messeinfo.salutation.AddressLabelGenerator;
import com.lambdalogic.report.oo.OpenOfficeConstants;
import com.lambdalogic.util.EqualsHelper;
import com.lambdalogic.util.MapHelper;
import com.lambdalogic.util.Triple;
import com.lambdalogic.util.exception.ErrorMessageException;
import com.lambdalogic.util.model.CacheModel;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.model.CacheModelOperation;

import de.regasus.auth.AuthorizationException;
import de.regasus.core.ClientCountryNameProvider;
import de.regasus.core.InvitationCardGeneratorModel;
import de.regasus.core.SalutationGeneratorModel;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.model.MICacheModel;
import de.regasus.event.ClientEventProvider;
import de.regasus.model.Activator;
import de.regasus.person.AddressLabelGeneratorModel;
import de.regasus.person.PersonLinkModel;


public class ParticipantModel extends MICacheModel<Long, Participant>
implements CacheModelListener<Long> {

	private static final ParticipantCVOSettings participantCVOSettings;

	private static ParticipantModel singleton = null;

	// models
	private PersonLinkModel personLinkModel;

	public static final Object GROUP_MANAGER_KEY = "GROUP_MANAGER";

	static {
		// set ClientEventProvider to take advantage of local caches
		Participant.setEventProvider(ClientEventProvider.getInstance());

		// set ClientCountryNameProvider to take advantage of local caches
		AddressLabelGenerator.setCountryNameProvider(ClientCountryNameProvider.getInstance());

		participantCVOSettings = new ParticipantCVOSettings();
		participantCVOSettings.withBadges = true;
	}


	public static Participant getInitialParticipant() throws Exception {
		Participant participant = new Participant();

		participant.setSettings(new ParticipantSettings(participantCVOSettings));

		List<ProgrammeBookingCVO> programmeBookingCVOs = Collections.emptyList();
    	participant.setProgrammeBookingCVOs(programmeBookingCVOs);

    	List<HotelBookingCVO> hotelBookingCVOs = Collections.emptyList();
    	participant.setHotelBookingCVOs(hotelBookingCVOs);

    	List<BadgeCVO> badgeCVOs = Collections.emptyList();
    	participant.setBadgeCVOs(badgeCVOs);

    	participant.setParticipantStatePK(ParticipantVO.STATE_REGISTRATION);

		prepareParticipant(participant);

    	return participant;
	}


	private static void prepareParticipant(Participant participant) throws Exception {
		// set the SalutationGenerator to avoid multiple instances
		participant.setSalutationGenerator(SalutationGeneratorModel.getInstance().getSalutationGenerator());
		participant.getSettings().withSalutationGenerator = true;

		// set the InvitationCardGeneratorModel to avoid multiple instances
		participant.setInvitationCardGenerator(InvitationCardGeneratorModel.getInstance().getInvitationCardGenerator());
		participant.getSettings().withInvitationCardGenerator = true;

		// set AddressLabelGenerator
		participant.setAddressLabelGenerator(AddressLabelGeneratorModel.getInstance().getAddressLabelGenerator());
	}


	public static ParticipantModel getInstance() {
		if (singleton == null) {
			singleton = new ParticipantModel();
			singleton.initModels();
		}
		return singleton;
	}


	private ParticipantModel() {
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
	}


	@Override
	protected Long getKey(Participant entity) {
		return entity.getID();
	}


	@Override
	protected boolean isSameVersion(Participant p1, Participant p2) {
		boolean sameVersion = false;

		// check if the participant itself has changed
		if (p1 != null && p2 != null) {
			Date editTime1 = p1.getEditTime();
			Date editTime2 = p2.getEditTime();
			sameVersion = EqualsHelper.isEqual(editTime1, editTime2);
		}

		// check if any of participant's badges has changed
		if (sameVersion) {
			List<BadgeCVO> badgeCVOs1 = p1.getBadgeCVOs();
			List<BadgeCVO> badgeCVOs2 = p2.getBadgeCVOs();

			sameVersion = AbstractCVO.checkSameCVOsSameEditTimes(badgeCVOs1, badgeCVOs2);
		}

		// check if any of participant's custom fields has changed
		if (sameVersion) {
			List<ParticipantCustomFieldValue> pcfvList1 = p1.getCustomFieldValues();
			List<ParticipantCustomFieldValue> pcfvList2 = p2.getCustomFieldValues();

			sameVersion = AbstractEntity2.checkSameEntitiesSameEditTimes(pcfvList1, pcfvList2);
		}
		return sameVersion;
	}


	@Override
	protected Participant getEntityFromServer(Long participantID) throws Exception {
		final Long participantPK = participantID;
		final ParticipantVO participantVO = getParticipantMgr().getParticipantVO(participantPK);

		Participant participant = getParticipant(participantVO);
		return participant;
	}


	public Participant getParticipant(Long participantID) throws Exception {
		return super.getEntity(participantID);
	}


	@Override
	protected List<Participant> getEntitiesFromServer(Collection<Long> participantIDs)
	throws Exception {
		final List<Long> pkList = new ArrayList<>(participantIDs.size());
		for (Long id : participantIDs) {
			final Long pk = id;
			pkList.add(pk);
		}

		List<ParticipantVO> participantVOs = getParticipantMgr().getParticipantVOs(pkList);

		List<Participant> participantList = getParticipantListFromVOs(participantVOs);
		return participantList;
	}


	public List<Participant> getParticipants(Collection<Long> participantIDs) throws Exception {
		return super.getEntities(participantIDs);
	}


	public Participant getParticipantByBarcode(String barcode) throws Exception {
		Participant participant = null;

		ParticipantCVO participantCVO = getParticipantMgr().getParticipantCVOByBarcode(barcode, participantCVOSettings);

		if (participantCVO != null) {
			participant = getParticipant(participantCVO);
			// we may put the participant because we loaded the extended version
			put(participant);

			fireRefresh(participant);
		}

		return participant;
	}


	// **************************************************************************
	// * Extensions
	// *

	@Override
	protected boolean isExtended(Participant participant) {
		// Do not check the data, but always the settings, because the data may initialized lazily.
		return
			participant != null &&
			participant.getSettings() != null &&
			participant.getSettings().withBadges;
	}


	@Override
	protected void copyExtendedValues(Participant fromParticipant, Participant toParticipant) {
		toParticipant.copyTransientValuesFrom(fromParticipant, false);
	}


	@Override
	protected Participant getExtendedEntityFromServer(Long participantID) throws Exception {
		Long participantPK = participantID;
		ParticipantCVO participantCVO = getParticipantMgr().getParticipantCVO(
			participantPK,
			participantCVOSettings
		);
		if (participantCVO == null) {
			throw new ErrorMessageException("Participant not found: " + participantID);
		}
		Participant participant = getParticipant(participantCVO);

		return participant;
	}


	public Participant getExtendedParticipant(Long participantID) throws Exception {
		return super.getExtendedEntity(participantID);
	}


	@Override
	protected List<Participant> getExtendedEntitiesFromServer(List<Long> participantIDs)
	throws Exception {
		List<Long> participantPKs = new ArrayList<>(participantIDs.size());
		for (Long id : participantIDs) {
			final Long pk = id;
			participantPKs.add(pk);
		}

		List<ParticipantCVO> participantCVOs = getParticipantMgr().getParticipantCVOs(
			participantPKs,
			participantCVOSettings
		);
		if (participantCVOs == null) {
			throw new ErrorMessageException("Participants not found: " + participantIDs);
		}

		List<Participant> participantList = getParticipantListFromCVOs(participantCVOs);
		return participantList;
	}


	public List<Participant> getExtendedParticipants(List<Long> participantIDs) throws Exception {
		return super.getExtendedEntities(participantIDs);
	}

	// *
	// * Extensions
	// **************************************************************************

	public void validate(Participant participant) throws Exception {
		ParticipantVO participantVO = participant.getParticipantVO();
		validate(participantVO);
	}


	public void validate(ParticipantVO participantVO) throws Exception {
		InvalidValuesException ive = null;
		try {
			boolean isNew = participantVO.getID() == null;
			if (isNew) {
				// Set valid dummy value for number to avoid InvalidValuesException. It's removed later.
				participantVO.setNumber( Participant.NUMBER.getMax().intValue() );
			}

			participantVO.validate();

			if (isNew) {
				// remove dummy value for number
				participantVO.setNumber(null);
			}
		}
		catch (InvalidValuesException e) {
			ive = e;
		}

		// validate custom fields
		Long eventID = participantVO.getEventId();
		ParticipantCustomFieldModel pcfModel = ParticipantCustomFieldModel.getInstance();
		List<ParticipantCustomField> customFieldList = pcfModel.getParticipantCustomFieldsByEventPK(eventID);
		// create a copy because customFieldList may be unmodifiable and the Validator needs a modifiable Collection
		customFieldList = new ArrayList<>(customFieldList);

		try {
			ParticipantCustomFieldValidator.validate(participantVO, customFieldList);
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

	@Override
	protected Participant updateEntityOnServer(Participant participant) throws Exception {
		Participant updatedParticipant = null;

		/* Der ParticipantManager arbeitet noch mit ParticipantVO.
		 * Daher wird aus dem Participant zuerst ein ParticipantVO erzeugt.
		 * Dieses an den Server zum Erzeugen der Daten gegeben und anschl.
		 * aus den zurückgegebenen ParticipantVO wieder ein Participant erzeugt.
		 */
		ParticipantVO participantVO = participant.getParticipantVO();

		validate(participantVO);

		participantVO = getParticipantMgr().updateParticipant(participantVO);
		updatedParticipant = getParticipant(participantVO);
		return updatedParticipant;
	}


	@Override
	public Participant update(Participant participant) throws Exception {
		return super.update(participant);
	}


	public Participant create(Participant participant, boolean force)
	throws ParticipantDuplicateException, Exception {
		Participant newParticipant = null;

		if (serverModel.isLoggedIn()) {
	        // init SQLParameter for duplicate search
	        List<SQLParameter> sqlParameterList = null;
	        if ( ! force) {
	            sqlParameterList = new ArrayList<>();

	            if (participant.getFirstName() != null) {
	                sqlParameterList.add(ParticipantSearch.FIRST_NAME.getSQLParameter(participant.getFirstName(), SQLOperator.FUZZY_REGEXP));
	                sqlParameterList.add(ParticipantSearch.FIRST_NAME.getSQLParameter(null, SQLOperator.EQUAL));
	            }

	            if (participant.getLastName() != null) {
	                sqlParameterList.add(ParticipantSearch.LAST_NAME.getSQLParameter(participant.getLastName(), SQLOperator.FUZZY_REGEXP));
	                sqlParameterList.add(ParticipantSearch.LAST_NAME.getSQLParameter(null, SQLOperator.EQUAL));
	            }
	        }


	        /* ParticipantManager expects ParticipantVO (not Participant entity).
	         * Therefore at first the Participant is converted into a ParticipantVO.
	         * Then the PartiicpantVO is used to create its data on the server.
	         * Finally the returned ParticipantVO is converted back.
	         */
			ParticipantVO participantVO = participant.getParticipantVO();
			validate(participantVO);

			boolean groupManager = participant.get(GROUP_MANAGER_KEY) == Boolean.TRUE;
			if (groupManager) {
				participantVO = getParticipantMgr().createParticipantAsGroupManager(participantVO, sqlParameterList);
			}
			else {
				participantVO = getParticipantMgr().createParticipant(participantVO, sqlParameterList);
			}

			newParticipant = getExtendedEntityFromServer(participantVO.getPK());

			put(newParticipant);

			fireCreate(newParticipant.getID());
		}

		return newParticipant;
	}


	public void createByProfiles(
    	List<Long> profileIDs,
    	Long eventPK,
    	Long participantStatePK,
    	Long participantTypePK,
    	List<Triple<Long, ProfileRelationTypeRole, Long>> profileRelations
	)
	throws Exception {
		/* It's important, that the created Participants are put into the model cache, because
		 * the PersonLinkModel will load each created Participant to check its personLink.
		 */

		if (serverModel.isLoggedIn()) {
			List<ParticipantVO> participantVOs = getParticipantMgr().createParticipantsByProfiles(
				profileIDs,
				eventPK,
				participantStatePK,
				participantTypePK,
				null,	// portalPK
				profileRelations
			);

			// assure that the cache is big enough
			int formerCacheSize = assureCacheSize(participantVOs.size());

			List<Participant> participants = new ArrayList<>(participantVOs.size());
			List<Long> participantIDs = new ArrayList<>(participantVOs.size());
			Set<Long> personLinks = createHashSet(participantVOs.size());
			for (ParticipantVO participantVO : participantVOs) {
				Participant participant = getParticipant(participantVO);
				Long participantID = participant.getID();

				participants.add(participant);
				participantIDs.add(participantID);
				// every Participant has a personLink because they are connected to a Profile
				personLinks.add(participant.getPersonLink());

				participant.setBadgeCVOs(new ArrayList<>(0));
				participant.getSettings().withBadges = true;

				put(participant);
			}

			fireCreate(participantIDs);

			// reset the initial cache size
			setCacheSize(formerCacheSize);
		}
	}


	public void createAnonymousGroupMembers(
    	int count,
    	Participant participantTemplate
	)
	throws Exception {
		if (serverModel.isLoggedIn()) {
			ParticipantVO participantTemplateVO = participantTemplate.getParticipantVO();
			Long groupManagerPK = participantTemplateVO.getGroupManagerPK();

			List<ParticipantVO> participantVOs = getParticipantMgr().createAnonymParticipant(
				participantTemplateVO,
				count,
				groupManagerPK
			);

			// assure that the cache is big enough
			int formerCacheSize = assureCacheSize(participantVOs.size());

			List<Participant> participants = new ArrayList<>(participantVOs.size());
			List<Long> participantIDs = new ArrayList<>(participantVOs.size());
			for (ParticipantVO participantVO : participantVOs) {
				Participant participant = getParticipant(participantVO);

				Long participantID = participant.getID();

				participants.add(participant);
				participantIDs.add(participantID);

				participant.setBadgeCVOs(new ArrayList<BadgeCVO>(0));
				participant.getSettings().withBadges = true;

				put(participant);
			}

			try {
				fireCreate(participantIDs);
			}
			catch (Exception e) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			}

			// reset the initial cache size
			setCacheSize(formerCacheSize);
		}
	}


	public void delete(Participant participant, boolean force) throws Exception {
		// do not call super.delete(participant), because of special handling of parameter 'force'
		if (serverModel.isLoggedIn()) {

			/*
			 * Code that is copied and adapted from super method CacheModel.delete(EntityType)
			 */

			Long key = getKey(participant);

			if (key == null) {
				throw new Exception("The key of entities to delete must not be null.");
			}

			if (participant != null) {
				// if possible save the old foreign key value
				List<?> oldForeignKeyList = null;
				Participant oldEntity = getEntityIfAvailable(key);
				if (oldEntity != null) {
					oldForeignKeyList = getForeignKeyList(oldEntity);
					if (oldForeignKeyList == CacheModel.UNKNOWN_FOREIGN_KEY) {
						oldForeignKeyList = null;
					}
				}


	    		/*
	    		 * Code that would have been implemented in local method deleteEntityOnServer(KeyType)
	    		 */

				getParticipantMgr().deleteParticipant(key, force);

				handleDelete(
					participant,
					oldForeignKeyList,
					true	// fireCoModelEvent
				);
			}


			/*
			 * Additional code
			 */

			// refresh main participant, because it could reference the deleted participant as second person
			Long mainParticipantID = participant.getCompanionOfPK();
			if (mainParticipantID != null) {
				Participant mainParticipant = getEntityIfAvailable(mainParticipantID);
				if (mainParticipant != null) {
					refresh(mainParticipantID);
				}
			}
		}
	}


	public void delete(Collection<Participant> participants, boolean force) throws Exception {
		// do not call super.delete(participants), because of special handling of parameter 'force'

		if (serverModel.isLoggedIn()) {

			/*
			 * Code that is copied and adapted from super method CacheModel.delete(Collection)
			 */

			if (notEmpty(participants)) {
				// if possible save the old foreign key value
				Map<Long, Collection<?>> oldKey2foreignKeysMap = MapHelper.createHashMap(participants.size());
				for (Participant entity : participants) {
					Long key = getKey(entity);
					if (key == null) {
						throw new Exception("The key of entities to delete must not be null.");
					}

					Participant oldEntity = getEntityIfAvailable(key);
					if (oldEntity != null) {
						List<?> oldForeignKeyList = getForeignKeyList(oldEntity);
						if (oldForeignKeyList != null) {
							oldKey2foreignKeysMap.put(key, oldForeignKeyList);
						}
					}
				}

				// delete entities on server
				/* Order iParticipantList to delete at first companion, then group members and finally group managers.
				 * Otherwise it might happen that some Participant cannot be deleted, because they are referenced by others.
				 * E.g. if a group manager and its group members are in different chunks
				 */
				List<Participant> participantList;
				if (participants instanceof List) {
					participantList = (List<Participant>) participants;
				}
				else {
					participantList = createArrayList(participants);
				}

				Collections.sort(participantList, IParticipantDeleteComparator.getInstance());

				List<Long> participantPKs = createArrayList(participantList.size());
	    		for (IParticipant iParticipant : participantList) {
					participantPKs.add(iParticipant.getPK());
				}


	    		/*
	    		 * Code that would have been implemented in local method deleteEntitiesOnServer(Collection)
	    		 */
				getParticipantMgr().deleteParticipants(participantPKs, force);

				handleDelete(
					participants,
					oldKey2foreignKeysMap,
					true	// fireCoModelEvent
				);

			}


			/*
			 * Additional code
			 */

			// refresh main participants, because they could reference the deleted participant as second person
			List<Long> mainParticipantIDs = createArrayList(participants.size());
			for (Participant deletedParticipant : participants) {
				Long mainParticipantID = deletedParticipant.getCompanionOfPK();
				if (mainParticipantID != null) {
					mainParticipantIDs.add(mainParticipantID);
				}
			}

			if (!mainParticipantIDs.isEmpty()) {
				/* Refresh only those Participants that are still available, because refreshing deleted Participants
				 * will cause errors.
				 */
				List<Participant> mainParticipants = getEntitiesIfAvailable(mainParticipantIDs);
				if (notEmpty(mainParticipants)) {
					mainParticipantIDs = Participant.getIDs(mainParticipants);
					refresh(mainParticipantIDs);
				}
			}
		}
	}


	public void cancelParticipantsByParticipant(List<IParticipant> list, boolean withSubParticipants)
	throws Exception {
		for (IParticipant iParticipant : list) {
			cancelParticipantByParticipant(iParticipant.getPK(), withSubParticipants);
		}
	}


	private void cancelParticipantByParticipant(Long participantPK, boolean withSubParticipants)
	throws Exception {
		if (serverModel.isLoggedIn()) {

			List<Long> canceledParticipantPKs = getParticipantMgr().cancelParticipantByParticipant(
				participantPK,
				withSubParticipants
			);

			handleUpdate(canceledParticipantPKs);
		}
	}


	public void cancelParticipantsByOrganizer(List<IParticipant> list, boolean withSubParticipants)
	throws Exception {
		for (IParticipant iParticipant : list) {
			cancelParticipantByOrganiser(iParticipant.getPK(), withSubParticipants);
		}
	}


	private void cancelParticipantByOrganiser(Long participantPK, boolean withSubParticipants)
	throws Exception {
		if (serverModel.isLoggedIn()) {

			List<Long> canceledParticipantPKs = getParticipantMgr().cancelParticipantByOrganiser(
				participantPK,
				withSubParticipants
			);

			handleUpdate(canceledParticipantPKs);
		}
	}


	public List<Long> copy(
		List<Long> participantPKs,
		Long destEventPK,
		Long participantStatePK,
		Long participantTypePK,
		boolean link,
		boolean copyCustomFieldValues
	)
	throws ErrorMessageException {
		List<Long> copyPKs = null;

		if (serverModel.isLoggedIn()) {
			copyPKs = getParticipantMgr().copy(
				participantPKs,
				destEventPK,
				participantStatePK,
				participantTypePK,
				link,
				copyCustomFieldValues
			);

			try {
				fireDataChange(CacheModelOperation.CREATE);
			}
			catch (Exception e) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			}

			/* Prinzipiell können durch das Kopieren von Teilnehmern auch neue Verbindungen
			 * zu Profilen entstehen, da die PersonLinks ebenfalls kopiert werden.
			 * Dieser Umstand wird aus Performanzgründen ignoriert.
			 * Dies führt dazu, dass in ProfileEditor nicht automatisch die neue Veranstaltung angezeigt wird.
			 */
		}
		else {
			copyPKs = Collections.emptyList();
		}

		return copyPKs;
	}


	public void copyPersonDataFromProfile(Long participantPK)
	throws AuthorizationException, ErrorMessageException, Exception {
		if (serverModel.isLoggedIn()) {
			ParticipantVO participantVO = getParticipantMgr().copyPersonDataFromProfile(participantPK);
			Participant participant = getParticipant(participantVO);

			put(participant);
			fireUpdate(participant.getID());
		}
	}

	// **************************************************************************
	// * Group
	// *

	public void makeGroupManager(Collection<IParticipant> participantList)
	throws Exception {
		if (serverModel.isLoggedIn() && participantList != null) {

			for (IParticipant iParticipant : participantList) {
				if (iParticipant != null) {
					Long participantPK = iParticipant.getPK();
					if (participantPK != null && !iParticipant.isInGroup()) {
						try {
							// do the work on the server
							Collection<Long> updatedParticipantPKs = getParticipantMgr().makeGroupManager(participantPK);

							handleUpdate(updatedParticipantPKs);
						}
						catch (Exception e) {
							RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
						}
					}
				}
			}
		}
	}


	public void ungroup(Collection<IParticipant> participantList)
	throws Exception {
		if (serverModel.isLoggedIn() && participantList != null) {


			for (IParticipant iParticipant : participantList) {
				if (iParticipant != null) {
					Long participantPK = iParticipant.getPK();
					if (participantPK != null && iParticipant.isGroupManager()) {
						try {
							// do the work on the server
							Collection<Long> updatedParticipantPKs = getParticipantMgr().setGroupManager(
								participantPK,
								null
							);

							/* Clear foreign key data.
							 * If the Group Manager has any companions, this info will be loaded
							 * again, during the next access to the foreign key infos.
							 */
							removeForeignKeyData(participantPK);


							// drop updated participantPKs which are not loaded
							Collection<Long> updatedParticipantIDs = new ArrayList<>(updatedParticipantPKs.size());
							for (Iterator<Long> it = updatedParticipantPKs.iterator(); it.hasNext();) {
								Long id = it.next();
								if (isLoaded(id)) {
									updatedParticipantIDs.add(id);
								}
							}

							handleUpdate(updatedParticipantIDs);

						}
						catch (Exception e) {
							RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
						}
					}
				}
			}
		}
	}


	/**
	 * Sets a new or no group manager to a participant.
	 *
	 * @param participantList the participants whose group manager is set, must not
	 *  be null and must not be a group managers or a companions
	 * @param groupManagerID Long of the group manager which is set, may be null,
	 *  so it it's possible to unset a participants group manager also
	 * @throws Exception
	 */
	public void setGroupManager(Collection<? extends IParticipant> participantList, Long groupManagerID)
	throws Exception {
		if (serverModel.isLoggedIn() && participantList != null) {
			// PKs of participants whose group manager is set
			Set<Long> refreshPartcipantPKs = createHashSet(participantList.size() * 2);

			Set<Long> oldRootPKs = createHashSet(participantList.size());



			Long groupManagerPK = null;
			if (groupManagerID != null) {
				groupManagerPK = groupManagerID;
			}

			for (IParticipant participant : participantList) {
				if (participant != null) {
					try {
						if (participant.isGroupManager()) {
							throw new Exception("The participant " + participant.getNumber() + "(" + participant.getName() + ") must not be a group manager.");
						}
						if (participant.isCompanion()) {
							throw new Exception("The participant " + participant.getNumber() + "(" + participant.getName() + ") must not be a companion.");
						}

						Long participantPK = participant.getPK();

						Long oldRootPK = participant.getRootPK();
						if (oldRootPK != null && !oldRootPK.equals(groupManagerID)) {
							oldRootPKs.add(oldRootPK);
						}

						// do the work on the server
						getParticipantMgr().setGroupManager(
							participantPK,
							groupManagerPK
						);

						refreshPartcipantPKs.add(participantPK);

						// add all known companions to refreshPartcipantPKs
						for (Participant p : getLoadedAndCachedEntities()) {
							if (participantPK.equals(p.getCompanionOfPK())) {
								refreshPartcipantPKs.add(p.getID());
							}
						}
					}
					catch (Exception e) {
						RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
					}

				}
			}


			/* fire an UPDATE-Event for all participants whose group manager was set
			 * But don't fire if refreshPartcipantPKs is empty, because otherwise all listeners will be
			 * called!
			 */
			if (!refreshPartcipantPKs.isEmpty()) {
				handleUpdate(refreshPartcipantPKs);
			}

			// refresh foreign key listeners of the old root-PKs
			if (!oldRootPKs.isEmpty()) {
				for (Long oldRootPK : oldRootPKs) {
					fireRefreshForForeignKey(oldRootPK);
				}
			}
		}
	}

	// *
	// * Group
	// **************************************************************************

	// **************************************************************************
	// * Companion
	// *

	/**
	 * Sets a new or no main participant to a participant which will be a companion after this.
	 *
	 * @param companionList the participants whose main partcipant is set, must not
	 *  be null and must not be a group managers and must not have companions itself
	 * @param mainParticipantID Long of the main participant which is set, may be null,
	 *  so it it's possible to unset a partcipants main participant also, so they
	 *  won_t be companions anymore
	 * @throws Exception
	 */
	public void setCompanion(Collection<? extends IParticipant> companionList, Long mainParticipantID)
	throws Exception {
		if (serverModel.isLoggedIn() && companionList != null) {
			// PKs of participants whose group manager is set
			Set<Long> updatedParticipantPKs = new HashSet<>();
			Set<Long> oldRootPKs = new HashSet<>();



			Long mainPartcipantPK = null;
			if (mainParticipantID != null) {
				mainPartcipantPK = mainParticipantID;
			}

			for (IParticipant companion : companionList) {
				if (companion != null) {
					try {
						if (companion.isGroupManager()) {
							throw new Exception("The participant " + companion.getNumber() + "(" + companion.getName() + ") must not be a group manager.");
						}
						/* The participnts in companionList must not have companions itself, but we cannot check this!
						 * This is checked on the server.
						 */

						Long companionPK = companion.getPK();

						Long oldRootPK = companion.getRootPK();
						if (oldRootPK != null) {
							oldRootPKs.add(oldRootPK);
						}

						// old main participants are updated by the server automatically if they loose the companion that is their secondPerson
						Long oldAccompaniedParticipantPK = companion.getCompanionOfPK();
						if (oldAccompaniedParticipantPK != null && !oldAccompaniedParticipantPK.equals(mainParticipantID)) {
							updatedParticipantPKs.add(oldAccompaniedParticipantPK);
						}

						// do the work on the server
						getParticipantMgr().makeCompanion(companionPK, mainPartcipantPK);

						updatedParticipantPKs.add(companionPK);
					}
					catch (Exception e) {
						RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
					}

				}
			}


			/* fire an UPDATE-Event for all participants whose main participant was set
			 * But don't fire if updatedParticipantPKs is empty, because otherwise all listeners
			 * will be called!
			 *
			 * This informs the foreignKeyListener of the new rootPK, too!
			 */
			if (!updatedParticipantPKs.isEmpty()) {
				handleUpdate(updatedParticipantPKs);
			}

			// refresh foreign key listeners of the old root-PKs
			if (!oldRootPKs.isEmpty()) {
				for (Long oldRootPK : oldRootPKs) {
					fireRefreshForForeignKey(oldRootPK);
				}
			}


			/* Do not refresh the main particpant, because companions are not included in the model data.
			 * Even not in the enriched data.
			 *
			 */
			// refresh listener to the main participant, so they might learn that there are new companions
//			if (mainParticipantID != null) {
//				refresh(mainParticipantID);
//			}
		}
	}


	// *
	// * Companion
	// **************************************************************************

	// **************************************************************************
	// * FK-Methods / Tree-Methods
	// *

	@Override
	protected boolean isForeignKeySupported() {
		return true;
	}


	@Override
	public Object getForeignKey(Participant participant) {
		Long fk = null;
		if (participant != null) {
			fk= participant.getRootPK();
		}
		return fk;
	}


	@Override
	protected List<Participant> getEntitiesByForeignKeyFromServer(Object foreignKey) throws Exception {
		// foreignKey can also be Long
		Long rootPK = (Long) foreignKey;

		List<ParticipantVO> participantVOs = getParticipantMgr().getParticipantTreeVOs(
			rootPK
		);


		List<Participant> participantList = getParticipantListFromVOs(participantVOs);
		return participantList;
	}


	public List<Participant> getParticipantTreeByRootPK(Long rootPK) throws Exception {
		return getEntityListByForeignKey(rootPK);
	}


	public void addForeignKeyListener(CacheModelListener<?> listener, Long foreignKey) {
		super.addForeignKeyListener(listener, foreignKey);
	}


	public void removeForeignKeyListener(CacheModelListener<?> listener, Long foreignKey) {
		super.removeForeignKeyListener(listener, foreignKey);
	}


	// *
	// * FK-Methods / Tree-Methods
	// **************************************************************************

	@Override
	public void dataChange(CacheModelEvent<Long> event) {
		if (!serverModel.isLoggedIn()) {
			return;
		}

		try {
			if (event.getSource() == personLinkModel && ! isFiringDataChange()) {
				/* If isFiringDataChange() returns true, the call of this method is a result of
				 * firing a CacheModelEvent by the ParticipantModel itself and can be ignored.
				 * Especially when deleting participants refresh() must not be called, because
				 * this would remove the data of the deleted Participants from the cache.
				 * So other Listeners (the ProgrammeBookingsTableComposite) that are informed later
				 * could not get these Participants from cache. If they call getParticipant(),
				 * this model tries to load them anew from the server what leads to an Exception!
				 */

				/* get personLinks
				 * Load all PersonLinkData at once, because there might not be all PersonLinkData
				 * in the cache of the PersonLinkModel.
				 */
				List<Long> personLinks = event.getKeyList();
				List<PersonLinkData> personLinkDatas = personLinkModel.getPersonLinkDatas(personLinks);

				// List with participantIDs that need a refresh
				List<Long> refreshParticipantIDs = new ArrayList<>(personLinks.size());

				// get loaded Participants
				Collection<Participant> loadedParticipants = getLoadedAndCachedEntities();

				/* Check for each loaded Participant if it is linked with an updated personLink.
				 * For a new link, the ID of a loaded Participant is contained in PersonLinkData.
				 * For a deleted link, the loaded Participant has still the old personLink.
				 */
				for (Participant participant : loadedParticipants) {
					Long participantID = participant.getID();
					Long participantPersonLink = participant.getPersonLink();

					for (PersonLinkData personLinkData : personLinkDatas) {
						if (! personLinkData.getPersonLink().equals(participantPersonLink) &&
							personLinkData.getParticipantIDs().contains(participantID)
						) {
							// participant got a new personLink
							refreshParticipantIDs.add(participantID);
						}
						else if (personLinkData.getPersonLink().equals(participantPersonLink) &&
							! personLinkData.getParticipantIDs().contains(participantID)
						) {
							// personLink of participant has been deleted
							refreshParticipantIDs.add(participantID);
						}
					}
				}

				// refresh Participants
				if ( ! refreshParticipantIDs.isEmpty()) {
					refresh(refreshParticipantIDs);
				}
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	private static Participant getParticipant(ParticipantVO participantVO) throws Exception {
		Participant participant = null;
		if (participantVO != null) {
			participant = participantVO.getParticipant();

			prepareParticipant(participant);
		}
		return participant;
	}


	private static Participant getParticipant(ParticipantCVO participantCVO) throws Exception {
		Participant participant = null;

		if (participantCVO != null) {
			participant = participantCVO.getParticipant();

			// set the SalutationGenerator to avoid multiple instances
			participant.setSalutationGenerator(SalutationGeneratorModel.getInstance().getSalutationGenerator());

			// set the InvitationCardGeneratorModel to avoid multiple instances
			participant.setInvitationCardGenerator(InvitationCardGeneratorModel.getInstance().getInvitationCardGenerator());

			// set AddressLabelGenerator
			participant.setAddressLabelGenerator(AddressLabelGeneratorModel.getInstance().getAddressLabelGenerator());
		}

		return participant;
	}


	private static List<Participant> getParticipantListFromVOs(List<ParticipantVO> participantVOs)
	throws Exception {
		List<Participant> participantList = null;
		if (participantVOs != null) {
			participantList = new ArrayList<>(participantVOs.size());
			for (ParticipantVO participantVO : participantVOs) {
				Participant participant = getParticipant(participantVO);
				participantList.add(participant);
			}
		}
		return participantList;
	}


	private static List<Participant> getParticipantListFromCVOs(List<ParticipantCVO> participantCVOs)
	throws Exception {
		List<Participant> participantList = null;
		if (participantCVOs != null) {
			participantList = new ArrayList<>(participantCVOs.size());
			for (ParticipantCVO participantCVO : participantCVOs) {
				Participant participant = getParticipant(participantCVO);
				participantList.add(participant);
			}
		}
		return participantList;
	}


	public void toggleBadgeState(BadgeCVO badgeCVO) throws Exception {
		if (badgeCVO.getVO().isDisabled()) {
			getBadgeMgr().enableBadge(badgeCVO.getPK());
		}
		else {
			getBadgeMgr().disableBadge(badgeCVO.getPK());
		}

		handleUpdate(badgeCVO.getVO().getParticipantPK());
	}

	// **************************************************************************
	// * Collective Changes
	// *

	public void updateNotificationTimes(
		List<Long> participantPKs,
		Date programmeNoteTime,
		boolean shouldSetProgrammeNoteTime,
		Date hotelNoteTime,
		boolean shouldSetHotelNoteTime
	) {

		boolean refreshNeeded = false;

		try {
			// since there may be two different times, and even one setting and one deletion,
			// we might have to make two server calls
			if (shouldSetProgrammeNoteTime) {
				getParticipantMgr().updateNoteTime(programmeNoteTime, participantPKs, true, false);
				refreshNeeded = true;
			}

			if (shouldSetHotelNoteTime) {
				getParticipantMgr().updateNoteTime(hotelNoteTime, participantPKs, false, true);
				refreshNeeded = true;
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
		finally {
			if (refreshNeeded) {
				try {
					handleUpdate(participantPKs);
				}
				catch (Exception e) {
					RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
				}
			}
		}
	}


	public void updateNoteTime(
		Date date,
		List<Long> participantPKs,
		boolean markProgramPointBookingsAsConfirmed,
		boolean markHotelPointBookingsAsConfirmed
	) {
		try {
			getParticipantMgr().updateNoteTime(
				date,
				participantPKs,
				markProgramPointBookingsAsConfirmed,
				markHotelPointBookingsAsConfirmed
			);

			// The updating of the note time sets also the edit time in the server, so especially the
			// participant editor needs to get informed (MIRCP-484 - Zugriffskonflikt beim Speichern)

			handleUpdate(participantPKs);
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	public void updateRegisterDate(Date registrationDate, Collection<Long> participantPKs) {
		try {

			getParticipantMgr().updateRegisterDate(registrationDate, participantPKs);

			handleUpdate(participantPKs);
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	public void updateCertificatePrintDate(Date certificatePrintDate, Collection<Long> participantPKs) {
		try {

			getParticipantMgr().updateCertificatePrintDate(certificatePrintDate, participantPKs);

			handleUpdate(participantPKs);
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	/**
	 * Since the participant state of possibly many entities may be seen in the search view, we
	 * obtain the entities together with the server call to avoid additional server calls.
	 */
	public void updateParticipantState(Long participantStatePK, Collection<Long> participantPKs) {
		try {
			List<ParticipantVO> updatedParticipantVOs = getParticipantMgr().updateParticipantState(
				participantStatePK,
				participantPKs
			);


			// assure that the cache is big enough
			int formerCacheSize = assureCacheSize(updatedParticipantVOs.size());

			for (ParticipantVO participantVO : updatedParticipantVOs) {
				Participant participant = getParticipant(participantVO);
				put(participant);
			}

			fireUpdate(participantPKs);
			setCacheSize(formerCacheSize);
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	/**
	 * Since the participant type of possibly many entities may be seen in the search view, we
	 * obtain the entities together with the server call to avoid additional server calls.
	 */
	public void updateParticipantType(Long participantTypePK, Collection<Long> participantPKs) {
		try {

			List<ParticipantVO> updatedParticipantVOs = getParticipantMgr().updateParticipantType(
				participantTypePK,
				participantPKs
			);


			// assure that the cache is big enough
			int formerCacheSize = assureCacheSize(updatedParticipantVOs.size());

			for (ParticipantVO participantVO : updatedParticipantVOs) {
				Participant participant = getParticipant(participantVO);
				put(participant);
			}

			fireUpdate(participantPKs);
			setCacheSize(formerCacheSize);
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	public void updateCustomFields(
		List<OldCustomFieldUpdateParameter> updateParameterList,
		List<Long> participantPKs
	) {
		try {

			getParticipantMgr().updateCustomFields(
				updateParameterList,
				participantPKs
			);

			handleUpdate(participantPKs);
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	public void updateParticipantCustomFields(
		List<CustomFieldUpdateParameter> updateParameterList,
		List<Long> participantPKs
	) {
		try {
			getParticipantMgr().updateParticipantCustomFields(
				updateParameterList,
				participantPKs
			);

			handleUpdate(participantPKs);
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}

	// *
	// * Collective Changes
	// **************************************************************************

	public CertificateResult generateCertificateDocuments(
		Long participantID,
		String certificatePolicySource
	)
	throws Exception {
		CertificateResult certificateResult = getParticipantMgr().generateCertificateDocuments(
			participantID,
			certificatePolicySource
		);

		if (notEmpty( certificateResult.getDocumentContainerList() )) {
			// refresh participant because attribute certificatePrint changed
			refresh(participantID);
		}

		return certificateResult;
	}


	public BadgeVO createBadge(
		Long participantPK,
		boolean checkDoubleBadges,
		boolean checkFullPayment
	)
	throws Exception {
		BadgeVO badgeVO = getBadgeMgr().createBadge(
			participantPK,
			null,			// byte[] badgeID
			0,	// Integer type
			false,			// boolean forceBadgeID
			checkDoubleBadges,
			checkFullPayment
		);

		refresh(participantPK);

		return badgeVO;
	}


	public BadgeDocumentCVO createBadgeWithDocument(
		Long participantPK,
		boolean checkDoubleBadges,
		boolean checkFullPayment
	)
	throws Exception {
		BadgeDocumentCVO badgeDocumentCVO = getBadgeMgr().createBadgeWithDocument(
			participantPK,
			checkDoubleBadges,
			checkFullPayment,
			OpenOfficeConstants.FORMAT_KEY_ODT
		);

		refresh(participantPK);

		return badgeDocumentCVO;
	}


	public void setBadgeId(Participant participant, Long assignBadgePK, byte[] badgeID, boolean force) throws Exception {
		// Store the id of the participant that gets a new badge id assigned
		List<Long> idsToBeRefreshed = new ArrayList<>();
		idsToBeRefreshed.add(participant.getPK());

		// Find whether there was already a participant with that badge id
		ParticipantCVO previousParticipantWithThatBadge = getParticipantMgr().getParticipantCVOByBarcode(
			new String(badgeID),
			new ParticipantCVOSettings()
		);
		if (previousParticipantWithThatBadge != null) {
			// If yes, store that ones id as well
			idsToBeRefreshed.add(previousParticipantWithThatBadge.getVO().getPK());
		}

		// Try to make the assigment
		getBadgeMgr().setBadgeID(assignBadgePK, badgeID, force);

		// If no exception happened, update the one or two participant(s)
		handleUpdate(idsToBeRefreshed);
	}


	public int forceExitForParticipant(Long participantPK)
	throws Exception {
		int count = getLeadMgr().forceExitForParticipant(participantPK);

		handleUpdate(participantPK);

		return count;
	}


	public int forceExitForLocation(Long locationPK)
	throws Exception {
		int count = getLeadMgr().forceExitForLocation(locationPK);

		refresh();

		return count;
	}


	public int forceExitForEvent(Long eventPK)
	throws Exception {
		int count = getLeadMgr().forceExitForEvent(eventPK);

		refresh();

		return count;
	}

}
