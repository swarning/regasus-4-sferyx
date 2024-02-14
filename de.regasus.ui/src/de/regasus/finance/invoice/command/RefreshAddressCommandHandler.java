package de.regasus.finance.invoice.command;

import java.util.Collection;

import org.eclipse.core.commands.ExecutionEvent;

import com.lambdalogic.messeinfo.invoice.data.InvoiceVO;

import de.regasus.finance.AccountancyModel;

/**
 * Updates address and salutation of selected invoices with
 * <p>
 * The algorithm used in this handler is adapted from that in the Swing client's
 * InvoiceRefreshAddressAction.commandActionPerformed() method.
 * <p>
 * See also https://lambdalogic.atlassian.net/browse/MIRCP-136
 * 
 * @author manfred
 * 
 */
public class RefreshAddressCommandHandler extends AbstractInvoiceCommandHandler {


	public void execute(ExecutionEvent event, Collection<InvoiceVO> invoiceVOs) throws Exception {
		// Hand the address over to the server so that they are used for
		AccountancyModel.getInstance().updateInvoiceAddresses(invoiceVOs);
	}

}
