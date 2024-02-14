package de.regasus.participant.badge.command;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import com.lambdalogic.messeinfo.participant.data.IParticipant;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.participant.ParticipantSelectionHelper;
import de.regasus.participant.badge.BadgePrintController;
import de.regasus.participant.badge.BadgePrintData;
import de.regasus.ui.Activator;

public class PrintBadgeCommandHandler extends AbstractHandler {


	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			// Determine the Participants
			final List<IParticipant> participantList = ParticipantSelectionHelper.getParticipants(event);

			if (participantList != null && !participantList.isEmpty()) {
				List<BadgePrintData> badgePrintDataList = new ArrayList<>( participantList.size() );

				for (IParticipant iParticipant : participantList) {
					BadgePrintData badgePrintData = new BadgePrintData();
					badgePrintData.participantPK = iParticipant.getPK();
					badgePrintData.name = iParticipant.getName();
					badgePrintData.number = iParticipant.getNumber();

					badgePrintDataList.add(badgePrintData);
				}


				new BadgePrintController().createBadgeWithDocument(badgePrintDataList);
			}
		}
		catch (Throwable e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}

		return null;
	}

}
