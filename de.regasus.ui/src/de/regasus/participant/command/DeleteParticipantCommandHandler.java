package de.regasus.participant.command;

import static com.lambdalogic.util.CollectionsHelper.*;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;

import com.lambdalogic.messeinfo.participant.Participant;
import com.lambdalogic.messeinfo.participant.ParticipantMessage;
import com.lambdalogic.messeinfo.participant.data.IParticipant;
import com.lambdalogic.messeinfo.participant.data.IParticipantDeleteComparator;
import com.lambdalogic.util.exception.ErrorMessageException;
import com.lambdalogic.util.rcp.chunk.ChunkExecutor;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.I18N;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.participant.ParticipantModel;
import de.regasus.participant.ParticipantSelectionHelper;
import de.regasus.participant.editor.ParticipantEditor;
import de.regasus.ui.Activator;


public class DeleteParticipantCommandHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			Shell shell = HandlerUtil.getActiveShell(event);

			// determine selected Participants
			final List<IParticipant> participantList = ParticipantSelectionHelper.getParticipants(event);

			if (notEmpty(participantList)) {

				// extract IDs of accompanied participants and assure that their editors are not dirty
				Set<Long> accompaniedParticipantIDs = createHashSet(participantList.size());
				for (IParticipant iParticipant : participantList) {
					if (iParticipant.isCompanion()) {
						accompaniedParticipantIDs.add(iParticipant.getCompanionOfPK());
					}
				}
				boolean editorSaveCkeckOK = ParticipantEditor.saveEditor(accompaniedParticipantIDs);
				if (!editorSaveCkeckOK) {
					return null;
				}


				// confirmation
				String title = null;
				String message = null;

				if (participantList.size() == 1) {
					title = I18N.DeleteOneParticipantConfirmation_Title;
					message = I18N.DeleteOneParticipantConfirmation_Message;

					// insert the name of the Participant that will be deleted
					String name = participantList.get(0).getName();
					message = message.replaceFirst("<name>", name);
				}
				else {
					title = I18N.DeleteManyParticipantsConfirmation_Title;
					message = I18N.DeleteManyParticipantsConfirmation_Message;

					// insert the number of Participant that will be deleted
					String count = String.valueOf(participantList.size());
					message = message.replaceFirst("<count>", count);
				}

				// open dialog
				boolean deleteOK = MessageDialog.openQuestion(shell, title, message);

				// If the user answered 'Yes' in the dialog
				if (deleteOK) {
					executeInChunks(shell, participantList);
				}
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}

		return null;
	}


	private void executeInChunks(
		final Shell shell,
		final List<IParticipant> iParticipantList
	) {
		final int[] counter = {0};

		final ParticipantModel paModel = ParticipantModel.getInstance();

		/* Order iParticipantList to delete at first companion, then group members and finally group managers.
		 * Otherwise it might happen that some Participant cannot be deleted, because they are referenced by others.
		 * E.g. if a group manager and its group members are in different chunks
		 */
		Collections.sort(iParticipantList, IParticipantDeleteComparator.getInstance());

		ChunkExecutor<IParticipant> chunkExecutor = new ChunkExecutor<IParticipant>() {

			private boolean force = false;

			@Override
			protected void executeChunk(List<IParticipant> chunkList) throws Exception {
				// load participant data
				List<Long> participantPKs = Participant.getIParticipantPKs(chunkList);
				List<Participant> paList = paModel.getParticipants(participantPKs);

				// delete participants
				try {
					paModel.delete(paList, force);

					// add number of Participants that have just been deleted
					counter[0] = counter[0] + chunkList.size();
				}
				catch (ErrorMessageException e) {
					if (   e.getErrorCode().equals(ParticipantMessage.CannotDeleteParticipantBecauseOfProgrammeBookings.name())
						|| e.getErrorCode().equals(ParticipantMessage.CannotDeleteParticipantBecauseOfHotelBookings.name())
					) {
						SWTHelper.syncExecDisplayThread(new Runnable() {
							@Override
							public void run() {
								force = MessageDialog.openQuestion(
									shell,
									I18N.DeleteParticipantEnforceConfirmation_Title,
									I18N.DeleteParticipantEnforceConfirmation_Message
								);
							}
						});
					}
					else {
						/* Handle error here to use de.regasus.core.error.ErrorHandler.
						 * If the error is thrown, it would be handled by com.lambdalogic.util.rcp.error.ErrorHandler in
						 * ChunkExecutor.
						 */
						RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
						cancel();
					}

					if (force) {
						paModel.delete(paList, force);

						// add number of Participants that have just been deleted
						counter[0] = counter[0] + chunkList.size();
					}
					else {
						cancel();
					}
				}

			}


			@Override
			protected Collection<IParticipant> getItems() {
				return iParticipantList;
			}
		};

		chunkExecutor.setOperationMessage(I18N.DeleteManyParticipantsConfirmation_Title);
		chunkExecutor.executeInChunks();


		// show final confirmation message, but only if more than 1 Participant has been deleted
		if (iParticipantList.size() > 1) {
			SWTHelper.syncExecDisplayThread(new Runnable() {
				@Override
				public void run() {
					try {
						// Show dialog that tells the user how many Participant have been deleted from the selected once.
						String title = I18N.DeleteManyParticipantsConfirmation_Title;

						String message = I18N.DeleteManyParticipants_FinalMessage;
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

}
