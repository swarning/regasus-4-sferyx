/**
 * CertificateWizard.java
 * created on 16.09.2013 17:28:17
 */
package de.regasus.report.wizard.participant.list;

import com.lambdalogic.messeinfo.participant.report.certificate.CertificateReportParameter;
import com.lambdalogic.messeinfo.report.data.BaseReportVO;
import com.lambdalogic.report.parameter.IReportParameter;
import com.lambdalogic.util.rcp.SelectionMode;
import com.lambdalogic.xml.XMLContainer;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.report.dialog.DefaultReportWizard;
import de.regasus.report.dialog.IReportWizard;
import de.regasus.report.wizard.common.EventWizardPage;
import de.regasus.report.wizard.common.ProgrammePointWizardPage;
import de.regasus.report.wizard.ui.Activator;

public class CertificateWizard
extends DefaultReportWizard
implements IReportWizard {

	private CertificateReportParameter certificateReportParameter;

	private EventWizardPage eventWizardPage;
	private ParticipantSelectionReportWizardPage participantWhereWizardPage;
	private ProgrammePointWizardPage programmePointWizardPage;


	public CertificateWizard() {
	}


	@Override
	public IReportParameter getReportParameter() {
		return certificateReportParameter;
	}


	@Override
	public void initialize(BaseReportVO baseReportVO, String reportName, XMLContainer xmlRequest) {
		try {
			setReportName(reportName);
			certificateReportParameter = new CertificateReportParameter(xmlRequest);
			setReportParameter(certificateReportParameter);
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	@Override
	protected void doAddPages() {
		eventWizardPage = new EventWizardPage();
		addPage(eventWizardPage);

		participantWhereWizardPage = new ParticipantSelectionReportWizardPage(SelectionMode.NO_SELECTION);
		addPage(participantWhereWizardPage);

		programmePointWizardPage = new ProgrammePointWizardPage(SelectionMode.SINGLE_SELECTION);
		addPage(programmePointWizardPage);
	}

}
