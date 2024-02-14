package de.regasus.report.wizard.hotel.booking.list;

import com.lambdalogic.messeinfo.hotel.report.hotelBookings.HotelBookingsReportParameter;
import com.lambdalogic.messeinfo.report.data.BaseReportVO;
import com.lambdalogic.xml.XMLContainer;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.report.dialog.DefaultReportWizard;
import de.regasus.report.dialog.IReportWizard;
import de.regasus.report.wizard.common.EventWizardPage;
import de.regasus.report.wizard.common.ParticipantTypeWizardPage;
import de.regasus.report.wizard.ui.Activator;

public class HotelBookingListWizard extends DefaultReportWizard implements IReportWizard {
	private HotelBookingsReportParameter hotelBookingsReportParameter;

	public HotelBookingListWizard() {
		super();
	}


	@Override
	public void initialize(BaseReportVO baseReportVO, String reportName, XMLContainer xmlRequest) {
		try {
			setReportName(reportName);

			// create ReportParameter of the desired type
	        hotelBookingsReportParameter = new HotelBookingsReportParameter(xmlRequest);

	        // promote ReportParameter to super class
	        setReportParameter(hotelBookingsReportParameter);
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	@Override
	protected void doAddPages() {
		addPage(new EventWizardPage());
		addPage(new ParticipantTypeWizardPage());
		addPage(new HotelBookingListWizardPage());
	}

}
