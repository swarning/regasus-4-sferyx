package com.lambdalogic.util.rcp;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.PlatformUI;

import com.lambdalogic.util.rcp.error.ErrorHandler;

public class EditorHelper {

	public static void openEditor(IEditorInput editorInput, String editorId) {
		try {
			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().openEditor(editorInput, editorId);
		}
		catch (Exception e) {
			ErrorHandler.handleApplicationError(Activator.PLUGIN_ID, EditorHelper.class.getName(), e);
		}
	}

}
