package de.regasus.participant.dialog;

import org.eclipse.jface.wizard.Wizard;

import com.lambdalogic.messeinfo.profile.Profile;
import com.lambdalogic.util.exception.ErrorMessageException;
import com.lambdalogic.util.rcp.SelectionMode;

import de.regasus.I18N;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.person.PersonLinkModel;
import de.regasus.profile.dialog.ProfileSelectionWizardPage;
import de.regasus.ui.Activator;


/**
 * Wizard to assign a Profile to a given Participant.
 * The Wizard contains a page to select a Profile.
 */
public class AssignParticipantToProfileWizard extends Wizard {

	private ProfileSelectionWizardPage profileSelectionPage;

	private ForceWizardPage forcePage;

	private String lastName;

	private String firstName;

	private Long participantPK;


	public AssignParticipantToProfileWizard(String lastName, String firstName, Long participantPK) {
		this.lastName = lastName;
		this.firstName = firstName;
		this.participantPK = participantPK;
	}



	@Override
	public void addPages() {
		try {
			profileSelectionPage = new ProfileSelectionWizardPage(SelectionMode.SINGLE_SELECTION);

			// set initial name values
			profileSelectionPage.setInitialLastName(lastName);
			profileSelectionPage.setInitialFirstName(firstName);

			addPage(profileSelectionPage);

			forcePage = new ForceWizardPage();
			addPage(forcePage);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}


	@Override
	public boolean performFinish() {
		boolean force = forcePage.isForce();

		try {
			Profile profile = profileSelectionPage.getSelectedProfiles().get(0);
			if (profile != null) {
				PersonLinkModel.getInstance().link(profile.getID(), participantPK, force);
			}
		}
		catch (ErrorMessageException e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			return false;
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			return false;
		}

		return true;
	}


	@Override
	public String getWindowTitle() {
		return I18N.ParticipantAssignProfile_Text;
	}


	@Override
	public boolean canFinish() {
		return getContainer().getCurrentPage() == forcePage;
	}

}
