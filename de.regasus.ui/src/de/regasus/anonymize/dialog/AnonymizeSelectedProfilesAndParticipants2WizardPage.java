package de.regasus.anonymize.dialog;

import java.lang.invoke.MethodHandles;

import org.eclipse.swt.widgets.Table;

import de.regasus.I18N;
import de.regasus.profile.dialog.ProfileSelectionWizardPage;

public class AnonymizeSelectedProfilesAndParticipants2WizardPage extends AbstractSelectedProfilesAndParticipantsWizardPage {

	public static final String NAME = MethodHandles.lookup().lookupClass().getName();

	public AnonymizeSelectedProfilesAndParticipants2WizardPage() {
		super(NAME, true /*selectable*/);

		setTitle(I18N.AnonymizeSelectedProfilesAndParticipants2WizardPage_Title);
		setDescription(I18N.AnonymizeSelectedProfilesAndParticipants2WizardPage_Description);
	}


	@Override
	protected ProfileParticipantTable createProfileParticipantTable(Table table) {
		return new AnonymizeProfileParticipantTable(table);
	}


	@Override
	public void setVisible(boolean visible) {
		if (visible) {
			ProfileSelectionWizardPage profileSelectionWizardPage =
				(ProfileSelectionWizardPage) getWizard().getPage(ProfileSelectionWizardPage.NAME);

			profilePKs = ((ProfileSelectionWizardPage) getWizard().getPage(ProfileSelectionWizardPage.NAME)).getSelectedPKs();

			profilePKs = profileSelectionWizardPage.getSelectedPKs();


			AnonymizeParticipantSelectionWizardPage participantSelectionWizardPage =
				(AnonymizeParticipantSelectionWizardPage) getWizard().
					getPage(AnonymizeParticipantSelectionWizardPage.NAME);

			participantPKs = participantSelectionWizardPage.getSelectedPKs();


			super.init(profilePKs, participantPKs);

			// set CheckBoxes active
			((AnonymizeProfileParticipantTable) profileParticipantTable).checkAll();
		}

		super.setVisible(visible);
	}


	@Override
	public boolean isPageComplete() {
		return !getCheckedProfilePKs().isEmpty() || !getCheckedParticipantPKs().isEmpty();
	}

}
