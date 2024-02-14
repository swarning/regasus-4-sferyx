package de.regasus.programme.booking.dialog;

import static de.regasus.LookupService.getProgrammeBookingMgr;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.lambdalogic.messeinfo.invoice.data.PriceVO;
import com.lambdalogic.messeinfo.participant.data.IParticipant;
import com.lambdalogic.messeinfo.participant.data.ProgrammeBookingParameter;
import com.lambdalogic.messeinfo.participant.data.ProgrammeOfferingCVO;

import de.regasus.participant.booking.BookingParameterHelper;
import de.regasus.participant.booking.SelectInvoiceRecipientPage;

public class ProgrammeBookingParameterHelper {

	/**
	 * Builds bookingParameters as a cartesian product of participants contained in the participantSearchDataList and
	 * offerings that have a non-zero count in the selectProgrammeOfferingsPage
	 */
	protected static List<ProgrammeBookingParameter> createProgrammeBookingParameterList(
		List<? extends IParticipant> iParticipantList,
		SelectProgrammeOfferingsPage selectProgrammeOfferingsPage,
		SelectInvoiceRecipientPage selectInvoiceRecipientPage,
		InfoAndOptionsPage infoAndOptionsPage
	)
	throws Exception {

		// The list in which to return all created bookingParameters
		List<ProgrammeBookingParameter> resultBookingParameters = new ArrayList<>();

		// Find the offerings to book from the page
		List<ProgrammeOfferingCVO> offeringsCVO = selectProgrammeOfferingsPage.getBookedProgrammeOfferingsCVO();

		boolean onlyOnce = infoAndOptionsPage.isOnlyOnce();

		// Get the Map of all booked programme point PKs by all the selected participants
		Map<Long, List<Long>> benefitRecipientPKToProgrammePointPKsMap = null;
		if (onlyOnce) {
			// Make the list of PKs of
			Set<Long> participantPKs = new HashSet<>();
			for (IParticipant p : iParticipantList) {
				participantPKs.add(p.getPK());
			}

			benefitRecipientPKToProgrammePointPKsMap =
				getProgrammeBookingMgr().getBenefitRecipientPKToProgrammePointPKsMap(participantPKs);
		}

		// Go through all originally selected participants
		for (IParticipant iParticipant : iParticipantList) {
			List<Long> bookedProgrammePointPKs = null;
			if (onlyOnce) {
				bookedProgrammePointPKs = benefitRecipientPKToProgrammePointPKsMap.get(iParticipant.getPK());
			}

			// Go through all the booked offerings and create ProgrammeBookingParameters
			for (ProgrammeOfferingCVO offeringCVO : offeringsCVO) {
				// Skip this offering if its programme point is among those that are already booked
				Long programmePointPK = offeringCVO.getProgrammePointCVO().getPK();
				if (bookedProgrammePointPKs == null || !bookedProgrammePointPKs.contains(programmePointPK)) {
					/* Build the bookingParamenter for the current participant and booked offering,
					 * using additional data from the pages.
					 */
					ProgrammeBookingParameter pbp = createBookingParameter(
						offeringCVO,
						iParticipant,
						selectProgrammeOfferingsPage,
						selectInvoiceRecipientPage,
						infoAndOptionsPage
					);

					resultBookingParameters.add(pbp);
				}
			}
		}
		return resultBookingParameters;
	}


	/**
	 * The created list serves at the same time to provide the data for the overview page, and also to create the
	 * bookings on the server.
	 *
	 * The {@link ProgrammeBookingParameterExtended} contains some attributes more thatn the
	 * {@link ProgrammeBookingParameter}, but those are declared transient and are therefore not sent to the server.
	 */

	protected static ProgrammeBookingParameter createBookingParameter(
		ProgrammeOfferingCVO offeringCVO,
		IParticipant iParticipant,
		SelectProgrammeOfferingsPage selectProgrammeOfferingsPage,
		SelectInvoiceRecipientPage selectInvoiceRecipientPage,
		InfoAndOptionsPage infoAndOptionsPage
	)
	throws Exception {
		ProgrammeBookingParameter pbp = new ProgrammeBookingParameter();
		PriceVO priceVO = selectProgrammeOfferingsPage.getPrice(offeringCVO);
		pbp.amount = priceVO.getAmount();
		pbp.currency = priceVO.getCurrency();

		pbp.workGroupPK = selectProgrammeOfferingsPage.getWorkGroupPK(offeringCVO);

		pbp.invoiceRecipientPK = BookingParameterHelper.findInvoiceRecipient(selectInvoiceRecipientPage, iParticipant);
		pbp.invoiceRecipient = BookingParameterHelper.findInvoiceRecipientSearchData(selectInvoiceRecipientPage, iParticipant);

		pbp.benefitRecipientPK = iParticipant.getPK();
		pbp.benefitRecipient = iParticipant;

		pbp.programmeOfferingPK = offeringCVO.getPK();
		pbp.programmeOfferingVO = offeringCVO.getVO();
		pbp.programmePointName = offeringCVO.getProgrammePointCVO().getPpName();
		pbp.programmePointPK = offeringCVO.getProgrammePointCVO().getPK();

		pbp.count = selectProgrammeOfferingsPage.getCount(offeringCVO);
		pbp.info = infoAndOptionsPage.getInfo();

		// Workgroup not done
		return pbp;
	}

}
