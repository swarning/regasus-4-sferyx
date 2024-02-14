package de.regasus.anonymize.dialog;

import java.util.List;

import com.lambdalogic.messeinfo.kernel.sql.SQLParameter;
import com.lambdalogic.util.rcp.SelectionMode;

import de.regasus.I18N;
import de.regasus.participant.dialog.ParticipantSelectionWizardPage;
import de.regasus.profile.dialog.ProfileSelectionWizardPage;



public class AnonymizeParticipantSelectionWizardPage extends ParticipantSelectionWizardPage {

	public AnonymizeParticipantSelectionWizardPage() {
		super(SelectionMode.MULTI_OPTIONAL_SELECTION, null /*eventPK*/);

		setDescription(I18N.AnonymizeWizard_ParticipantSelectionWizardPage_Description);
	}


	@Override
	public void setVisible(boolean visible) {
		if (visible) {
			// copy SQLParameters from ProfileSelectionWizardPage
			ProfileSelectionWizardPage profileSelectionWizardPage =
				(ProfileSelectionWizardPage) getWizard().getPage(ProfileSelectionWizardPage.NAME);

			List<SQLParameter> sqlParameters = profileSelectionWizardPage.getSQLParameters();
			setInitialSQLParameters(sqlParameters);


			doSearch();


			// select Participants that have been identified by SelectedProfilesAndParticipants1WizardPage
			AnonymizeSelectedProfilesAndParticipants1WizardPage selectedProfilesAndParticipants1WizardPage =
				(AnonymizeSelectedProfilesAndParticipants1WizardPage) getWizard().
					getPage(AnonymizeSelectedProfilesAndParticipants1WizardPage.NAME);

			List<Long> participantPKs = selectedProfilesAndParticipants1WizardPage.getParticipantPKs();
			participantSearchComposite.setSelection(participantPKs);
		}

		super.setVisible(visible);
	}

}
