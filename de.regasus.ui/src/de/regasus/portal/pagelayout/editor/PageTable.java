package de.regasus.portal.pagelayout.editor;

import org.eclipse.swt.widgets.Table;

import com.lambdalogic.util.rcp.simpleviewer.SimpleTable;

import de.regasus.portal.Page;

enum PageTableColumns {NAME}

public class PageTable extends SimpleTable<Page, PageTableColumns> {

	public PageTable(Table table) {
		super(table, PageTableColumns.class);
	}


	@Override
	public String getColumnText(
		Page page,
		PageTableColumns column
	) {
		String label = null;

		try {
			switch (column) {
				case NAME:
					label = page.getName().getString();
					break;
			}

			if (label == null) {
				label = "";
			}
		}
		catch (Exception e) {
			label = e.getMessage();
			com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
		}

		return label;
	}


	@Override
	protected PageTableColumns getDefaultSortColumn() {
		return PageTableColumns.NAME;
	}

}
