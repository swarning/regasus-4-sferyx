package de.regasus.users.group.command;

import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.handlers.HandlerUtil;

import com.lambdalogic.messeinfo.account.data.UserGroupVO;
import com.lambdalogic.messeinfo.kernel.data.AbstractVO;
import com.lambdalogic.util.rcp.SelectionHelper;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.users.UserGroupModel;
import de.regasus.users.ui.Activator;

public class RefreshUserGroupCommandHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			ISelection selection = HandlerUtil.getCurrentSelection(event);
			List<UserGroupVO> userAccountList = SelectionHelper.toList(selection);
			List<String> groupIDs = AbstractVO.getPKs(userAccountList);
			UserGroupModel.getInstance().refresh(groupIDs);
		}
		catch (Throwable e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
		return null;
	}
}
