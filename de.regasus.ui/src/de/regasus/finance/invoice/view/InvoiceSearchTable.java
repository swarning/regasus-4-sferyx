package de.regasus.finance.invoice.view;

import java.util.Date;

import org.eclipse.swt.widgets.Table;

import com.lambdalogic.messeinfo.invoice.data.InvoiceCVO;
import com.lambdalogic.messeinfo.invoice.data.InvoiceNoRangeCVO;
import com.lambdalogic.util.FormatHelper;
import com.lambdalogic.util.StringHelper;
import com.lambdalogic.util.rcp.simpleviewer.SimpleTable;

import de.regasus.finance.InvoiceNoRangeModel;


enum InvoiceSearchTableColumns {
	NUMBER, NUMBER_RANGE, NAME, DATE, AMOUNT, OPEN, REMINDER_STATE, NEXT_REMINDER, FINAL_PAY_TIME_DATE
};


public class InvoiceSearchTable extends SimpleTable<InvoiceCVO, InvoiceSearchTableColumns> {

	private FormatHelper formatHelper = FormatHelper.getDefaultLocaleInstance();

	private InvoiceNoRangeModel invoiceNoRangeModel = InvoiceNoRangeModel.getInstance();


	public InvoiceSearchTable(Table table) {
		super(table, InvoiceSearchTableColumns.class, true, false);
	}


	@Override
	public String getColumnText(InvoiceCVO invoiceCVO, InvoiceSearchTableColumns column) {
		switch (column) {
			case NUMBER:
				return StringHelper.avoidNull(invoiceCVO.getVO().getNumber());
			case NUMBER_RANGE:
				Long invoiceNoRangePK = invoiceCVO.getVO().getInvoiceNoRangePK();
				try {
					InvoiceNoRangeCVO invoiceNoRangeCVO = invoiceNoRangeModel.getInvoiceNoRangeCVO(invoiceNoRangePK);
					return invoiceNoRangeCVO.getVO().getName();
				}
				catch (Exception e) {
					com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
					return "";
				}
			case NAME:
				return StringHelper.avoidNull( invoiceCVO.getRecipient().getName(true) );
			case DATE:
				return formatHelper.formatDate( invoiceCVO.getInvoiceDate() );
			case AMOUNT:
				return invoiceCVO.getVO().getAmountGrossAsCurrencyAmount().format(false, true);
			case OPEN:
				return invoiceCVO.getVO().getAmountOpenAsCurrencyAmount().format(false, true);
			case REMINDER_STATE:
				return invoiceCVO.getVO().getReminderState().getString();
			case NEXT_REMINDER:
				return formatHelper.formatDate( invoiceCVO.getNextReminder() );
			case FINAL_PAY_TIME_DATE:
				Date finalPayTimeDate = invoiceCVO.getFinalPayTimeDate();
				if (finalPayTimeDate != null) {
					return formatHelper.formatDate(finalPayTimeDate);
				}
				else {
					return "";
				}
			default:
				return "";
		}
	}


	@Override
	public Comparable<? extends Object> getColumnComparableValue(InvoiceCVO invoiceCVO, InvoiceSearchTableColumns column) {
		switch (column) {
			case NUMBER:
				return invoiceCVO.getVO().getNumber();
			case DATE:
				return invoiceCVO.getInvoiceDate();
			case AMOUNT:
				return invoiceCVO.getVO().getAmountGross();
			case OPEN:
				return invoiceCVO.getVO().getAmountOpen();
			case REMINDER_STATE:
				return invoiceCVO.getVO().getReminderState().ordinal();
			case NEXT_REMINDER:
				return invoiceCVO.getNextReminder();
			case FINAL_PAY_TIME_DATE:
				return invoiceCVO.getFinalPayTimeDate();
			default:
				return super.getColumnComparableValue(invoiceCVO, column);
		}
	}


	@Override
	protected InvoiceSearchTableColumns getDefaultSortColumn() {
		return InvoiceSearchTableColumns.NUMBER;
	}

}
