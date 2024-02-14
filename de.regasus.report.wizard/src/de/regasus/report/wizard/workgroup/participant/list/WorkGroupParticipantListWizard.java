package de.regasus.report.wizard.workgroup.participant.list;

import com.lambdalogic.messeinfo.participant.report.workGroupParticipantList.WorkGroupParticipantListReportParameter;
import com.lambdalogic.messeinfo.report.data.BaseReportVO;
import com.lambdalogic.xml.XMLContainer;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.report.dialog.DefaultReportWizard;
import de.regasus.report.dialog.IReportWizard;
import de.regasus.report.wizard.common.EventWizardPage;
import de.regasus.report.wizard.ui.Activator;

public class WorkGroupParticipantListWizard extends DefaultReportWizard implements IReportWizard {
	private WorkGroupParticipantListReportParameter workGroupParticipantListReportParameter;
	

	public WorkGroupParticipantListWizard() {
		super();
	}


	@Override
	public void initialize(BaseReportVO baseReportVO, String reportName, XMLContainer xmlRequest) {
		try {
			setReportName(reportName);
			
			// ReportParameter des benötigten Typs erzeugen
	        workGroupParticipantListReportParameter = new WorkGroupParticipantListReportParameter(xmlRequest);
	
	        // ReportParameter der Superklasse bekannt machen
	        setReportParameter(workGroupParticipantListReportParameter);
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	@Override
	protected void doAddPages() {
		addPage(new EventWizardPage());
		addPage(new WorkGroupListWizardPage());
	}

}
