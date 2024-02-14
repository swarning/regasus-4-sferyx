package de.regasus.users;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

import com.lambdalogic.messeinfo.account.data.UserGroupVO;
import com.lambdalogic.messeinfo.kernel.data.AbstractVO_ID_EditTime_Comparator;
import com.lambdalogic.messeinfo.kernel.sql.SearchValuesProvider;
import com.lambdalogic.util.MapHelper;

public class GroupSearchValuesProvider implements SearchValuesProvider {

	@Override
	public LinkedHashMap<String, String> getValues() throws Exception {

		List<UserGroupVO> allUserGroupVOs = new ArrayList<>( UserGroupModel.getInstance().getAllUserGroupVOs() );
		Collections.sort(allUserGroupVOs, AbstractVO_ID_EditTime_Comparator.getInstance());

		LinkedHashMap<String, String> userGroupMap = MapHelper.createLinkedHashMap(allUserGroupVOs.size());
		for (UserGroupVO userGroupVO : allUserGroupVOs) {
			String pk = userGroupVO.getPK();
			/* Looks strange, but is a consequence of the fact that the key is at the same time
			 * that what is to be shown.
			 */
			userGroupMap.put(pk, pk);
		}

		return userGroupMap;
	}

}
