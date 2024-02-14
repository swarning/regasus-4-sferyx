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
import com.lambdalogic.util.rcp.BusyCursorHelper;

import de.regasus.I18N;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.event.view.ParticipantCustomFieldTreeNode;
import de.regasus.participant.ParticipantCustomFieldModel;
import de.regasus.ui.Activator;

public class DeleteParticipantCustomFieldHandler extends AbstractHandler {

	public Object execute(ExecutionEvent event) throws ExecutionException {

		// Iterate through whatever is currently selected
		IStructuredSelection currentSelection = (IStructuredSelection) HandlerUtil.getCurrentSelection(event);

		if (!currentSelection.isEmpty()) {
			final List<ParticipantCustomField> customFieldsToDelete = new ArrayList<ParticipantCustomField>();
			Iterator<?> iterator = currentSelection.iterator();
			while (iterator.hasNext()) {
				Object object = iterator.next();

				if (object instanceof ParticipantCustomFieldTreeNode) {
					ParticipantCustomFieldTreeNode node = (ParticipantCustomFieldTreeNode) object;
					customFieldsToDelete.add(node.getValue());
				}
			}
			
			boolean deleteOK = false;
			String title = null;
			String msg = null;
			
			/* Open confirmation dialog ask the user if the custom fields shall be deleted.
			 */
			if (customFieldsToDelete.size() == 1) {
				title = I18N.DeleteParticipantCustomFieldConfirmation_Title;
				msg = I18N.DeleteParticipantCustomFieldConfirmation_Message;
				ParticipantCustomField customField = customFieldsToDelete.get(0);
				String name = customField.getName();
				msg = msg.replaceFirst("<name>", name); 
			}
			else if (!customFieldsToDelete.isEmpty()) {
				title = I18N.DeleteParticipantCustomFieldListConfirmation_Title;
				msg = I18N.DeleteParticipantCustomFieldListConfirmation_Message;
			}
			if (msg != null) {
				deleteOK = MessageDialog.openQuestion(HandlerUtil.getActiveShell(event), title, msg);
			}

			
			if (deleteOK) {
				try {
					// check if there are existing custom field values
					boolean exist = false;
					if (!customFieldsToDelete.isEmpty()) {
						List<Long> customFieldPKs = ParticipantCustomField.getPrimaryKeyList(customFieldsToDelete);
						ParticipantCustomFieldModel pcfModel = ParticipantCustomFieldModel.getInstance();
						exist = pcfModel.existParticipantCustomFieldValue(customFieldPKs);
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
				
					public void run() {
						for (ParticipantCustomField customField : customFieldsToDelete) {
							try {
								ParticipantCustomFieldModel pcfModel = ParticipantCustomFieldModel.getInstance();
								pcfModel.delete(customField);
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
