package de.regasus.event.customfield.command;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.event.customfield.editor.ParticipantCustomFieldEditor;
import de.regasus.event.customfield.editor.ParticipantCustomFieldEditorInput;
import de.regasus.event.view.EventTreeNode;
import de.regasus.event.view.ParticipantCustomFieldGroupTreeNode;
import de.regasus.event.view.ParticipantCustomFieldListTreeNode;
import de.regasus.event.view.ParticipantCustomFieldTreeNode;
import de.regasus.ui.Activator;

public class CreateParticipantCustomFieldHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IStructuredSelection currentSelection = (IStructuredSelection) HandlerUtil.getCurrentSelection(event);
		Object object = currentSelection.getFirstElement();

		// Determine out eventPK and groupPK from seletcted object
		Long eventPK = null;
		Long groupPK = null;

		if (object instanceof EventTreeNode) {
			eventPK = ((EventTreeNode) object).getEventId();
		}
		else if (object instanceof ParticipantCustomFieldListTreeNode) {
			eventPK = ((ParticipantCustomFieldListTreeNode) object).getEventId();
		}
		else if (object instanceof ParticipantCustomFieldGroupTreeNode) {
			ParticipantCustomFieldGroupTreeNode groupTreeNode = (ParticipantCustomFieldGroupTreeNode) object;
			eventPK = groupTreeNode.getEventId();
			groupPK = groupTreeNode.getParticipantCustomFieldGroupID();
		}
		else if (object instanceof ParticipantCustomFieldTreeNode) {
			ParticipantCustomFieldTreeNode customFieldTreeNode = (ParticipantCustomFieldTreeNode) object;
			eventPK = customFieldTreeNode.getEventId();
			groupPK = customFieldTreeNode.getGroupPK();
		}


		if (eventPK != null) {
			// Open editor for new ProgrammePointVO
			ParticipantCustomFieldEditorInput input = ParticipantCustomFieldEditorInput.getCreateInstance(eventPK, groupPK);
			try {
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().openEditor(
					input,
					ParticipantCustomFieldEditor.ID
				);
			}
			catch (PartInitException e) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			}
		}

		return null;
	}

}
