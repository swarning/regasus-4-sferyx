package de.regasus.finance.payment.editor;

import org.eclipse.swt.widgets.Table;

import com.lambdalogic.messeinfo.invoice.data.PaymentReceiptType;
import com.lambdalogic.messeinfo.kernel.KernelLabel;
import com.lambdalogic.messeinfo.kernel.data.DataStoreVO;
import com.lambdalogic.util.FileHelper;
import com.lambdalogic.util.rcp.simpleviewer.SimpleTable;

import de.regasus.finance.PaymentType;

enum TypedTemplateTableColumns {PAYMENT_RECEIPT_TYPE, PAYMENT_TYPE, LANGUAGE, FILE};

public class PaymentReceiptTemplateTable extends SimpleTable<DataStoreVO, TypedTemplateTableColumns> {

	public PaymentReceiptTemplateTable(Table table) {
		super(table, TypedTemplateTableColumns.class);
	}


	@Override
	public String getColumnText(DataStoreVO dataStoreVO, TypedTemplateTableColumns column) {
		switch (column) {
			case PAYMENT_RECEIPT_TYPE:
				try {
					/* Determine PaymentReceiptType from docType that is a combination of PaymentReceiptType and PaymentType, 
					 * e.g. "PAYMENT.CASH".
					 */
					String docType = dataStoreVO.getDocType();
					int dotIdx = docType.indexOf('.');
					String name = docType.substring(0, dotIdx);
					PaymentReceiptType type = PaymentReceiptType.valueOf(name);
					
					return type.getString();
				}
				catch (Exception e) {
					com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
					return null;
				}
			case PAYMENT_TYPE:
				try {
					/* Determine PaymentType from docType that is a combination of PaymentReceiptType and PaymentType, 
					 * e.g. "PAYMENT.CASH".
					 */
					String docType = dataStoreVO.getDocType();
					int dotIdx = docType.indexOf('.');
					String name = docType.substring(dotIdx + 1);
					PaymentType type = PaymentType.valueOf(name);
					
					return type.getString();
				}
				catch (Exception e) {
					com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
					return null;
				}
			case LANGUAGE:
				return dataStoreVO.getLanguage();
			case FILE: {
				String fileName = KernelLabel.DefaultTemplate.getString();
				if (dataStoreVO.getID() != null) {
					fileName = dataStoreVO.getExtFileName();
					try {
						fileName = FileHelper.getName(fileName);
					}
					catch (Exception e) {
						com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
					}
				}
				return fileName;
			}
		}
		return null;
	}


	@Override
	protected boolean shouldSortInitialTable() {
		//return super.shouldSortInitialTable();
		return false;
	}

}
