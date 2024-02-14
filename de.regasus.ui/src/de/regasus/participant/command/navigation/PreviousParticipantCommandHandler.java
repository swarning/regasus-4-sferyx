package de.regasus.participant.command.navigation;

import static de.regasus.LookupService.getParticipantMgr;

public class PreviousParticipantCommandHandler extends AbstractNextPreviousParticipantNavigationCommandHandler {

	@Override
	protected Long findPKOfTargetParticipant(Long eventPK, Long participantPK) {
		return getParticipantMgr().getPreviousPK(eventPK, participantPK);
	}

}
