package de.regasus.users.group.command;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.handlers.HandlerUtil;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.users.group.editor.UserGroupEditor;
import de.regasus.users.group.editor.UserGroupEditorInput;
import de.regasus.users.ui.Activator;

public class CreateUserGroupCommandHandler extends AbstractHandler implements IHandler {

	public Object execute(ExecutionEvent event) throws ExecutionException {
		UserGroupEditorInput editorInput = UserGroupEditorInput.getCreateInstance();
		try {
			IWorkbenchPage page = HandlerUtil.getActiveWorkbenchWindow(event).getActivePage();
			page.openEditor(editorInput, UserGroupEditor.ID);
		}
		catch (PartInitException e) {
			RegasusErrorHandler.handleApplicationError(Activator.PLUGIN_ID, EditUserGroupCommandHandler.class.getName(), e);
		}
		return null;
	}

}
