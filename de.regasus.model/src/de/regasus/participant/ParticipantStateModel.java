package de.regasus.participant;

import static de.regasus.LookupService.*;

import java.util.Collection;
import java.util.List;

import com.lambdalogic.messeinfo.participant.ParticipantState;

import de.regasus.core.model.MICacheModel;

/**
 * Model zum Zugriff auf ParticipantStates.
 *
 * @author sacha
 */
public class ParticipantStateModel
extends MICacheModel<Long, ParticipantState> {
	private static ParticipantStateModel singleton = null;


	private ParticipantStateModel() {
		super();
	}


	public static ParticipantStateModel getInstance() {
		if (singleton == null) {
			singleton = new ParticipantStateModel();
		}
		return singleton;
	}


	public String getTypeName() {
		return getClass().getSimpleName();
	}


	@Override
	protected ParticipantState getEntityFromServer(Long id) throws Exception {
		ParticipantState participantState = getParticipantStateMgr().getParticipantState(id);
		return participantState;
	}


	public ParticipantState getParticipantState(Long id) throws Exception {
		return super.getEntity(id);
	}


	@Override
	protected List<ParticipantState> getEntitiesFromServer(Collection<Long> idCol) throws Exception {
		List<ParticipantState> participantStates = getParticipantStateMgr().getParticipantStates(idCol);
		return participantStates;
	}


	public List<ParticipantState> getParticipantStates(Collection<Long> idCol) throws Exception {
		return super.getEntities(idCol);
	}


	@Override
	protected boolean isForeignKeySupported() {
		return false;
	}


	@Override
	protected Long getKey(ParticipantState entity) {
		return entity.getID();
	}


	@Override
	protected List<ParticipantState> getAllEntitiesFromServer() throws Exception {
		List<ParticipantState> participantStates = getParticipantStateMgr().getParticipantStates();
		return participantStates;
	}


	@Override
	public void loadAll() throws Exception {
		super.loadAll();
	}


	public List<ParticipantState> getParticipantStates() throws Exception {
		return getAllEntities();
	}


	@Override
	public ParticipantState create(ParticipantState participantState) throws Exception {
		return super.create(participantState);
	}


	@Override
	public ParticipantState update(ParticipantState entity) throws Exception {
		return super.update(entity);
	}


	@Override
	public void delete(ParticipantState participantState) throws Exception {
		super.delete(participantState);
	}


	@Override
	protected ParticipantState createEntityOnServer(ParticipantState participantState) throws Exception {
		participantState.validate();
		participantState = getParticipantStateMgr().create(participantState);
		return participantState;
	}


	@Override
	protected ParticipantState updateEntityOnServer(ParticipantState participantState) throws Exception {
		participantState.validate();
		participantState = getParticipantStateMgr().updateParticipantState(participantState);
		return participantState;
	}


	@Override
	protected void deleteEntityOnServer(ParticipantState participantState) throws Exception {
		if (participantState != null) {
			Long id = participantState.getID();
			getParticipantStateMgr().deleteParticipantState(id);
		}
	}
}
