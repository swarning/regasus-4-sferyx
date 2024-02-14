package de.regasus.finance.customeraccount.view;

import org.eclipse.swt.widgets.Table;

import com.lambdalogic.messeinfo.invoice.data.CustomerAccountVO;
import com.lambdalogic.util.StringHelper;
import com.lambdalogic.util.rcp.simpleviewer.SimpleTable;

enum CustomerAccountTableColumns {
	ID, NAME
};

public class CustomerAccountTable extends SimpleTable<CustomerAccountVO, CustomerAccountTableColumns> {

	public CustomerAccountTable(Table table) {
		super(table, CustomerAccountTableColumns.class);
	}


	@Override
	public String getColumnText(CustomerAccountVO customerAccountVO, CustomerAccountTableColumns column) {
		switch (column) {
		case ID:
			return customerAccountVO.getNo();
		case NAME:
			return StringHelper.avoidNull(customerAccountVO.getName());
		}
		return "";
	}

	@Override
	protected CustomerAccountTableColumns getDefaultSortColumn() {
		return CustomerAccountTableColumns.ID;
	}

}
