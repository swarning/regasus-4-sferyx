package de.regasus.profile.relationtype.view;

import org.eclipse.swt.widgets.Table;

import com.lambdalogic.i18n.LanguageString;
import com.lambdalogic.messeinfo.profile.ProfileRelationType;
import com.lambdalogic.util.rcp.simpleviewer.SimpleTable;

enum ProfileRelationTypeTableColumns {NAME, ROLE1, ROLE2, DESC12, DESC21};

public class ProfileRelationTypeTable extends SimpleTable<ProfileRelationType, ProfileRelationTypeTableColumns> {

	public ProfileRelationTypeTable(Table table) {
		super(table, ProfileRelationTypeTableColumns.class);
	}

	
	@Override
	public String getColumnText(ProfileRelationType profileRelationType, ProfileRelationTypeTableColumns column) {
		String label = null;
		
		LanguageString lsLabel = null;
		
		switch (column) {
			case NAME:
				lsLabel = profileRelationType.getName();
				break;
			case ROLE1:
				lsLabel = profileRelationType.getRole1();
				break;
			case ROLE2:
				lsLabel = profileRelationType.getRole2();
				break;
			case DESC12:
				lsLabel = profileRelationType.getDescription12();
				break;
			case DESC21:
				lsLabel = profileRelationType.getDescription21();
				break;
		}
		
		if (lsLabel != null) {
			label = lsLabel.toString();
		}
		else {
			label = "";
		}
		
		return label;
	}
	
	
	@Override
	protected ProfileRelationTypeTableColumns getDefaultSortColumn() {
		return ProfileRelationTypeTableColumns.NAME;
	}

}
