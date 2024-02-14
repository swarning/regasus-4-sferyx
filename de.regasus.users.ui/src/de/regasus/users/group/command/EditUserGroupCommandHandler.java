package de.regasus.users.group.command;

import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.handlers.HandlerUtil;

import com.lambdalogic.messeinfo.account.data.UserGroupVO;
import com.lambdalogic.util.rcp.SelectionHelper;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.users.group.editor.UserGroupEditor;
import de.regasus.users.group.editor.UserGroupEditorInput;
import de.regasus.users.ui.Activator;

public class EditUserGroupCommandHandler extends AbstractHandler {

	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			ISelection selection = HandlerUtil.getCurrentSelection(event);
			IWorkbenchPage page = HandlerUtil.getActiveWorkbenchWindow(event).getActivePage();

			List<UserGroupVO> userGroupList = SelectionHelper.toList(selection);

			for (UserGroupVO userGroupVO : userGroupList) {
				openUserGroupEditor(page, userGroupVO.getGroupID());
			}
		}
		catch (Throwable e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
		return null;
	}


	public static void openUserGroupEditor(IWorkbenchPage page, String userGroupID) {
		UserGroupEditorInput editorInput = UserGroupEditorInput.getEditInstance(userGroupID);
		try {
			page.openEditor(editorInput, UserGroupEditor.ID);
		}
		catch (PartInitException e) {
			RegasusErrorHandler.handleApplicationError(Activator.PLUGIN_ID, EditUserGroupCommandHandler.class.getName(), e);
		}
	}
}
