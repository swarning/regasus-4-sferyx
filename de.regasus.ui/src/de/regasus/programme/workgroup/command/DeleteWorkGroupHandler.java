package de.regasus.programme.workgroup.command;

import java.util.Iterator;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;

import com.lambdalogic.messeinfo.participant.data.WorkGroupVO;
import com.lambdalogic.util.rcp.BusyCursorHelper;

import de.regasus.I18N;
import de.regasus.core.error.RegasusErrorHandler;
import com.lambdalogic.util.rcp.UtilI18N;
import de.regasus.event.view.WorkGroupTreeNode;
import de.regasus.programme.WorkGroupModel;
import de.regasus.ui.Activator;

public class DeleteWorkGroupHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		// Iterate through whatever is currently selected
		final IStructuredSelection currentSelection = (IStructuredSelection) HandlerUtil.getCurrentSelection(event);

		if (!currentSelection.isEmpty()) {
			boolean deleteOK = MessageDialog.openConfirm(
				HandlerUtil.getActiveShell(event),
				UtilI18N.Question,
				I18N.Delete_WorkGroup_ConfirmationMessage
			);

			if (deleteOK) {
				BusyCursorHelper.busyCursorWhile(new Runnable() {

					@Override
					public void run() {
						Iterator<?> iterator = currentSelection.iterator();
						while (iterator.hasNext()) {
							Object object = iterator.next();

							// If you can find out what Work Group to delete, do it.
							if (object instanceof WorkGroupTreeNode) {
								WorkGroupTreeNode node = (WorkGroupTreeNode) object;
								WorkGroupVO workGroupVO = node.getValue();
								try {
									WorkGroupModel.getInstance().delete(workGroupVO);
								}
								catch (Exception e) {
									RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
								}
							}
						}
					}

				});
			}
		}
		return null;
	}

}
