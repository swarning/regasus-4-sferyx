package de.regasus.report.wizard.participant.change;

import com.lambdalogic.messeinfo.participant.report.participantChange.ParticipantChangeReportParameter;
import com.lambdalogic.messeinfo.report.data.BaseReportVO;
import com.lambdalogic.xml.XMLContainer;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.report.dialog.DefaultReportWizard;
import de.regasus.report.dialog.IReportWizard;
import de.regasus.report.wizard.common.EventWizardPage;
import de.regasus.report.wizard.ui.Activator;

public class ParticipantChangeWizard extends DefaultReportWizard implements IReportWizard {
	private ParticipantChangeReportParameter participantChangeReportParameter;
	

	public ParticipantChangeWizard() {
		super();
	}


	@Override
	public void initialize(BaseReportVO baseReportVO, String reportName, XMLContainer xmlRequest) {
		try {
			setReportName(reportName);
				
			// ReportParameter des benötigten Typs erzeugen
			participantChangeReportParameter = new ParticipantChangeReportParameter(xmlRequest);
	
	        // ReportParameter der Superklasse bekannt machen
	        setReportParameter(participantChangeReportParameter);
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	@Override
	protected void doAddPages() {
		addPage(new EventWizardPage());
		addPage(new ParticipantChangeOptionsWizardPage());
	}

}
