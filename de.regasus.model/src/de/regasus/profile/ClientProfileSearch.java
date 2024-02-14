package de.regasus.profile;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.lambdalogic.messeinfo.config.parameterset.ConfigParameterSet;
import com.lambdalogic.messeinfo.profile.ProfileCustomField;
import com.lambdalogic.messeinfo.profile.ProfileCustomFieldGroup;
import com.lambdalogic.messeinfo.profile.sql.ProfileSearch;

import de.regasus.event.EventYesNoSearchValuesProvider;
import de.regasus.person.ClientPersonSearch;

public class ClientProfileSearch extends ProfileSearch {

	private static final long serialVersionUID = 1L;


    static {
   		ClientPersonSearch.initStaticFields();

    	EventYesNoSearchValuesProvider eventSearchValuesProvider = new EventYesNoSearchValuesProvider();
        IS_PARTICIPANT.setSearchValuesProvider(eventSearchValuesProvider);

        ProfileRoleSearchValuesProvider roleSearchValuesProvider = new ProfileRoleSearchValuesProvider();
        PROFILE_ROLE.setSearchValuesProvider(roleSearchValuesProvider);
    }


	public ClientProfileSearch(boolean withValues, ConfigParameterSet configParameterSet)
    throws Exception {
        super(configParameterSet);
    }


	@Override
	protected List<ProfileCustomField> getProfileCustomFields() throws Exception {
		Collection<ProfileCustomField> customFields = ProfileCustomFieldModel.getInstance().getAllProfileCustomFields();

		List<ProfileCustomField> resultList = new ArrayList<>(customFields.size());

		// add ProfileCustomFieldGroups which are needed for comparison
		ProfileCustomFieldGroupModel groupModel = ProfileCustomFieldGroupModel.getInstance();
		for (ProfileCustomField customField : customFields) {
			// clone to avoid impact to entities in the model
			customField = customField.clone();

			// add group which is necessary for sorting and building the path (directories) and labels
			ProfileCustomFieldGroup group = groupModel.getProfileCustomFieldGroup( customField.getGroupPK() );
			customField.setProfileCustomFieldGroup(group);
			customField.getProfileCustomFieldSettings().withProfileCustomFieldGroup = true;

			resultList.add(customField);
		}

		return resultList;
	}

}
