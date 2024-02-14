package de.regasus.report.wizard.participant.lead;

import com.lambdalogic.messeinfo.participant.report.participantLead.ParticipantLeadReportParameter;
import com.lambdalogic.messeinfo.report.data.BaseReportVO;
import com.lambdalogic.util.rcp.SelectionMode;
import com.lambdalogic.xml.XMLContainer;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.report.dialog.DefaultReportWizard;
import de.regasus.report.dialog.IReportWizard;
import de.regasus.report.wizard.common.EventWizardPage;
import de.regasus.report.wizard.common.ProgrammePointListWizardPage;
import de.regasus.report.wizard.participant.list.ParticipantSelectionReportWizardPage;
import de.regasus.report.wizard.ui.Activator;

public class ParticipantLeadWizard extends DefaultReportWizard implements IReportWizard {
	private ParticipantLeadReportParameter participantLeadReportParameter;


	public ParticipantLeadWizard() {
	}


	@Override
	public void initialize(BaseReportVO baseReportVO, String reportName, XMLContainer xmlRequest) {
		try {
			setReportName(reportName);

			// ReportParameter des benötigten Typs erzeugen
			participantLeadReportParameter = new ParticipantLeadReportParameter(xmlRequest);

	        // ReportParameter der Superklasse bekannt machen
	        setReportParameter(participantLeadReportParameter);
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	@Override
	protected void doAddPages() {
		addPage( new EventWizardPage() );
		addPage( new ProgrammePointListWizardPage() );
		addPage( new ParticipantSelectionReportWizardPage(SelectionMode.MULTI_SELECTION) );
		addPage( new ParticipantLeadSettingsWizardPage() );
	}

}

