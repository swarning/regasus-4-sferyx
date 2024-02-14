package de.regasus.users.user.command;

import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.handlers.HandlerUtil;

import com.lambdalogic.messeinfo.account.data.UserAccountVO;
import com.lambdalogic.util.rcp.SelectionHelper;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.users.ui.Activator;
import de.regasus.users.user.editor.UserAccountEditor;
import de.regasus.users.user.editor.UserAccountEditorInput;

public class EditUserAccountCommandHandler extends AbstractHandler {

	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			ISelection selection = HandlerUtil.getCurrentSelection(event);
			IWorkbenchPage page = HandlerUtil.getActiveWorkbenchWindow(event).getActivePage();

			List<UserAccountVO> userAccountList = SelectionHelper.toList(selection);

			for (UserAccountVO userAccountVO : userAccountList) {
				openUserAccountEditor(page, userAccountVO.getID());
			}
		}
		catch (Throwable e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
		return null;
	}


	public static void openUserAccountEditor(IWorkbenchPage page, Long userAccountPK) {
		UserAccountEditorInput editorInput = UserAccountEditorInput.getEditInstance(userAccountPK);
		try {
			page.openEditor(editorInput, UserAccountEditor.ID);
		}
		catch (PartInitException e) {
			RegasusErrorHandler.handleApplicationError(Activator.PLUGIN_ID, EditUserAccountCommandHandler.class.getName(), e);
		}
	}
}
