package de.regasus.report.wizard.hotel.commission;

import com.lambdalogic.messeinfo.hotel.report.parameter.EventOrTimePeriodReportParameter;
import com.lambdalogic.messeinfo.report.data.BaseReportVO;
import com.lambdalogic.xml.XMLContainer;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.report.dialog.DefaultReportWizard;
import de.regasus.report.dialog.IReportWizard;
import de.regasus.report.wizard.common.EventOrTimePeriodWizardPage;
import de.regasus.report.wizard.ui.Activator;


public class HotelCommissionWizard extends DefaultReportWizard implements IReportWizard {

	private EventOrTimePeriodReportParameter reportParameter;

	private EventOrTimePeriodWizardPage eventOrTimePeriodWizardPage;


	@Override
	public void initialize(BaseReportVO baseReportVO, String reportName, XMLContainer xmlRequest) {
		try {
			setReportName(reportName);

			// create ReportParameter of demanded type
			reportParameter = new EventOrTimePeriodReportParameter(xmlRequest);

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
	}


	@Override
	public boolean canFinish() {
		return reportParameter.isComplete();
	}
}
