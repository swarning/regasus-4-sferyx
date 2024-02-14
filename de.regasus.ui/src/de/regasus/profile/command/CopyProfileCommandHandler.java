package de.regasus.profile.command;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.PlatformUI;

import com.lambdalogic.messeinfo.profile.Profile;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.profile.ProfileSelectionHelper;
import de.regasus.profile.editor.ProfileEditor;
import de.regasus.profile.editor.ProfileEditorInput;
import de.regasus.ui.Activator;

public class CopyProfileCommandHandler extends AbstractHandler {

	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			// Determine the Profile
			final Profile profile = ProfileSelectionHelper.getProfile(event);

			if (profile != null) {
				final ProfileEditorInput editorInput = new ProfileEditorInput(profile.getID(), true);
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().openEditor(editorInput, ProfileEditor.ID);
			}
		}
		catch (Throwable t) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t);
		}

		return null;
	}
}
