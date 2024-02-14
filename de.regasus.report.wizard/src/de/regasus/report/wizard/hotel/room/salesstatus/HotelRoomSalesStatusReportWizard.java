package de.regasus.report.wizard.hotel.room.salesstatus;

import com.lambdalogic.messeinfo.hotel.report.hotelRoomSalesStatus.HotelRoomSalesStatusReportParameter;
import com.lambdalogic.messeinfo.report.data.BaseReportVO;
import com.lambdalogic.xml.XMLContainer;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.report.dialog.DefaultReportWizard;
import de.regasus.report.dialog.IReportWizard;
import de.regasus.report.wizard.common.EventWizardPage;
import de.regasus.report.wizard.common.HotelsWizardPage;
import de.regasus.report.wizard.ui.Activator;

public class HotelRoomSalesStatusReportWizard extends DefaultReportWizard implements IReportWizard {

	private HotelRoomSalesStatusReportParameter reportParameter;


	public HotelRoomSalesStatusReportWizard() {
		super();
	}


	@Override
	public void initialize(BaseReportVO baseReportVO, String reportName, XMLContainer xmlRequest) {
		try {
			setReportName(reportName);

			// create ReportParameter
			reportParameter = new HotelRoomSalesStatusReportParameter(xmlRequest);

	        // propagate Report Parameter to super class
	        setReportParameter(reportParameter);
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	@Override
	protected void doAddPages() {
		addPage(new EventWizardPage());
		addPage(new HotelsWizardPage());
	}

}
