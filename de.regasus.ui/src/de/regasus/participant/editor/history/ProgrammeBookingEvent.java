package de.regasus.participant.editor.history;

import java.util.Date;

import com.lambdalogic.messeinfo.participant.data.ProgrammeBookingCVO;

import de.regasus.I18N;
import de.regasus.history.IHistoryEvent;


public class ProgrammeBookingEvent implements IHistoryEvent {

	private ProgrammeBookingCVO programmeBookingCVO;
	private boolean forCancellation;

	public ProgrammeBookingEvent(ProgrammeBookingCVO programmeBookingCVO, boolean forCancellation) {
		this.programmeBookingCVO = programmeBookingCVO;
		this.forCancellation = forCancellation;
	}


	@Override
	public String getHtmlDescription() {
		StringBuilder sb = new StringBuilder("<DIV>");
		sb.append(programmeBookingCVO.getLabel());
		sb.append("</DIV>");
		return sb.toString();
	}

	@Override
	public Date getTime() {
		if (forCancellation) {
			return programmeBookingCVO.getVO().getCancelationDate();
		} else if (programmeBookingCVO.getVO().getBookingDate() != null) {
			return programmeBookingCVO.getVO().getBookingDate();
		} else {
			return null;
		}
	}


	@Override
	public String getType() {
		if (forCancellation) {
			return I18N.ProgrammeBooking_Cancellation;
		} else {
			return I18N.ProgrammeBooking;
		}
	}


	@Override
	public String getUser() {
		if (forCancellation) {
			return programmeBookingCVO.getVO().getEditDisplayUserStr();
		} else {
			return programmeBookingCVO.getVO().getNewDisplayUserStr();
		}
	}

}
