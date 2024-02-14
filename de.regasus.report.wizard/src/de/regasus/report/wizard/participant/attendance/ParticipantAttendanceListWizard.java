package de.regasus.report.wizard.participant.attendance;

import com.lambdalogic.messeinfo.participant.report.participantAttendanceList.ParticipantAttendanceListReportParameter;
import com.lambdalogic.messeinfo.report.data.BaseReportVO;
import com.lambdalogic.xml.XMLContainer;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.report.dialog.DefaultReportWizard;
import de.regasus.report.dialog.IReportWizard;
import de.regasus.report.wizard.common.EventWizardPage;
import de.regasus.report.wizard.common.ProgrammePointListWizardPage;
import de.regasus.report.wizard.ui.Activator;

public class ParticipantAttendanceListWizard extends DefaultReportWizard implements IReportWizard {
	private ParticipantAttendanceListReportParameter participantAttendanceListReportParameter;
	

	public ParticipantAttendanceListWizard() {
		super();
	}


	@Override
	public void initialize(BaseReportVO baseReportVO, String reportName, XMLContainer xmlRequest) {
		try {
			setReportName(reportName);
			
			// ReportParameter des benötigten Typs erzeugen
			participantAttendanceListReportParameter = new ParticipantAttendanceListReportParameter(xmlRequest);
	
	        // ReportParameter der Superklasse bekannt machen
	        setReportParameter(participantAttendanceListReportParameter);
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	@Override
	protected void doAddPages() {
		addPage(new EventWizardPage());
		addPage(new ProgrammePointListWizardPage());
		addPage(new ParticipantAttendanceListOptionsWizardPage());
	}

}
