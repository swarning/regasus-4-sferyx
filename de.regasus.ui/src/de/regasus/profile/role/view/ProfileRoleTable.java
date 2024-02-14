package de.regasus.profile.role.view;

import org.eclipse.swt.widgets.Table;

import com.lambdalogic.messeinfo.profile.ProfileRole;
import com.lambdalogic.util.rcp.simpleviewer.SimpleTable;

enum ProfileRoleTableColumns {NAME, DESCRIPTION};

public class ProfileRoleTable extends SimpleTable<ProfileRole, ProfileRoleTableColumns> {

	public ProfileRoleTable(Table table) {
		super(table, ProfileRoleTableColumns.class);
	}

	
	@Override
	public String getColumnText(ProfileRole profileRole, ProfileRoleTableColumns column) {
		String label = null;
		
		
		switch (column) {
			case NAME:
				label = profileRole.getName();
				break;
			case DESCRIPTION:
				label = profileRole.getDescription();
				break;
		}
		
		if (label == null) {
			label = "";
		}
		
		return label;
	}
	
	
	@Override
	protected ProfileRoleTableColumns getDefaultSortColumn() {
		return ProfileRoleTableColumns.NAME;
	}

}
