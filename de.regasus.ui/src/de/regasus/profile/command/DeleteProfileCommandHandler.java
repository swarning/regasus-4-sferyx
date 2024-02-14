package de.regasus.profile.command;

import java.util.Collection;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;

import com.lambdalogic.messeinfo.profile.Profile;
import com.lambdalogic.util.rcp.chunk.ChunkExecutor;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.I18N;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.profile.ProfileModel;
import de.regasus.profile.ProfileSelectionHelper;
import de.regasus.ui.Activator;

public class DeleteProfileCommandHandler extends AbstractHandler{

	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			Shell shell = HandlerUtil.getActiveShell(event);
			
			// determine selected Profiles
			final List<Profile> profileList = ProfileSelectionHelper.getProfiles(event);

			if (profileList != null && !profileList.isEmpty()) {
				// confirmation
				String title = null;
				String message = null;
				
				if (profileList.size() == 1) {
					title = I18N.DeleteOneProfileConfirmation_Title;
					message = I18N.DeleteOneProfileConfirmation_Message;

					// insert the name of the Profile that will be deleted
					String name = profileList.get(0).getName();
					message = message.replaceFirst("<name>", name); 
				}
				else {
					title = I18N.DeleteManyProfilesConfirmation_Title;
					message = I18N.DeleteManyProfilesConfirmation_Message;

					// insert the number of Profiles that will be deleted
					String count = String.valueOf(profileList.size());
					message = message.replaceFirst("<count>", count); 
				}
				
				// open dialog
				boolean deleteOK = MessageDialog.openQuestion(shell, title, message);
				
				// If the user answered 'Yes' in the dialog
				if (deleteOK) {
					executeInChunks(shell, profileList);
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
		final List<Profile> profileList
	) {
		final int[] counter = {0};
		
		ChunkExecutor<Profile> chunkExecutor = new ChunkExecutor<Profile>() {
			@Override
			protected void executeChunk(List<Profile> chunkList) throws Exception {
				ProfileModel.getInstance().delete(chunkList);
				
				// add number of Profiles that have just been deleted 
				counter[0] = counter[0] + chunkList.size();
			}

			
			@Override
			protected Collection<Profile> getItems() {
				return profileList;
			}
		};
		
		chunkExecutor.setOperationMessage(I18N.DeleteManyProfilesConfirmation_Title);
		chunkExecutor.setErrorMessage(I18N.DeleteProfileErrorMessage);
		chunkExecutor.executeInChunks();
		
		
		// show final confirmation message, but only if more than 1 Profile has been deleted
		if (profileList.size() > 1) {
			SWTHelper.syncExecDisplayThread(new Runnable() {
				public void run() {
					try {						
						// Show dialog that tells the user how many Participant have been deleted from the selected once.
						String title = I18N.DeleteManyProfilesConfirmation_Title;
						
						String message = I18N.DeleteManyProfiles_FinalMessage;
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
