package de.regasus.participant;

import static de.regasus.LookupService.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.lambdalogic.messeinfo.exception.WarnMessageException;
import com.lambdalogic.util.model.CacheModelListener;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.model.MICacheModel;
import de.regasus.event.ParticipantType;
import de.regasus.model.Activator;

public class ParticipantTypeModel extends MICacheModel<Long, ParticipantType> {
	private static ParticipantTypeModel singleton = null;

	private ParticipantTypeModel() {
	}


	public static ParticipantTypeModel getInstance() {
		if (singleton == null) {
			singleton = new ParticipantTypeModel();
		}
		return singleton;
	}


	@Override
	protected Long getKey(ParticipantType entity) {
		return entity.getId();
	}

	@Override
	protected ParticipantType getEntityFromServer(Long participantTypePK) throws Exception {
		ParticipantType participantType = getParticipantTypeMgr().read(participantTypePK);
		return participantType;
	}

	public ParticipantType getParticipantType(Long participantTypeID) throws Exception {
		return super.getEntity(participantTypeID);
	}

	@Override
	protected List<ParticipantType> getEntitiesFromServer(Collection<Long> participantTypePKs) throws Exception {
		List<ParticipantType> participantTypes = getParticipantTypeMgr().read(participantTypePKs);
		return participantTypes;
	}


	public List<ParticipantType> getParticipantTypes(Collection<Long> participantTypePKs) throws Exception {
		return super.getEntities(participantTypePKs);
	}


	@Override
	protected ParticipantType createEntityOnServer(ParticipantType participantType) throws Exception {
		participantType.validate();
		participantType = getParticipantTypeMgr().create(participantType);
		return participantType;
	}


	@Override
	public ParticipantType create(ParticipantType participantType) throws Exception {
		return super.create(participantType);
	}


	@Override
	protected ParticipantType updateEntityOnServer(ParticipantType participantType) throws Exception {
		participantType.validate();
		participantType = getParticipantTypeMgr().update(participantType);
		return participantType;
	}


	@Override
	public ParticipantType update(ParticipantType participantType) throws Exception {
		return super.update(participantType);
	}


	public void setEventParticipantTypes(Long eventID, List<Long> participantTypePKs) throws Exception {
		if (serverModel.isLoggedIn()) {

			try {
				getEventMgr().setParticipantTypes(eventID, participantTypePKs);
			}
			catch (WarnMessageException e) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			}

			/* In the case of a WarnMessageException the actual list of participant types may
			 * differ from the one we just set, because participant types might not have been
			 * deleted.
			 * Therefore we remove all information about FKs in the model to enforce a reload
			 * from the server if they are needed.
			 */
			removeForeignKeyData(eventID);

			fireRefreshForForeignKey(eventID);
		}
	}


	@Override
	protected void deleteEntityOnServer(ParticipantType participantType) throws Exception {
		if (participantType != null) {
			getParticipantTypeMgr().delete( participantType.getId() );
		}
	}


	@Override
	public void delete(ParticipantType participantType) throws Exception {
		super.delete(participantType);
	}


	@Override
	protected boolean isForeignKeySupported() {
		return true;
	}


	@Override
	protected List<ParticipantType> getEntitiesByForeignKeyFromServer(Object foreignKey) throws Exception {
		Long eventPK = (Long) foreignKey;
		List<ParticipantType> participantTypes = getParticipantTypeMgr().readByEvent(eventPK);
		return participantTypes;
	}


	public List<ParticipantType> getParticipantTypesByEvent(Long eventPK) throws Exception {
		return getEntityListByForeignKey(eventPK);
	}


	@Override
	protected List<ParticipantType> getAllEntitiesFromServer() throws Exception {
		List<ParticipantType> participantTypes = getParticipantTypeMgr().readAll(true /*withDeleted*/);
		return participantTypes;
	}


	@Override
	public void loadAll() throws Exception {
		super.loadAll();
	}


	public List<ParticipantType> getAllUndeletedParticipantTypes() throws Exception {
		Collection<ParticipantType> allParticipantTypes = getAllEntities();
		List<ParticipantType> undeletedParticipantTypes = new ArrayList<>(allParticipantTypes.size());
		for (ParticipantType participantType : allParticipantTypes) {
			if (!participantType.isDeleted()) {
				undeletedParticipantTypes.add(participantType);
			}
		}

		return undeletedParticipantTypes;
	}



	public void addForeignKeyListener(CacheModelListener<?> listener, Long foreignKey) {
		Long eventPK = foreignKey;
		super.addForeignKeyListener(listener, eventPK);
	}


	public void removeForeignKeyListener(CacheModelListener<?> listener, Long foreignKey) {
		super.removeForeignKeyListener(listener, foreignKey);
	}

}
