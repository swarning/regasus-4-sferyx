package de.regasus.participant;

import static de.regasus.LookupService.*;

import java.util.Collection;
import java.util.List;

import com.lambdalogic.messeinfo.participant.ParticipantCorrespondence;

import de.regasus.core.model.MICacheModel;

public class ParticipantCorrespondenceModel extends MICacheModel<Long, ParticipantCorrespondence> {

	private static ParticipantCorrespondenceModel singleton;
		
	
	private ParticipantCorrespondenceModel() {
		super();
	}
	

	public static ParticipantCorrespondenceModel getInstance() {
		if (singleton == null) {
			singleton = new ParticipantCorrespondenceModel();
		}
		return singleton;
	}
	
	@Override
	protected Long getKey(ParticipantCorrespondence entity) {
		return entity.getId();
	}

	@Override
	protected ParticipantCorrespondence getEntityFromServer(Long correspondenceID) throws Exception {
		return getParticipantCorrespondenceMgr().find(correspondenceID);
	}
	

	@Override
	protected List<ParticipantCorrespondence> getEntitiesFromServer(Collection<Long> correspondenceIDs) throws Exception {
		List<ParticipantCorrespondence> list = getParticipantCorrespondenceMgr().findByPKs(correspondenceIDs);
		return list;
	}

	
	@Override
	protected ParticipantCorrespondence createEntityOnServer(ParticipantCorrespondence correspondence) throws Exception {
		correspondence.validate();
		correspondence = getParticipantCorrespondenceMgr().create(correspondence);
		return correspondence;
	}

	
	public ParticipantCorrespondence create(ParticipantCorrespondence correspondence) throws Exception {
		return super.create(correspondence);
	}

	
	@Override
	protected ParticipantCorrespondence updateEntityOnServer(ParticipantCorrespondence correspondence) throws Exception {
		correspondence.validate();
		correspondence = getParticipantCorrespondenceMgr().update(correspondence);
		return correspondence;
	}

	
	public ParticipantCorrespondence update(ParticipantCorrespondence correspondence) throws Exception {
		return super.update(correspondence);
	}

	
	@Override
	protected void deleteEntityOnServer(ParticipantCorrespondence correspondence) throws Exception {
		if (correspondence != null) {
			Long id = correspondence.getId();
			getParticipantCorrespondenceMgr().deleteByPK(id);
		}
	}

	public void delete(ParticipantCorrespondence correspondence) throws Exception {
		super.delete(correspondence);
	}


	@Override
	protected boolean isForeignKeySupported() {
		return true;
	}

	
	protected Object getForeignKey(ParticipantCorrespondence correspondence) {
		Long fk = null;
		if (correspondence != null) {
			fk = correspondence.getParticipantId();
		}
		return fk;
	}

	
	protected List<ParticipantCorrespondence> getEntitiesByForeignKeyFromServer(Object foreignKey) throws Exception {
		Long participantId = (Long) foreignKey;
		List<ParticipantCorrespondence> list = getParticipantCorrespondenceMgr().findByParticipantId(participantId);
		return list;
	}
	
	
	public List<ParticipantCorrespondence> getCorrespondenceListByParticipantId(Long participantID) throws Exception {
		return getEntityListByForeignKey(participantID);
	}

	public ParticipantCorrespondence getCorrespondence(Long correspondenceID) throws Exception {
		return getEntity(correspondenceID);
	}

}
