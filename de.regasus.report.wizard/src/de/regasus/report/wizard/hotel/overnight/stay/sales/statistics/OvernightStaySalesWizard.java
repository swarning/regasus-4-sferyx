package de.regasus.report.wizard.hotel.overnight.stay.sales.statistics;

import com.lambdalogic.messeinfo.hotel.report.groupedRoomStat.GroupedEventOrTimePeriodReportParameter;
import com.lambdalogic.messeinfo.report.data.BaseReportVO;
import com.lambdalogic.xml.XMLContainer;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.report.dialog.DefaultReportWizard;
import de.regasus.report.dialog.IReportWizard;
import de.regasus.report.wizard.common.EventOrTimePeriodWizardPage;
import de.regasus.report.wizard.common.GroupedRoomStatisticsWizardPage;
import de.regasus.report.wizard.ui.Activator;


public class OvernightStaySalesWizard extends DefaultReportWizard implements IReportWizard {	
	
	private GroupedEventOrTimePeriodReportParameter reportParameter;
	
	private EventOrTimePeriodWizardPage eventOrTimePeriodWizardPage;

	private GroupedRoomStatisticsWizardPage groupedRoomStatisticsWizardPage;
	
	
	@Override
	public void initialize(BaseReportVO baseReportVO, String reportName, XMLContainer xmlRequest) {
		try {
			setReportName(reportName);
			
			// create ReportParameter of demanded type
			reportParameter = new GroupedEventOrTimePeriodReportParameter(xmlRequest);
	        
	        // publish ReportParameter to super class
	        setReportParameter(reportParameter);	        	       						
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	@Override
	protected void doAddPages() {
		eventOrTimePeriodWizardPage = new EventOrTimePeriodWizardPage();
		addPage(eventOrTimePeriodWizardPage);
		
		groupedRoomStatisticsWizardPage = new GroupedRoomStatisticsWizardPage();
		addPage(groupedRoomStatisticsWizardPage);
	}

	
	@Override
	public boolean canFinish() {
		return reportParameter.isComplete(); 
	}
}
