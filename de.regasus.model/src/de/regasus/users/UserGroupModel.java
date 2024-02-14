package de.regasus.users;

import static de.regasus.LookupService.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.lambdalogic.i18n.I18NMessageException;
import com.lambdalogic.messeinfo.account.data.AccessControlEntryCVO;
import com.lambdalogic.messeinfo.account.data.AccessControlEntryCVOSettings;
import com.lambdalogic.messeinfo.account.data.AccessControlEntryVO;
import com.lambdalogic.messeinfo.account.data.UserGroupVO;
import com.lambdalogic.messeinfo.account.interfaces.IUserManager;
import com.lambdalogic.messeinfo.kernel.data.AbstractCVO;

import de.regasus.auth.AuthorizationException;
import de.regasus.core.model.MICacheModel;

public class UserGroupModel extends MICacheModel<String, UserGroupVO>{

	private static UserGroupModel singleton = null;

	private AccessControlEntryCVOSettings aclSettings;

	private UserGroupModel() {
		aclSettings = new AccessControlEntryCVOSettings();
		aclSettings.withConstraintNames = true;
	}


	public static UserGroupModel getInstance() {
		if (singleton == null) {
			singleton = new UserGroupModel();
		}
		return singleton;
	}


	@Override
	protected UserGroupVO getEntityFromServer(String userGroupId) throws Exception {
		IUserManager userManager = getUserMgr();
		return userManager.getUserGroupVO(userGroupId);
	}


	@Override
	protected boolean isForeignKeySupported() {
		return false;
	}


	@Override
	protected String getKey(UserGroupVO userAccountVO) {
		return userAccountVO.getPK();
	}

	@Override
	protected List<UserGroupVO> getAllEntitiesFromServer() throws Exception {
		IUserManager userManager = getUserMgr();
		return userManager.getUserGroupVOs();
	}


	public  Collection<UserGroupVO> getAllUserGroupVOs() throws Exception {
		return getAllEntities();
	}


	public UserGroupVO getUserGroupVO(String userGroupId) throws Exception {
		return getEntity(userGroupId);
	}


	@Override
	public UserGroupVO create(UserGroupVO userGroupVO) throws Exception {
		return super.create(userGroupVO);
	}

	@Override
	protected UserGroupVO createEntityOnServer(UserGroupVO userGroupVO) throws Exception {
		IUserManager userManager = getUserMgr();
		UserGroupVO createdUserGroup = userManager.createUserGroup(userGroupVO);
		return createdUserGroup;
	}


	public void delete(UserGroupVO userGroupVO, boolean force) throws Exception {
		if (userGroupVO != null) {
    		getUserMgr().deleteUserGroup(userGroupVO.getGroupID(), force);
    		handleDelete(userGroupVO, true);
		}
	}


	@Override
	public UserGroupVO update(UserGroupVO userGroupVO) throws Exception {
		return super.update(userGroupVO);
	}


	@Override
	protected UserGroupVO updateEntityOnServer(UserGroupVO userGroupVO) throws Exception {
		IUserManager userManager = getUserMgr();
		return userManager.updateUserGroup(userGroupVO);
	}


	public List<AccessControlEntryVO> getACLsForUserGroup(UserGroupVO userGroupVO) throws AuthorizationException {
		IUserManager userManager = getUserMgr();
		return userManager.getACL(userGroupVO.getGroupID());
	}

	public List<AccessControlEntryCVO> getACLCVOsForUserGroup(UserGroupVO userGroupVO) throws I18NMessageException {
		IUserManager userManager = getUserMgr();
		String groupID = userGroupVO.getGroupID();
		List<AccessControlEntryCVO> acl = null;
		if (groupID != null) {
			acl = userManager.getAccessControlEntryCVOs(groupID, aclSettings);
		}
		else {
			acl = new ArrayList<>();
		}
		return acl;
	}


	public void setACLCVOsForUserGroup(UserGroupVO userGroupVO, List<AccessControlEntryCVO> aceCVOs) throws Exception {
		List<AccessControlEntryVO> aceVOs = null;

		if (aceCVOs == null) {
			aceVOs = Collections.emptyList();
		}
		else {
			aceVOs = AbstractCVO.getVOs(aceCVOs);
		}


		IUserManager userManager = getUserMgr();
		String groupID = userGroupVO.getGroupID();


		List<AccessControlEntryVO> subjectsAceVOs = new ArrayList<>();
		for (AccessControlEntryVO aceVO : aceVOs) {
			if (aceVO.getSubject().equals(groupID)) {
				subjectsAceVOs.add(aceVO);
			}
		}
		userManager.setACLForSubject(groupID, subjectsAceVOs);
	}

}
