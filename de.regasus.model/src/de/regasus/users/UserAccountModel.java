package de.regasus.users;

import static de.regasus.LookupService.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.lambdalogic.i18n.I18NMessageException;
import com.lambdalogic.i18n.I18NString;
import com.lambdalogic.messeinfo.account.data.AccessControlEntryCVO;
import com.lambdalogic.messeinfo.account.data.AccessControlEntryCVOSettings;
import com.lambdalogic.messeinfo.account.data.AccessControlEntryVO;
import com.lambdalogic.messeinfo.account.data.UserAccountCVO;
import com.lambdalogic.messeinfo.account.data.UserAccountCVOSettings;
import com.lambdalogic.messeinfo.account.data.UserAccountVO;
import com.lambdalogic.messeinfo.account.data.UserGroupVO;
import com.lambdalogic.messeinfo.account.interfaces.IUserManager;
import com.lambdalogic.messeinfo.exception.WarnMessageException;
import com.lambdalogic.messeinfo.kernel.data.AbstractCVO;
import com.lambdalogic.messeinfo.kernel.data.AbstractVO;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.model.CacheModelOperation;

import de.regasus.core.ServerModel;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.model.Activator;
import de.regasus.core.model.MICacheModel;

public class UserAccountModel extends MICacheModel<Long, UserAccountCVO>{

	private static final UserAccountCVOSettings USER_ACCOUNT_SETTINGS = new UserAccountCVOSettings();
	static {
    	USER_ACCOUNT_SETTINGS.withUserGroups = true;
	}


	private static final AccessControlEntryCVOSettings ACL_SETTINGS = new AccessControlEntryCVOSettings();
	static {
		ACL_SETTINGS.withConstraintNames = true;
	}


	private static UserAccountModel singleton = null;

	// models
	private UserGroupModel userGroupModel;


	public static UserAccountModel getInstance() {
		if (singleton == null) {
			singleton = new UserAccountModel();
			singleton.initModels();
		}
		return singleton;
	}


	private UserAccountModel() {
	}


	/**
	 * Initialize references to other Models.
	 * Models are initialized outside the constructor to avoid OutOfMemoryErrors when two Models
	 * reference each other.
	 * This happens because the variable is set after the constructor is finished.
	 * If the constructor calls getInstance() of another Model that calls getInstance() of this Model,
	 * the variable instance is still null. So this Model would be created again and so on.
	 * To avoid this, the constructor has to finish before calling getInstance() of another Model.
	 * The initialization of references to other Models is done in getInstance() right after
	 * the constructor has finished.
	 */
	private void initModels() {
		userGroupModel = UserGroupModel.getInstance();
		userGroupModel.addListener(groupModelListener);
	}


