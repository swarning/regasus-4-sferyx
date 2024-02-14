package de.regasus.report.wizard.programme.point.lead;

import com.lambdalogic.messeinfo.participant.report.programmePointLead.ProgrammePointLeadReportParameter;
import com.lambdalogic.messeinfo.report.data.BaseReportVO;
import com.lambdalogic.xml.XMLContainer;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.report.dialog.DefaultReportWizard;
import de.regasus.report.dialog.IReportWizard;
import de.regasus.report.wizard.common.EventWizardPage;
import de.regasus.report.wizard.common.ProgrammePointListWizardPage;
import de.regasus.report.wizard.ui.Activator;

public class ProgrammePointLeadWizard extends DefaultReportWizard implements IReportWizard {

	private ProgrammePointLeadReportParameter programmePointLeadReportParameter;


	public ProgrammePointLeadWizard() {
		super();
	}


	@Override
	public void initialize(BaseReportVO baseReportVO, String reportName, XMLContainer xmlRequest) {
		try {
			setReportName(reportName);

			// ReportParameter des benötigten Typs erzeugen
			programmePointLeadReportParameter = new ProgrammePointLeadReportParameter(xmlRequest);

	        // ReportParameter der Superklasse bekannt machen
	        setReportParameter(programmePointLeadReportParameter);
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	@Override
	protected void doAddPages() {
		addPage(new EventWizardPage());
		addPage(new ProgrammePointListWizardPage());
		addPage(new ProgrammePointLeadSettingsWizardPage());
	}

}

