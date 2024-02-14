package de.regasus.users.group.command;

import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;

import com.lambdalogic.messeinfo.account.AccountMessage;
import com.lambdalogic.messeinfo.account.data.UserGroupVO;
import com.lambdalogic.util.exception.ErrorMessageException;
import com.lambdalogic.util.rcp.SelectionHelper;

import de.regasus.core.error.RegasusErrorHandler;
import com.lambdalogic.util.rcp.UtilI18N;
import de.regasus.users.UserGroupModel;
import de.regasus.users.UsersI18N;
import de.regasus.users.ui.Activator;
import de.regasus.users.user.editor.UserAccountEditor;

public class DeleteUserGroupCommandHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		final IStructuredSelection currentSelection = (IStructuredSelection) HandlerUtil.getCurrentSelection(event);

		if ( currentSelection.isEmpty() ) {
			return null;
		}

		if ( ! UserAccountEditor.isEditorsSaved(UserAccountEditor.class) ) {
			MessageDialog.openWarning(
				HandlerUtil.getActiveShell(event),
				UtilI18N.Warning,
				UsersI18N.DeleteUserGroups_UnsavedUserEditors
			);

			return null;
		}


		boolean deleteOK = MessageDialog.openConfirm(
			HandlerUtil.getActiveShell(event),
			UtilI18N.Question,
			UsersI18N.DeleteUserGroups_Confirmation
		);

		if (deleteOK) {
			List<UserGroupVO> userAccountVOs = SelectionHelper.toList(currentSelection);

			for (UserGroupVO userGroupVO : userAccountVOs) {
				try {
					UserGroupModel.getInstance().delete(userGroupVO, false);
				}
				catch (ErrorMessageException e) {
					if ( e.getErrorCode().equals(AccountMessage.UserGroupNotDeletedBecauseUsersAssignedToIt.name()) ) {
						// ask user if he want to delete anyway
						boolean forceDelete = MessageDialog.openQuestion(
							HandlerUtil.getActiveShell(event),
							UtilI18N.Question,
							e.getMessage() + "\n\n" + UsersI18N.DeleteUserGroupsAnywayQuestion
						);

						if (forceDelete) {
							try {
								UserGroupModel.getInstance().delete(userGroupVO, true);
							}
							catch (Exception e2) {
								RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e2);
							}
						}
					}
					else {
						RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
					}
				}
				catch (Exception e) {
					RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
				}
			}
		}

		return null;
	}
}
