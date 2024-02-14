package de.regasus.finance.impersonalaccount.view;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Table;

import com.lambdalogic.messeinfo.invoice.data.ImpersonalAccountVO;
import com.lambdalogic.util.StringHelper;
import com.lambdalogic.util.rcp.simpleviewer.SimpleTable;

import de.regasus.core.ui.IImageKeys;
import de.regasus.core.ui.IconRegistry;

enum ImpersonalAccountTableColumns {
	ID, NAME, FINANCE_ACCOUNT
};

public class ImpersonalAccountTable extends SimpleTable<ImpersonalAccountVO, ImpersonalAccountTableColumns> {

	public ImpersonalAccountTable(Table table) {
		super(table, ImpersonalAccountTableColumns.class);
	}


	@Override
	public String getColumnText(ImpersonalAccountVO impersonalAccountVO, ImpersonalAccountTableColumns column) {
		switch (column) {
    		case ID:
    			return ImpersonalAccountVO.NUMBER_FORMAT.format(impersonalAccountVO.getNo().longValue());
    		case NAME:
    			return StringHelper.avoidNull(impersonalAccountVO.getName());
    		default:
    			return "";
		}
	}
	

	@Override
	protected ImpersonalAccountTableColumns getDefaultSortColumn() {
		return ImpersonalAccountTableColumns.ID;
	}
	
	
	@Override
	public Image getColumnImage(ImpersonalAccountVO element, ImpersonalAccountTableColumns column) {
		switch (column) {
		case FINANCE_ACCOUNT:
			if (element.isFinanceAccount()) {
				return IconRegistry.getImage(IImageKeys.CHECKED);
			}
			else {
				return IconRegistry.getImage(IImageKeys.UNCHECKED);
			}
		default:
			return null;
		}
	}


	@Override
	protected Comparable<? extends Object> getColumnComparableValue(
		ImpersonalAccountVO element,
		ImpersonalAccountTableColumns column
	) {
		switch (column) {
    		case FINANCE_ACCOUNT:
    			return Boolean.toString(element.isFinanceAccount());
    		default:
    			return super.getColumnComparableValue(element, column);
		}
	}
	
}
