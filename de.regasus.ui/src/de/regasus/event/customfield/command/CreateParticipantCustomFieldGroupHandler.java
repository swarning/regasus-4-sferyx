package de.regasus.event.customfield.command;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

import com.lambdalogic.messeinfo.participant.ParticipantCustomFieldGroupLocation;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.event.customfield.editor.ParticipantCustomFieldGroupEditor;
import de.regasus.event.customfield.editor.ParticipantCustomFieldGroupEditorInput;
import de.regasus.event.view.ParticipantCustomFieldGroupLocationTreeNode;
import de.regasus.event.view.ParticipantCustomFieldGroupTreeNode;
import de.regasus.ui.Activator;

public class CreateParticipantCustomFieldGroupHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IStructuredSelection currentSelection = (IStructuredSelection) HandlerUtil.getCurrentSelection(event);
		Object object = currentSelection.getFirstElement();

		// Find out EventPK
		Long eventPK = null;
		ParticipantCustomFieldGroupLocation location = null;

		if (object instanceof ParticipantCustomFieldGroupLocationTreeNode) {
			ParticipantCustomFieldGroupLocationTreeNode treeNode = (ParticipantCustomFieldGroupLocationTreeNode) object;
			eventPK = treeNode.getEventId();
			location = treeNode.getValue();
		}
		else if (object instanceof ParticipantCustomFieldGroupTreeNode) {
			ParticipantCustomFieldGroupTreeNode treeNode = (ParticipantCustomFieldGroupTreeNode) object;
			eventPK = treeNode.getEventId();
			location = treeNode.getValue().getLocation();
		}

		if (eventPK != null) {
			// Open editor for new ProgrammePointVO
			ParticipantCustomFieldGroupEditorInput input = ParticipantCustomFieldGroupEditorInput.getCreateInstance(
				eventPK,
				location
			);

			try {
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().openEditor(
					input,
					ParticipantCustomFieldGroupEditor.ID
				);
			}
			catch (PartInitException e) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			}
		}

		return null;
	}

}
