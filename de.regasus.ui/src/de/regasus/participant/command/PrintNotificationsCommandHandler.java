package de.regasus.participant.command;

import java.util.List;

import org.eclipse.jface.wizard.Wizard;

import com.lambdalogic.messeinfo.participant.data.IParticipant;

import de.regasus.participant.dialog.PrintNotificationsWizard;

public class PrintNotificationsCommandHandler extends AbstractIParticipantSelectionWithWizardHandler {

	@Override
	protected Wizard createWizard(List<IParticipant> participantList) {
		return new PrintNotificationsWizard(participantList);
	}

}
