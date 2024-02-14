package de.regasus.email;

import static de.regasus.LookupService.getEmailDispatchMgr;

import java.util.Collection;
import java.util.List;

import com.lambdalogic.messeinfo.email.EmailDispatch;
import com.lambdalogic.util.CollectionsHelper;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.model.CacheModelOperation;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.model.MICacheModel;
import de.regasus.model.Activator;


/**
 * Model for EmailDispatch with the Email Dispatch Order as foreign key.
 * This model is observing the EmailDispatchOrderModel.
 * This model and EmailDispatchRecipientModel are Co-Models for the same entity!
 */
public class EmailDispatchModel 
extends MICacheModel<Long, EmailDispatch> 
implements CacheModelListener<Long> {
	
	private static EmailDispatchModel singleton = null;

	private EmailDispatchOrderModel emailDispatchOrderModel;
	
	
	private EmailDispatchModel() {
	}


	public static EmailDispatchModel getInstance() {
		if (singleton == null) {
			singleton = new EmailDispatchModel();
			singleton.initModels();
		}
		return singleton;
	}
	
	
	private void initModels() {
		// This model and EmailDispatchRecipientModel are Co-Models for the same entity!
		EmailDispatchRecipientModel.getInstance().addCoModelListener(this);

		emailDispatchOrderModel = EmailDispatchOrderModel.getInstance();
		emailDispatchOrderModel.addListener(this);
	}
	
	
	@Override
	protected Long getKey(EmailDispatch entity) {
		return entity.getID();
	}


	@Override
	protected boolean isForeignKeySupported() {
		return true;
	}

	
	@Override
	protected Long getForeignKey(EmailDispatch emailDispatch) {
		return emailDispatch.getEmailDispatchOrderPK();
	}
	
	
	@Override
	protected EmailDispatch getEntityFromServer(Long id) throws Exception {
		EmailDispatch emailDispatch = getEmailDispatchMgr().find(id);
		return emailDispatch;
	}
	
	
	public EmailDispatch getEmailDispatch(Long id) throws Exception {
		return super.getEntity(id);
	}
	
	
	@Override
	protected List<EmailDispatch> getEntitiesFromServer(Collection<Long> ids) throws Exception {
		List<EmailDispatch> emailDispatchList = getEmailDispatchMgr().findByPKs(ids);
		return emailDispatchList;
	}


	public List<EmailDispatch> getEmailDispatches(Collection<Long> ids) throws Exception {
		return super.getEntities(ids);
	}
	
	
	@Override
	protected List<EmailDispatch> getEntitiesByForeignKeyFromServer(Object foreignKey) throws Exception {
		// cast foreignKey
		Long emailDispatchOrderID = (Long) foreignKey;
		
		// load data from server
		return getEmailDispatchMgr().getEmailDispatchByEmailDispatchOrder(emailDispatchOrderID);
	}

	
	public List<EmailDispatch> getEmailDispatchesByEmailDispatchOrder(Long emailDispatchOrderID) throws Exception {
		return super.getEntityListByForeignKey(emailDispatchOrderID);
	}
		
	
	@Override
	public void dataChange(CacheModelEvent<Long> event) {
		if (!serverModel.isLoggedIn()) {
			return;
		}

		try {
			if (event.getSource() == emailDispatchOrderModel) {
				// ignore CREATE, because there cannot be a FK listener for new Email Dispatch Order
				if (event.getOperation() == CacheModelOperation.UPDATE ||
					event.getOperation() == CacheModelOperation.DELETE ||
					event.getOperation() == CacheModelOperation.REFRESH
				) {
    				for (Long emailDispatchOrderID : event.getKeyList()) {
    					refreshEntitiesOfForeignKey(emailDispatchOrderID);
    				}
				}
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}

	
	public void cancel(List<Long> emailDispatchIDs)	throws Exception {
		if (CollectionsHelper.notEmpty(emailDispatchIDs)) {
			getEmailDispatchMgr().cancel(emailDispatchIDs);
			refresh(emailDispatchIDs);
		}
	}
	
}
