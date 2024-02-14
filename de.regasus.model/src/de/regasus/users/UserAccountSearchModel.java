package de.regasus.users;

import static de.regasus.LookupService.getUserMgr;

import java.util.ArrayList;
import java.util.List;

import com.lambdalogic.messeinfo.account.data.UserAccountVO;
import com.lambdalogic.messeinfo.kernel.sql.SQLParameter;
import com.lambdalogic.util.model.ModelEvent;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.model.Activator;
import de.regasus.core.model.MIListModel;

public class UserAccountSearchModel extends MIListModel<UserAccountVO> {

	private static UserAccountSearchModel singleton = null;

	private List<SQLParameter> sqlParameterList = null;


	private UserAccountSearchModel() {
		super();
	}


	public static UserAccountSearchModel getInstance() {
		if (singleton == null) {
			singleton = new UserAccountSearchModel();
		}
		return singleton;
	}


	/**
	 * Such an individual instance is used within wizards and dialogs, which should show a temporary selection of
	 * profiles that is distinct from that in the workbench.
	 */
	public static UserAccountSearchModel getDetachedInstance() {
		return new UserAccountSearchModel();
	}


	@Override
	protected List<UserAccountVO> getModelDataFromServer() {
		List<UserAccountVO> userAccountSearchDataList = null;
		try {
			if (sqlParameterList != null) {
				userAccountSearchDataList = getUserMgr().searchUserAccount(sqlParameterList);
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
		return userAccountSearchDataList;
	}


	@Override
	public void dataChange(ModelEvent event) {
		if (!serverModel.isLoggedIn()) {
			sqlParameterList = null;
		}

		super.dataChange(event);
	}



	public List<SQLParameter> getSqlParameterList() {
		return sqlParameterList;
	}


	public void setSqlParameterList(List<SQLParameter> sqlParameterList) {
		// copy list of SQLParameters but ignore inactive ones
		if (sqlParameterList == null) {
			this.sqlParameterList = null;
		}
		else {
			this.sqlParameterList = new ArrayList<>(sqlParameterList.size());
			for (SQLParameter sqlParameter : sqlParameterList) {
				if (sqlParameter != null && sqlParameter.isActive()) {
					this.sqlParameterList.add(sqlParameter);
				}
			}
		}


		try {
			refresh();
		}
		catch (Throwable t) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t);
		}
	}

}
