package de.regasus.users.user.command;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.handlers.HandlerUtil;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.users.ui.Activator;
import de.regasus.users.user.editor.UserAccountEditor;
import de.regasus.users.user.editor.UserAccountEditorInput;

public class CreateUserAccountCommandHandler extends AbstractHandler implements IHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		System.out.println("CreateUserAccountCommandHandler.execute()");
		UserAccountEditorInput editorInput = UserAccountEditorInput.getCreateInstance();
		try {
			IWorkbenchPage page = HandlerUtil.getActiveWorkbenchWindow(event).getActivePage();
			page.openEditor(editorInput, UserAccountEditor.ID);
		}
		catch (PartInitException e) {
			RegasusErrorHandler.handleApplicationError(Activator.PLUGIN_ID, CreateUserAccountCommandHandler.class.getName(), e);
		}
		return null;
	}

}
