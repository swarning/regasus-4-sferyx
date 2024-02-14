package de.regasus.report.wizard.person.information;

import java.lang.invoke.MethodHandles;

import com.lambdalogic.i18n.I18NPattern;
import com.lambdalogic.messeinfo.participant.report.parameter.IParticipantPKsReportParameter;
import com.lambdalogic.messeinfo.profile.report.profileList.IProfilePKsReportParameter;
import com.lambdalogic.report.parameter.IReportParameter;

import de.regasus.anonymize.dialog.AbstractSelectedProfilesAndParticipantsWizardPage;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.report.dialog.IReportWizardPage;
import de.regasus.report.personalInformation.PersonalInformationReportParameter;
import de.regasus.report.wizard.ReportWizardI18N;
import de.regasus.report.wizard.ui.Activator;


public class SelectedProfilesAndParticipants2ReportWizardPage extends AbstractSelectedProfilesAndParticipantsWizardPage implements IReportWizardPage {

	public static final String NAME = MethodHandles.lookup().lookupClass().getName();
	private PersonalInformationReportParameter personalInfoParameter;


	public SelectedProfilesAndParticipants2ReportWizardPage() {
		super(NAME, false /*selectable*/);

		setTitle(ReportWizardI18N.SelectedProfilesAndParticipants2ReportWizardPage_Title);
		setDescription(ReportWizardI18N.SelectedProfilesAndParticipants2ReportWizardPage_Description);
	}


	@Override
	public void init(IReportParameter reportParameter) {
		try {
			personalInfoParameter = (PersonalInformationReportParameter) reportParameter;
			profilePKs = personalInfoParameter.getProfilePKs();

			// if the participantPKs in the report parameters are processed depends on processParticipantPKs
			participantPKs = personalInfoParameter.getParticipantPKs();

			super.init(profilePKs, participantPKs);

			// save immediately, because the user might generate the report directly from the wizard
			save();
		}
		catch (Throwable t) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t);
		}
	}


	private void save() {
		if (personalInfoParameter != null) {
			// save all Profile PKs in report parameters
			personalInfoParameter.setProfilePKs(profilePKs);

			I18NPattern desc = new I18NPattern();
			desc.append(ReportWizardI18N.ProfileWhereWizardPage_SelectedProfileNumber);
			desc.append(": ");
			desc.append(profilePKs.size());

			personalInfoParameter.setDescription(
				IProfilePKsReportParameter.DESCRIPTION_ID,
				desc.getString()
			);


			// save all Participant PKs in report parameters
			personalInfoParameter.setParticipantPKs(participantPKs);

			desc = new I18NPattern();
			desc.append(ReportWizardI18N.ParticipantWhereWizardPage_SelectedParticipantsNumber);
			desc.append(": ");
			desc.append(participantPKs.size());

			personalInfoParameter.setDescription(
				IParticipantPKsReportParameter.DESCRIPTION_ID,
				desc.getString()
			);
		}
	}


	@Override
	public void saveReportParameters() {
		// nothing to do, because changes have been saved already after initialization
	}

}
