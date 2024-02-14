package de.regasus.participant.dialog;

import java.util.List;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;

import com.lambdalogic.messeinfo.participant.ParticipantState;
import com.lambdalogic.messeinfo.participant.data.EventVO;
import com.lambdalogic.messeinfo.profile.ProfileRelationTypeRole;
import com.lambdalogic.util.Triple;

import de.regasus.I18N;
import de.regasus.event.ParticipantType;
import de.regasus.event.dialog.EventWizardPage;

public class CreateParticipantsFromProfilesWizard extends Wizard {

	private EventWizardPage eventWizardPage;
	private ParticipantTypeAndStateWizardPage participantTypeAndStateWizardPage;

	private EventVO eventVO;
	private ParticipantState participantState;
	private ParticipantType participantType;

	/**
	 * 	List of Triples that contain
     *  a: Long of a ProfileRelationType
     *  b: Role in the ProfileRelation
     *  c: ParticipantType of the created Participant
	 */
	private List<Triple<Long, ProfileRelationTypeRole, Long>> profileRelations;


	// **************************************************************************
	// * Constructors
	// *

	public CreateParticipantsFromProfilesWizard() {
	}


	@Override
	public void addPages() {
		try {
			eventWizardPage = new EventWizardPage();
			eventWizardPage.setDescription(I18N.EventPage_SelectEventInWhichToCreateParticipants);
			addPage(eventWizardPage);

			participantTypeAndStateWizardPage = new ParticipantTypeAndStateWizardPage(true, true, true);
			addPage(participantTypeAndStateWizardPage);

			// propagate changes in EventWizardPage to ParticipantTypeAndStatePage
			eventWizardPage.addModifyListener( new ModifyListener() {
				@Override
				public void modifyText(ModifyEvent e) {
					EventVO eventVO = eventWizardPage.getEvent();
					participantTypeAndStateWizardPage.setEvent(eventVO);
				}
			} );
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}


	@Override
	public boolean performFinish() {
		eventVO = eventWizardPage.getEvent();
		participantState = participantTypeAndStateWizardPage.getSelectedParticipantState();
		participantType = participantTypeAndStateWizardPage.getSelectedParticipantType();
		profileRelations = participantTypeAndStateWizardPage.getProfileRelations();

		return true;
	}


	@Override
	public String getWindowTitle() {
		return I18N.CreateParticipantsFromProfiles;
	}


	@Override
	public boolean canFinish() {
		IWizardPage currentPage = getContainer().getCurrentPage();
		if (currentPage == participantTypeAndStateWizardPage) {
			return participantTypeAndStateWizardPage.isPageComplete();
		}
		return false;
	}


	public EventVO getEventVO() {
	     return eventVO;
	}


	public ParticipantState getParticipantState() {
		return participantState;
	}


	public ParticipantType getParticipantType() {
		return participantType;
	}


	public List<Triple<Long, ProfileRelationTypeRole, Long>> getProfileRelations() {
		return profileRelations;
	}

}
