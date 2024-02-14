package de.regasus.report.wizard.participant.list;

import com.lambdalogic.messeinfo.participant.report.participantList.GroupedParticipantListReportParameter;
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

public class GroupedParticipantListWizard extends DefaultReportWizard implements IReportWizard {
	private GroupedParticipantListReportParameter groupedParticipantListReportParameter;

	private ParticipantSelectionReportWizardPage participantWhereWizardPage;
	private ParticipantSelectWizardPage participantSelectWizardPage;

	private ParticipantSearch participantSearch;


	public GroupedParticipantListWizard() {
		super();
	}


	@Override
	public IReportParameter getReportParameter() {
		return groupedParticipantListReportParameter;
	}


	@Override
	public void initialize(BaseReportVO baseReportVO, String reportName, XMLContainer xmlRequest) {
		try {
			setReportName(reportName);

			// ReportParameter des benötigten Typs erzeugen
			groupedParticipantListReportParameter = new GroupedParticipantListReportParameter(xmlRequest);

	        // ReportParameter der Superklasse bekannt machen
	        setReportParameter(groupedParticipantListReportParameter);
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	@Override
	protected void doAddPages() {
		addPage( new EventWizardPage() );

		participantWhereWizardPage = new ParticipantSelectionReportWizardPage(SelectionMode.NO_SELECTION);
		addPage(participantWhereWizardPage);

		participantSelectWizardPage = new ParticipantSelectWizardPage(true /* with groups, with selection */);
		addPage(participantSelectWizardPage);
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