	private CacheModelListener<String> groupModelListener = new CacheModelListener<String>() {
		@Override
		public void dataChange(CacheModelEvent<String> event) {
			if (!serverModel.isLoggedIn()) {
				return;
			}

			try {
				if (   event.getOperation() == CacheModelOperation.REFRESH
					|| event.getOperation() == CacheModelOperation.UPDATE
					|| event.getOperation() == CacheModelOperation.DELETE
				) {
					List<String> groupIDs = event.getKeyList();

					// collect PKs of User Accounts that belong to one of the changed groups
					Collection<UserAccountCVO> loadedUsers = getLoadedAndCachedEntities();
					List<Long> dirtyUserPKs = new ArrayList<>( loadedUsers.size() );
					for (UserAccountCVO userAccountCVO : loadedUsers) {
						if ( userAccountCVO.isIsAnyGroup(groupIDs) ) {
							dirtyUserPKs.add( userAccountCVO.getId() );
						}
					}

					refresh(dirtyUserPKs);
				}
			}
			catch (Exception e) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			}
		}
	};


	@Override
	protected UserAccountCVO getEntityFromServer(Long userAccountPK) throws Exception {
		IUserManager userManager = getUserMgr();
		UserAccountCVO userAccountCVO = userManager.getUserAccountCVO(userAccountPK, USER_ACCOUNT_SETTINGS);
		return userAccountCVO;
	}


	@Override
	protected boolean isForeignKeySupported() {
		return false;
	}


	@Override
	protected Long getKey(UserAccountCVO userAccountVO) {
		return userAccountVO.getVO().getID();
	}


	@Override
	protected List<UserAccountCVO> getAllEntitiesFromServer() throws Exception {
		IUserManager userManager = getUserMgr();
		List<UserAccountCVO> userAccountCVOs = userManager.getUserAccountCVOs(USER_ACCOUNT_SETTINGS);
		return userAccountCVOs;
	}


	public  Collection<UserAccountCVO> getAllUserAccountCVOs() throws Exception {
		return getAllEntities();
	}


	public UserAccountCVO getUserAccountCVO(Long pk) throws Exception {
		return getEntity(pk);
	}


	@Override
	public UserAccountCVO create(UserAccountCVO userAccountCVO) throws Exception {
		userAccountCVO = super.create(userAccountCVO);
		return userAccountCVO;
	}


	@Override
	public UserAccountCVO update(UserAccountCVO userAccountCVO) throws Exception {
		userAccountCVO = super.update(userAccountCVO);
		return userAccountCVO;
	}


	@Override
	protected UserAccountCVO createEntityOnServer(UserAccountCVO userAccountCVO) throws Exception {
		UserAccountVO userAccountVO = userAccountCVO.getVO();
		userAccountVO.validate();

		List<UserGroupVO> userGroupVOs = userAccountCVO.getUserGroupVOs();

		String[] userGroups;
		if (userGroupVOs != null) {
			userGroups = AbstractVO.getPKs(userGroupVOs).toArray(new String[0]);
		}
		else {
			userGroups = new String[0];
		}

		IUserManager userManager = getUserMgr();
		userAccountVO = userManager.createUserAccount(userAccountVO, userGroups );
		userAccountCVO.setVO(userAccountVO);
		return userAccountCVO;
	}


	@Override
	protected UserAccountCVO updateEntityOnServer(UserAccountCVO userAccountCVO) throws Exception {
		/* This method MUST return an updated COPY of its parameter.
		 * To return the same UserAccountCVO with an updated UserAccountVO is not sufficient!
		 * Therefore we have to clone the UserAccountCVO.
		 */

		IUserManager userManager = getUserMgr();

		// to remember a warn message to avoid showing the same message multiple times
		I18NString warnMessage = null;

		// Updating the VO
		UserAccountVO userAccountVO = userAccountCVO.getVO();
		userAccountVO.validate();

		/* If the current user has edited itself: store the password.
		 * If the user set a new password this is the original password he typed in (not a hash), otherwise a hash.
		 */
		String newPassword = null;
		// check if the current user edited itself
		if (ServerModel.getInstance().getUser().equals(userAccountCVO.getUserAccountVO().getUserID())) {
			// the current user edited itself

			// check if the password has changed
			UserAccountCVO oldUserAccountCVO = getUserAccountCVO(userAccountCVO.getPK());
			String oldEncryptedPassword = oldUserAccountCVO.getVO().getPassword();

			if ( ! oldEncryptedPassword.equals( userAccountCVO.getUserAccountVO().getPassword() )) {
				newPassword = userAccountCVO.getUserAccountVO().getPassword();
			}
		}

		try {
			userManager.updateUserAccount(userAccountVO);
		}
		catch (WarnMessageException e) {
			warnMessage = e.getI18NMessage();
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}

		// for the case, that the password changed, the new password must to be set as client password in ServerModel
		if (newPassword != null) {
			ServerModel.getInstance().setClientPassword(newPassword);
		}


		UserAccountVO newUserAccountVO = userManager.getUserAccountVO(userAccountVO.getID());

		// clone userAccountCVO without the included UserAccountVO (performance)
		UserAccountVO oldUserAccountVO = userAccountCVO.getUserAccountVO();
		userAccountCVO.setUserAccountVO(null);
		UserAccountCVO newUserAccountCVO = userAccountCVO.clone();
		userAccountCVO.setUserAccountVO(oldUserAccountVO);

		newUserAccountCVO.setVO(newUserAccountVO);

		// Updating the Groups
		List<UserGroupVO> userGroupVOs = userAccountCVO.getUserGroupVOs();
		String[] userGroups = AbstractVO.getPKs(userGroupVOs).toArray(new String[0]);

		try {
			userManager.setGroupsForUserAccount(userAccountVO.getID(), userGroups);
		}
		catch (WarnMessageException e) {
			if (warnMessage != null && !warnMessage.equals(e.getI18NMessage())) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			}
		}

		return newUserAccountCVO;
	}


	public void deleteByPK(Long pk) throws Exception {
		UserAccountCVO entity = getEntity(pk);
		delete(entity);
	}


	@Override
	protected void deleteEntityOnServer(UserAccountCVO entity) throws Exception {
		try {
			getUserMgr().deleteUserAccount(entity.getPK());
		}
		catch (WarnMessageException e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	public void addGroupToUserAccounts(List<UserAccountVO> userAccountList, String[] userGroupIDs) throws Exception {
		List<String> userIDs = new ArrayList<>();
		for (UserAccountVO userAccountVO : userAccountList) {
			userIDs.add(userAccountVO.getUserID());
		}

		try {
			getUserMgr().addGroupsToUserAccounts(userIDs, userGroupIDs);
		}
		catch (WarnMessageException e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}

		super.refresh(AbstractVO.getPKs(userAccountList));
	}


	public void addGroupsToUserAccounts(List<UserAccountVO> userAccountList, String[] userGroupIDs) throws Exception {
		List<String> userIDs = new ArrayList<>();
		for (UserAccountVO userAccountVO : userAccountList) {
			userIDs.add(userAccountVO.getUserID());
		}

		try {
			getUserMgr().addGroupsToUserAccounts(userIDs, userGroupIDs);
		}
		catch (WarnMessageException e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}

		super.refresh(AbstractVO.getPKs(userAccountList));
	}


	public void removeGroupFromUserAccounts(List<UserAccountVO> userAccountList, String[] userGroupIDs) throws Exception {
		List<String> userIDs = new ArrayList<>();
		for (UserAccountVO userAccountVO : userAccountList) {
			userIDs.add(userAccountVO.getUserID());
		}

		try {
			getUserMgr().removeGroupsFromUserAccounts(userIDs, userGroupIDs);
		}
		catch (WarnMessageException e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}

		super.refresh(AbstractVO.getPKs(userAccountList));
	}


	public List<AccessControlEntryCVO> getACLCVOsForUserAccount(UserAccountCVO accountCVO) throws I18NMessageException {
		IUserManager userManager = getUserMgr();

		String userID = accountCVO.getVO().getUserID();
		List<AccessControlEntryCVO> acl = null;
		if (userID != null) {
			acl = userManager.getAccessControlEntryCVOs(userID, ACL_SETTINGS);
		}
		else {
			acl = new ArrayList<>();
		}
		return acl;
	}


	public void setACLCVOsForUserAccount(UserAccountCVO accountCVO, List<AccessControlEntryCVO> aceCVOs) throws Exception {
		List<AccessControlEntryVO> aceVOs = null;

		if (aceCVOs == null) {
			aceVOs = Collections.emptyList();
		}
		else {
			aceVOs = AbstractCVO.getVOs(aceCVOs);
		}

		String userID = accountCVO.getVO().getUserID();


		List<AccessControlEntryVO> subjectsAceVOs = new ArrayList<>();
		for (AccessControlEntryVO aceVO : aceVOs) {
			if (aceVO.getSubject().equals(userID)) {
				subjectsAceVOs.add(aceVO);
			}
		}

		try {
			getUserMgr().setACLForSubject(userID, subjectsAceVOs);
		}
		catch (WarnMessageException e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}

}
