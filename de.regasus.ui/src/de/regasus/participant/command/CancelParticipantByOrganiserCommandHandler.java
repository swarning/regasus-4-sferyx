package de.regasus.participant.command;

import static com.lambdalogic.util.CollectionsHelper.*;

import java.util.Collection;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;

import com.lambdalogic.messeinfo.participant.data.IParticipant;
import com.lambdalogic.util.rcp.chunk.ChunkExecutor;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.I18N;
import de.regasus.core.error.RegasusErrorHandler;
import com.lambdalogic.util.rcp.UtilI18N;
import de.regasus.participant.ParticipantModel;
import de.regasus.participant.ParticipantSelectionHelper;
import de.regasus.ui.Activator;

/**
 * A handler for the command "Cancellation-by-Organizer". 
 */
public class CancelParticipantByOrganiserCommandHandler extends AbstractHandler {

	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			Shell shell = HandlerUtil.getActiveShellChecked(event);
			
			// determine selected Participants
			List<IParticipant> participantList = ParticipantSelectionHelper.getParticipants(event);
			
			if (notEmpty(participantList)) {
				String question = null;
				if (participantList.size() == 1) {
					String name = participantList.get(0).getName();
					question = I18N.CancelOneParticipantByOrganiser_Question.replaceFirst("<name>", name);
				}
				else {
					String count = String.valueOf(participantList.size());
					question = I18N.CancelManyParticipantsByOrganiser_Question.replaceFirst("<count>", count);
				}
				
				MessageDialogWithToggle dialogWithToggle = MessageDialogWithToggle.openOkCancelConfirm(
					shell,
					UtilI18N.Question,
					question,
					I18N.CancelParticipant_SubordinateToggle,
					false,
					null,
					null
				);


				if (Window.OK == dialogWithToggle.getReturnCode()) {
					boolean withSubParticipants = dialogWithToggle.getToggleState();
					
					executeInChunks(
						shell,
						participantList, 
						withSubParticipants								
					);
				}
			}

		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
		
		return null;
	}
	
	
	private void executeInChunks (
		final Shell shell,
		final List<IParticipant> participantList,
		final boolean withSubParticipants		
	) {
		final int[] counter = {0};
		
		ChunkExecutor<IParticipant> chunkExecutor = new ChunkExecutor<IParticipant>() {
			@Override
			protected void executeChunk(List<IParticipant> chunkList) throws Exception {
				ParticipantModel.getInstance().cancelParticipantsByOrganizer(
					chunkList, 
					withSubParticipants
				);
				
				// add number of Participants that have just been canceled
				counter[0] = counter[0] + chunkList.size();
			}
	
			
			@Override
			protected Collection<IParticipant> getItems() {
				return participantList;
			}
		};
		
		// set operation message
		String operationMessage = I18N.CancelParticipantsByOrganiser;
		operationMessage = operationMessage.replaceFirst("<count>", String.valueOf(participantList.size()));	
		chunkExecutor.setOperationMessage(operationMessage);
		
		// set chunkSize down to 10, because this operation takes more time
		chunkExecutor.setChunkSize(10);

		chunkExecutor.executeInChunks();
		
		SWTHelper.syncExecDisplayThread(new Runnable() {
			public void run() {
				try {
					// Show dialog that tells the user how many Participant have been canceled from the selected once.
					String title = I18N.CollectiveChange;
					String message = I18N.CancelParticipantsByOrganiser_FinalMessage;
					message = message.replaceFirst("<count>", String.valueOf(counter[0]));

					MessageDialog.openInformation(shell, title, message);
				}
				catch (Exception e) {
					RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
				}
			}
		});
	}

}
