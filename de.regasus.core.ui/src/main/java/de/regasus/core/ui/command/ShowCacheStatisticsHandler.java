package de.regasus.core.ui.command;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.handlers.HandlerUtil;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.Activator;
import de.regasus.core.ui.editor.cache.FileCacheStatisticsEditor;
import de.regasus.core.ui.editor.cache.FileCacheStatisticsEditorInput;
import de.regasus.core.ui.editor.cache.PortalCacheStatisticsEditor;
import de.regasus.core.ui.editor.cache.PortalCacheStatisticsEditorInput;

public class ShowCacheStatisticsHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);

			openFileCacheStatisticsEditor(window);
			openPortalCacheStatisticsEditor(window);
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}

		return null;
	}


	private void openFileCacheStatisticsEditor(IWorkbenchWindow window) throws PartInitException {
		FileCacheStatisticsEditorInput editorInput = new FileCacheStatisticsEditorInput();
		window.getActivePage().openEditor(editorInput, FileCacheStatisticsEditor.ID);
	}


	private void openPortalCacheStatisticsEditor(IWorkbenchWindow window) throws PartInitException {
		PortalCacheStatisticsEditorInput editorInput = new PortalCacheStatisticsEditorInput();
		window.getActivePage().openEditor(editorInput, PortalCacheStatisticsEditor.ID);
	}

}
