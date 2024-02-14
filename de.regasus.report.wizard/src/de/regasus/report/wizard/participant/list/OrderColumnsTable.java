package de.regasus.report.wizard.participant.list;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Table;

import com.lambdalogic.util.rcp.simpleviewer.SimpleTable;

import de.regasus.report.IImageKeys;
import de.regasus.report.IconRegistry;

enum OrderColumnsTableColumns {NAME};

public class OrderColumnsTable extends SimpleTable<SQLFieldContainer, OrderColumnsTableColumns> {

	public OrderColumnsTable(Table table) {
		super(table, OrderColumnsTableColumns.class, false);
	}
	
	@Override
	public String getColumnText(SQLFieldContainer sqlFieldContainer, OrderColumnsTableColumns column) {
		String label = null;
		switch (column) {
			case NAME:
				label = sqlFieldContainer.getSqlField().getLabel();
				break;
		}
		
		if (label == null) {
			label = ""; 
		}
		
		return label;
	}

	@Override
	public Image getColumnImage(SQLFieldContainer element, OrderColumnsTableColumns column) {
		switch (column) {
		case NAME:
			return IconRegistry.getImage(IImageKeys.SQL_SELECT_FIELD);
		default:
			return super.getColumnImage(element, column);
		}
	}
}

