package de.regasus.users;

import static de.regasus.LookupService.getUserMgr;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.lambdalogic.messeinfo.account.data.UserAccountCVO;
import com.lambdalogic.messeinfo.account.data.UserGroupVO;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.model.Model;
import com.lambdalogic.util.model.ModelEvent;
import com.lambdalogic.util.model.ModelListener;

import de.regasus.core.ServerModel;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.model.Activator;
import de.regasus.core.model.ServerModelEvent;
import de.regasus.core.model.ServerModelEventType;


public class CurrentUserModel extends Model<CurrentUserModelData> {
	private static Logger log = Logger.getLogger("model.CurrentUserModel");

	public static final String PORTAL_EXPERT_GROUP = "Portal Expert";


	private static CurrentUserModel singleton = null;


	protected ServerModel serverModel;
	protected UserAccountModel userAccountModel;


	private CurrentUserModel() {
		serverModel = ServerModel.getInstance();
		serverModel.addListener(serverModelListener);

		userAccountModel = UserAccountModel.getInstance();
		userAccountModel.addListener(userAccountModelListener);
	}


	public static CurrentUserModel getInstance() {
		if (singleton == null) {
			singleton = new CurrentUserModel();
		}
		return singleton;
	}


	@Override
	protected CurrentUserModelData getModelDataFromServer() {
		String userName = serverModel.getUser();

		List<UserGroupVO> userGroupVOs = getUserMgr().getCurrentUserGroupVOs();
		List<String> userGroups = new ArrayList<>( userGroupVOs.size() );
		for (UserGroupVO userGroupVO : userGroupVOs) {
			userGroups.add( userGroupVO.getGroupID() );
		}

		CurrentUserModelData modelData = new CurrentUserModelData();
		modelData.setUserName(userName);
		modelData.setUserGroups(userGroups);

		return modelData;
	}


	private ModelListener serverModelListener = new ModelListener() {
		@Override
		public void dataChange(ModelEvent event) {
			try {
				ServerModelEvent serverModelEvent = (ServerModelEvent) event;

				if (!serverModel.isShutdown()
					&&
					(
						serverModelEvent.getType() == ServerModelEventType.REFRESH ||
						serverModelEvent.getType() == ServerModelEventType.LOGIN ||
						serverModelEvent.getType() == ServerModelEventType.LOGOUT
						)
					) {
					refresh();
				}
			}
			catch (Exception e) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			}
		}
	};


	private CacheModelListener<Long> userAccountModelListener = new CacheModelListener<Long>() {
		@Override
		public void dataChange(CacheModelEvent<Long> event) throws Exception {
			String currentUserName = getModelData().getUserName();
			for (Long userAccountPK : event.getKeyList()) {
				UserAccountCVO userAccountCVO = userAccountModel.getUserAccountCVO(userAccountPK);
    			if ( userAccountCVO.getVO().getUserID().equals(currentUserName) ) {
    				refresh();
    			}
			}
		}
	};


    public boolean isAdmin() throws Exception {
    	CurrentUserModelData modelData = getModelData();
    	String userName = modelData.getUserName();
		return userName != null && userName.equals("admin");
    }


	public boolean isPortalExpert() throws Exception {
		boolean isPortalExpert = false;

		CurrentUserModelData modelData = getModelData();
		if (modelData != null) {
			isPortalExpert = modelData.getUserGroups().contains(PORTAL_EXPERT_GROUP);
		}

		return isPortalExpert;
	}

}
