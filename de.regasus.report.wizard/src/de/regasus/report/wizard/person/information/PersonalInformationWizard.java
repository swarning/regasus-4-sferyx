package de.regasus.report.wizard.person.information;

import org.eclipse.jface.wizard.IWizardPage;

import com.lambdalogic.messeinfo.report.data.BaseReportVO;
import com.lambdalogic.report.parameter.IReportParameter;
import com.lambdalogic.util.rcp.SelectionMode;
import com.lambdalogic.xml.XMLContainer;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.report.dialog.DefaultReportWizard;
import de.regasus.report.dialog.IReportWizard;
import de.regasus.report.personalInformation.PersonalInformationReportParameter;
import de.regasus.report.wizard.participant.list.ParticipantSelectionReportWizardPage;
import de.regasus.report.wizard.profile.list.ProfileSelectionReportWizardPage;
import de.regasus.report.wizard.ui.Activator;


public class PersonalInformationWizard extends DefaultReportWizard implements IReportWizard {
	private PersonalInformationReportParameter personalInformationReportParameter;

	private ProfileSelectionReportWizardPage profileSelectionPage;
	private SelectedProfilesAndParticipants1ReportWizardPage selectedProfilesAndParticipants1Page;
	private ParticipantSelectionReportWizardPage participantSelectionPage;
	private SelectedProfilesAndParticipants2ReportWizardPage selectedProfilesAndParticipants2Page;


	public PersonalInformationWizard() {
		super();
	}


	@Override
	public void initialize(BaseReportVO baseReportVO, String reportName, XMLContainer xmlRequest) {
		try {
			setReportName(reportName);

			// create ReportParameter instance of the specific type
			personalInformationReportParameter = new PersonalInformationReportParameter(xmlRequest);

	        // propagate ReportParameter to the super class
	        setReportParameter(personalInformationReportParameter);
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	@Override
	protected void doAddPages() {
		profileSelectionPage = new ProfileSelectionReportWizardPage(SelectionMode.MULTI_OPTIONAL_SELECTION);
		addPage(profileSelectionPage);

		selectedProfilesAndParticipants1Page = new SelectedProfilesAndParticipants1ReportWizardPage();
		addPage(selectedProfilesAndParticipants1Page);

		participantSelectionPage = new ParticipantSelectionReportWizardPage(SelectionMode.MULTI_OPTIONAL_SELECTION);
		addPage(participantSelectionPage);

		selectedProfilesAndParticipants2Page = new SelectedProfilesAndParticipants2ReportWizardPage();
		addPage(selectedProfilesAndParticipants2Page);
	}


	@Override
	public IReportParameter getReportParameter() {
		return personalInformationReportParameter;
	}


	@Override
	public boolean canFinish() {
		IWizardPage currentWizardPage = getCurrentWizardPage();
		return
			currentWizardPage != profileSelectionPage &&
			currentWizardPage != selectedProfilesAndParticipants1Page &&
			currentWizardPage != participantSelectionPage;
	}

}
