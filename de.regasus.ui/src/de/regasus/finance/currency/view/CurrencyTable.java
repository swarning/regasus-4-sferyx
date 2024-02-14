package de.regasus.finance.currency.view;

import org.eclipse.swt.widgets.Table;

import com.lambdalogic.messeinfo.invoice.data.CurrencyVO;
import com.lambdalogic.util.rcp.simpleviewer.SimpleTable;


enum CurrencyTableColumns {NAME, DESCRIPTION};

public class CurrencyTable extends SimpleTable<CurrencyVO, CurrencyTableColumns> {

	public CurrencyTable(Table table) {
		super(table, CurrencyTableColumns.class);
	}

	@Override
	public String getColumnText(
		CurrencyVO currencyVO,
		CurrencyTableColumns column
	) {
		String label = null;

		switch (column) {
			case NAME:
				label = currencyVO.getID();
				break;
			case DESCRIPTION:
				label = currencyVO.getDescription();
				break;
		}

		if (label == null) {
			label = ""; 
		}

		return label;
	}


	@Override
	protected CurrencyTableColumns getDefaultSortColumn() {
		return CurrencyTableColumns.NAME;
	}

}