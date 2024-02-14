package de.regasus.report.wizard.hotel.reminder.list;

import com.lambdalogic.messeinfo.hotel.report.eventHotelReminderList.EventHotelReminderListReportParameter;
import com.lambdalogic.messeinfo.report.data.BaseReportVO;
import com.lambdalogic.xml.XMLContainer;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.report.dialog.DefaultReportWizard;
import de.regasus.report.dialog.IReportWizard;
import de.regasus.report.wizard.common.EventWizardPage;
import de.regasus.report.wizard.ui.Activator;


public class EventHotelReminderListWizard extends DefaultReportWizard implements IReportWizard {	
	
	private EventHotelReminderListReportParameter reportParameter;

	
	// *************************************************************************
	// * Pages
	// *

	// page 1
	private EventWizardPage eventWizardPage;
	
	// page 2
	private EventHotelReminderStatusWizardPage statusWizardPage;
	
	
	public EventHotelReminderListWizard() {
		super();
	}

	
	@Override
	public void initialize(BaseReportVO baseReportVO, String reportName, XMLContainer xmlRequest) {
		try {
			setReportName(reportName);
			
			// create ReportParameter of demanded type
			reportParameter = new EventHotelReminderListReportParameter(xmlRequest);
	        
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
				
		statusWizardPage = new EventHotelReminderStatusWizardPage();
		addPage(statusWizardPage);
	}

}
