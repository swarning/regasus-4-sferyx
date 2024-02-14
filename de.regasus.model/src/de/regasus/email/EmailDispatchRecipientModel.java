package de.regasus.email;
import static de.regasus.LookupService.*;

import java.util.Collection;
import java.util.List;

import com.lambdalogic.messeinfo.email.EmailDispatch;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.model.CacheModelOperation;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.model.MICacheModel;
import de.regasus.model.Activator;
import de.regasus.participant.ParticipantModel;


/**
 * Model for EmailDispatch with the recipient as foreign key.
 * This model is observing the ParticipantModel.
 * This model and EmailDispatchModel are Co-Models for the same entity!
 */
public class EmailDispatchRecipientModel 
extends MICacheModel<Long, EmailDispatch> 
implements CacheModelListener<Long> {
	
	private static EmailDispatchRecipientModel singleton = null;

	private ParticipantModel participantModel;
	private EmailDispatchOrderModel emailDispatchOrderModel;
	private EmailDispatchModel emailDispatchModel;
	
	
	private EmailDispatchRecipientModel() {
	}


	public static EmailDispatchRecipientModel getInstance() {
		if (singleton == null) {
			singleton = new EmailDispatchRecipientModel();
			singleton.initModels();
		}
		return singleton;
	}
	
	
	private void initModels() {
		// This model and EmailDispatchModel are Co-Models for the same entity!
		emailDispatchModel = EmailDispatchModel.getInstance();
		emailDispatchModel.addCoModelListener(this);
		
		participantModel = ParticipantModel.getInstance();
		participantModel.addListener(this);

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
		return emailDispatch.getAbstractPersonPK();
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
		Long abstractPersonPK = (Long) foreignKey;
		
		List<Long> idList = getEmailDispatchMgr().getEmailDispatchIDsByRecipient(abstractPersonPK);
		
		// Then use the cacheModel mechanisms for loading the EmailDispatches
		return getEmailDispatches(idList);
	}
	
	
	public List<EmailDispatch> getEmailDispatchesByRecipient(Long abstractPersonPK) throws Exception {
		return getEntityListByForeignKey(abstractPersonPK);
	}
		
	
	@Override
	public void dataChange(CacheModelEvent<Long> event) {
		if (!serverModel.isLoggedIn()) {
			return;
		}

		try {
			if (event.getSource() == participantModel && event.getOperation() == CacheModelOperation.DELETE) {
				for (Long abstractPersonPK : event.getKeyList()) {
					refreshEntitiesOfForeignKey(abstractPersonPK);
				}
			}
			else if (event.getSource() == emailDispatchOrderModel && event.getOperation() == CacheModelOperation.CREATE) {
				/* Newly created Email Dispatch Orders might already contain Email Dispatches. If one of this Email 
				 * Dispatches belong to a Participant that is a foreign key managed by this model, its data has to be 
				 * loaded.
				 * The only thing to do is calling 
				 * emailDispatchModel.getEmailDispatchesByEmailDispatchOrder(emailDispatchOrderID). Because the 
				 * EmailDispatchModel is a Co-Model of this model loading the entities there will cause to synchronize
				 * both models. Finally the entities will be in this model and even a CacheModelEvent will be fired.
				 */
				for (Long emailDispatchOrderID : event.getKeyList()) {
					emailDispatchModel.getEmailDispatchesByEmailDispatchOrder(emailDispatchOrderID);
				}
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}
	
}
