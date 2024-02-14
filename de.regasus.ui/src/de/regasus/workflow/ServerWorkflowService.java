package de.regasus.workflow;

import static de.regasus.LookupService.*;

import com.lambdalogic.messeinfo.participant.ParticipantCustomFieldValue;
import com.lambdalogic.messeinfo.participant.data.ParticipantCVO;
import com.lambdalogic.util.exception.ErrorMessageException;

public class ServerWorkflowService {

	public void update(ParticipantCustomFieldValue value) throws ErrorMessageException {
		if (value.getID() == null) {
			getParticipantCustomFieldValueMgr().create(value);
		}
		else {
			getParticipantCustomFieldValueMgr().update(value);
		}
	}


	public void update(ParticipantCVO p) throws ErrorMessageException {
		getParticipantMgr().updateParticipant(p.getParticipantVO());
	}

}
