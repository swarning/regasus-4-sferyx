package de.regasus.participant.command.navigation;

import static de.regasus.LookupService.getParticipantMgr;

public class NextParticipantCommandHandler extends AbstractNextPreviousParticipantNavigationCommandHandler {

	@Override
	protected Long findPKOfTargetParticipant(Long eventPK, Long participantPK) {
		return getParticipantMgr().getNextPK(eventPK, participantPK);
	}

}
