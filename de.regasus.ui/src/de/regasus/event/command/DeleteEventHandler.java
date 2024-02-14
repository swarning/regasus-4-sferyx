package de.regasus.event.command;

import java.util.Iterator;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;

import com.lambdalogic.messeinfo.participant.data.EventVO;
import com.lambdalogic.util.rcp.UtilI18N;
import com.lambdalogic.util.rcp.widget.MessageDialogBuilder;

import de.regasus.I18N;
import de.regasus.event.DeleteEventJob;
import de.regasus.event.view.EventTreeNode;

/**
 * Handler to delete the currently selected Event.
 */
public class DeleteEventHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		// identify the selected Event
		final EventVO eventVO = getSelectedEvent(event);
		final Shell activeShell = HandlerUtil.getActiveShell(event);

		if (eventVO != null) {
			// 1st confirmation dialog
			boolean deleteOK1 = open1stConfirmation(activeShell, eventVO);
			if (!deleteOK1) {
				return null;
			}


			// 2nd confirmation dialog
			boolean deleteOK2 = open2ndConfirmation(activeShell, eventVO);
			if (!deleteOK2) {
				return null;
			}


			// delete Event
			DeleteEventJob deleteEventJob = new DeleteEventJob(
				I18N.DeleteEventJobName,
				eventVO
			);
			deleteEventJob.setUser(true);
			deleteEventJob.schedule();
		}

		return null;
	}


	private EventVO getSelectedEvent(ExecutionEvent event) {
		// Iterate through whatever is currently selected
		IStructuredSelection currentSelection = (IStructuredSelection) HandlerUtil.getCurrentSelection(event);

		Iterator<?> iterator = currentSelection.iterator();
		while (iterator.hasNext()) {
			Object object = iterator.next();

			// If you can find out what Portal to delete, do it.
			if (object instanceof EventTreeNode) {
				EventTreeNode node = (EventTreeNode) object;
				EventVO eventVO = node.getValue();
				return eventVO;
			}
		}

		return null;
	}


    private boolean open1stConfirmation(Shell parentShell, EventVO eventVO) {
    	String title = UtilI18N.Question;
    	String message = I18N.DeleteEventConfirmation.replace("<name>", eventVO.getLabel().getString());

    	return MessageDialogBuilder.open1stConfirmation(parentShell, title, message);
    }


    private boolean open2ndConfirmation(Shell parentShell, EventVO eventVO) {
    	String title = UtilI18N.Question;
    	String message = I18N.DeleteEventConfirmation2.replace("<name>", eventVO.getLabel().getString());

    	return MessageDialogBuilder.open2ndConfirmation(parentShell, title, message);
    }

}
