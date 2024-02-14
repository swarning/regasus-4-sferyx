package de.regasus.core.ui.command;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.Activator;
import de.regasus.core.ui.CoreI18N;
import de.regasus.core.ui.editor.IRefreshableEditorPart;

/**
 * This handler can get called when a refreshableEditor is active, in which case he checks whether the data in the
 * editor already stems from a backing store, asks the user if they want to save the data first (if the editor is dirty)
 * and, if the user doesn't cancel, asks the editor to actually perform the refresh.
 * 
 * This handler is called by the command "org.eclipse.ui.file.refresh", which is put with it's standard icon in the
 * toolbar by the plugin.xml file.
 * 
 * @author manfred
 * 
 */
public class RefreshEditorHandler extends AbstractHandler {

	public Object execute(ExecutionEvent event) throws ExecutionException {

		final IRefreshableEditorPart refreshable = (IRefreshableEditorPart) HandlerUtil.getActiveEditorChecked(event);
		
		// Nothing to refresh when the editor doesn't hold an existing entity from the server
		if (!refreshable.isNew()) {

			if (refreshable.isDirty()) {
				/*
				 * Ask whether to discard the changes in the dirty editor
				 */
				Shell shell = HandlerUtil.getActiveShellChecked(event);
				boolean confirmed = MessageDialog.openConfirm(
					shell, 
					CoreI18N.ConfirmRefresh_Title, 
					CoreI18N.ConfirmRefresh_Message
				);
				
				if (!confirmed) {
					return null; // Nothing to do in this case, so just leave the method here 
				}
			}

			/*
			 * Go on refreshing, since the editor is not dirty, or the user has confirmed to discard their changes
			 */
			try {
				refreshable.refresh();
			}
			catch (Exception e) {
				RegasusErrorHandler.handleApplicationError(Activator.PLUGIN_ID, getClass().getName(), e);
			}

		}

		return null;
	}

}
