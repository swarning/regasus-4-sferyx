package de.regasus.workflow;

import com.lambdalogic.messeinfo.participant.data.ParticipantCVO;
import com.lambdalogic.util.exception.ErrorMessageException;

import de.regasus.participant.badge.BadgePrintController;

public class BadgeWorkflowService {

	public void print(ParticipantCVO p) throws ErrorMessageException {
		BadgePrintController badgePrintController = new BadgePrintController();
		badgePrintController.createBadgeWithDocument(p.getPK(), p.getName(), p.getNumber());
	}

}
