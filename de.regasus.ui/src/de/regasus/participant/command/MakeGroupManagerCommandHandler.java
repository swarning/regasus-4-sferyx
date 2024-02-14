package de.regasus.participant.command;

import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import com.lambdalogic.messeinfo.participant.data.IParticipant;
import com.lambdalogic.util.rcp.BusyCursorHelper;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.participant.ParticipantModel;
import de.regasus.participant.ParticipantSelectionHelper;
import de.regasus.ui.Activator;

public class MakeGroupManagerCommandHandler extends AbstractHandler {

	public Object execute(final ExecutionEvent event) throws ExecutionException {
		try {
			// Determine the Participants
			final List<IParticipant> participantList = ParticipantSelectionHelper.getParticipants(event);
			
			if (participantList != null && !participantList.isEmpty()) {
				BusyCursorHelper.busyCursorWhile(new Runnable() {
					public void run() {
						try {
							 SWTHelper.syncExecDisplayThread(new Runnable() {
								public void run() {
									try {
										ParticipantModel.getInstance().makeGroupManager(participantList);
									}
									catch (Exception e) {
										RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
									}
								}
							});
						}
						catch (Exception e) {
							RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
						}

					}
				});
			}

		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
		
		return null;
	}
	
}
