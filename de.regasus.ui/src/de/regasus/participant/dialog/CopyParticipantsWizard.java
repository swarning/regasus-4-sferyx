package de.regasus.participant.dialog;

import java.util.List;
import java.util.Objects;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;

import com.lambdalogic.messeinfo.participant.data.EventVO;
import com.lambdalogic.messeinfo.participant.data.IParticipant;

import de.regasus.I18N;
import de.regasus.event.dialog.EventWizardPage;

public class CopyParticipantsWizard extends Wizard {

	private EventWizardPage targetEventWizardPage;
	private ParticipantTypeAndStateWizardPage participantTypeAndStateWizardPage;
	private LinkParticipantsWizardPage linkParticipantsWizardPage;
	private CopyCustomFieldValuesOptionWizardPage copyCustomFieldValuesOptionWizardPage;

	private Long sourceEventPK;

	private List<? extends IParticipant> sourceParticipants;


	/*
	 * Values from GUI elements
	 * Values the user selected. They have to be saved outside the widgets, because they are read after the the widgets
	 * have been disposed.
	 */

	// Long of the Event the user selected as target Event
	private Long targetEventPK;

	// value the user selected if copied participants shall be linked with their originals
	private boolean linkParticipants;

	// selection of the user if custom field values shall be copied
	private boolean copyCustomFieldValues;

	// participant type the user has selected
	private Long participantTypePK;

	// participant state the user has selected
	private Long participantStatePK;



	// **************************************************************************
	// * Constructors
	// *

	/**
	 * This constructor is given the Event of the selected Participants, so that it can be excluded in the Event search
	 * page.
	 */
	public CopyParticipantsWizard(Long sourceEventPK, List<IParticipant> sourceParticipants) {
		Objects.requireNonNull(sourceEventPK);
		Objects.requireNonNull(sourceParticipants);

		this.sourceEventPK = sourceEventPK;
		this.sourceParticipants = sourceParticipants;
	}


	@Override
	public void addPages() {
		try {
			// add page to determine the target Event
			targetEventWizardPage = new EventWizardPage();
			targetEventWizardPage.setDescription(I18N.EventPage_SelectEventIntoWhichParticipantsAreToBeCopied);
			targetEventWizardPage.setInitiallySelectedEventPK(sourceEventPK);
			addPage(targetEventWizardPage);


			// add page to determine the Participant Type and State depending on the selected Event
			participantTypeAndStateWizardPage = new ParticipantTypeAndStateWizardPage(false, false, false);
			addPage(participantTypeAndStateWizardPage);

			participantTypeAndStateWizardPage.setDefaultParticipantStatePK( determineDefaultParticipantState() );
			participantTypeAndStateWizardPage.setDefaultParticipantTypePK( determineDefaultParticipantType() );

			// propagate changes in EventWizardPage to ParticipantTypeAndStatePage
			targetEventWizardPage.addModifyListener( new ModifyListener() {
				@Override
				public void modifyText(ModifyEvent e) {
					EventVO eventVO = targetEventWizardPage.getEvent();
					participantTypeAndStateWizardPage.setEvent(eventVO);
				}
			} );

			/* Don't add LinkParticipantsWizardPage if there is only one Participant selected that doesn't have a
			 * personLink, because then the two Participants cannot be linked.
			 * If multiple Participants are selected, they can be linked even if the
			 * originals don't have a personLink.
			 */
			boolean showLinkPage =
				sourceParticipants.size() > 1 ||
				sourceParticipants.get(0).getPersonLink() != null;

			if (showLinkPage) {
    			linkParticipantsWizardPage = new LinkParticipantsWizardPage();
    			addPage(linkParticipantsWizardPage);
			}

			copyCustomFieldValuesOptionWizardPage = new CopyCustomFieldValuesOptionWizardPage();
			addPage(copyCustomFieldValuesOptionWizardPage);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}


	/**
	 * Determine default value for participant state:
	 * If all participants have the same state this state is the default state.
	 */
	private Long determineDefaultParticipantState() {
		Long defaultParticipantStatePK = null;
		for (IParticipant iParticipant : sourceParticipants) {
			Long statePK = iParticipant.getStatePK();
			if (defaultParticipantStatePK == null) {
				defaultParticipantStatePK = statePK;
			}
			else if (!defaultParticipantStatePK.equals(statePK)) {
				defaultParticipantStatePK = null;
				break;
			}
		}
		return defaultParticipantStatePK;
	}


	/**
	 * Determine default value for participant type:
	 * If all participants have the same type this state is the default type.
	 */
	private Long determineDefaultParticipantType() {
		Long defaultParticipantType = null;
		for (IParticipant iParticipant : sourceParticipants) {
			Long participantTypePK = iParticipant.getParticipantTypePK();
			if (defaultParticipantType == null) {
				defaultParticipantType = participantTypePK;
			}
			else if (!defaultParticipantType.equals(participantTypePK)) {
				defaultParticipantType = null;
				break;
			}
		}
		return defaultParticipantType;
	}


	@Override
	public boolean performFinish() {
		targetEventPK = targetEventWizardPage.getEventId();
		participantStatePK = participantTypeAndStateWizardPage.getSelectedParticipantStatePK();
		participantTypePK = participantTypeAndStateWizardPage.getSelectedParticipantTypePK();
		linkParticipants = linkParticipantsWizardPage != null && linkParticipantsWizardPage.isLink().booleanValue();
		copyCustomFieldValues = copyCustomFieldValuesOptionWizardPage.isYes();

		return true;
	}


	@Override
	public String getWindowTitle() {
		return I18N.CopyParticipantsToOtherEvent_Text;
	}


	@Override
	public boolean canFinish() {
		IWizardPage currentPage = getContainer().getCurrentPage();
		return
			(
    			currentPage == participantTypeAndStateWizardPage &&
    			linkParticipantsWizardPage == null &&
    			participantTypeAndStateWizardPage.isPageComplete()
			)
			||
			(
    			currentPage == linkParticipantsWizardPage &&
    			linkParticipantsWizardPage.isPageComplete()
			)
			||
			currentPage == copyCustomFieldValuesOptionWizardPage;
	}


	public Long getTargetEventPK() throws Exception {
		return targetEventPK;
	}


	public Long getParticipantStatePK() throws Exception {
		return participantStatePK;
	}


	public Long getParticipantTypePK() throws Exception {
		return participantTypePK;
	}


	public boolean isLink() throws Exception {
		return linkParticipants;
	}


	public boolean isCopyCustomFieldValues() throws Exception {
	    return copyCustomFieldValues;
	}

}
