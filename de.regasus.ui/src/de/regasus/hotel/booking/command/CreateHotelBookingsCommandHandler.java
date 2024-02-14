package de.regasus.hotel.booking.command;

import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;

import com.lambdalogic.messeinfo.participant.Participant;
import com.lambdalogic.messeinfo.participant.data.IParticipant;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.hotel.booking.dialog.CreateHotelBookingDialog;
import de.regasus.participant.ParticipantSelectionHelper;
import de.regasus.participant.editor.ParticipantEditor;
import de.regasus.ui.Activator;

/**
 * See https://mi2.lambdalogic.de/jira/browse/MIRCP-104
 *
 * @author manfred
 *
 */
public class CreateHotelBookingsCommandHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			// Determine the Participants
			List<IParticipant> participantList = ParticipantSelectionHelper.getParticipants(event);

			// Determine the eventPK
			if (participantList != null && !participantList.isEmpty()) {
				// try to save the editors
				List<Long> participantPKs = Participant.getIParticipantPKs(participantList);
				boolean editorSaveCkeckOK = ParticipantEditor.saveEditor(participantPKs);
				if (!editorSaveCkeckOK) {
					return null;
				}


				Shell shell = HandlerUtil.getActiveWorkbenchWindow(event).getShell();
				CreateHotelBookingDialog.create(shell, participantList).open();
			}
		}
		catch (Throwable e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}

		return null;
	}

}
