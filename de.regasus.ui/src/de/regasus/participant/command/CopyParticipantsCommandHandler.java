package de.regasus.participant.command;

import static com.lambdalogic.util.CollectionsHelper.*;

import java.util.Collection;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

import com.lambdalogic.messeinfo.participant.data.EventVO;
import com.lambdalogic.messeinfo.participant.data.IParticipant;
import com.lambdalogic.util.exception.ErrorMessageException;
import com.lambdalogic.util.rcp.chunk.ChunkExecutor;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.I18N;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.event.EventModel;
import de.regasus.participant.ParticipantModel;
import de.regasus.participant.ParticipantSelectionHelper;
import de.regasus.participant.dialog.CopyParticipantsWizard;
import de.regasus.participant.editor.ParticipantEditor;
import de.regasus.participant.editor.ParticipantEditorInput;
import de.regasus.ui.Activator;

/**
 * Handler for "Teilnehmer zu anderer Veranstaltung kopieren...", which can be executed for a selection of Participants.
 */
public class CopyParticipantsCommandHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			Shell shell = HandlerUtil.getActiveShell(event);

			// determine selected Participants
			List<IParticipant> participantList = ParticipantSelectionHelper.getParticipants(event);
			if (notEmpty(participantList)) {
				CopyParticipantsWizard wizard = null;

				// Prepare the data needed for the wizard
				Long sourceEventPK = null;
				for (IParticipant participant : participantList) {
					Long tmpEventPK = participant.getEventId();
					if (sourceEventPK == null) {
						sourceEventPK = tmpEventPK;
					}
					else if (!sourceEventPK.equals(tmpEventPK)) {
						throw new ErrorMessageException(I18N.SelectedParticipantsDontBelongToSameEvent);
					}
				}

				wizard = new CopyParticipantsWizard(sourceEventPK, participantList);

				WizardDialog wizardDialog = new WizardDialog(shell, wizard);
				wizardDialog.create();

				// set size of Dialog
				wizardDialog.getShell().setSize(700, 600);

				int returnCode = wizardDialog.open();
				if (returnCode == WizardDialog.OK) {
					// copy values from wizard
	    			Long targetEventPK = wizard.getTargetEventPK();
	    			Long participantStatePK =  wizard.getParticipantStatePK();
	    			Long participantTypePK = wizard.getParticipantTypePK();

	    			boolean link = wizard.isLink();
					boolean copyCustomFieldValues = wizard.isCopyCustomFieldValues();

	    			executeInChunks(
						shell,
						participantList,
						targetEventPK,
						participantStatePK,
						participantTypePK,
						link,
						copyCustomFieldValues
	    			);
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
		final List<IParticipant> participants,
		final Long targetEventPK,
		final Long participantStatePK,
		final Long participantTypePK,
		final boolean link,
		final boolean copyCustomFieldValues
	) {

		final int[] counter = {0};
		final Long[] firstCopyParticipantPK = new Long[1];


		// temporary List of Participant PKs that is used several times
		final List<Long> participantPKs = createArrayList(ChunkExecutor.DEFAULT_CHUNK_SIZE);

		ChunkExecutor<IParticipant> chunkExecutor = new ChunkExecutor<IParticipant>() {
			@Override
			protected void executeChunk(List<IParticipant> chunkList) throws Exception {

				// build List of PKs of current chunk (that are IParticipant)
				participantPKs.clear();
				for (IParticipant iParticipant : chunkList) {
					Long participantPK = iParticipant.getPK();
					participantPKs.add(participantPK);
				}

				List<Long> copyPKs = ParticipantModel.getInstance().copy(
					participantPKs,
					targetEventPK,
					participantStatePK,
					participantTypePK,
					link,
					copyCustomFieldValues
				);

				// add number of Participants that have just been copied in current chunk
				counter[0] = counter[0] + copyPKs.size();

				// safe Long of the first created/copied Participant
				if (firstCopyParticipantPK[0] == null) {
					firstCopyParticipantPK[0] = copyPKs.get(0);
				}
			}

			@Override
			protected Collection<IParticipant> getItems() {
				return participants;
			}
	    };

	    // set operation message
 		String operationMessage = I18N.CopyParticipantsToOtherEventWizard_finalDialogTitle;
 		chunkExecutor.setOperationMessage(operationMessage);

 		chunkExecutor.executeInChunks();


 		// open dialog with user feedback
		SWTHelper.syncExecDisplayThread(new Runnable() {
			@Override
			public void run() {
				try {
					// get data or target Event
					EventVO targetEventVO = EventModel.getInstance().getEventVO(targetEventPK);

					// open final dialog
					String title = I18N.CopyParticipantsToOtherEventWizard_finalDialogTitle;
					String eventLabel = targetEventVO.getLabel().getString();
					if (counter[0] == 1) {
						// Only one Participant has been copied --> Ask user if its editor shall be opened.
						String message = I18N.CopyParticipantsToOtherEventWizard_finalDialogMessageOneParticipant;
						message = message.replaceFirst("<eventLabel>", eventLabel);

						boolean yes = MessageDialog.openQuestion(shell, title, message);

						if (yes) {
							// open ParticipantEditor with created Participant
							try {
								Long participantPK = firstCopyParticipantPK[0];
								ParticipantEditorInput editorInput = ParticipantEditorInput.getEditInstance(participantPK);
								IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
								page.openEditor(editorInput, ParticipantEditor.ID);
							}
							catch (PartInitException e) {
								RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
							}
						}
					}
					else if (counter[0] > 1) {
						// Many Participants have been copied: Show dialog that tells the user howmany Participant have been copied.
						String message = I18N.CopyParticipantsToOtherEventWizard_finalDialogMessageManyParticipants;
						message = message.replaceFirst("<count>", String.valueOf(counter[0]));
						message = message.replaceFirst("<eventLabel>", eventLabel);

						MessageDialog.openInformation(shell, title, message);
					}
			     }
			     catch (Throwable t) {
			    	 RegasusErrorHandler.handleApplicationError(Activator.PLUGIN_ID, getClass().getName(), t);
			     }
			}
		});

	}

}
