package de.regasus.programme.booking.dialog;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.wizard.Wizard;

import com.lambdalogic.messeinfo.participant.data.ProgrammeBookingCVO;
import com.lambdalogic.messeinfo.participant.data.ProgrammeCancelationTermVO;

import de.regasus.I18N;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.programme.ProgrammeBookingModel;
import de.regasus.ui.Activator;

public class CancelProgrammeBookingsWizard extends Wizard {

	private List<ProgrammeBookingCVO> programmeBookingList;
	private SelectCancelationTermsPage selectCancelationTermsPage;


	// **************************************************************************
	// * Constructors
	// *

	public CancelProgrammeBookingsWizard(List<ProgrammeBookingCVO> programmeBookingList) {
		this.programmeBookingList = programmeBookingList;
	}


	// **************************************************************************
	// * Initializers
	// *

	@Override
	public void addPages() {
		selectCancelationTermsPage = new SelectCancelationTermsPage(programmeBookingList);
		addPage(selectCancelationTermsPage);
	}


	@Override
	public boolean performFinish() {
		boolean canceled = false;

		Map<ProgrammeBookingCVO, Long> bookingCVO2cancelationTermPkMap = new HashMap<>();

		for (ProgrammeBookingCVO bookingCVO : programmeBookingList) {
			ProgrammeCancelationTermVO cancelationTermVO = selectCancelationTermsPage.getChosenCancelationTermForBooking(bookingCVO);
			if (cancelationTermVO != null) {
				bookingCVO2cancelationTermPkMap.put(bookingCVO, cancelationTermVO.getPK());
			}
			else {
				// Cancellation of cancellation, without cancellation terms
				bookingCVO2cancelationTermPkMap.put(bookingCVO, null);
			}
		}

		try {
			ProgrammeBookingModel.getInstance().cancelProgrammeBookings(bookingCVO2cancelationTermPkMap);
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
