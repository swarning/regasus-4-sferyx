package de.regasus.profile.relation;

import java.util.Collection;
import java.util.Objects;

import org.eclipse.jface.wizard.Wizard;

import com.lambdalogic.messeinfo.profile.Profile;
import com.lambdalogic.messeinfo.profile.ProfileRelationType;
import com.lambdalogic.util.rcp.SelectionMode;

import de.regasus.I18N;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.profile.ProfileRelationModel;
import de.regasus.profile.ProfileRelationTypeModel;
import de.regasus.profile.dialog.ProfileSelectionWizardPage;
import de.regasus.profile.relationtype.ProfileRelationTypeSelectionPage;
import de.regasus.ui.Activator;

public class CreateProfileRelationWizard extends Wizard {

	private ProfileSelectionWizardPage profileSelectionWizardPage;

	private ProfileRelationTypeSelectionPage profileRelationTypeSelectionPage;

	private Profile profile1;

	private Long profileRelationTypeID;

	private String profileName2;


	public CreateProfileRelationWizard(Profile profile1) {
		Objects.requireNonNull(profile1);

		this.profile1 = profile1;
	}


	@Override
	public String getWindowTitle() {
		return I18N.CreateProfileRelation_Title;
	}


	@Override
	public void addPages() {
		profileSelectionWizardPage = new ProfileSelectionWizardPage(SelectionMode.SINGLE_SELECTION);
		addPage(profileSelectionWizardPage);

		try {
			// check if there is exactly one ProfileRelationType
			Collection<ProfileRelationType> profileRelationTypes = ProfileRelationTypeModel.getInstance().getAllProfileRelationTypes();
			if (profileRelationTypes.size() == 1) {
				ProfileRelationType profileRelationType = profileRelationTypes.iterator().next();
				if (!profileRelationType.isDirected()) {
					profileRelationTypeID = profileRelationType.getID();
				}
			}

			// In case the above 1 relation is directed, we need the selection page anyhow
			if (profileRelationTypeID == null) {
				profileRelationTypeSelectionPage = new ProfileRelationTypeSelectionPage(profile1.getName());
				addPage(profileRelationTypeSelectionPage);
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}

	}


	@Override
	public boolean performFinish() {
		boolean result = false;

		try {
			// because of SelectionMode.SINGLE_SELECTION there is always one Profile selected
			Profile profile2 = profileSelectionWizardPage.getSelectedProfiles().get(0);
			if (profile2 != null) {
				if (profileRelationTypeSelectionPage != null &&
					!profileRelationTypeSelectionPage.isReverseRelation()
				) {
					ProfileRelationModel.getInstance().createProfileRelation(
						profile1.getID(),				// profile1ID
						profile2.getID(), 				// profile2ID
						getProfileRelationTypeID()		// profileRelationTypeID
					);
				}
				else {
					ProfileRelationModel.getInstance().createProfileRelation(
						profile2.getID(), 				// profile2ID
						profile1.getID(),				// profile1ID
						getProfileRelationTypeID()		// profileRelationTypeID
					);
				}
			}
			result = true;
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}

		return result;
	}


	@Override
	public boolean canFinish() {
		return 	profileSelectionWizardPage != null &&
				profileSelectionWizardPage.isPageComplete() &&
				getProfileRelationTypeID() != null;
	}


	public Long getProfileId() {
		return profile1.getID();
	}


	public String getProfile2() {
		return profileName2;
	}


	public void setProfile2(String profile2) {
		this.profileName2 = profile2;
	}


	public Long getProfileRelationTypeID() {
		if (profileRelationTypeSelectionPage != null) {
			profileRelationTypeID = profileRelationTypeSelectionPage.getProfileRelationTypeID();
			System.out.println("Using profileRelationTypeID "+profileRelationTypeID+" from profileRelationTypeSelectionPage");
		}
		else {
			System.out.println("Using profileRelationTypeID "+profileRelationTypeID+" which already was initialized because there seemed to be only one (undirected)");
		}
		return profileRelationTypeID;
	}

}
