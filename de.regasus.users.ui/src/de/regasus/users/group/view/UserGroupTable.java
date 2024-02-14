package de.regasus.users.group.view;

import org.eclipse.swt.widgets.Table;

import com.lambdalogic.messeinfo.account.data.UserGroupVO;
import com.lambdalogic.util.StringHelper;
import com.lambdalogic.util.rcp.simpleviewer.SimpleTable;

enum UserGroupTableColumns {
	ID, DESCRIPTION
};

public class UserGroupTable extends SimpleTable<UserGroupVO, UserGroupTableColumns> {

	public UserGroupTable(Table table) {
		super(table, UserGroupTableColumns.class, true, false);
	}


	public String getColumnText(UserGroupVO userAccountVO, UserGroupTableColumns column) {
		switch (column) {
		case ID:
			return userAccountVO.getPK();
		case DESCRIPTION:
			return StringHelper.avoidNull(userAccountVO.getDescription());
		}
		return "";
	}
	
	@Override
	protected UserGroupTableColumns getDefaultSortColumn() {
		return UserGroupTableColumns.ID;
	}
}
