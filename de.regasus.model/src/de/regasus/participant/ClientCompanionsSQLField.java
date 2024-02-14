package de.regasus.participant;

import java.util.Collection;
import java.util.List;

import com.lambdalogic.messeinfo.kernel.interfaces.SQLFieldPath;
import com.lambdalogic.messeinfo.participant.sql.CompanionsSQLField;

import de.regasus.event.ParticipantType;

public class ClientCompanionsSQLField extends CompanionsSQLField {

	private static final long serialVersionUID = 1L;


	public ClientCompanionsSQLField(List<SQLFieldPath> pathList) {
		super(pathList);
	}


	@Override
	protected Collection<ParticipantType> getParticipantTypesByEventPK(Long eventPK) throws Exception {
		return ParticipantTypeModel.getInstance().getParticipantTypesByEvent(eventPK);
	}

}
