package de.regasus.finance.invoicenumberrange.command;

import java.util.Iterator;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;

import com.lambdalogic.messeinfo.invoice.data.InvoiceNoRangeCVO;
import com.lambdalogic.util.rcp.BusyCursorHelper;

import de.regasus.I18N;
import de.regasus.core.error.RegasusErrorHandler;
import com.lambdalogic.util.rcp.UtilI18N;
import de.regasus.event.view.InvoiceNoRangeTreeNode;
import de.regasus.finance.InvoiceNoRangeModel;
import de.regasus.ui.Activator;

/**
 * A command handler who deletes InvoiceNoRangeCVOs based on the given selection (eg from TreeNodes from the MasterData
 * view).
 *
 * @author manfred
 *
 */
public class DeleteInvoiceNoRangeHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		// Iterate through whatever is currently selected
		final IStructuredSelection currentSelection = (IStructuredSelection) HandlerUtil.getCurrentSelection(event);

		if (!currentSelection.isEmpty()) {
			boolean deleteOK = MessageDialog.openConfirm(
				HandlerUtil.getActiveShell(event),
				UtilI18N.Question,
				I18N.DeleteInvoiceNoRangeAction_Confirmation
			);

			if (deleteOK) {
				BusyCursorHelper.busyCursorWhile(new Runnable() {

					@Override
					public void run() {
						Iterator<?> iterator = currentSelection.iterator();
						while (iterator.hasNext()) {
							Object object = iterator.next();

							// If you can find out what InvoiceNoRangeCVO to delete, do it.
							if (object instanceof InvoiceNoRangeTreeNode) {
								InvoiceNoRangeTreeNode node = (InvoiceNoRangeTreeNode) object;
								InvoiceNoRangeCVO invoiceNoRangeCVO = node.getValue();
								try {
									InvoiceNoRangeModel.getInstance().delete(invoiceNoRangeCVO);
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
