package de.regasus.users;

import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;

import de.regasus.core.ServerModel;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.model.Activator;
import de.regasus.core.model.MIModel;


public class UserAccountSQLFieldsModel extends MIModel<ClientUserAccountSearch> {
	
	private static UserAccountSQLFieldsModel singleton = null;
	
	
	// Models
	private UserGroupModel userGroupModel;
	

	private UserAccountSQLFieldsModel() {
		super();
	}
	
	public static UserAccountSQLFieldsModel getInstance() {
		if (singleton == null) {
			singleton = new UserAccountSQLFieldsModel();
			singleton.init();
		}
		return singleton;
	}
	
	
	private void init() {
		// init Models
		userGroupModel = UserGroupModel.getInstance();
		userGroupModel.addListener(cacheModelListener);
	}
	
	
	@SuppressWarnings("rawtypes")
	private CacheModelListener cacheModelListener = new CacheModelListener() {
		@Override
		public void dataChange(CacheModelEvent event) {
			if (!serverModel.isLoggedIn()) {
				return;
			}
			
			try {
				ClientUserAccountSearch userAccountSearch = getModelData();
				/* userAccountSearch can be null
				 * This may happen if the user logs off and this model
				 * is informed before other models (e.g. languageModel).
				 */
				if (userAccountSearch != null) {
					// Models with global data (independent from Event)
					if (event.getSource() == userGroupModel) {
						userAccountSearch.initGroupValues();
					}
				}
				
			
				fireDataChange();
			}
			catch (Exception e) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			}
		}
	};

	
	@Override
	protected ClientUserAccountSearch getModelDataFromServer() {
		ClientUserAccountSearch userAccountSearch = null;
		try {
			if (ServerModel.getInstance().isLoggedIn()) {
				userAccountSearch = new ClientUserAccountSearch();
				userAccountSearch.initGroupValues();
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
		return userAccountSearch;
	}

}
