package de.regasus.profile.command;

import static com.lambdalogic.util.CollectionsHelper.*;

import java.util.Collection;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;

import com.lambdalogic.messeinfo.contact.CustomFieldUpdateParameter;
import com.lambdalogic.messeinfo.profile.Profile;
import com.lambdalogic.util.rcp.chunk.ChunkExecutor;

import de.regasus.I18N;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.profile.ProfileModel;
import de.regasus.profile.ProfileSelectionHelper;
import de.regasus.profile.customfield.dialog.CollectiveChangeProfileCustomFieldsWizard;
import de.regasus.profile.editor.ProfileEditor;
import de.regasus.ui.Activator;


public class CollectiveChangeProfileCustomFieldsCommandHandler extends AbstractHandler {

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
					// open wizard
					CollectiveChangeProfileCustomFieldsWizard wizard = new CollectiveChangeProfileCustomFieldsWizard(profileIDs);
					Shell shell = HandlerUtil.getActiveShell(event);
					
	    			WizardDialog dialog = new WizardDialog(shell, wizard);
	    			dialog.create();
	    			
	    			// set size of Dialog
	    			dialog.getShell().setSize(800, 600);
	    			
	    			int returnCode = dialog.open();
	    			if (returnCode == WizardDialog.OK) {
		    			List<CustomFieldUpdateParameter> parameters = wizard.getParameters();
		    			executeInChunks(profileIDs, parameters);
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

	
	private void executeInChunks(
			final List<Long> profilePKs,
		final List<CustomFieldUpdateParameter> parameters 
	) {
		ChunkExecutor<Long> chunkExecutor = new ChunkExecutor<Long>() {
			@Override
			protected void executeChunk(List<Long> chunkList) throws Exception {
				ProfileModel.getInstance().updateProfileCustomFields(parameters, chunkList);
			}

			
			@Override
			protected Collection<Long> getItems() {
				return profilePKs;
			}
		};
		
		// set operation message
		String operationMessage = I18N.CollectiveChangeProfileCustomFields;
		operationMessage = operationMessage.replaceFirst("<count>", String.valueOf(profilePKs.size()));
		chunkExecutor.setOperationMessage(operationMessage);

		chunkExecutor.executeInChunks();
	}
	
}
