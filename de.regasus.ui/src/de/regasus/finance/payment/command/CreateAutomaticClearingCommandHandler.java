package de.regasus.finance.payment.command;

import java.util.Collection;
import java.util.List;

import static com.lambdalogic.util.CollectionsHelper.notEmpty;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;

import com.lambdalogic.messeinfo.participant.data.IParticipant;
import com.lambdalogic.util.rcp.chunk.ChunkExecutor;

import de.regasus.I18N;
import de.regasus.core.error.RegasusErrorHandler;
import com.lambdalogic.util.rcp.UtilI18N;
import de.regasus.finance.AccountancyModel;
import de.regasus.participant.ParticipantSelectionHelper;
import de.regasus.ui.Activator;

public class CreateAutomaticClearingCommandHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
    		// Determine the Participants
    		List<IParticipant> participantList = ParticipantSelectionHelper.getParticipants(event);
    		if (notEmpty(participantList)) {
    			String message = I18N.CreateAutomaticClearingQuestion;
    			message = message.replaceFirst("<count>", String.valueOf(participantList.size()));

        		boolean confirmed = MessageDialog.openQuestion(
        			HandlerUtil.getActiveShell(event),
        			UtilI18N.Confirm,
        			message
        		);

        		if (confirmed) {
    				executeInChunks(
    					HandlerUtil.getActiveShell(event),
    					participantList
    				);
        		}
    		}
		}
		catch (Throwable e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}

		return null;
	}


	private void executeInChunks (
		final Shell shell,
		final List<IParticipant> participantList
	) {
		final int[] counter = {0};

		ChunkExecutor<IParticipant> chunkExecutor = new ChunkExecutor<IParticipant>() {
			@Override
			protected void executeChunk(List<IParticipant> chunkList) throws Exception {
				// Each list contains only one element, but for the sake of form we still iterate over this list.
				for (IParticipant participant : chunkList) {
					AccountancyModel.getInstance().createAutomaticClearings( participant.getPK() );
				}

				// add number of Participants that have just been processed
				counter[0] = counter[0] + chunkList.size();
			}


			@Override
			protected Collection<IParticipant> getItems() {
				return participantList;
			}
		};

		chunkExecutor.setChunkSize(1);

		// set operation message
		String operationMessage = I18N.CreateAutomaticClearingOperationMessage;
		operationMessage = operationMessage.replaceFirst("<count>", String.valueOf(participantList.size()));
		chunkExecutor.setOperationMessage(operationMessage);

		chunkExecutor.executeInChunks();
	}

}
