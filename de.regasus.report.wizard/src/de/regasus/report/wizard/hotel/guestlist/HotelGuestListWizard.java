package de.regasus.report.wizard.hotel.guestlist;

import com.lambdalogic.messeinfo.hotel.report.hotelGuestList.HotelGuestListReportParameter;
import com.lambdalogic.messeinfo.report.data.BaseReportVO;
import com.lambdalogic.xml.XMLContainer;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.report.dialog.DefaultReportWizard;
import de.regasus.report.dialog.IReportWizard;
import de.regasus.report.wizard.common.EventWizardPage;
import de.regasus.report.wizard.common.HotelWizardPage;
import de.regasus.report.wizard.ui.Activator;

public class HotelGuestListWizard extends DefaultReportWizard implements IReportWizard {
	
	public HotelGuestListWizard() {
		super();
	}

	
	@Override
	public void initialize(BaseReportVO baseReportVO, String reportName, XMLContainer xmlRequest) {
		try {
			setReportName(reportName);
			
			// ReportParameter des benötigten Typs erzeugen
			HotelGuestListReportParameter hotelGuestListReportParameter = new HotelGuestListReportParameter(xmlRequest);
	        
	        // ReportParameter der Superklasse bekannt machen
	        setReportParameter(hotelGuestListReportParameter);
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	@Override
	protected void doAddPages() {
		addPage(new EventWizardPage());
		addPage(new HotelWizardPage());
		addPage(new HotelGuestListOptionsWizardPage());
	}

}
