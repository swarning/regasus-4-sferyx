package de.regasus.report.wizard.hotel.guestlist;

import com.lambdalogic.messeinfo.hotel.report.hotelGuestList.HotelGuestListReportParameter;
import com.lambdalogic.messeinfo.report.data.BaseReportVO;
import com.lambdalogic.xml.XMLContainer;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.report.wizard.ui.Activator;

public class HotelCommissionListWizard extends HotelGuestListWizard {
	public HotelCommissionListWizard() {
		super();
	}

	@Override
	public void initialize(BaseReportVO baseReportVO, String reportName, XMLContainer xmlRequest) {
		try {
			setReportName(reportName);
			
			// ReportParameter des benötigten Typs erzeugen
			HotelGuestListReportParameter hotelGuestListReportParameter = new HotelGuestListReportParameter(xmlRequest);
	        // we need the contingent data to calc the commission amount
	        hotelGuestListReportParameter.setWithContingent(Boolean.TRUE);
	        
	        // ReportParameter der Superklasse bekannt machen
	        setReportParameter(hotelGuestListReportParameter);
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}

}
