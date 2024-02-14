package de.regasus.participant.command.navigation;

import static de.regasus.LookupService.getParticipantMgr;

public class LastParticipantCommandHandler extends AbstractFirstLastParticipantNavigationCommandHandler {

	@Override
	protected Long findPKOfTargetParticipant(Long eventPK) {
		return getParticipantMgr().getLastPK(eventPK);
	}

}
