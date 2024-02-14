package de.regasus.email.template.editor;

import java.io.File;

import org.eclipse.swt.widgets.Table;

import com.lambdalogic.util.FileHelper;
import com.lambdalogic.util.rcp.simpleviewer.SimpleTable;


enum EmailAttachmentTableColumns {
	  FILE_NAME
//	, OBSERVE
}

public class EmailAttachmentTable extends SimpleTable<File, EmailAttachmentTableColumns> {

	public EmailAttachmentTable(Table table) {
		super(table, EmailAttachmentTableColumns.class);
	}


	@Override
	public String getColumnText(File file, EmailAttachmentTableColumns column) {
		switch (column) {
			case FILE_NAME: {
				String fileName = file.getName();
				try {
					fileName = FileHelper.getName(fileName);
				}
				catch (Exception e) {
					com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
				}
				return fileName;
			}
			default:
				return null;
		}
	}


	@Override
	protected EmailAttachmentTableColumns getDefaultSortColumn() {
		return EmailAttachmentTableColumns.FILE_NAME;
	}


//	@Override
//	public Image getColumnImage(File file, EmailAttachmentTableColumns column) {
//		switch (column) {
//		case OBSERVE:
//			if ( file.exists() ) {
//				return IconRegistry.getImage(IImageKeys.CHECKED);
//			}
//			else {
//				return IconRegistry.getImage(IImageKeys.UNCHECKED);
//			}
//		default:
//			return super.getColumnImage(file, column);
//		}
//	}

}
