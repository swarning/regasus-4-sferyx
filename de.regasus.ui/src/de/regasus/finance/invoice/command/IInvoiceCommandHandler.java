package de.regasus.finance.invoice.command;

import java.util.Collection;

import org.eclipse.core.commands.ExecutionEvent;

import com.lambdalogic.messeinfo.invoice.data.InvoiceVO;

 public interface IInvoiceCommandHandler {


	void execute(
		ExecutionEvent event, 
		Collection<InvoiceVO> invoiceVos
	) throws Exception;

}
