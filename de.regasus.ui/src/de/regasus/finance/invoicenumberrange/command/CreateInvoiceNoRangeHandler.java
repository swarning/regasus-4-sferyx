package de.regasus.finance.invoicenumberrange.command;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

import com.lambdalogic.messeinfo.invoice.data.InvoiceNoRangeCVO;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.event.view.EventTreeNode;
import de.regasus.event.view.InvoiceNoRangeListTreeNode;
import de.regasus.event.view.InvoiceNoRangeTreeNode;
import de.regasus.finance.invoicenumberrange.editor.InvoiceNoRangeEditor;
import de.regasus.finance.invoicenumberrange.editor.InvoiceNoRangeEditorInput;
import de.regasus.ui.Activator;

/**
 * This command handler finds out what event is currently in focus (based on the selection), creates locally an
 * {@link InvoiceNoRangeCVO} for that event and opens an editor to fill in more data and save (and therewith store) the
 * object.
 *
 * @author manfred
 *
 */
public class CreateInvoiceNoRangeHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IStructuredSelection currentSelection = (IStructuredSelection) HandlerUtil.getCurrentSelection(event);
		Object object = currentSelection.getFirstElement();

		// Find out EventPK
		Long eventPK = null;

		if (object instanceof EventTreeNode) {
			eventPK = ((EventTreeNode) object).getEventId();
		}
		else if (object instanceof InvoiceNoRangeListTreeNode) {
			eventPK = ((InvoiceNoRangeListTreeNode) object).getEventId();
		}
		else if (object instanceof InvoiceNoRangeTreeNode) {
			eventPK = ((InvoiceNoRangeTreeNode) object).getEventId();
		}

		if (eventPK != null) {
			// Open editor for new InvoiceNoRangeCVO
			InvoiceNoRangeEditorInput input = InvoiceNoRangeEditorInput.getCreateInstance(eventPK);
			try {
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().openEditor(
					input,
					InvoiceNoRangeEditor.ID
				);
			}
			catch (PartInitException e) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			}
		}

		return null;
	}

}
