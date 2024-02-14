package de.regasus.profile.role.view;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import com.lambdalogic.messeinfo.profile.ProfileRole;
import com.lambdalogic.util.exception.ErrorMessageException;
import com.lambdalogic.util.rcp.BusyCursorHelper;

import de.regasus.I18N;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.Activator;
import de.regasus.core.ui.IImageKeys;
import de.regasus.core.ui.action.AbstractAction;
import de.regasus.profile.ProfileRoleModel;
import de.regasus.profile.role.editor.ProfileRoleEditor;

public class DeleteProfileRoleAction extends AbstractAction implements ISelectionListener {

	public static final String ID = "de.regasus.profile.action.DeleteProfileRoleAction"; 
	
	private final IWorkbenchWindow window;
	private List<ProfileRole> selectedProfileRoles = new ArrayList<ProfileRole>();
	
	
	public DeleteProfileRoleAction(IWorkbenchWindow window) {
		/* disableWhenLoggedOut is not needed, because this Action enabled/disables depending on the 
		 * current selection. When the user is not logged in, no data can be selected. So the Action
		 * is disabled then.
		 */
		super(false /*disableWhenLoggedOut*/);
		
		this.window = window;
		
		setId(ID);
		setText(I18N.DeleteProfileRoleAction_Text);
		setToolTipText(I18N.DeleteProfileRoleAction_ToolTip);
		setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(
			Activator.PLUGIN_ID, 
			IImageKeys.DELETE
		));
	
		window.getSelectionService().addSelectionListener(this);
	}


	@Override
	public void dispose() {
		window.getSelectionService().removeSelectionListener(this);
	}
	
	
	@Override
	public void run() {
		if (!selectedProfileRoles.isEmpty()) {
			boolean deleteOK = false;
			if (selectedProfileRoles.size() == 1) {
				final String title = I18N.DeleteProfileRoleConfirmation_Title;
				String message = I18N.DeleteProfileRoleConfirmation_Message;
				
				final ProfileRole profileRole = selectedProfileRoles.get(0);
				final String name = profileRole.getName();
				message = message.replaceFirst("<name>", name); 
				
				deleteOK = MessageDialog.openQuestion(window.getShell(), title, message);
			}
			else {
				final String title = I18N.DeleteProfileRoleListConfirmation_Title;
				String message = I18N.DeleteProfileRoleListConfirmation_Message;
				
				deleteOK = MessageDialog.openQuestion(window.getShell(), title, message);
			}
			
			if (deleteOK) {
				BusyCursorHelper.busyCursorWhile(new Runnable() {
					@Override
					public void run() {
						final List<Long> profileRoleIDs = 
							ProfileRole.getPrimaryKeyList(selectedProfileRoles);
						
						try {
							List<ProfileRole> copies = new ArrayList<ProfileRole>(selectedProfileRoles);
							for (ProfileRole profileRole : copies) {
								ProfileRoleModel.getInstance().delete(profileRole);
							}
						}
						catch (ErrorMessageException e) {
							RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
							return;
						}
						catch (Throwable t) {
							String msg = I18N.DeleteProfileRoleErrorMessage;
							RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t, msg);
							return;
						}
						
						ProfileRoleEditor.closeEditors(profileRoleIDs);
					}
				});
			}
		}
	}


	@Override
	@SuppressWarnings("rawtypes")
	public void selectionChanged(IWorkbenchPart part, ISelection incoming) {
		selectedProfileRoles.clear();
		if (incoming instanceof IStructuredSelection) {
			IStructuredSelection selection = (IStructuredSelection) incoming;
			
			for (Iterator it = selection.iterator(); it.hasNext();) {
				Object selectedObject = it.next();
				if (selectedObject instanceof ProfileRole) {
					ProfileRole profileRole = (ProfileRole) selectedObject;
					selectedProfileRoles.add(profileRole);
				}
				else {
					selectedProfileRoles.clear();
					break;
				}
			}
		}
		setEnabled(!selectedProfileRoles.isEmpty());
	}

}
