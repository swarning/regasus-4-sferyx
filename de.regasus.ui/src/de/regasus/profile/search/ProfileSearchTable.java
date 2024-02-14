package de.regasus.profile.search;

import org.eclipse.swt.widgets.Table;

import com.lambdalogic.messeinfo.profile.Profile;
import com.lambdalogic.util.rcp.simpleviewer.SimpleTable;

enum ProfileSearchTableColumns {FIRST_NAME, LAST_NAME, ORGANISATION, CITY};

public class ProfileSearchTable extends SimpleTable<Profile, ProfileSearchTableColumns> {

	
	public ProfileSearchTable(Table table) {
		super(table, ProfileSearchTableColumns.class, true, false);
	}

	
	@Override
	public String getColumnText(Profile profile, ProfileSearchTableColumns column) {
		String label = null;
		
		switch (column) {
			case FIRST_NAME:
				label = profile.getFirstName();
				break;
			case LAST_NAME:
				label = profile.getLastName();
				break;
			case ORGANISATION:
				label = profile.getMainAddress().getOrganisation();
				break;
			case CITY:
				label = profile.getMainAddress().getCity();
				break;
		}
		
		if (label == null) {
			label = ""; 
		}
		
		return label;
	}


	@Override
	protected ProfileSearchTableColumns getDefaultSortColumn() {
		return ProfileSearchTableColumns.LAST_NAME;
	}

	

}
