package de.regasus.finance.paymentsystem.view;

import org.eclipse.swt.widgets.Table;

import com.lambdalogic.util.rcp.UtilI18N;
import com.lambdalogic.util.rcp.simpleviewer.SimpleTable;

import de.regasus.finance.PaymentSystemSetup;

enum PaymentSystemSetupTableColumns {NAME, PAYMENT_SYSTEM, TEST};

public class PaymentSystemSetupTable extends SimpleTable<PaymentSystemSetup, PaymentSystemSetupTableColumns> {

	public PaymentSystemSetupTable(Table table) {
		super(table, PaymentSystemSetupTableColumns.class);
	}

	@Override
	public String getColumnText(PaymentSystemSetup paymentSystemSetup, PaymentSystemSetupTableColumns column) {
		String label = null;

		switch (column) {
			case NAME:
				label = paymentSystemSetup.getName();
				break;
			case PAYMENT_SYSTEM:
				label = paymentSystemSetup.getPaymentSystem().getString();
				break;
			case TEST:
				if ( paymentSystemSetup.isTest() ) {
					label = UtilI18N.Yes;
				}
				else {
					label = UtilI18N.No;
				}
				break;
		}

		if (label == null) {
			label = "";
		}

		return label;
	}


	@Override
	protected PaymentSystemSetupTableColumns getDefaultSortColumn() {
		return PaymentSystemSetupTableColumns.NAME;
	}

}
