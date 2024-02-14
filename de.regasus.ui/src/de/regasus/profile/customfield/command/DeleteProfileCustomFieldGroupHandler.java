package de.regasus.profile.customfield.command;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;

import com.lambdalogic.messeinfo.profile.ProfileCustomField;
import com.lambdalogic.messeinfo.profile.ProfileCustomFieldGroup;
import com.lambdalogic.util.rcp.BusyCursorHelper;

import de.regasus.I18N;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.profile.ProfileCustomFieldGroupModel;
import de.regasus.profile.ProfileCustomFieldModel;
import de.regasus.profile.customfield.view.ProfileCustomFieldGroupTreeNode;
import de.regasus.ui.Activator;

public class DeleteProfileCustomFieldGroupHandler extends AbstractHandler {

	public Object execute(ExecutionEvent event) throws ExecutionException {

		// Iterate through whatever is currently selected
		IStructuredSelection currentSelection = (IStructuredSelection) HandlerUtil.getCurrentSelection(event);

		if (!currentSelection.isEmpty()) {
			final List<ProfileCustomFieldGroup> groupsToDelete = new ArrayList<ProfileCustomFieldGroup>();
			Iterator<?> iterator = currentSelection.iterator();
			while (iterator.hasNext()) {
				Object object = iterator.next();

				if (object instanceof ProfileCustomFieldGroupTreeNode) {
					ProfileCustomFieldGroupTreeNode node = (ProfileCustomFieldGroupTreeNode) object;
					groupsToDelete.add(node.getValue());
				}
			}
			
			boolean deleteOK = false;
			String title = null;
			String msg = null;
			
			/* Open confirmation dialog ask the user if the custom field groups shall be deleted.
			 */
			if (groupsToDelete.size() == 1) {
				title = I18N.DeleteProfileCustomFieldGroupConfirmation_Title;
				msg = I18N.DeleteProfileCustomFieldGroupConfirmation_Message;
				ProfileCustomFieldGroup group = groupsToDelete.get(0);
				msg = msg.replaceFirst("<name>", group.getName().getString()); 
			}
			else if (!groupsToDelete.isEmpty()) {
				title = I18N.DeleteProfileCustomFieldGroupListConfirmation_Title;
				msg = I18N.DeleteProfileCustomFieldGroupListConfirmation_Message;
			}
			if (msg != null) {
				deleteOK = MessageDialog.openQuestion(HandlerUtil.getActiveShell(event), title, msg);
			}

			
			if (deleteOK) {
				try {
					boolean exist = false;
					if (!groupsToDelete.isEmpty()) {
						// get PKs of all CustomFields of all selected CustomFieldGroups
						ProfileCustomFieldModel pcfModel = ProfileCustomFieldModel.getInstance();
						List<Long> customFieldPKs = new ArrayList<Long>();
						for (ProfileCustomFieldGroup customFieldGroup : groupsToDelete) {
							List<ProfileCustomField> customFields = pcfModel.getProfileCustomFieldsByGroup(
								customFieldGroup.getID()
							);
							for (ProfileCustomField customField : customFields) {
								customFieldPKs.add(customField.getID());
							}
						}
						
						
						// check if there are existing custom field values
						if (!customFieldPKs.isEmpty()) {
							exist = pcfModel.existProfileCustomFieldValue(customFieldPKs);
						}
					}
					
					if (exist) {
						/* Open confirmation dialog to inform the user about existing custom field values 
						 * and ask if the custom fields shall be delete anyway.
						 */
						title = I18N.DeleteProfileCustomFieldWithValuesConfirmation_Title;
						msg = I18N.DeleteProfileCustomFieldWithValuesConfirmation_Message;
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
						for (ProfileCustomFieldGroup group : groupsToDelete) {
							try {
								ProfileCustomFieldGroupModel.getInstance().delete(group);
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
