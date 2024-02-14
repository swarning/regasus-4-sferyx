package de.regasus.report.wizard.programme.registrationsPerWeek;

import com.lambdalogic.messeinfo.participant.report.registrationsPerWeek.RegistrationsPerWeekReportParameter;
import com.lambdalogic.messeinfo.report.data.BaseReportVO;
import com.lambdalogic.xml.XMLContainer;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.report.dialog.DefaultReportWizard;
import de.regasus.report.dialog.IReportWizard;
import de.regasus.report.wizard.common.EventListWizardPage;
import de.regasus.report.wizard.common.ProgrammePointTypeListWizardPage;
import de.regasus.report.wizard.ui.Activator;

public class RegistrationsPerWeekWizard extends DefaultReportWizard implements IReportWizard {

	private RegistrationsPerWeekReportParameter reportParameter;


	public RegistrationsPerWeekWizard() {
	}


	@Override
	public void initialize(BaseReportVO baseReportVO, String reportName, XMLContainer xmlRequest) {
		try {
			setReportName(reportName);

			// create ReportParameter of required type
			reportParameter = new RegistrationsPerWeekReportParameter(xmlRequest);

	        // promote ReportParameter to super class
	        setReportParameter(reportParameter);
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	@Override
	protected void doAddPages() {
		EventListWizardPage eventListWizardPage = new EventListWizardPage();
		eventListWizardPage.setMandatory(true);
		addPage(eventListWizardPage);

		addPage( new RegistrationsPerWeekSettingsWizardPage() );
		addPage( new ProgrammePointTypeListWizardPage() );
	}

}
