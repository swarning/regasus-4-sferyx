package de.regasus.report.wizard.hotel.peaknight;

import com.lambdalogic.messeinfo.hotel.report.groupedRoomStat.GroupedRoomStatisticsForTimePeriodReportParameter;
import com.lambdalogic.messeinfo.report.data.BaseReportVO;
import com.lambdalogic.xml.XMLContainer;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.report.dialog.DefaultReportWizard;
import de.regasus.report.dialog.IReportWizard;
import de.regasus.report.wizard.common.GroupedRoomStatisticsWizardPage;
import de.regasus.report.wizard.ui.Activator;


public class PeakNightRoomStatisticsForTimePeriodWizard extends DefaultReportWizard implements IReportWizard {	
	
	private GroupedRoomStatisticsForTimePeriodReportParameter reportParameter;

	
	// *************************************************************************
	// * Pages
	// *

	// page 1
	private PeakNightRoomStatisticsForTimePeriodWizardPage timePeriodWizardPage;
	
	// page 2
	private GroupedRoomStatisticsWizardPage groupSelectionPage;
	
	
	public PeakNightRoomStatisticsForTimePeriodWizard() {
		super();
	}

	
	@Override
	public void initialize(BaseReportVO baseReportVO, String reportName, XMLContainer xmlRequest) {
		try {
			setReportName(reportName);
			
			// create ReportParameter of demanded type
			reportParameter = new GroupedRoomStatisticsForTimePeriodReportParameter(xmlRequest);
	        
	        // publish ReportParameter to super class
	        setReportParameter(reportParameter);	        	       						
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	@Override
	protected void doAddPages() {
		
		timePeriodWizardPage = new PeakNightRoomStatisticsForTimePeriodWizardPage();
		addPage(timePeriodWizardPage);
		
		groupSelectionPage = new GroupedRoomStatisticsWizardPage();
		addPage(groupSelectionPage);
	}

	
	@Override
	public boolean canFinish() {
		return 
			reportParameter.isComplete();
			
	}
}
