package de.regasus.report.wizard.participant.duplicates;

import com.lambdalogic.messeinfo.participant.report.duplicates.ParticipantDuplicatesReportParameter;
import com.lambdalogic.messeinfo.report.data.BaseReportVO;
import com.lambdalogic.report.parameter.IReportParameter;
import com.lambdalogic.xml.XMLContainer;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.report.dialog.DefaultReportWizard;
import de.regasus.report.dialog.IReportWizard;
import de.regasus.report.wizard.common.EventWizardPage;
import de.regasus.report.wizard.ui.Activator;

public class ParticipantDuplicatesWizard extends DefaultReportWizard implements IReportWizard {

	private ParticipantDuplicatesReportParameter duplicatesReportParameter;

	private EventWizardPage eventWizardPage;

	private ParticipantDuplicatesSearchOptionsWizardPage optionsWizardPage;

	
	@Override
	public IReportParameter getReportParameter() {
		return duplicatesReportParameter;
	}


	@Override
	public void initialize(BaseReportVO baseReportVO, String reportName, XMLContainer xmlRequest) {
		try {
			setReportName(reportName);

			// ReportParameter des benötigten Typs erzeugen
			duplicatesReportParameter = new ParticipantDuplicatesReportParameter(xmlRequest);

			// ReportParameter der Superklasse bekannt machen
			setReportParameter(duplicatesReportParameter);
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	@Override
	protected void doAddPages() {
		eventWizardPage = new EventWizardPage();
		addPage(eventWizardPage);

		optionsWizardPage = new ParticipantDuplicatesSearchOptionsWizardPage();
		addPage(optionsWizardPage);
	}
}

