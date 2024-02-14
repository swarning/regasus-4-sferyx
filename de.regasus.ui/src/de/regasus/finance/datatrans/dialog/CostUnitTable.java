package de.regasus.finance.datatrans.dialog;

import java.text.DecimalFormat;

import org.eclipse.swt.widgets.Table;

import com.lambdalogic.messeinfo.invoice.data.CostCenterVO;
import com.lambdalogic.util.StringHelper;
import com.lambdalogic.util.rcp.simpleviewer.SimpleTable;

enum CostUnitTableColumns {
	ID, NAME
};

public class CostUnitTable extends SimpleTable<CostCenterVO, CostUnitTableColumns> {

	DecimalFormat df = new DecimalFormat("00000");
	
	public CostUnitTable(Table table) {
		super(table, CostUnitTableColumns.class);
	}


	@Override
	public String getColumnText(CostCenterVO costUnitVO, CostUnitTableColumns column) {
		switch (column) {
		case ID:
			return df.format(costUnitVO.getNo().longValue());
		case NAME:
			return StringHelper.avoidNull(costUnitVO.getName());
		}
		return "";
	}
	
	@Override
	protected Comparable<? extends Object> getColumnComparableValue(CostCenterVO costUnitVO, CostUnitTableColumns column) {
		switch (column) {
			case ID:
				return costUnitVO.getNo().longValue();
			case NAME:
				return StringHelper.avoidNull(costUnitVO.getName());
			default: 
				return super.getColumnComparableValue(costUnitVO, column);
		}
	}

	@Override
	protected CostUnitTableColumns getDefaultSortColumn() {
		return CostUnitTableColumns.ID;
	}

}
