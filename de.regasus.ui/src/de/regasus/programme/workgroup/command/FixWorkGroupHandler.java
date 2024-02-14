package de.regasus.programme.workgroup.command;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.event.view.EventTreeNode;
import de.regasus.event.view.ProgrammePointTreeNode;
import de.regasus.programme.WorkGroupActionModel;
import de.regasus.ui.Activator;

public class FixWorkGroupHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IStructuredSelection currentSelection = (IStructuredSelection) HandlerUtil.getCurrentSelection(event);
		Object object = currentSelection.getFirstElement();

		try {
			if (object instanceof EventTreeNode) {
				Long eventPK = ((EventTreeNode) object).getEventId();
				WorkGroupActionModel.getInstance().fixWorkGroupsByEvent(eventPK);
			}
			else if (object instanceof ProgrammePointTreeNode) {
				Long programmePointPK = ((ProgrammePointTreeNode) object).getProgrammePointPK();
				WorkGroupActionModel.getInstance().fixWorkGroupsByProgrammePoint(programmePointPK);
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}

		return null;
	}

}
