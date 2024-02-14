package de.regasus.participant.booking;

import com.lambdalogic.messeinfo.participant.Participant;
import com.lambdalogic.messeinfo.participant.data.IParticipant;

import de.regasus.participant.ParticipantModel;

public class BookingParameterHelper {

	public static Long findInvoiceRecipient(
		SelectInvoiceRecipientPage selectInvoiceRecipientPage,
		IParticipant participant
	)
	throws Exception {

		switch (selectInvoiceRecipientPage.getInvoiceRecipientSelectionPolicy()) {
		case EACH_PARTICIPANT_THEMSELF:
			return participant.getPK();
		case GROUPMANAGER_OR_MAIN_PARTICIPANT_OR_THEMSELF:

			if (participant.getGroupManagerPK() != null) {
				return participant.getGroupManagerPK();
			}
			else if (participant.getCompanionOfPK() != null) {
				return participant.getCompanionOfPK();
			}
			else {
				return participant.getPK();
			}
		case OTHER_PRATICIPANT:
			return selectInvoiceRecipientPage.getParticipant().getPK();
		}
		return null;
	}


	public static IParticipant findInvoiceRecipientSearchData(
		SelectInvoiceRecipientPage selectInvoiceRecipientPage,
		IParticipant participant
	)
	throws Exception {

		switch (selectInvoiceRecipientPage.getInvoiceRecipientSelectionPolicy()) {
		case EACH_PARTICIPANT_THEMSELF:
			return participant;
		case GROUPMANAGER_OR_MAIN_PARTICIPANT_OR_THEMSELF:
			if (participant.getGroupManagerPK() != null) {
				return findByPK(participant.getGroupManagerPK());
			}
			else if (participant.getCompanionOfPK() != null) {
				return findByPK(participant.getCompanionOfPK());
			}
			else {
				return participant;
			}
		case OTHER_PRATICIPANT:
			return selectInvoiceRecipientPage.getParticipant();
		}
		return null;
	}


	private static IParticipant findByPK(Long pk) throws Exception {
		Participant participant = ParticipantModel.getInstance().getParticipant(pk);
		return participant;
	}

}
