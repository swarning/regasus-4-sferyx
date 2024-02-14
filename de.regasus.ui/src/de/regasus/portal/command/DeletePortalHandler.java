package de.regasus.portal.command;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;

import com.lambdalogic.util.rcp.BusyCursorHelper;
import com.lambdalogic.util.rcp.SelectionHelper;
import com.lambdalogic.util.rcp.UtilI18N;
import com.lambdalogic.util.rcp.widget.MessageDialogBuilder;

import de.regasus.I18N;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.event.view.PortalTreeNode;
import de.regasus.portal.Portal;
import de.regasus.portal.PortalModel;
import de.regasus.ui.Activator;

public class DeletePortalHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		final Portal portal = getSelectedPortal(event);
		final Shell activeShell = HandlerUtil.getActiveShell(event);

		if (portal != null) {
			// 1st confirmation dialog
			boolean deleteOK1 = openConfirmationDialog1(activeShell, portal);
			if (!deleteOK1) {
				return null;
			}


			// 2nd confirmation dialog
			boolean deleteOK2 = openConfirmationDialog2(activeShell, portal);
			if (!deleteOK2) {
				return null;
			}


			// delete Portal
			BusyCursorHelper.busyCursorWhile(new Runnable() {
				@Override
				public void run() {
					try {
						PortalModel.getInstance().delete(portal);
					}
					catch (Exception e) {
						RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
					}
				}
			});
		}

		return null;
	}


	private Portal getSelectedPortal(ExecutionEvent event) {
		Portal portal = null;

		ISelection currentSelection = HandlerUtil.getCurrentSelection(event);
		Object selectedObject = SelectionHelper.getUniqueSelected(currentSelection);
		if (selectedObject instanceof Portal) {
			portal = (Portal) selectedObject;
		}
		else if (selectedObject instanceof PortalTreeNode) {
			PortalTreeNode node = (PortalTreeNode) selectedObject;
			portal = node.getValue();
		}

		return portal;
	}


    private boolean openConfirmationDialog1(Shell parentShell, Portal portal) {
    	String title = UtilI18N.Question;
    	String message = I18N.DeletePortal_Confirmation.replace("<name>", portal.getName());

    	return MessageDialogBuilder.open1stConfirmation(parentShell, title, message);
    }


    private boolean openConfirmationDialog2(Shell parentShell, Portal portal) {
    	String title = UtilI18N.Question;
    	String message = I18N.DeletePortal_Confirmation2.replace("<name>", portal.getName());

    	return MessageDialogBuilder.open2ndConfirmation(parentShell, title, message);
    }

}
