package de.regasus.report.wizard.programme.point.attendance;

import com.lambdalogic.messeinfo.participant.report.programmePointAttendance.ProgrammePointAttendanceReportParameter;
import com.lambdalogic.messeinfo.report.data.BaseReportVO;
import com.lambdalogic.xml.XMLContainer;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.report.dialog.DefaultReportWizard;
import de.regasus.report.dialog.IReportWizard;
import de.regasus.report.wizard.common.EventWizardPage;
import de.regasus.report.wizard.common.ProgrammePointWizardPage;
import de.regasus.report.wizard.ui.Activator;

public class ProgrammePointAttendanceWizard extends DefaultReportWizard implements IReportWizard {
	private ProgrammePointAttendanceReportParameter programmePointAttendanceReportParameter;
	

	public ProgrammePointAttendanceWizard() {
		super();
	}


	@Override
	public void initialize(BaseReportVO baseReportVO, String reportName, XMLContainer xmlRequest) {
		try {
			setReportName(reportName);
			
			// ReportParameter des benötigten Typs erzeugen
	        programmePointAttendanceReportParameter = new ProgrammePointAttendanceReportParameter(xmlRequest);
	
	        // ReportParameter der Superklasse bekannt machen
	        setReportParameter(programmePointAttendanceReportParameter);
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	@Override
	protected void doAddPages() {
		addPage(new EventWizardPage());
		addPage(new ProgrammePointWizardPage());
		addPage(new ProgrammePointAttendanceOptionsWizardPage());
	}

}
