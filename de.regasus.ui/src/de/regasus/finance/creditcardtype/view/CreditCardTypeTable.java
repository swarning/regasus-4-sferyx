package de.regasus.finance.creditcardtype.view;

import java.text.DecimalFormat;

import org.eclipse.swt.widgets.Table;

import com.lambdalogic.messeinfo.contact.data.CreditCardTypeVO;
import com.lambdalogic.util.StringHelper;
import com.lambdalogic.util.rcp.simpleviewer.SimpleTable;

enum CreditCardTypeTableColumns {
	NAME, MNEMONIC
};

public class CreditCardTypeTable extends SimpleTable<CreditCardTypeVO, CreditCardTypeTableColumns> {

	DecimalFormat df = new DecimalFormat("00000");
	
	public CreditCardTypeTable(Table table) {
		super(table, CreditCardTypeTableColumns.class);
	}


	@Override
	public String getColumnText(CreditCardTypeVO creditCardTypeVO, CreditCardTypeTableColumns column) {
		switch (column) {
		case MNEMONIC:
			return StringHelper.avoidNull(creditCardTypeVO.getMnemonic());
		case NAME:
			return StringHelper.avoidNull(creditCardTypeVO.getName());
		}
		return "";
	}

	@Override
	protected CreditCardTypeTableColumns getDefaultSortColumn() {
		return CreditCardTypeTableColumns.NAME;
	}

}
