package de.regasus.anonymize.dialog;

import java.lang.invoke.MethodHandles;
import java.util.Collections;

import de.regasus.I18N;
import de.regasus.profile.dialog.ProfileSelectionWizardPage;

public class AnonymizeSelectedProfilesAndParticipants1WizardPage extends AbstractSelectedProfilesAndParticipantsWizardPage {

	public static final String NAME = MethodHandles.lookup().lookupClass().getName();

	public AnonymizeSelectedProfilesAndParticipants1WizardPage() {
		super(NAME, false /*selectable*/);

		setTitle(I18N.AnonymizeSelectedProfilesAndParticipants1WizardPage_Title);
		setDescription(I18N.AnonymizeSelectedProfilesAndParticipants1WizardPage_Description);
	}


	@Override
	public void setVisible(boolean visible) {
		if (visible) {
			ProfileSelectionWizardPage profileSelectionWizardPage = (ProfileSelectionWizardPage) getWizard().getPage(ProfileSelectionWizardPage.NAME);
			profilePKs = profileSelectionWizardPage.getSelectedPKs();

			participantPKs = Collections.emptyList();

			super.init(profilePKs, participantPKs);
		}

		super.setVisible(visible);
	}

}
