package de.regasus.common.document.command;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.PlatformUI;

import de.regasus.common.document.editor.GlobalPrivacyPolicyEditor;
import de.regasus.common.document.editor.GlobalPrivacyPolicyEditorInput;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.ui.Activator;


public class GlobalPrivacyPolicyCommandHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow()
				.getActivePage()
				.openEditor(new GlobalPrivacyPolicyEditorInput(), GlobalPrivacyPolicyEditor.ID);
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}

		return null;
	}

}
