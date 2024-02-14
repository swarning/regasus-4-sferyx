package de.regasus.portal.command;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

import com.lambdalogic.util.rcp.BusyCursorHelper;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.event.view.EventTreeNode;
import de.regasus.event.view.PortalListTreeNode;
import de.regasus.event.view.PortalTreeNode;
import de.regasus.portal.Portal;
import de.regasus.portal.PortalModel;
import de.regasus.portal.dialog.CreatePortalDialog;
import de.regasus.portal.portal.editor.PortalEditor;
import de.regasus.portal.portal.editor.PortalEditorInput;
import de.regasus.ui.Activator;

public class CreatePortalHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		Long eventId = determnineEventPK(event);
		boolean eventDependent = eventId != null;

		Shell shell = HandlerUtil.getActiveShell(event);
		CreatePortalDialog dialog = new CreatePortalDialog(shell, eventDependent);
		int open = dialog.open();
		if (open == Window.OK) {
			// create Portal
			Long[] portalId = new Long[1];
			BusyCursorHelper.busyCursorWhile(new Runnable() {
				@Override
				public void run() {
					try {
	    				Portal portal = PortalModel.getInstance().create(
	        				eventId,
	        				dialog.getPortalType(),
	        				dialog.getMnemonic(),
	        				dialog.getName(),
	        				dialog.getLanguageIds()
	    				);

	    				portalId[0] = portal.getId();
					}
					catch (Exception e) {
						RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
					}
				}
			});


			// open editor
			try {
				if (portalId[0] != null) {
    				PortalEditorInput input = PortalEditorInput.getEditInstance(portalId[0]);
    				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().openEditor(
    					input,
    					PortalEditor.ID
    				);
				}
			}
			catch (Exception e) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			}

		}

		return null;
	}


	private Long determnineEventPK(ExecutionEvent event) {
		Long eventPK = null;

		IStructuredSelection currentSelection = (IStructuredSelection) HandlerUtil.getCurrentSelection(event);
		Object object = currentSelection.getFirstElement();

		if (object instanceof EventTreeNode) {
			eventPK = ((EventTreeNode) object).getEventId();
		}
		else if (object instanceof PortalListTreeNode) {
			eventPK = ((PortalListTreeNode) object).getEventId();
		}
		else if (object instanceof PortalTreeNode) {
			eventPK = ((PortalTreeNode) object).getEventId();
		}

		return eventPK;
	}

}
