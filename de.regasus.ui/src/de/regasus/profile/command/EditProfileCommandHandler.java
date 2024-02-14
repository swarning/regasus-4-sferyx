package de.regasus.profile.command;

import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.handlers.HandlerUtil;

import com.lambdalogic.messeinfo.profile.Profile;
import com.lambdalogic.util.CollectionsHelper;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.profile.ProfileSelectionHelper;
import de.regasus.profile.editor.ProfileEditor;
import de.regasus.profile.editor.ProfileEditorInput;
import de.regasus.ui.Activator;

public class EditProfileCommandHandler extends AbstractHandler{

	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			// Determine the Profiles
			List<Profile> profileList = ProfileSelectionHelper.getProfiles(event);

			if (CollectionsHelper.notEmpty(profileList)) {
				for (Profile profile : profileList) {
					Long profileID = profile.getID();
					
					ProfileEditorInput editorInput = new ProfileEditorInput(profileID);
					
					IWorkbenchPage page = HandlerUtil.getActiveWorkbenchWindow(event).getActivePage();
					page.openEditor(editorInput, ProfileEditor.ID);
				}
				return null;
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}			
		return null;
	}

	
	public static void openProfileEditor(IWorkbenchPage page, Long profileID) {
		ProfileEditorInput editorInput = new ProfileEditorInput(profileID);
		try {
			page.openEditor(editorInput, ProfileEditor.ID);
		}
		catch (PartInitException e) {
			RegasusErrorHandler.handleApplicationError(Activator.PLUGIN_ID, EditProfileCommandHandler.class.getName(), e);
		}
	}

}
