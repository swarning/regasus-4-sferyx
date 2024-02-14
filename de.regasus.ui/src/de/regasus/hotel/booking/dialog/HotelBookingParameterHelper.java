package de.regasus.hotel.booking.dialog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.lambdalogic.messeinfo.hotel.data.HotelBookingParameter;
import com.lambdalogic.messeinfo.hotel.data.HotelOfferingCVO;
import com.lambdalogic.messeinfo.participant.data.IParticipant;

import de.regasus.participant.booking.BookingParameterHelper;
import de.regasus.participant.booking.SelectInvoiceRecipientPage;

public class HotelBookingParameterHelper {

	public static List<HotelBookingParameter> createBookingParameter(
		List<? extends IParticipant> participantList,
		HotelSelectionCriteriaPage hotelSelectionCriteriaPage,
		HotelOfferingsTablePage hotelOfferingsTablePage,
		CreateHotelBookingPaymentConditionsPage hotelPaymentConditionsPage,
		SelectInvoiceRecipientPage selectInvoiceRecipientPage,
		CreateHotelBookingInfoPage createHotelBookingInfoPage
	)
	throws Exception {
		// The list in which to return all created bookingParameters
		List<HotelBookingParameter> resultBookingParameters = new ArrayList<HotelBookingParameter>();

		// Find the booked offering from the page
		HotelOfferingCVO bookedHotelOfferingCVO = hotelOfferingsTablePage.getBookedHotelOfferingCVO();

		// Go through all originally selected participants
		for (IParticipant participant : participantList) {
			Integer count = hotelOfferingsTablePage.getBookedCount();
			for (int i = 0; i < count; i++) {
				HotelBookingParameter hbp = new HotelBookingParameter();
				hbp.setPaymentCondition(hotelPaymentConditionsPage.getPaymentCondition());
				hbp.setArrival( hotelSelectionCriteriaPage.getArrival() );
				hbp.setDeparture( hotelSelectionCriteriaPage.getDeparture() );
				hbp.setArrivalInfo( createHotelBookingInfoPage.getArrivalInfo() );
				hbp.setArrivalNote( createHotelBookingInfoPage.getArrivalNote() );
				hbp.setBenefitRecipientPKs( Collections.singletonList(participant.getPK()) );
				hbp.setDepositAmount( hotelPaymentConditionsPage.getDepositAmount() );
				hbp.setGuestNames( participant.getName() );
				hbp.setHotelInfo( createHotelBookingInfoPage.getHotelInfo() );
				hbp.setHotelPaymentInfo( createHotelBookingInfoPage.getHotelPaymentInfo() );
				hbp.setAdditionalGuests( createHotelBookingInfoPage.getAdditionalGuests() );
				hbp.setHotelOfferingPK( bookedHotelOfferingCVO.getPK() );
				hbp.setHotelOfferingReferenceCode( bookedHotelOfferingCVO.getHotelOfferingVO().getReferenceCode() );
				hbp.setInfo( createHotelBookingInfoPage.getInfo() );
				hbp.setInvoiceRecipientPK( BookingParameterHelper.findInvoiceRecipient(selectInvoiceRecipientPage, participant) );
				hbp.setSmokerType( createHotelBookingInfoPage.getSmokerType() );
				hbp.setTwinRoom( createHotelBookingInfoPage.isTwin() );

				hbp.setBenefitRecipient(participant);
				hbp.setHotel( bookedHotelOfferingCVO.getHotelContingent().getHotelName() );
				hbp.setHotelOfferingVO( bookedHotelOfferingCVO.getVO() );
				hbp.setInvoiceRecipient( BookingParameterHelper.findInvoiceRecipientSearchData(selectInvoiceRecipientPage, participant) );

				resultBookingParameters.add(hbp);
			}
		}
		return resultBookingParameters;
	}

}
