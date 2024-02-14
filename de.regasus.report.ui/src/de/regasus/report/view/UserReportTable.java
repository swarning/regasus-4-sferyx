package de.regasus.report.view;

import java.util.Locale;

import org.eclipse.swt.widgets.Table;

import com.lambdalogic.messeinfo.report.data.UserReportVO;
import com.lambdalogic.util.rcp.simpleviewer.SimpleTable;

enum UserReportTableColumns {NAME_COL, DESCRIPTION_COL};

public class UserReportTable extends SimpleTable<UserReportVO, UserReportTableColumns> {

	private final String language = Locale.getDefault().getLanguage();
	
	public UserReportTable(Table table) {
		super(table, UserReportTableColumns.class);
	}

	@Override
	public String getColumnText(UserReportVO userReportVO, UserReportTableColumns column) {
		String label = null;
		
		switch (column) {
			case NAME_COL:
				if (userReportVO.getName() != null) {
					label = userReportVO.getName().getString(language);
				}
				break;
			case DESCRIPTION_COL:
				if (userReportVO.getDescription() != null) {
					label = userReportVO.getDescription().getString(language);
				}
				break;
		}

		if (label == null) {
			label = ""; 
		}
		
		return label;
	}



	@Override
	protected UserReportTableColumns getDefaultSortColumn() {
		return UserReportTableColumns.NAME_COL;
	}

	

}
