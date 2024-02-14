package de.regasus.profile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

import com.lambdalogic.messeinfo.kernel.sql.SearchValuesProvider;
import com.lambdalogic.messeinfo.profile.ProfileRole;
import com.lambdalogic.messeinfo.profile.ProfileRoleComparator;
import com.lambdalogic.util.MapHelper;


public class ProfileRoleSearchValuesProvider implements SearchValuesProvider {

	@Override
	public LinkedHashMap<Long, String> getValues() throws Exception {
		ProfileRoleModel prModel = ProfileRoleModel.getInstance();
		List<ProfileRole> profileRoles = new ArrayList<>( prModel.getAllProfileRoles() );

		Collections.sort(profileRoles, ProfileRoleComparator.getInstance());

		LinkedHashMap<Long, String> profileRoleMap = MapHelper.createLinkedHashMap(profileRoles.size());
		for (ProfileRole profileRole : profileRoles) {
			profileRoleMap.put(profileRole.getID(), profileRole.getName());
		}

		return profileRoleMap;
	}

}
