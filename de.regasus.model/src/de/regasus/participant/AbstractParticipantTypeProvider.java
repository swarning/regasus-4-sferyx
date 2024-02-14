package de.regasus.participant;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.lambdalogic.util.TypeHelper;
import com.lambdalogic.util.rcp.widget.EntityProvider;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.event.ParticipantType;
import de.regasus.model.Activator;

/**
 * Base class for different implementations of EntityProvider<ParticipantType>.
 * This base class includes the general methods to retrieve Participant Types by one or more PKs
 * {@link EntityProvider#findEntity(Object)} and {@link EntityProvider#findEntities(Collection)}.
 *
 * The method {@link EntityProvider#getEntityList()} that provides the list of Participant Types for the
 * individual purpose is not implemented.
 */
public abstract class AbstractParticipantTypeProvider implements EntityProvider<ParticipantType> {

	private ParticipantTypeModel participantTypeModel = ParticipantTypeModel.getInstance();


	@Override
	public ParticipantType findEntity(Object entityId) {
		try {
			Long participantTypeId = TypeHelper.toLong(entityId);
			return participantTypeModel.getParticipantType(participantTypeId);
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			return null;
		}
	}


	@Override
	public Collection<ParticipantType> findEntities(Collection<?> entityIds) {
		List<ParticipantType> participantTypes = new ArrayList<>( entityIds.size() );
		for (Object entityId : entityIds) {
			ParticipantType participantType = findEntity(entityId);
			participantTypes.add(participantType);
		}
		return participantTypes;
	}

}
