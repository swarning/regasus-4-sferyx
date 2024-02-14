package de.regasus.core.ui.dialog;

import org.eclipse.swt.widgets.Table;

import com.lambdalogic.messeinfo.kernel.Session;
import com.lambdalogic.util.rcp.simpleviewer.SimpleTable;

enum SessionTableColumns {
	USER, HOST
};

public class SessionTable extends SimpleTable<Session, SessionTableColumns> {


	public SessionTable(Table table) {
		super(table, SessionTableColumns.class);
	}


	@Override
	public String getColumnText(Session session, SessionTableColumns column) {
		switch (column) {
		case USER:
			return session.getUserName();
		case HOST:
			return session.getHostName();
		}
		return "";
	}

	@Override
	protected SessionTableColumns getDefaultSortColumn() {
		return SessionTableColumns.USER;
	}

}
