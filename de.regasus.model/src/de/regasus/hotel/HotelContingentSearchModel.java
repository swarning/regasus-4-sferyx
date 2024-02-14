package de.regasus.hotel;

import static de.regasus.LookupService.getHotelContingentMgr;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.lambdalogic.messeinfo.hotel.data.HotelContingentVO;
import com.lambdalogic.messeinfo.kernel.sql.SQLParameter;
import com.lambdalogic.util.model.ModelEvent;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.model.Activator;
import de.regasus.core.model.MIListModel;


public class HotelContingentSearchModel extends MIListModel<HotelContingentVO> {

	private static HotelContingentSearchModel singleton = null;

	private Long eventPK = null;
	
	
	private List<SQLParameter> sqlParameterList = null;

	/**
	 * Number of records that a search result should contain at most. 
	 */
	private Integer resultCountLimit;
	


	private HotelContingentSearchModel() {
		super();
	}


	public static HotelContingentSearchModel getInstance() {
		if (singleton == null) {
			singleton = new HotelContingentSearchModel();
		}
		return singleton;
	}


	/**
	 * Such an individual instance is used within wizards and dialogs, which should show a 
	 * temporary selection of entities that is distinct from that in the workbench.
	 */
	public static HotelContingentSearchModel getDetachedInstance() {
		return new HotelContingentSearchModel();
	}


	@Override
	protected List<HotelContingentVO> getModelDataFromServer() {
		List<HotelContingentVO> hotelContingentVOs = null;
		
		try {
			if (sqlParameterList != null) {
				hotelContingentVOs = getHotelContingentMgr().searchHotelContingent(
					sqlParameterList,
					resultCountLimit
				);
			}
			else {
				hotelContingentVOs = Collections.emptyList();
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
		
		return hotelContingentVOs;
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
		
		try {
			refresh();
		}
		catch (Throwable t) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t);
		}
	}
	

	public void setSqlParameterList(List<SQLParameter> sqlParameterList) {
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


	public void setResultCountLimit(Integer limit) {
		this.resultCountLimit = limit;
	}

}
