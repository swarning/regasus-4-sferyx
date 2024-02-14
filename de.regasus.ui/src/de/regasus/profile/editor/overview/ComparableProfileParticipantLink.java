package de.regasus.profile.editor.overview;

import com.lambdalogic.messeinfo.participant.Participant;
import com.lambdalogic.messeinfo.participant.data.EventVO;
import com.lambdalogic.messeinfo.profile.ProfileParticipantLink;
import com.lambdalogic.time.I18NDate;


/**
 * Extension of ProfileParticipantLink used in ProfileOverviewForm to order ProfileParticipantLinks
 * by the startTime of their event and the state of their participant.
 */
class ComparableProfileParticipantLink extends ProfileParticipantLink {
	private static final long serialVersionUID = 1L;

	private Participant participant;
	private EventVO eventVO;


	ComparableProfileParticipantLink() {
	}


	ComparableProfileParticipantLink(ProfileParticipantLink profileParticipantLink) {
		super(
			profileParticipantLink.getParticipantID(),
			profileParticipantLink.getEventID()
		);
	}


	Participant getParticipant() {
		return participant;
	}


	void setParticipant(Participant participant) {
		this.participant = participant;
	}


	EventVO getEventVO() {
		return eventVO;
	}


	void setEventVO(EventVO eventVO) {
		this.eventVO = eventVO;
	}


	// Delegate method for ComparableProfileParticipantLinkComparator
	I18NDate getEventBeginDate() {
		if (eventVO != null) {
			return eventVO.getBeginDate();
		}
		return null;
	}

}
