package de.regasus.participant.state;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.lambdalogic.messeinfo.participant.ParticipantState;
import com.lambdalogic.util.TypeHelper;
import com.lambdalogic.util.rcp.widget.EntityProvider;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.participant.ParticipantStateModel;
import de.regasus.ui.Activator;

public class ParticipantStateProvider implements EntityProvider<ParticipantState> {

	private ParticipantStateModel participantStateModel = ParticipantStateModel.getInstance();


	private Collection<Long> whitelistIds;


	public ParticipantStateProvider() {
	}


	public ParticipantStateProvider(Collection<Long> whitelistIds) {
		this.whitelistIds = whitelistIds;
	}


	@Override
	public List<ParticipantState> getEntityList() {
		List<ParticipantState> participantStates = Collections.emptyList();
		try {
			participantStates = participantStateModel.getParticipantStates();

			if (whitelistIds != null) {
				// remove Participant States whose ID are not whitelisted
    			for (Iterator<ParticipantState> it = participantStates.iterator(); it.hasNext();) {
    				ParticipantState participantState = it.next();
    				if ( ! whitelistIds.contains(participantState.getID()) ) {
    					it.remove();
    				}
    			}
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
		return participantStates;
	}


	@Override
	public ParticipantState findEntity(Object id) {
		try {
			Long participantStateId = TypeHelper.toLong(id);
			return participantStateModel.getParticipantState(participantStateId);
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			return null;
		}
	}


	@Override
	public Collection<ParticipantState> findEntities(Collection<?> idCol) {
		try {
			List<Long> idList = TypeHelper.toLongList(idCol);
			return participantStateModel.getParticipantStates(idList);
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			return null;
		}
	}

}
