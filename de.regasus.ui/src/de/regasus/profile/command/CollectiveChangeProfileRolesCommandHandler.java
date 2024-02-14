package de.regasus.profile.command;

import static com.lambdalogic.util.CollectionsHelper.*;

import java.util.Collection;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.ui.handlers.HandlerUtil;

import com.lambdalogic.messeinfo.profile.Profile;
import com.lambdalogic.util.rcp.chunk.ChunkExecutor;

import de.regasus.I18N;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.participant.collectivechange.dialog.CollectiveChangeOfProfileRolesDialog;
import de.regasus.profile.ProfileModel;
import de.regasus.profile.ProfileSelectionHelper;
import de.regasus.profile.editor.ProfileEditor;
import de.regasus.ui.Activator;


public class CollectiveChangeProfileRolesCommandHandler extends AbstractHandler {
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			// determine selected Profiles
			List<Profile> profileList = ProfileSelectionHelper.getProfiles(event);

			if (notEmpty(profileList)) {
				List<Long> profileIDs = Profile.getIDs(profileList);
			
				// Check if any of the selected Profiles has an editor with unsaved data.
				boolean allEditorSaved = ProfileEditor.saveEditor(profileIDs);
				if (allEditorSaved) {					
					// open Dialog
	    			CollectiveChangeOfProfileRolesDialog dialog = new CollectiveChangeOfProfileRolesDialog(
	    				HandlerUtil.getActiveShell(event),
	    				profileIDs.size()
	    			);
	    			dialog.create();;
	    			
	    			// set size of Dialog
	    			dialog.getShell().setSize(550, 500);
	    			
	    			int returnCode = dialog.open();
	    			if (returnCode == TitleAreaDialog.OK) {	    		
	    				List<Long> profileRoleIDs = dialog.getSelectedProfileRoleIDs();
	    				boolean addMode = dialog.isAddMode();
	    				boolean setMode = dialog.isSetMode();
	    				boolean removeMode = dialog.isRemoveMode();
		    			
		    			executeInChunks(
	    					profileIDs, 
	    					profileRoleIDs, 
	    					addMode,
	    					removeMode, 
	    					setMode
		    			);		    			
	    			}
				}
			}
			else {
				// This should not happen !
				System.err.println("Empty profile list encountered in " + getClass().getName());
			}
		}
		catch (Throwable e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}

		return null;
	}

	
	private void executeInChunks (
		final List<Long> profilePKs,
		final List<Long> profileRoleIDs, 
		final boolean addProfileRoles,
		final boolean removeProfileRoles,
		final boolean setProfileRoles
	) {

		final ProfileModel profileModel = ProfileModel.getInstance();
		
		ChunkExecutor<Long> chunkExecutor = new ChunkExecutor<Long>() {
			@Override
			protected void executeChunk(List<Long> chunkList) throws Exception {
				if (addProfileRoles) {
					profileModel.addProfileRoles(profileRoleIDs, chunkList);
				}
				else if (removeProfileRoles) {
					profileModel.removeProfileRoles(profileRoleIDs, chunkList);
				}
				else if (setProfileRoles) {
					profileModel.setProfileRoles(profileRoleIDs, chunkList);
				}
			}

			
			@Override
			protected Collection<Long> getItems() {
				return profilePKs;
			}
		};
		
		// set operation message
		String operationMessage = I18N.CollectiveChangeOfProfileRolesDialog_title;
		operationMessage = operationMessage.replaceFirst("<count>", String.valueOf(profilePKs.size()));
		chunkExecutor.setOperationMessage(operationMessage);
		
		chunkExecutor.executeInChunks();
	}

}
