package de.regasus.event.customfield.command;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;

import com.lambdalogic.messeinfo.participant.ParticipantCustomField;
import com.lambdalogic.messeinfo.participant.ParticipantCustomFieldGroup;
import com.lambdalogic.util.rcp.BusyCursorHelper;

import de.regasus.I18N;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.event.view.ParticipantCustomFieldGroupTreeNode;
import de.regasus.participant.ParticipantCustomFieldGroupModel;
import de.regasus.participant.ParticipantCustomFieldModel;
import de.regasus.ui.Activator;

public class DeleteParticipantCustomFieldGroupHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		// Iterate through whatever is currently selected
		IStructuredSelection currentSelection = (IStructuredSelection) HandlerUtil.getCurrentSelection(event);

		if (!currentSelection.isEmpty()) {
			final List<ParticipantCustomFieldGroup> groupsToDelete = new ArrayList<>();
			Iterator<?> iterator = currentSelection.iterator();
			while (iterator.hasNext()) {
				Object object = iterator.next();

				if (object instanceof ParticipantCustomFieldGroupTreeNode) {
					ParticipantCustomFieldGroupTreeNode node = (ParticipantCustomFieldGroupTreeNode) object;
					groupsToDelete.add(node.getValue());
				}
			}

			boolean deleteOK = false;
			String title = null;
			String msg = null;

			/* Open confirmation dialog ask the user if the custom field groups shall be deleted.
			 */
			if (groupsToDelete.size() == 1) {
				title = I18N.DeleteParticipantCustomFieldGroupConfirmation_Title;
				msg = I18N.DeleteParticipantCustomFieldGroupConfirmation_Message;
				ParticipantCustomFieldGroup group = groupsToDelete.get(0);
				msg = msg.replaceFirst("<name>", group.getName().getString()); 
			}
			else if (!groupsToDelete.isEmpty()) {
				title = I18N.DeleteParticipantCustomFieldGroupListConfirmation_Title;
				msg = I18N.DeleteParticipantCustomFieldGroupListConfirmation_Message;
			}
			if (msg != null) {
				deleteOK = MessageDialog.openQuestion(HandlerUtil.getActiveShell(event), title, msg);
			}


			if (deleteOK) {
				try {
					boolean exist = false;
					if (!groupsToDelete.isEmpty()) {
						// get PKs of all CustomFields of all selected CustomFieldGroups
						ParticipantCustomFieldModel pcfModel = ParticipantCustomFieldModel.getInstance();
						List<Long> customFieldPKs = new ArrayList<>();
						for (ParticipantCustomFieldGroup customFieldGroup : groupsToDelete) {
							List<ParticipantCustomField> customFields = pcfModel.getParticipantCustomFieldsByGroup(
								customFieldGroup.getEventPK(),
								customFieldGroup.getID()
							);
							for (ParticipantCustomField customField : customFields) {
								customFieldPKs.add(customField.getID());
							}
						}


						// check if there are existing custom field values
						if (!customFieldPKs.isEmpty()) {
							exist = pcfModel.existParticipantCustomFieldValue(customFieldPKs);
						}
					}

					if (exist) {
						/* Open confirmation dialog to inform the user about existing custom field values
						 * and ask if the custom fields shall be delete anyway.
						 */
						title = I18N.DeleteParticipantCustomFieldWithValuesConfirmation_Title;
						msg = I18N.DeleteParticipantCustomFieldWithValuesConfirmation_Message;
						deleteOK = MessageDialog.openQuestion(HandlerUtil.getActiveShell(event), title, msg);
					}
				}
				catch (Exception e) {
					RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
					deleteOK = false;
				}
			}


			// if the user answered 'Yes' in the dialog
			if (deleteOK) {
				BusyCursorHelper.busyCursorWhile(new Runnable() {

					@Override
					public void run() {
						for (ParticipantCustomFieldGroup group : groupsToDelete) {
							try {
								ParticipantCustomFieldGroupModel.getInstance().delete(group);
							}
							catch (Exception e) {
								RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
							}
						}
					}

				});
			}
		}
		return null;
	}

}
