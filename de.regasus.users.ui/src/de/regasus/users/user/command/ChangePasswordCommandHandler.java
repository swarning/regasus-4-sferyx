package de.regasus.users.user.command;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.handlers.HandlerUtil;

import com.lambdalogic.messeinfo.account.data.UserAccountVO;
import com.lambdalogic.util.rcp.SelectionHelper;

import de.regasus.core.ServerModel;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.dialog.ChangePasswordDialog;
import de.regasus.users.ui.Activator;

public class ChangePasswordCommandHandler extends AbstractHandler {

	public Object execute(ExecutionEvent event) throws ExecutionException {
		ISelection selection = HandlerUtil.getCurrentSelection(event);

		UserAccountVO userAccountVO = SelectionHelper.getUniqueSelected(selection);
		
		String userID = userAccountVO.getUserID();

		String text = de.regasus.core.ui.CoreI18N.ChangePasswordAction_Text;
		ChangePasswordDialog changePasswordDialog = new ChangePasswordDialog(HandlerUtil.getActiveShell(event), text, "", false);
		int result = changePasswordDialog.open();
		
		if (result == Window.OK) {
			try {
				// Do the change
				ServerModel.getInstance().changePassword(userID, null, changePasswordDialog.getNewPassword());
			}
			catch (Exception e) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			}
		}
		return null;
	}

}
