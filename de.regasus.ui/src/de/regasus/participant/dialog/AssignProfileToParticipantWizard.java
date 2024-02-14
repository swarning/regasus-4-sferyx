package de.regasus.participant.dialog;

import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;

import com.lambdalogic.messeinfo.participant.data.ParticipantSearchData;
import com.lambdalogic.util.exception.ErrorMessageException;
import com.lambdalogic.util.rcp.SelectionMode;

import de.regasus.I18N;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.event.dialog.EventWizardPage;
import de.regasus.person.PersonLinkModel;
import de.regasus.ui.Activator;


/**
 * Wizard to assign a Participant to a given Profile.
 * The Wizard contains pages to select an Event and a Participant.
 */
public class AssignProfileToParticipantWizard extends Wizard {

	private EventWizardPage eventWizardPage;
	private ParticipantSelectionWizardPage participantSelectionWizardPage;
	private ForceWizardPage forceWizardPage;

	private String lastName;

	private String firstName;

	private Long profileId;


	public AssignProfileToParticipantWizard(String lastName, String firstName, Long profileId) {
		this.lastName = lastName;
		this.firstName = firstName;
		this.profileId = profileId;
	}


	@Override
	public void addPages() {
		try {
			eventWizardPage = new EventWizardPage();
			eventWizardPage.setDescription(I18N.EventPage_SelectEventForParticipantToBeAssigned);
			addPage(eventWizardPage);

			participantSelectionWizardPage = new ParticipantSelectionWizardPage(
				SelectionMode.SINGLE_SELECTION,
				null // eventPK
			);

			// set initial name values
			participantSelectionWizardPage.setInitialLastName(lastName);
			participantSelectionWizardPage.setInitialFirstName(firstName);

			// propagate changes in EventWizardPage to ParticipantTypeAndStatePage
			eventWizardPage.addModifyListener( new ModifyListener() {
				@Override
				public void modifyText(ModifyEvent e) {
					Long eventId = eventWizardPage.getEventId();
					participantSelectionWizardPage.setEventPK(eventId);
				}
			} );

			addPage(participantSelectionWizardPage);

			forceWizardPage = new ForceWizardPage();
			addPage(forceWizardPage);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}


	@Override
	public boolean performFinish() {
		boolean force = forceWizardPage.isForce();

		try {
			ParticipantSearchData participant = participantSelectionWizardPage.getSelectedParticipants().get(0);
			if (participant != null) {
				PersonLinkModel.getInstance().link(profileId, participant.getPK(), force);
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
		return I18N.ProfileAssignParticipantAction_Text;
	}


	@Override
	public boolean canFinish() {
		return getContainer().getCurrentPage() == forceWizardPage;
	}

}
