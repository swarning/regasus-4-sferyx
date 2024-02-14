package de.regasus.report.wizard.hotel.peaknight;

import com.lambdalogic.messeinfo.hotel.report.groupedRoomStat.GroupedRoomStatisticsForEventReportParameter;
import com.lambdalogic.messeinfo.report.data.BaseReportVO;
import com.lambdalogic.xml.XMLContainer;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.report.dialog.DefaultReportWizard;
import de.regasus.report.dialog.IReportWizard;
import de.regasus.report.wizard.common.EventWizardPage;
import de.regasus.report.wizard.common.GroupedRoomStatisticsWizardPage;
import de.regasus.report.wizard.ui.Activator;


public class PeakNightRoomStatisticsForEventWizard extends DefaultReportWizard implements IReportWizard {	
	
	private GroupedRoomStatisticsForEventReportParameter reportParameter;

	
	// *************************************************************************
	// * Pages
	// *

	// page 1
	private EventWizardPage eventWizardPage;
	
	// page 2
	private GroupedRoomStatisticsWizardPage groupSelectionPage;
	
	
	public PeakNightRoomStatisticsForEventWizard() {
		super();
	}

	
	@Override
	public void initialize(BaseReportVO baseReportVO, String reportName, XMLContainer xmlRequest) {
		try {
			setReportName(reportName);
			
			// create ReportParameter of demanded type
			reportParameter = new GroupedRoomStatisticsForEventReportParameter(xmlRequest);
	        
	        // publish ReportParameter to super class
	        setReportParameter(reportParameter);	        	       						
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	@Override
	protected void doAddPages() {
		eventWizardPage = new EventWizardPage();
		addPage(eventWizardPage);
				
		groupSelectionPage = new GroupedRoomStatisticsWizardPage();
		addPage(groupSelectionPage);
	}

	
	@Override
	public boolean canFinish() {
		return reportParameter.isComplete(); 
	}
}
