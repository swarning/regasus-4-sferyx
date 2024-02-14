package de.regasus.report.wizard.programme.participant.list;

import com.lambdalogic.messeinfo.participant.report.progpartlist.ProgrammeParticipantListReportParameter;
import com.lambdalogic.messeinfo.report.data.BaseReportVO;
import com.lambdalogic.xml.XMLContainer;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.report.dialog.DefaultReportWizard;
import de.regasus.report.dialog.IReportWizard;
import de.regasus.report.wizard.common.EventWizardPage;
import de.regasus.report.wizard.common.ProgrammePointListWizardPage;
import de.regasus.report.wizard.ui.Activator;

public class ProgrammeParticipantListWizard extends DefaultReportWizard implements IReportWizard {
	private ProgrammeParticipantListReportParameter programmeParticipantListReportParameter;
	

	public ProgrammeParticipantListWizard() {
		super();
	}


	@Override
	public void initialize(BaseReportVO baseReportVO, String reportName, XMLContainer xmlRequest) {
		try {
			setReportName(reportName);
			
			// ReportParameter des benötigten Typs erzeugen
	        programmeParticipantListReportParameter = new ProgrammeParticipantListReportParameter(xmlRequest);
	
	        // ReportParameter der Superklasse bekannt machen
	        setReportParameter(programmeParticipantListReportParameter);
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	@Override
	protected void doAddPages() {
		addPage(new EventWizardPage());
		addPage(new ProgrammePointListWizardPage());
	}

}
