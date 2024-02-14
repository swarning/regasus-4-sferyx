package de.regasus.report.wizard.programme.point.statistics;

import org.eclipse.jface.wizard.IWizardPage;

import com.lambdalogic.messeinfo.participant.report.programmePointStatistics.ProgrammePointStatisticsReportParameter;
import com.lambdalogic.messeinfo.report.data.BaseReportVO;
import com.lambdalogic.xml.XMLContainer;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.report.dialog.DefaultReportWizard;
import de.regasus.report.dialog.IReportWizard;
import de.regasus.report.wizard.common.EventWizardPage;
import de.regasus.report.wizard.common.ParticipantStateWizardPage;
import de.regasus.report.wizard.common.ParticipantTypeWizardPage;
import de.regasus.report.wizard.common.ProgrammePointListWizardPage;
import de.regasus.report.wizard.ui.Activator;

public class ProgrammePointStatisticsWizard extends DefaultReportWizard implements IReportWizard {
	private ProgrammePointStatisticsReportParameter programmePointStatisticReportParameter;
	private ProgrammePointListWizardPage programmePointListWizardPage;


	public ProgrammePointStatisticsWizard() {
		super();
	}


	@Override
	public void initialize(BaseReportVO baseReportVO, String reportName, XMLContainer xmlRequest) {
		try {
			setReportName(reportName);

			// ReportParameter des benötigten Typs erzeugen
	        programmePointStatisticReportParameter = new ProgrammePointStatisticsReportParameter(xmlRequest);

	        // ReportParameter der Superklasse bekannt machen
	        setReportParameter(programmePointStatisticReportParameter);
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	@Override
	protected void doAddPages() {
		addPage(new EventWizardPage());

		programmePointListWizardPage = new ProgrammePointListWizardPage();
		addPage(programmePointListWizardPage);

		addPage(new ParticipantStateWizardPage());
		addPage(new ParticipantTypeWizardPage());
		addPage(new ProgrammePointStatisticsOptionsWizardPage());
	}


	@Override
	protected IWizardPage getFirstFinishablePage() {
		return programmePointListWizardPage;
	}

}
