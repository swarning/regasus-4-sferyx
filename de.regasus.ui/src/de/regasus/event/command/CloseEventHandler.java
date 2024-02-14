package de.regasus.event.command;

import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.Locale;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.handlers.HandlerUtil;
import com.lambdalogic.messeinfo.participant.data.EventVO;
import com.lambdalogic.util.rcp.BusyCursorHelper;

import de.regasus.core.error.RegasusErrorHandler;
import com.lambdalogic.util.rcp.UtilI18N;
import de.regasus.event.EventModel;
import de.regasus.event.dialog.CloseEventDialog;
import de.regasus.event.view.EventTreeNode;
import de.regasus.ui.Activator;

/**
 * This action deletes the currently selected event(s).
 */
public class CloseEventHandler extends AbstractHandler {

	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		// Iterate through whatever is currently selected
		final IStructuredSelection currentSelection = (IStructuredSelection) HandlerUtil.getCurrentSelection(event);


		if (!currentSelection.isEmpty()) {
			CloseEventDialog dialog = new CloseEventDialog(HandlerUtil.getActiveShell(event));

			dialog.create();
			dialog.getShell().setSize(500, 300);
			int open = dialog.open();

			if (open == Window.OK) {

				final boolean deleteACL = dialog.isDeleteAcl();
				final boolean deleteHistory = dialog.isDeleteHistory();
				final boolean deleteLeads = dialog.isDeleteLeads();
				final boolean deleteCreditCard = dialog.isDeleteCreditCard();
				final boolean deletePortalPhotos = dialog.isDeletePortalPhotos();

				BusyCursorHelper.busyCursorWhile(new IRunnableWithProgress() {

					@Override
					public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
						monitor.beginTask(UtilI18N.Closing, currentSelection.size());
						Iterator<?> iterator = currentSelection.iterator();
						while (iterator.hasNext()) {
							Object object = iterator.next();

							// If you can find out what Event to close, do it.
							if (object instanceof EventTreeNode) {
								EventTreeNode node = (EventTreeNode) object;
								EventVO eventVO = node.getValue();

								monitor.subTask( eventVO.getName(Locale.getDefault()) );

								try {
									EventModel.getInstance().closeEvent(
										eventVO,
										deleteHistory,
										deleteACL,
										deleteLeads,
										deleteCreditCard,
										deletePortalPhotos
									);
								}
								catch (Exception e) {
									RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
								}

							}
							monitor.worked(1);
						}
					}
				});
			}
		}

		return null;
	}
}
