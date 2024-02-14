package de.regasus.portal.command;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;

import com.lambdalogic.util.rcp.BrowserHelper;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.event.view.PortalTreeNode;
import de.regasus.portal.Portal;
import de.regasus.portal.PortalModel;
import de.regasus.ui.Activator;


public class OpenPortalHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			Long portalPK = determninePortalPK(event);
			if (portalPK != null) {
				String url = PortalModel.getInstance().getPortalUrl(portalPK);
				BrowserHelper.openBrowser(url);
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
