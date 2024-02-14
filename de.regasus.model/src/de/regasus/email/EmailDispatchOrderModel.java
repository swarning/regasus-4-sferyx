package de.regasus.email;

import static de.regasus.LookupService.getEmailDispatchOrderMgr;

import java.lang.invoke.MethodHandles;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lambdalogic.messeinfo.email.EmailDispatchOrder;
import com.lambdalogic.messeinfo.email.EmailDispatchService;
import com.lambdalogic.messeinfo.email.data.SmtpSettingsVO;
import com.lambdalogic.util.CollectionsHelper;
import com.lambdalogic.util.DateHelper;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.model.CacheModelOperation;

import de.regasus.LookupService;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.model.MICacheModel;
import de.regasus.model.Activator;

public class EmailDispatchOrderModel
extends MICacheModel<Long, EmailDispatchOrder>
implements CacheModelListener<Long> {

	private static final Logger log = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );

	private static EmailDispatchOrderModel singleton = null;

	private EmailTemplateModel emailTemplateModel;


	public static EmailDispatchOrderModel getInstance() {
		if (singleton == null) {
			singleton = new EmailDispatchOrderModel();
		}
		return singleton;
	}


	private EmailDispatchOrderModel() {
		emailTemplateModel = EmailTemplateModel.getInstance();
		emailTemplateModel.addListener(this);
	}


	@Override
	protected Long getKey(EmailDispatchOrder emailDispatchOrder) {
		return emailDispatchOrder.getID();
	}


	@Override
	protected boolean isForeignKeySupported() {
		return true;
	}


	@Override
	protected Object getForeignKey(EmailDispatchOrder emailDispatchOrder) {
		return emailDispatchOrder.getEmailTemplatePK();
	}


	@Override
	protected EmailDispatchOrder getEntityFromServer(Long emailDispatchOrderID) throws Exception {
		EmailDispatchOrder emailTemplate = getEmailDispatchOrderMgr().find(emailDispatchOrderID);
		return emailTemplate;
	}


	public EmailDispatchOrder getEmailDispatchOrder(Long id) throws Exception {
		return super.getEntity(id);
	}


	@Override
	protected List<EmailDispatchOrder> getEntitiesFromServer(Collection<Long> emailDispatchOrderIDs) throws Exception {
		List<EmailDispatchOrder> emailTemplateList = getEmailDispatchOrderMgr().findByPKs(emailDispatchOrderIDs);
		return emailTemplateList;
	}


	public List<EmailDispatchOrder> getEmailDispatchOrders(List<Long> idList) throws Exception {
		return super.getEntities(idList);
	}


	@Override
	protected List<EmailDispatchOrder> getEntitiesByForeignKeyFromServer(Object foreignKey) throws Exception {
		// cast foreignKey
		Long emailTemplateID = (Long) foreignKey;

		// load data from server
		List<EmailDispatchOrder> emailDispatchOrderList = getEmailDispatchOrderMgr().getEmailDispatchOrdersByTemplateID(emailTemplateID);
		return emailDispatchOrderList;
	}


	public List<EmailDispatchOrder> getEmailDispatchOrdersByEmailTemplate(Long emailTemplateID)
	throws Exception {
		return super.getEntityListByForeignKey(emailTemplateID);
	}


	@Override
	public void dataChange(CacheModelEvent<Long> event) {
		if (!serverModel.isLoggedIn()) {
			return;
		}

		try {
			if (event.getSource() == emailTemplateModel) {
				if (event.getOperation() == CacheModelOperation.DELETE) {
					for (Long emailTemplateID : event.getKeyList()) {
						refreshEntitiesOfForeignKey(emailTemplateID);
					}
				}
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	public void cancel(List<Long> emailDispatchOrderIDs) throws Exception {
		if (CollectionsHelper.notEmpty(emailDispatchOrderIDs)) {
			List<EmailDispatchOrder> cancelledDispatchOrders = getEmailDispatchOrderMgr().cancel(emailDispatchOrderIDs);
			put(cancelledDispatchOrders);
			fireUpdate(emailDispatchOrderIDs);
		}
	}


	public EmailDispatchOrder dispatchImmediatelyFromClient(
		SmtpSettingsVO settings,
		Long emailTemplateID,
		List<Long> abstractPersonIds
	)
	throws Exception {
		EmailDispatchOrder emailDispatchOrder = EmailDispatchService.createDispatchOrderAndSendDispatches(
			settings,
			emailTemplateID,
			abstractPersonIds
		);

		put(emailDispatchOrder);
		fireCreate(emailDispatchOrder.getID());
		return emailDispatchOrder;
	}


	public EmailDispatchOrder dispatchImmediatelyOnServer(
		SmtpSettingsVO settings,
		Long emailTemplateID,
		List<Long> abstractPersonIds,
		Map<String, Object> variables
	)
	throws Exception {
		long startTime = System.currentTimeMillis();
		System.out.println("Sending " + abstractPersonIds.size() + " emails");

		EmailDispatchOrder emailDispatchOrder = null;
		try {
			LookupService.setReadTimeout(24 * 60 * 60 * 1000); // 24 hours

			emailDispatchOrder = getEmailDispatchOrderMgr().createDispatchOrderAndDispatchesImmediately(
				settings,
				emailTemplateID,
				abstractPersonIds,
				variables,
				null	// attachments
			);
		}
		finally {
			LookupService.initReadTimeout();

			long duration = System.currentTimeMillis() - startTime;
			System.out.println("Sending " + abstractPersonIds.size() + " emails lasted " + DateHelper.getTimeLagString(duration));
		}


		put(emailDispatchOrder);
		fireCreate(emailDispatchOrder.getID());
		return emailDispatchOrder;
	}


	public EmailDispatchOrder schedule(
		SmtpSettingsVO settings,
		Date scheduledDate,
		Long emailTemplateID,
		List<Long> abstractPersonIds
	)
	throws Exception {
		EmailDispatchOrder emailDispatchOrder = getEmailDispatchOrderMgr().createDispatchOrderAndScheduleDispatches(
			settings,
			scheduledDate,
			emailTemplateID,
			abstractPersonIds
		);

		put(emailDispatchOrder);
		fireCreate(emailDispatchOrder.getID());
		return emailDispatchOrder;
	}

}
