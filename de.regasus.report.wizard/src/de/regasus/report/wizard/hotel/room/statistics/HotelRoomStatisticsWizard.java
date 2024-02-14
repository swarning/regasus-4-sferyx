package de.regasus.report.wizard.hotel.room.statistics;

import com.lambdalogic.messeinfo.hotel.report.hotelRoomStat.HotelRoomStatisticsReportParameter;
import com.lambdalogic.messeinfo.report.data.BaseReportVO;
import com.lambdalogic.xml.XMLContainer;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.report.dialog.DefaultReportWizard;
import de.regasus.report.dialog.IReportWizard;
import de.regasus.report.wizard.common.EventWizardPage;
import de.regasus.report.wizard.common.HotelsWizardPage;
import de.regasus.report.wizard.ui.Activator;

public class HotelRoomStatisticsWizard extends DefaultReportWizard implements IReportWizard {
	private HotelRoomStatisticsReportParameter hotelRoomStatisticsReportParameter;
	
	public HotelRoomStatisticsWizard() {
		super();
	}


	@Override
	public void initialize(BaseReportVO baseReportVO, String reportName, XMLContainer xmlRequest) {
		try {
			setReportName(reportName);
			
			// ReportParameter des benötigten Typs erzeugen
			hotelRoomStatisticsReportParameter = new HotelRoomStatisticsReportParameter(xmlRequest);
	        
	        // ReportParameter der Superklasse bekannt machen
	        setReportParameter(hotelRoomStatisticsReportParameter);
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
