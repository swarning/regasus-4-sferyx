package de.regasus.profile;

import static de.regasus.LookupService.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.lambdalogic.messeinfo.kernel.sql.SQLParameter;
import com.lambdalogic.messeinfo.profile.Profile;
import com.lambdalogic.util.model.ModelEvent;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.model.Activator;
import de.regasus.core.model.MIListModel;

/**
 * @author manfred
 * 
 */
public class ProfileSearchModel extends MIListModel<Profile> {

	private static ProfileSearchModel singleton = null;

	private static final String language = Locale.getDefault().getLanguage();

	private List<SQLParameter> sqlParameterList = null;
	private String quickSearchInput = null;

	/**
	 * Number of records that a search result should contain at most. 
	 */
	private Integer resultCountLimit;
	

	private ProfileSearchModel() {
		super();
	}


	public static ProfileSearchModel getInstance() {
		if (singleton == null) {
			singleton = new ProfileSearchModel();
		}
		return singleton;
	}


	/**
	 * Such an individual instance is used within wizards and dialogs, which should show a temporary selection of
	 * profiles that is distinct from that in the workbench.
	 */
	public static ProfileSearchModel getDetachedInstance() {
		return new ProfileSearchModel();
	}


	@Override
	protected List<Profile> getModelDataFromServer() {
		List<Profile> profileList = null;
		try {
			if (sqlParameterList != null) {
				profileList = getProfileMgr().findBySQLParameter(
					sqlParameterList, 
					language, 
					resultCountLimit
				);
			}
			else if (quickSearchInput != null) {
				profileList = getProfileMgr().findByQuickSearch(quickSearchInput, language);
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
		return profileList;
	}

	
	@Override
	public void dataChange(ModelEvent event) {
		if (serverModel.isLoggedIn()) {
			sqlParameterList = null;
		}

		super.dataChange(event);
	}


	public List<SQLParameter> getSqlParameterList() {
		return sqlParameterList;
	}


	public void setSqlParameterList(List<SQLParameter> sqlParameterList) {
		quickSearchInput = null;
		
		// copy list of SQLParameters but ignore inactive ones
		if (sqlParameterList == null) {
			this.sqlParameterList = null;
		}
		else {
			this.sqlParameterList = new ArrayList<SQLParameter>(sqlParameterList.size());
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

	
	public void setQuickSearchInput(String quickSearchInput) {
		sqlParameterList = null;
		
		// copy list of SQLParameters but ignore inactive ones
		this.quickSearchInput = quickSearchInput;
		
		try {
			refresh();
		}
		catch (Throwable t) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t);
		}
	}


	public void setResultCountLimit(Integer limit) {
		this.resultCountLimit = limit;
	}

}
