package de.regasus.participant.dialog;

import org.eclipse.swt.widgets.Table;

import com.lambdalogic.i18n.I18NString;
import com.lambdalogic.messeinfo.hotel.data.HotelNoteCVO;
import com.lambdalogic.messeinfo.invoice.data.BookingNoteCVO;
import com.lambdalogic.messeinfo.participant.ParticipantLabel;
import com.lambdalogic.messeinfo.participant.data.ProgrammeNoteCVO;
import com.lambdalogic.util.rcp.simpleviewer.SimpleTable;

enum NotificationsOverviewTableColumns {
	NAME, TYPE, SUBTYPE, DESCRIPTION
};

public class NotificationsOverviewTable extends SimpleTable<BookingNoteCVO, NotificationsOverviewTableColumns> {

	/**
	 * This table is by purpose not sortable, to keep the order of participants
	 * unchanged between selection, overview and printing.
	 * 
	 * https://mi2.lambdalogic.de/jira/browse/MIRCP-185
	 */
	public NotificationsOverviewTable(Table table) {
		super(table, NotificationsOverviewTableColumns.class, false, false);
	}


	@Override
	public String getColumnText(BookingNoteCVO noteCVO, NotificationsOverviewTableColumns column) {

		if (noteCVO instanceof ProgrammeNoteCVO) {
			ProgrammeNoteCVO programmeNoteCVO = (ProgrammeNoteCVO) noteCVO;
			switch (column) {
    			case NAME:
    				return programmeNoteCVO.getRecipientVO().getName();
    			case TYPE:
    				return ParticipantLabel.ProgrammeNote.getString();
    			case SUBTYPE:
    				return getBookingNoteSubType(programmeNoteCVO);
    			case DESCRIPTION: {
    				String descStr = null;
    				
    				I18NString description = programmeNoteCVO.getProgrammeBookingCVO().getDescription();
    				if (description != null) {
    					descStr = description.getString();
    				}
    				
    				if (descStr == null) {
    					descStr = "";
    				}
    				
    				return descStr;
    			}
			}
		}
		else if (noteCVO instanceof HotelNoteCVO) {
			HotelNoteCVO hotelNoteCVO = (HotelNoteCVO) noteCVO;
			switch (column) {
			case NAME:
				return hotelNoteCVO.getRecipientVO().getName();
			case TYPE:
				return ParticipantLabel.HotelNote.getString();
			case SUBTYPE:
				return getBookingNoteSubType(hotelNoteCVO);
			case DESCRIPTION: {
				String descStr = null;
				
				I18NString description = hotelNoteCVO.getHotelBookingCVO().getDescription();
				if (description != null) {
					descStr = description.getString();
				}
				
				if (descStr == null) {
					descStr = "";
				}
				
				return descStr;
			}
			}
		}
		return null;
	}


	private String getBookingNoteSubType(BookingNoteCVO bookingNoteCVO) {
		StringBuilder subtype = new StringBuilder();
		if (bookingNoteCVO.isBookingNote() && !bookingNoteCVO.isCancelNote()) {
			subtype.append(ParticipantLabel.Booking.getString());
		}
		else if (!bookingNoteCVO.isBookingNote() && bookingNoteCVO.isCancelNote()) {
			subtype.append(ParticipantLabel.Cancelation.getString());
		}

		if (bookingNoteCVO.isInvoiceRecipient() && !bookingNoteCVO.isBenefitRecipient()) {
			newLine(subtype);
			subtype.append(ParticipantLabel.IsInvoicRecipient.getString());
		}
		else if (!bookingNoteCVO.isInvoiceRecipient() && bookingNoteCVO.isBenefitRecipient()) {
			newLine(subtype);
			subtype.append(ParticipantLabel.IsBenefitRecipient.getString());
		}

		if (bookingNoteCVO.isGotInvoiceRecipientNote() && !bookingNoteCVO.isLostInvoiceRecipientNote()) {
			newLine(subtype);
			subtype.append(ParticipantLabel.BecomesInvoiceRecipient.getString());
		}
		else if (!bookingNoteCVO.isGotInvoiceRecipientNote() && bookingNoteCVO.isLostInvoiceRecipientNote()) {
			newLine(subtype);
			subtype.append(ParticipantLabel.WasInvoiceRecipient.getString());
		}

		if (bookingNoteCVO.isGotBenefitRecipientNote() && !bookingNoteCVO.isLostBenefitRecipientNote()) {
			newLine(subtype);
			subtype.append(ParticipantLabel.BecomesBenefitRecipient.getString());
		}
		else if (!bookingNoteCVO.isGotBenefitRecipientNote() && bookingNoteCVO.isLostBenefitRecipientNote()) {
			newLine(subtype);
			subtype.append(ParticipantLabel.WasBenefitRecipient.getString());
		}

		if (bookingNoteCVO.isInfoNote()) {
			newLine(subtype);
			subtype.append(ParticipantLabel.Information.getString());
		}

		if (bookingNoteCVO.isChangeBenefitRecipientNote()) {
			newLine(subtype);
			subtype.append(ParticipantLabel.ChangeBenefitRecipients.getString());
		}

		return subtype.toString();
	}


	/**
	 * Appends newLine to the StringBuilder if necessary
	 */
	private void newLine(StringBuilder subtype) {
		if (subtype.length() > 0) {
			subtype.append(", ");
		}
	}

}
