package de.regasus.finance.costcenter.view;

import java.text.DecimalFormat;

import org.eclipse.swt.widgets.Table;

import com.lambdalogic.messeinfo.invoice.data.CostCenterVO;
import com.lambdalogic.util.StringHelper;
import com.lambdalogic.util.rcp.simpleviewer.SimpleTable;

enum CostCenterTableColumns {
	ID, NAME
};

public class CostCenterTable extends SimpleTable<CostCenterVO, CostCenterTableColumns> {

	DecimalFormat df = new DecimalFormat("00000");
	
	public CostCenterTable(Table table) {
		super(table, CostCenterTableColumns.class);
	}


	@Override
	public String getColumnText(CostCenterVO costCenter1VO, CostCenterTableColumns column) {
		switch (column) {
    		case ID:
    			return df.format(costCenter1VO.getNo().longValue());
    		case NAME:
    			return StringHelper.avoidNull(costCenter1VO.getName());
   		}
		return "";
	}

	
	@Override
	protected Comparable<? extends Object> getColumnComparableValue(CostCenterVO costCenter1VO, CostCenterTableColumns column) {
		switch (column) {
			case ID:
				return costCenter1VO.getNo();
			case NAME:
				return StringHelper.avoidNull(costCenter1VO.getName());
			default: 
				return super.getColumnComparableValue(costCenter1VO, column);
		}
	}
	
	@Override
	protected CostCenterTableColumns getDefaultSortColumn() {
		return CostCenterTableColumns.ID;
	}

}
