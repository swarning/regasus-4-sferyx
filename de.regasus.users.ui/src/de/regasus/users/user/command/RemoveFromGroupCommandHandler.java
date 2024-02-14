package de.regasus.users.user.command;

import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.handlers.HandlerUtil;

import com.lambdalogic.messeinfo.account.data.UserAccountVO;
import com.lambdalogic.util.rcp.SelectionHelper;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.users.UserAccountModel;
import de.regasus.users.UsersAdministrationHelper;
import de.regasus.users.UsersI18N;
import de.regasus.users.ui.Activator;

public class RemoveFromGroupCommandHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		try {
			ISelection selection = HandlerUtil.getCurrentSelection(event);
			List<UserAccountVO> userAccountList = SelectionHelper.toList(selection);

			String title = UsersI18N.RemoveFromGroup;
			String message = UsersI18N.SelectUserGroupForRemoval;

			String[] userGroupIDs = UsersAdministrationHelper.openGroupSelectionDialog(HandlerUtil.getActiveShell(event), title, message);

			UserAccountModel.getInstance().removeGroupFromUserAccounts(userAccountList, userGroupIDs);

		}
		catch (Throwable e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
		return null;
	}
}
