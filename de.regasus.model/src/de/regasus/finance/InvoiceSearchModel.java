package de.regasus.finance;

import static de.regasus.LookupService.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.lambdalogic.messeinfo.invoice.data.AccountancyCVO;
import com.lambdalogic.messeinfo.invoice.data.InvoiceCVO;
import com.lambdalogic.messeinfo.invoice.data.InvoiceVO;
import com.lambdalogic.messeinfo.invoice.interfaces.InvoiceCVOSettings;
import com.lambdalogic.messeinfo.kernel.sql.SQLParameter;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.model.ModelEvent;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.model.Activator;
import de.regasus.core.model.MIListModel;

public class InvoiceSearchModel extends MIListModel<InvoiceCVO> implements CacheModelListener<Long> {

	private static InvoiceSearchModel singleton = null;

	private static InvoiceCVOSettings settings;

	private Long eventPK = null;

	private List<SQLParameter> sqlParameterList = null;

	/**
	 * Number of records that a search result should contain at most.
	 */
	private Integer resultCountLimit;

	// models
	private AccountancyModel accountancyModel;


	private InvoiceSearchModel() {
		super();

		settings = new InvoiceCVOSettings();
		settings.withRecipientInfo = true;
	}


	public static InvoiceSearchModel getInstance() {
		if (singleton == null) {
			singleton = new InvoiceSearchModel();
			singleton.initModels();
		}
		return singleton;
	}


	private void initModels() {
		accountancyModel = AccountancyModel.getInstance();
		accountancyModel.addListener(this);
	}

	/**
	 * Such an individual instance is used within wizards and dialogs, which should show a temporary selection of
	 * invoices that is distinct from that in the workbench.
	 */
	public static InvoiceSearchModel getDetachedInstance() {
		return new InvoiceSearchModel();
	}


	@Override
	protected List<InvoiceCVO> getModelDataFromServer() {
		List<InvoiceCVO> invoiceCVOs = null;
		try {
			if (sqlParameterList != null) {
				invoiceCVOs = getInvoiceMgr().searchInvoiceCVOs(
					sqlParameterList,
					settings,
					resultCountLimit
				);
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
		return invoiceCVOs;
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


	@Override
	public void dataChange(CacheModelEvent<Long> event) {
		if (!serverModel.isLoggedIn()) {
			return;
		}

		try {
			if (event.getSource() == accountancyModel) {
				/* If the model is loaded, replace the InvoiceVOs in the InvoiceCVOs.
				 * This is possible, because the InvoiceCVO doesn't contain InvoicePositionVOs.
				 * If any replacement happened, fire a DataChangeEvent.
				 */
				if (isLoaded() && modelData != null) {
					boolean change = false;

					List<Long> participantIDs = event.getKeyList();
					for (Long participantID : participantIDs) {
						AccountancyCVO accountancyCVO = accountancyModel.getAccountancyCVO(participantID);
						List<InvoiceVO> invoiceVOs = accountancyCVO.getInvoiceVOs();
						HashMap<Long, InvoiceVO> invoiceVOMap = InvoiceVO.abstractVOs2Map(invoiceVOs);

						for (InvoiceCVO invoiceCVO : modelData) {
							InvoiceVO invoiceVO = invoiceVOMap.get(invoiceCVO.getPK());
							if (invoiceVO != null) {
								invoiceCVO.setVO(invoiceVO);
								change = true;
							}
						}
					}


					if (change) {
						fireDataChange();
					}
				}
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	public void setResultCountLimit(Integer limit) {
		this.resultCountLimit = limit;
	}

}
