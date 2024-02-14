package de.regasus.finance.invoice;

import org.eclipse.swt.widgets.Table;

import com.lambdalogic.messeinfo.invoice.data.InvoiceNoRangeCVO;
import com.lambdalogic.messeinfo.invoice.data.InvoiceNoRangeVO;
import com.lambdalogic.util.rcp.simpleviewer.SimpleTable;

enum InvoiceNoRangeTableColumns {NAME, START_NO, END_NO};

public class InvoiceNoRangeTable extends SimpleTable<InvoiceNoRangeCVO, InvoiceNoRangeTableColumns> {

	public InvoiceNoRangeTable(Table table) {
		super(table, InvoiceNoRangeTableColumns.class);
	}
	
	
	@Override
	public String getColumnText(InvoiceNoRangeCVO invoiceNoRangeCVO, InvoiceNoRangeTableColumns column) {
		String label = null;
			
		final InvoiceNoRangeVO invoiceNoRangeVO = invoiceNoRangeCVO.getVO();
		
		switch (column) {
			case NAME:
				label = invoiceNoRangeVO.getName();
				break;
			case START_NO:
				label = invoiceNoRangeVO.getStartNo().toString();
				break;
			case END_NO:
				label = invoiceNoRangeVO.getEndNo().toString();
				break;
		}
	
		if (label == null) {
			label = ""; 
		}
		
		return label;
	}

	
	@Override
	protected Comparable<?> getColumnComparableValue(InvoiceNoRangeCVO invoiceNoRangeCVO, InvoiceNoRangeTableColumns column) {
		/* Für Werte, die nicht vom Typ String sind, die Originalwerte (z.B. Date, Integer)
		 * zurückgeben.
		 * Werte vom Typ String können pauschal über super.getColumnComparableValue(eventVO, column)
		 * zurückgegeben werden, weil sie Sortierwerte den angezeigten entsprechen.
		 */
		final InvoiceNoRangeVO invoiceNoRangeVO = invoiceNoRangeCVO.getVO();
		
		switch (column) {
			case START_NO:
				return invoiceNoRangeVO.getStartNo();
			case END_NO:
				return invoiceNoRangeVO.getEndNo();
			default:
				return super.getColumnComparableValue(invoiceNoRangeCVO, column);	
		}
	}
	
	
	@Override
	protected InvoiceNoRangeTableColumns getDefaultSortColumn() {
		return InvoiceNoRangeTableColumns.NAME;
	}

}
