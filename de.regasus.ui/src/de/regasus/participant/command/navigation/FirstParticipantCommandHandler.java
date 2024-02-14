package de.regasus.participant.command.navigation;

import static de.regasus.LookupService.getParticipantMgr;


public class FirstParticipantCommandHandler extends AbstractFirstLastParticipantNavigationCommandHandler {

	@Override
	protected Long findPKOfTargetParticipant(Long eventPK) {
		return getParticipantMgr().getFirstPK(eventPK);
	}

}
