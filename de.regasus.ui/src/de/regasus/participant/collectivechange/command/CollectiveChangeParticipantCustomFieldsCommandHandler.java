package de.regasus.participant.collectivechange.command;

import static com.lambdalogic.util.CollectionsHelper.notEmpty;

import java.util.Collection;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;

import com.lambdalogic.messeinfo.contact.CustomFieldUpdateParameter;
import com.lambdalogic.messeinfo.participant.Participant;
import com.lambdalogic.messeinfo.participant.ParticipantMessage;
import com.lambdalogic.messeinfo.participant.data.IParticipant;
import com.lambdalogic.messeinfo.participant.data.ParticipantHelper;
import com.lambdalogic.util.rcp.UtilI18N;
import com.lambdalogic.util.rcp.chunk.ChunkExecutor;

import de.regasus.I18N;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.participant.ParticipantModel;
import de.regasus.participant.ParticipantSelectionHelper;
import de.regasus.participant.collectivechange.dialog.CollectiveChangeParticipantCustomFieldsWizard;
import de.regasus.participant.editor.ParticipantEditor;
import de.regasus.ui.Activator;


public class CollectiveChangeParticipantCustomFieldsCommandHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			Shell shell = HandlerUtil.getActiveShell(event);

			// determine selected Profiles
			List<IParticipant> participantList = ParticipantSelectionHelper.getParticipants(event);

			if (notEmpty(participantList)) {
	    		// check if all Participants belong to the same Event
	    		boolean sameEvent = ParticipantHelper.isSameEvent(participantList);
	    		if (sameEvent) {
	        		Long eventPK = participantList.get(0).getEventId();
	    			List<Long> participantPKs = Participant.getIParticipantPKs(participantList);

	    			// Check if any of the selected Participants has an editor with unsaved data.
	    			boolean allEditorSaved = ParticipantEditor.saveEditor(participantPKs);
	    			if (allEditorSaved) {
						// open wizard
						CollectiveChangeParticipantCustomFieldsWizard wizard = new CollectiveChangeParticipantCustomFieldsWizard(eventPK, participantPKs);


		    			WizardDialog wizardDialog = new WizardDialog(shell, wizard);
		    			wizardDialog.create();

		    			// set size of Dialog
		    			Point size = new Point(800, 600);
		    			wizardDialog.getShell().setSize(size);

		    			int returnCode = wizardDialog.open();
		    			if (returnCode == WizardDialog.OK) {
		    				// copy values from wizard
			    			List<CustomFieldUpdateParameter> parameters = wizard.getParameters();
			    			executeInChunks(participantPKs, parameters);
		    			}
	    			}
	    		}
	    		else {
	    			// show info dialog
	    			MessageDialog.openInformation(
	    				shell,
	    				UtilI18N.Info,
	    				ParticipantMessage.ParticipantsAreNotOfTheSameEvent.getString()	// message
	    			);
	    		}
			}
			else {
				// This should not happen !
				System.err.println("Empty participant list encountered in " + getClass().getName());
			}
		}
		catch (Throwable e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}

		return null;
	}


	private void executeInChunks(
		final List<Long> participantPKs,
		final List<CustomFieldUpdateParameter> parameters
	) {
		ChunkExecutor<Long> chunkExecutor = new ChunkExecutor<Long>() {
			@Override
			protected void executeChunk(List<Long> chunkList) throws Exception {
				ParticipantModel.getInstance().updateParticipantCustomFields(parameters, chunkList);
			}


			@Override
			protected Collection<Long> getItems() {
				return participantPKs;
			}
		};

		// set operation message
		String operationMessage = I18N.CollectiveChangeParticipantCustomFields;
		operationMessage = operationMessage.replaceFirst("<count>", String.valueOf(participantPKs.size()));
		chunkExecutor.setOperationMessage(operationMessage);

		chunkExecutor.executeInChunks();
	}

}
