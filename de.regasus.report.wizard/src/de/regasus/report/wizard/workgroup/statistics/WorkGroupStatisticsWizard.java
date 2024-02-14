package de.regasus.report.wizard.workgroup.statistics;

import com.lambdalogic.messeinfo.participant.report.workGroupStatistics.WorkGroupStatisticsReportParameter;
import com.lambdalogic.messeinfo.report.data.BaseReportVO;
import com.lambdalogic.xml.XMLContainer;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.report.dialog.DefaultReportWizard;
import de.regasus.report.dialog.IReportWizard;
import de.regasus.report.wizard.common.EventWizardPage;
import de.regasus.report.wizard.ui.Activator;

public class WorkGroupStatisticsWizard extends DefaultReportWizard implements IReportWizard {
	private WorkGroupStatisticsReportParameter workGroupStatisticsReportParameter;
	

	public WorkGroupStatisticsWizard() {
		super();
	}


	@Override
	public void initialize(BaseReportVO baseReportVO, String reportName, XMLContainer xmlRequest) {
		try {
			setReportName(reportName);
			
			// ReportParameter des benötigten Typs erzeugen
	        workGroupStatisticsReportParameter = new WorkGroupStatisticsReportParameter(xmlRequest);
	
	        // ReportParameter der Superklasse bekannt machen
	        setReportParameter(workGroupStatisticsReportParameter);
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	@Override
	protected void doAddPages() {
		addPage(new EventWizardPage());
	}

}
