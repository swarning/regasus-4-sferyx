package de.regasus.participant.collectivechange.command;

import static com.lambdalogic.util.CollectionsHelper.notEmpty;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;

import com.lambdalogic.messeinfo.participant.Participant;
import com.lambdalogic.messeinfo.participant.data.IParticipant;
import com.lambdalogic.util.rcp.chunk.ChunkExecutor;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.I18N;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.participant.ParticipantModel;
import de.regasus.participant.ParticipantSelectionHelper;
import de.regasus.participant.collectivechange.dialog.CollectiveChangeRegistrationDateDialog;
import de.regasus.participant.editor.ParticipantEditor;
import de.regasus.ui.Activator;

public class CollectiveChangeRegistrationDateCommandHandler extends AbstractHandler {


	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			Shell shell = HandlerUtil.getActiveShell(event);

			// determine selected Participants
			List<IParticipant> participants = ParticipantSelectionHelper.getParticipants(event);

			if (notEmpty(participants)) {

				List<Long> participantPKs = Participant.getIParticipantPKs(participants);

				// Check if any of the selected Participants has an editor with unsaved data.
				boolean allEditorSaved = ParticipantEditor.saveEditor(participantPKs);
				if (allEditorSaved) {
					// open Dialog
					CollectiveChangeRegistrationDateDialog dialog = new CollectiveChangeRegistrationDateDialog(
						shell,
						participantPKs.size()
					);
					dialog.create();

					int returnCode = dialog.open();
					if (returnCode == TitleAreaDialog.OK) {
						Date registrationDate = dialog.getRegistartionDate();


						executeInChunks(
							shell,
							participantPKs,
							registrationDate
						);
					}
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
		final List<Long> participantPKs,
		final Date registerDate
	) {
		final int[] counter = {0};

		ChunkExecutor<Long> chunkExecutor = new ChunkExecutor<Long>() {
			@Override
			protected void executeChunk(List<Long> chunkList) throws Exception {

				ParticipantModel.getInstance().updateRegisterDate(registerDate, chunkList);

				// add number of Participants that have just been changed registration date
				counter[0] = counter[0] + chunkList.size();
			}


			@Override
			protected Collection<Long> getItems() {
				return participantPKs;
			}
		};

		// set operation message
		String operationMessage = I18N.CollectiveChangeRegistrationDateDialog_Title;
		operationMessage = operationMessage.replaceFirst("<count>", String.valueOf(participantPKs.size()));
		chunkExecutor.setOperationMessage(operationMessage);

		chunkExecutor.executeInChunks();

		SWTHelper.syncExecDisplayThread(new Runnable() {
			@Override
			public void run() {
				try{
				  // Show dialog that tells the user how many Participants have been updated.
				  String title = I18N.CollectiveChange;
					String message;
					if (registerDate == null) {
						message = I18N.CollectiveChangeRegistrationDate_FinalMessageDelete;
					}
					else {
						message = I18N.CollectiveChangeRegistrationDate_FinalMessageSet;
					}
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
