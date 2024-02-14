package de.regasus.finance.invoicenumberrange.editor;

import org.eclipse.swt.widgets.Table;

import com.lambdalogic.messeinfo.invoice.interfaces.InvoiceTemplateType;
import com.lambdalogic.messeinfo.kernel.data.DataStoreVO;
import com.lambdalogic.util.FileHelper;
import com.lambdalogic.util.rcp.simpleviewer.SimpleTable;

enum TypedTemplateTableColumns {TYPE, LANGUAGE, FILE};

public class InvoiceTemplateTable extends SimpleTable<DataStoreVO, TypedTemplateTableColumns> {

	public InvoiceTemplateTable(Table table) {
		super(table, TypedTemplateTableColumns.class);
	}


	@Override
	public String getColumnText(DataStoreVO dataStoreVO, TypedTemplateTableColumns column) {
		switch (column) {
			case TYPE:
				try {
					String docType = dataStoreVO.getDocType();
					InvoiceTemplateType type = InvoiceTemplateType.valueOf(docType);
					return type.getString();
				}
				catch (Exception e) {
					com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
					return null;
				}
			case LANGUAGE:
				return dataStoreVO.getLanguage();
			case FILE: {
				String fileName = dataStoreVO.getExtFileName();
				try {
					fileName = FileHelper.getName(fileName);
				}
				catch (Exception e) {
					com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
				}
				return fileName;
			}
		}
		return null;
	}
}
