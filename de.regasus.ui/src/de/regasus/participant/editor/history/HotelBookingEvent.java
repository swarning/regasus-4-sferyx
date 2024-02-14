package de.regasus.participant.editor.history;

import java.util.Date;

import com.lambdalogic.messeinfo.hotel.data.HotelBookingCVO;
import com.lambdalogic.util.HtmlHelper;

import de.regasus.I18N;
import de.regasus.history.IHistoryEvent;


public class HotelBookingEvent implements IHistoryEvent {

	private HotelBookingCVO hotelBookingCVO;
	private boolean forCancellation;

	public HotelBookingEvent(HotelBookingCVO hotelBookingCVO, boolean forCancellation) {
		this.hotelBookingCVO = hotelBookingCVO;
		this.forCancellation = forCancellation;
	}


	@Override
	public String getHtmlDescription() {
		StringBuilder sb = new StringBuilder("<DIV>");
		sb.append(HtmlHelper.escape(
			hotelBookingCVO.getLabel().getString(),
			true	// replaceLineBreakWithBR
		));
		sb.append("</DIV>");
		return sb.toString();
	}

	@Override
	public Date getTime() {
		if (forCancellation) {
			return hotelBookingCVO.getVO().getCancelationDate();
		}
		else {
			return hotelBookingCVO.getVO().getBookingDate();
		}
	}


	@Override
	public String getType() {
		if (forCancellation) {
			return I18N.HotelBooking_Cancellation;
		}
		else {
			return I18N.HotelBooking;
		}
	}


	@Override
	public String getUser() {
		if (forCancellation) {
			return hotelBookingCVO.getVO().getEditDisplayUserStr();
		}
		else {
			return hotelBookingCVO.getVO().getNewDisplayUserStr();
		}
	}

}
