package de.regasus.event.command;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.event.EventSelectionHelper;
import de.regasus.event.dialog.CopyEventWizard;
import de.regasus.ui.Activator;

public class CopyEventHandler extends AbstractHandler {

	public Object execute(ExecutionEvent event) throws ExecutionException {
		// Iterate through whatever is currently selected
		final IStructuredSelection currentSelection = (IStructuredSelection) HandlerUtil.getCurrentSelection(event);

		if (!currentSelection.isEmpty()) {
			try {
				Long eventID = EventSelectionHelper.getEventID(event);
				if (eventID != null) {
					Shell shell = HandlerUtil.getActiveShellChecked(event);

					CopyEventWizard wizard = new CopyEventWizard(eventID);
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
		}

		return null;
	}
}
