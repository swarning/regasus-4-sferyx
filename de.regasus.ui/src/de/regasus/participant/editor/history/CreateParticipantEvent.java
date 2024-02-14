package de.regasus.participant.editor.history;

import java.util.Date;

import de.regasus.history.HistoryLabel;
import de.regasus.history.IHistoryEvent;

class CreateParticipantEvent implements IHistoryEvent {
	private Date newTime;
	private String newUser;


	public CreateParticipantEvent(Date newTime, String newUser) {
		this.newTime = newTime;
		this.newUser = newUser;
	}


	@Override
	public boolean isFirstEvent() {
		return true;
	}


	@Override
	public String getType() {
		return HistoryLabel.ParticipantCreated.getString();
	}


	@Override
	public Date getTime() {
		return newTime;
	}


	@Override
	public String getUser() {
		return newUser;
	}


	@Override
	public String getHtmlDescription() {
		return "&#160;";
	}


	@Override
	public String toString() {
		return "CreateParticipantEvent newTime=" + newTime + ", newUser=" + newUser;
	}

}
