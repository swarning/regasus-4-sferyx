package de.regasus.report.wizard.participant.list;

import com.lambdalogic.messeinfo.participant.report.participantList.ParticipantListReportParameter;
import com.lambdalogic.messeinfo.participant.sql.ParticipantSearch;
import com.lambdalogic.messeinfo.report.data.BaseReportVO;
import com.lambdalogic.report.parameter.IReportParameter;
import com.lambdalogic.util.rcp.SelectionMode;
import com.lambdalogic.xml.XMLContainer;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.report.dialog.DefaultReportWizard;
import de.regasus.report.dialog.IReportWizard;
import de.regasus.report.dialog.IReportWizardPage;
import de.regasus.report.wizard.common.EventWizardPage;
import de.regasus.report.wizard.ui.Activator;

public class ParticipantListWizard extends DefaultReportWizard implements IReportWizard {

	private ParticipantListReportParameter participantListReportParameter;

	private EventWizardPage eventWizardPage;
	private ParticipantSelectionReportWizardPage participantWhereWizardPage;
	private ParticipantSelectWizardPage participantSelectWizardPage;

	private ParticipantSearch participantSearch;


	public ParticipantListWizard() {
		super();
	}


	@Override
	public IReportParameter getReportParameter() {
		return participantListReportParameter;
	}


	@Override
	public void initialize(BaseReportVO baseReportVO, String reportName, XMLContainer xmlRequest) {
		try {
			setReportName(reportName);

			// create the required ReportParameter parameter
			participantListReportParameter = new ParticipantListReportParameter(xmlRequest);

			// propagate ReportParameter to super class
	        setReportParameter(participantListReportParameter);
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

		participantSelectWizardPage = new ParticipantSelectWizardPage(false /* no groups, with selection */);
		addPage(participantSelectWizardPage);

		addPage(new ParticipantSettingsWizardPage());
	}


	@Override
	protected void doNextPressed(IReportWizardPage currentPage) {
		super.doNextPressed(currentPage);

		if (currentPage == participantWhereWizardPage) {
			// copy ParticipantSearch to next page
			participantSearch = participantWhereWizardPage.getParticipantSearch();
			participantSelectWizardPage.setAbstractSearch(participantSearch);
		}
	}

}

