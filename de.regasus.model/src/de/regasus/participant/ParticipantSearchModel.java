package de.regasus.participant;

import static de.regasus.LookupService.getParticipantMgr;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.lambdalogic.messeinfo.kernel.sql.SQLParameter;
import com.lambdalogic.messeinfo.participant.data.ParticipantSearchData;
import com.lambdalogic.util.model.ModelEvent;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.model.Activator;
import de.regasus.core.model.MIListModel;

public class ParticipantSearchModel extends MIListModel<ParticipantSearchData> {

	private static ParticipantSearchModel singleton = null;

	private static final String language = Locale.getDefault().getLanguage();

	private Long eventPK = null;

	private List<SQLParameter> sqlParameterList = null;
	private String quickSearchInput = null;

	/**
	 * Number of records that a search result should contain at most.
	 */
	private Integer resultCountLimit;



	private ParticipantSearchModel() {
	}


	public static ParticipantSearchModel getInstance() {
		if (singleton == null) {
			singleton = new ParticipantSearchModel();
		}
		return singleton;
	}


	/**
	 * Such an individual instance is used within wizards and dialogs, which should show a temporary selection of
	 * profiles that is distinct from that in the workbench.
	 */
	public static ParticipantSearchModel getDetachedInstance() {
		return new ParticipantSearchModel();
	}


	@Override
	protected List<ParticipantSearchData> getModelDataFromServer() {
		List<ParticipantSearchData> participantSearchDataList = null;
		try {
			if (sqlParameterList != null) {
				participantSearchDataList = getParticipantMgr().searchParticipant(
					eventPK,
					sqlParameterList,
					language,
					resultCountLimit
				);
			}
			else if (quickSearchInput != null && eventPK != null) {
				participantSearchDataList = getParticipantMgr().searchParticipant(eventPK, quickSearchInput, language);
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
		return participantSearchDataList;
	}


	@Override
	public void dataChange(ModelEvent event) {
		if (serverModel.isLoggedIn()) {
			sqlParameterList = null;
		}

		super.dataChange(event);
	}


	public Long getEventPK() {
		return eventPK;
	}


	public void setEventPK(Long eventPK) {
		this.eventPK = eventPK;
		sqlParameterList = null;
		quickSearchInput = null;

		try {
			refresh();
		}
		catch (Throwable t) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t);
		}
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
