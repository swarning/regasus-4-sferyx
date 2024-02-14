package de.regasus.users.user.command;

import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;

import com.lambdalogic.messeinfo.account.data.UserAccountVO;
import com.lambdalogic.util.rcp.BusyCursorHelper;
import com.lambdalogic.util.rcp.SelectionHelper;

import de.regasus.core.error.RegasusErrorHandler;
import com.lambdalogic.util.rcp.UtilI18N;
import de.regasus.users.UserAccountModel;
import de.regasus.users.ui.Activator;

public class DeleteUserAccountCommandHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		final IStructuredSelection currentSelection = (IStructuredSelection) HandlerUtil.getCurrentSelection(event);

		if (!currentSelection.isEmpty()) {
			boolean deleteOK = MessageDialog.openConfirm(
				HandlerUtil.getActiveShell(event),
				UtilI18N.Question,
				de.regasus.users.UsersI18N.DeleteUserAccounts_Confirmation
			);

			if (deleteOK) {
				BusyCursorHelper.busyCursorWhile(new Runnable() {

					@Override
					public void run() {

						List<UserAccountVO> userAccountVOs = SelectionHelper.toList(currentSelection);

						for(UserAccountVO userAccountVO : userAccountVOs) {
							try {
								UserAccountModel.getInstance().deleteByPK(userAccountVO.getPK());
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
