package de.regasus.profile.command;

import static com.lambdalogic.util.CollectionsHelper.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;

import com.lambdalogic.messeinfo.participant.data.IParticipant;
import com.lambdalogic.messeinfo.profile.Profile;
import com.lambdalogic.util.rcp.chunk.ChunkExecutor;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.I18N;
import de.regasus.core.error.RegasusErrorHandler;
import com.lambdalogic.util.rcp.UtilI18N;
import de.regasus.participant.ParticipantSelectionHelper;
import de.regasus.profile.ProfileModel;
import de.regasus.ui.Activator;

/**
 * Handler for "Create profiles from participants...", which can be started via the "Editor" menu
 * or a context menu of a selected participant.
 * <p>
 * Does not work with a wizard, so we open a little dialog to ask whether the user wants to continue.
 */
public class CreateProfileFromParticipantCommandHandler extends AbstractHandler {

	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			Shell shell = HandlerUtil.getActiveShell(event);
			
			// determine selected Participants
			List<IParticipant> participantList = ParticipantSelectionHelper.getParticipants(event);
			
			if (notEmpty(participantList)) {
				// create question text
				String question = null;
				if (participantList.size() == 1) {
					String name = participantList.get(0).getName();
					question = I18N.CreateProfileFromParticipant_Question.replaceFirst("<name>", name);
				}
				else {
					String count = String.valueOf(participantList.size());
					question = I18N.CreateProfilesFromParticipants_Question.replaceFirst("<count>", count);
				}
				
				// ask question
				boolean answer = MessageDialog.openQuestion(
					shell, 
					UtilI18N.Question, 
					question
				);


				if (answer) {
					// get PKs of participants
					final List<Long> participantPKs = new ArrayList<Long>(participantList.size());
					for (IParticipant iParticipant : participantList) {
						participantPKs.add(iParticipant.getPK());
					}

					executeInChunks(shell, participantPKs);
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
		final List<Long> participantPKs
	) {
		final int[] counter = {0};
		
		ChunkExecutor<Long> chunkExecutor = new ChunkExecutor<Long>() {
			@Override
			protected void executeChunk(List<Long> chunkList) throws Exception {				
				
				List<Profile> createdProfileList = ProfileModel.getInstance().createByParticipants(chunkList);
				
				// add number of Participants that have just been changed notification time 
				counter[0] = counter[0] + createdProfileList.size();
			}
	
			
			@Override
			protected Collection<Long> getItems() {
				return participantPKs;
			}
		};
		
		// set operation message
		String operationMessage = I18N.CreateProfilesFromParticipants;
		chunkExecutor.setOperationMessage(operationMessage);
		
		chunkExecutor.executeInChunks();
		
		SWTHelper.syncExecDisplayThread(new Runnable() {
			public void run() {
				try {						
					// Show dialog that tells the user how many Profiles have been created.
					String title = I18N.CreateProfilesFromParticipants;
					
					int numberOfCreatedProfiles = counter[0];
					int numberOfSelectedParticipants = participantPKs.size();
					
					String message;
					if (numberOfSelectedParticipants != numberOfCreatedProfiles) {
						message = I18N.CreateProfilesFromParticipants_Confirmation_SomeAlreadyExist;
						message = message.replace("<numberOfCreatedProfiles>", String.valueOf(numberOfCreatedProfiles));
						message = message.replace("<numberOfNotCreatedProfiles>", String.valueOf(numberOfSelectedParticipants - numberOfCreatedProfiles));
					}
					else if (numberOfCreatedProfiles > 1) {
						message = I18N.CreateProfilesFromParticipants_Confirmation_AllNew;
						message = message.replace("<numberOfCreatedProfiles>", String.valueOf(numberOfCreatedProfiles));
					}
					else {
						message = I18N.CreateProfilesFromParticipants_Confirmation_OneNew;
					}
					MessageDialog.openInformation(shell, title, message);
				}
				catch (Exception e) {
					RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
				}
			}
		});			

	}

}
