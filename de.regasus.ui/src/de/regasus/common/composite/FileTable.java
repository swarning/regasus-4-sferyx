package de.regasus.common.composite;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;

import com.lambdalogic.util.FileHelper;
import com.lambdalogic.util.StringHelper;
import com.lambdalogic.util.rcp.Activator;
import com.lambdalogic.util.rcp.simpleviewer.SimpleTable;

import de.regasus.common.FileSummary;

enum FileTableColumns {
	FILE_ICON, FILE_NAME, FILE_SIZE, DOCUMENT_NAME, DESCRIPTION
};

public abstract class FileTable extends SimpleTable<FileSummary, FileTableColumns> {

	public FileTable(Table table) {
		super(table, FileTableColumns.class, true, true);
	}


	@Override
	protected Comparable<? extends Object> getColumnComparableValue(
		FileSummary fileSummary,
		FileTableColumns column
	) {

		switch (column) {
		case FILE_SIZE:
			return fileSummary.getSize();
		default:
			return super.getColumnComparableValue(fileSummary, column);
		}
	}


	@Override
	public String getColumnText(FileSummary fileSummary, FileTableColumns column) {
		switch (column) {
		case FILE_NAME:
			return fileSummary.getExternalFileName();
		case FILE_SIZE:
			return FileHelper.computeReadableFileSize(fileSummary.getSize());
		case DOCUMENT_NAME:
			return StringHelper.avoidNull( fileSummary.getName() );
		case DESCRIPTION:
			return StringHelper.avoidNull( fileSummary.getDescription() );
		default:
			return null;
		}
	}


	@Override
	public Image getColumnImage(FileSummary fileSummary, FileTableColumns column) {
		if (column == FileTableColumns.FILE_ICON) {
			String fileName = fileSummary.getExternalPath();
			String extension = FileHelper.getExtension(fileName);
			if (extension != null) {
				return Activator.getDefault().findImageForExtension(extension);
			}
		}
		return null;
	}


	@Override
	public CellEditor getColumnCellEditor(Composite parent, FileTableColumns column) {
		switch (column) {
    		case DOCUMENT_NAME:
    		case DESCRIPTION:
    			return new TextCellEditor(parent);
    		default:
    			return null;
		}
	}


	@SuppressWarnings("unchecked")
	@Override
	public boolean setColumnEditValue(
		FileSummary fileSummary,
		FileTableColumns column,
		Object value
	) {
		// clone before changing its data to avoid dirty entity if update fails
		fileSummary = fileSummary.clone();

		String newString = (String) value;
		boolean changed = false;

		switch (column) {
    		case DOCUMENT_NAME:
    			String documentName = fileSummary.getName();
    			if (!StringHelper.isEqual(documentName, newString)) {
    				fileSummary.setName(newString);
    				changed = true;
    			}
    			break;
    		case DESCRIPTION:
    			String description = fileSummary.getDescription();
    			if (!StringHelper.isEqual(description, newString)) {
    				fileSummary.setDescription(newString);
    				changed = true;
    			}
    			break;
    		default:
    			break;
		}


		if (changed) {
			changed = updateOnChange(fileSummary);
		}

		return changed;
	}


	protected abstract boolean updateOnChange(FileSummary fileSummary);


	@Override
	public Object getColumnEditValue(FileSummary fileSummary, FileTableColumns column) {
		switch (column) {
    		case DOCUMENT_NAME:
    		case DESCRIPTION:
    			return getColumnText(fileSummary, column);
    		default:
    			return null;
		}
	}

	@Override
	protected FileTableColumns getDefaultSortColumn() {
		return FileTableColumns.FILE_NAME;
	}

}
