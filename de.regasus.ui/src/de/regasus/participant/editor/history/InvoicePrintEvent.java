package de.regasus.participant.editor.history;

import java.util.Date;

import com.lambdalogic.messeinfo.invoice.data.InvoiceVO;
import com.lambdalogic.util.CurrencyAmount;
import com.lambdalogic.util.FormatHelper;

import de.regasus.I18N;
import de.regasus.history.IHistoryEvent;

public class InvoicePrintEvent implements IHistoryEvent {


	FormatHelper formatHelper = new FormatHelper();

	private InvoiceVO invoiceVO;

	public InvoicePrintEvent(InvoiceVO invoiceVO) {
		this.invoiceVO = invoiceVO;
	}


	@Override
	public String getHtmlDescription() {
		StringBuilder sb = new StringBuilder("<DIV>");
		sb.append(I18N.Invoice_Amount_Gross);
		sb.append(": ");
		CurrencyAmount currencyAmount = invoiceVO.getAmountGrossAsCurrencyAmount();
		sb.append(currencyAmount.format(false, false));
		sb.append(", ");
		sb.append(I18N.Invoice_Number);
		sb.append(": ");
		sb.append(invoiceVO.getNumber());
		sb.append(", ");
		sb.append(I18N.Invoice_Date);
		sb.append(": ");
		sb.append(formatHelper.formatDate(invoiceVO.getInvoiceDate()));
		sb.append("</DIV>");

		return sb.toString();
	}


	@Override
	public Date getTime() {
		return invoiceVO.getPrint();
	}


	@Override
	public String getType() {
			return I18N.Invoice_Printed;

	}


	@Override
	public String getUser() {
		return invoiceVO.getPrintDisplayUserStr();
	}


}
