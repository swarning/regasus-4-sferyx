package de.regasus.portal.command;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.event.view.PortalTreeNode;
import de.regasus.portal.Portal;
import de.regasus.portal.dialog.CopyPortalWizard;
import de.regasus.ui.Activator;


public class CopyPortalHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			Long portalPK = determninePortalPK(event);
			if (portalPK != null) {
				Shell shell = HandlerUtil.getActiveShellChecked(event);

				CopyPortalWizard wizard = new CopyPortalWizard(portalPK);
				WizardDialog dialog = new WizardDialog(shell, wizard);
				dialog.create();

				Point preferredSize = wizard.getPreferredSize();
				dialog.getShell().setSize(preferredSize.x, preferredSize.y);

				dialog.open();
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}

		return null;
	}


	private Long determninePortalPK(ExecutionEvent event) {
		Long portalPK = null;

		IStructuredSelection currentSelection = (IStructuredSelection) HandlerUtil.getCurrentSelection(event);
		Object object = currentSelection.getFirstElement();

		if (object instanceof Portal) {
			portalPK = ((Portal) object).getId();
		}
		else if (object instanceof PortalTreeNode) {
			portalPK = ((PortalTreeNode) object).getPortalId();
		}

		return portalPK;
	}

}
