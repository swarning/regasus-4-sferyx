package de.regasus.hotel.booking.dialog;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.wizard.Wizard;

import com.lambdalogic.messeinfo.hotel.data.HotelBookingCVO;
import com.lambdalogic.messeinfo.hotel.data.HotelCancelationTermVO;

import de.regasus.I18N;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.hotel.HotelBookingModel;
import de.regasus.ui.Activator;

public class CancelHotelBookingsWizard extends Wizard {


	private List<HotelBookingCVO> hotelBookingList;
	private SelectCancelationTermsPage selectCancelationTermsPage;


	// **************************************************************************
	// * Constructors
	// *

	public CancelHotelBookingsWizard(List<HotelBookingCVO> hotelBookingList) {

		this.hotelBookingList = hotelBookingList;
	}


	// **************************************************************************
	// * Initializers
	// *

	@Override
	public void addPages() {
		selectCancelationTermsPage = new SelectCancelationTermsPage(hotelBookingList);
		addPage(selectCancelationTermsPage);
	}


	@Override
	public boolean performFinish() {
		boolean canceled = false;

		Map<HotelBookingCVO, Long> bookingCVO2cancelationTermPkMap = new HashMap<>();

		for (HotelBookingCVO bookingCVO : hotelBookingList) {
			HotelCancelationTermVO cancelationTermVO = selectCancelationTermsPage.getChosenCancelationTermForBooking(bookingCVO);
			if (cancelationTermVO != null) {
				bookingCVO2cancelationTermPkMap.put(bookingCVO, cancelationTermVO.getPK());
			}
			else {
				// Cancellation of cancellation, without cancellation terms
				bookingCVO2cancelationTermPkMap.put(bookingCVO, null);
			}
		}

		try {
			HotelBookingModel.getInstance().cancelHotelBookings(bookingCVO2cancelationTermPkMap);
			canceled = true;
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
		return canceled;
	}


	@Override
	public String getWindowTitle() {
		return I18N.CancelBooking;
	}

}
