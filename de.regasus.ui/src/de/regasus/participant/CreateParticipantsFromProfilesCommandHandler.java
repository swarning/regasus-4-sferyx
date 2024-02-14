package de.regasus.participant;

import static com.lambdalogic.util.CollectionsHelper.notEmpty;

import java.util.Collection;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;

import com.lambdalogic.messeinfo.participant.ParticipantState;
import com.lambdalogic.messeinfo.participant.data.EventVO;
import com.lambdalogic.messeinfo.profile.Profile;
import com.lambdalogic.messeinfo.profile.ProfileRelationTypeRole;
import com.lambdalogic.util.Triple;
import com.lambdalogic.util.rcp.chunk.ChunkExecutor;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.I18N;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.event.ParticipantType;
import de.regasus.participant.ParticipantModel;
import de.regasus.participant.dialog.CreateParticipantsFromProfilesWizard;
import de.regasus.profile.ProfileSelectionHelper;
import de.regasus.ui.Activator;

public class CreateParticipantsFromProfilesCommandHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			Shell shell = HandlerUtil.getActiveShell(event);

			// determine selected Profiles
			List<Profile> profileList = ProfileSelectionHelper.getProfiles(event);

			if (notEmpty(profileList)) {
				List<Long> profileIDs = Profile.getIDs(profileList);

				CreateParticipantsFromProfilesWizard wizard = new CreateParticipantsFromProfilesWizard();

				WizardDialog wizardDialog = new WizardDialog(shell, wizard);
    			wizardDialog.create();

    			// set size of Dialog
    			wizardDialog.getShell().setSize(700, 600);

    			int returnCode = wizardDialog.open();
    			if (returnCode == WizardDialog.OK) {
    				EventVO eventVO = wizard.getEventVO();
    				ParticipantState participantState = wizard.getParticipantState();
    				ParticipantType participantType = wizard.getParticipantType();
    				List<Triple<Long, ProfileRelationTypeRole, Long>> profileRelations = wizard.getProfileRelations();

	    			executeInChunks(
	    				shell,
    					profileIDs,
    					eventVO,
    					participantState,
    					participantType,
    					profileRelations
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
		final List<Long> profileIDs,
		final EventVO eventVO,
		final ParticipantState participantState,
		final ParticipantType participantType,
		final List<Triple<Long, ProfileRelationTypeRole, Long>> profileRelations
	) {
		final int[] counter = {0};

		ChunkExecutor<Long> chunkExecutor = new ChunkExecutor<Long>() {
			@Override
			protected void executeChunk(List<Long> chunkList) throws Exception {

				ParticipantModel.getInstance().createByProfiles(
					chunkList,
					eventVO.getID(),
					participantState.getID(),
					participantType.getId(),
					profileRelations
				);

				// add number of Participants that have just been changed notification time
				counter[0] = counter[0] + chunkList.size();
			}


			@Override
			protected Collection<Long> getItems() {
				return profileIDs;
			}
		};

		// set operation message
		String operationMessage = I18N.CreateParticipantsFromProfiles;
		chunkExecutor.setOperationMessage(operationMessage);

		chunkExecutor.executeInChunks();

		SWTHelper.syncExecDisplayThread(new Runnable() {
			@Override
			public void run() {
				try {
					// Show dialog that tells the user how many Participant have been changed certificate Print from the selected once.
					String title = I18N.CreateParticipantsFromProfiles;

					String message = I18N.CreateParticipantsFromProfiles_FinalMessage;
					message = message.replaceFirst("<count>", String.valueOf(counter[0]));
					message = message.replaceFirst("<event>", eventVO.getName().getString());

					MessageDialog.openInformation(shell, title, message);
				}
				catch (Exception e) {
					RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
				}
			}
		});
	}

}
