package de.regasus.profile.relation.view;

import org.eclipse.swt.widgets.Table;

import com.lambdalogic.messeinfo.profile.Profile;
import com.lambdalogic.messeinfo.profile.ProfileRelation;
import com.lambdalogic.messeinfo.profile.ProfileRelationType;
import com.lambdalogic.util.rcp.simpleviewer.SimpleTable;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.profile.ProfileModel;
import de.regasus.profile.ProfileRelationTypeModel;
import de.regasus.ui.Activator;

enum ProfileRelationTableColumns {
	
	/** role of the selected profile */
	PROFILE_RELATION_TYPE_DESC,

	/** name of the connected profile */
	OTHER_PROFILE_NAME
};

public class ProfileRelationTable extends SimpleTable<ProfileRelation, ProfileRelationTableColumns> {
	
	private Long profileID;
	
	private ProfileModel pModel;
	private ProfileRelationTypeModel prtModel;
	
	
	public ProfileRelationTable(Table table) {
		super(table, ProfileRelationTableColumns.class, true, false);
		pModel = ProfileModel.getInstance();
		prtModel = ProfileRelationTypeModel.getInstance();
	}
	
	
	@Override
	public String getColumnText(ProfileRelation profileRelation, ProfileRelationTableColumns column) {
		String label = "";
		
		try {
			if (profileID != null) {
				
				switch (column) {
					case PROFILE_RELATION_TYPE_DESC:
						// get ProfileRelationType from model
						ProfileRelationType profileRelationType = prtModel.getProfileRelationType(
							profileRelation.getProfileRelationTypeID()
						);
						
						// determine label for ProfileRelationType
						if (profileID.equals(profileRelation.getProfile1ID())) {
							label = profileRelationType.getDescription12().getString();
						}
						else {
							label = profileRelationType.getDescription21().getString();
						}
						break;
						
					case OTHER_PROFILE_NAME:
						// get other Profile from model
						Long otherProfileID = profileRelation.getOtherProfileID(profileID);
						Profile otherProfile = pModel.getProfile(otherProfileID);
						
						// determine label for other Profile
						label = otherProfile.getName();
						break;
						
					default:
						// do nothing because label is already initialized
				}
				
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
		
		return label;
	}
	
	
	@Override
	protected ProfileRelationTableColumns getDefaultSortColumn() {
		return ProfileRelationTableColumns.OTHER_PROFILE_NAME;
	}
	
	
	public void setProfileID(Long profileID) {
		this.profileID = profileID;
	}
	
}
