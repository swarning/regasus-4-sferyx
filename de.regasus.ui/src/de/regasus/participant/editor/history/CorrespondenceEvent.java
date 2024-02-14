package de.regasus.participant.editor.history;

import java.util.Date;

import com.lambdalogic.messeinfo.contact.ContactLabel;
import com.lambdalogic.messeinfo.participant.ParticipantCorrespondence;

import de.regasus.history.IHistoryEvent;

class CorrespondenceEvent implements IHistoryEvent {

	private ParticipantCorrespondence contact;

	public CorrespondenceEvent(ParticipantCorrespondence hotelBookingCVO) {
		this.contact = hotelBookingCVO;
	}


	@Override
	public String getHtmlDescription() {
		StringBuilder sb = new StringBuilder("<DIV>");

		String subject = contact.getSubject();
		if (subject != null) {
			sb.append(subject);
		}

		sb.append("</DIV>");
		return sb.toString();
	}


	@Override
	public Date getTime() {
		return contact.getCorrespondenceTime();
	}


	@Override
	public String getType() {
		return ContactLabel.Correspondence.getString() + " (" + contact.getType().getString() + ")";
	}


	@Override
	public String getUser() {
		return contact.getEditDisplayUserStr();
	}

}
