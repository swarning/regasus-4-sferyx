package de.regasus.users.user.view;

import org.eclipse.swt.widgets.Table;

import com.lambdalogic.messeinfo.account.data.UserAccountVO;
import com.lambdalogic.util.StringHelper;
import com.lambdalogic.util.rcp.simpleviewer.SimpleTable;

enum UserAccountSearchTableColumns {
	USER_ID, FIRST_NAME, LAST_NAME, EMAIL
};

public class UserAccountSearchTable extends SimpleTable<UserAccountVO, UserAccountSearchTableColumns> {

	public UserAccountSearchTable(Table table) {
		super(table, UserAccountSearchTableColumns.class, true, false);
	}


	public String getColumnText(UserAccountVO userAccountVO, UserAccountSearchTableColumns column) {
		switch (column) {
		case USER_ID:
			return userAccountVO.getUserID();
		case FIRST_NAME:
			return StringHelper.avoidNull(userAccountVO.getFirstName());
		case LAST_NAME:
			return StringHelper.avoidNull(userAccountVO.getLastName());
		case EMAIL:
			return StringHelper.avoidNull(userAccountVO.getEmail());
		}
		return "";
	}
	
	@Override
	protected UserAccountSearchTableColumns getDefaultSortColumn() {
		return UserAccountSearchTableColumns.USER_ID;
	}
}
