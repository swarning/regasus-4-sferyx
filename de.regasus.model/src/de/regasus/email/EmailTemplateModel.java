package de.regasus.email;

import static com.lambdalogic.util.CollectionsHelper.createArrayList;
import static de.regasus.LookupService.*;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.lambdalogic.messeinfo.email.EmailTemplate;
import com.lambdalogic.messeinfo.email.EmailTemplateSystemRole;
import com.lambdalogic.messeinfo.kernel.AbstractEntity;
import com.lambdalogic.messeinfo.regasus.RegistrationFormConfig;
import com.lambdalogic.util.exception.ErrorMessageException;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.model.CacheModelOperation;
import com.lambdalogic.util.model.CoModelEvent;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.model.MICacheModel;
import de.regasus.event.EventModel;
import de.regasus.model.Activator;
import de.regasus.onlineform.RegistrationFormConfigModel;

/**
 * Model for managing {@link EmailTemplate}.
 *
 * This implementation uses the support of normal and extended entities in a different way than
 * originally intended. Actually both versions are valid entities where the extended version
 * contains additional but optional data.
 *
 * Here, the standard version is used for search results that are incomplete entities and cannot be
 * used for persistence operations.
 */
public class EmailTemplateModel
extends MICacheModel<Long, EmailTemplate>
implements CacheModelListener<Long> {

	private static EmailTemplateModel singleton = null;

	private static Long NULL_FOREIGN_KEY = 0L;

	private EventModel eventModel = EventModel.getInstance();


	public static EmailTemplateModel getInstance() {
		if (singleton == null) {
			singleton = new EmailTemplateModel();
			singleton.init();
		}
		return singleton;
	}


	private EmailTemplateModel() {
	}


	private void init() {
		eventModel.addListener(this);
	}



	@Override
	protected boolean isExtended(EmailTemplate entity) {
		return !entity.isSearchResult();
	}


	@Override
	protected boolean isForeignKeySupported() {
		return true;
	}


	@Override
	protected Long getKey(EmailTemplate entity) {
		return entity.getID();
	}


	@Override
	protected Object getForeignKey(EmailTemplate emailTemplate) {
		Long fk = null;

		if (emailTemplate != null) {
			fk = emailTemplate.getEventPK();
			if (fk == null) {
				fk = NULL_FOREIGN_KEY;
			}
		}

		return fk;
	}


	@Override
	protected EmailTemplate getEntityFromServer(Long emailTemplateID) throws Exception {
		// return entity as search result
		EmailTemplate emailTemplate = getEmailTemplateMgr().getEmailTemplateSearchData(emailTemplateID);
		return emailTemplate;
	}


	@Override
	protected List<EmailTemplate> getEntitiesFromServer(Collection<Long> emailTemplateIDs) throws Exception {
		// return entities as search result
		List<EmailTemplate> emailTemplateList = getEmailTemplateMgr().getEmailTemplateSearchData(emailTemplateIDs);
		return emailTemplateList;
	}


	@Override
	protected EmailTemplate getExtendedEntityFromServer(Long emailTemplateID) throws Exception {
		// return the standard entity, nothing is extended
		EmailTemplate emailTemplate = getEmailTemplateMgr().find(emailTemplateID);
		return emailTemplate;
	}


	@Override
	protected List<EmailTemplate> getExtendedEntitiesFromServer(List<Long> emailTemplateIDs) throws Exception {
		// return the standard entities, nothing is extended
		List<EmailTemplate> emailTemplateList = getEmailTemplateMgr().findByPKs(emailTemplateIDs);
		return emailTemplateList;
	}


	@Override
	protected List<EmailTemplate> getEntitiesByForeignKeyFromServer(Object foreignKey) throws Exception {
		Long eventPK = (Long) foreignKey;
		if (eventPK == NULL_FOREIGN_KEY) {
			eventPK = null;
		}

		List<EmailTemplate> emailTemplateSearchDataList = getEmailTemplateMgr().getEmailTemplateSearchDataByEvent(
			eventPK
		);

		return emailTemplateSearchDataList;
	}


	public EmailTemplate getEmailTemplateSearchData(Long id) throws Exception {
		return super.getEntity(id);
	}


	public List<EmailTemplate> getEmailTemplateSearchDatas(List<Long> idList) throws Exception {
		return super.getEntities(idList);
	}


	public List<EmailTemplate> getEmailTemplateSearchDataByEvent(Long eventPK) throws Exception {
		if (eventPK == null) {
			eventPK = NULL_FOREIGN_KEY;
		}
		List<EmailTemplate> entityList = getEntityListByForeignKey(eventPK);

		return entityList;
	}


	public List<EmailTemplate> getEmailTemplateSearchDataByEvent(Long eventPK, EmailTemplateSystemRole systemRole)
	throws Exception {
		List<EmailTemplate> allEmailTemplateList = getEmailTemplateSearchDataByEvent(eventPK);

		// copy EmailTemplates with same systemRole
		List<EmailTemplate> emailTemplateList = createArrayList(allEmailTemplateList.size());
		for (EmailTemplate emailTemplate : allEmailTemplateList) {
			if (systemRole == null && emailTemplate.getSystemRole() == null
				||
				systemRole == emailTemplate.getSystemRole()
			) {
				emailTemplateList.add(emailTemplate);
			}
		}

		return emailTemplateList;
	}


	public EmailTemplate getEmailTemplate(Long id) throws Exception {
		return super.getExtendedEntity(id);
	}


	public List<EmailTemplate> getEmailTemplates(List<Long> idList) throws Exception {
		return super.getExtendedEntities(idList);
	}


	public void delete(Long emailTemplateID, boolean force) throws Exception {
		delete(Collections.singletonList(emailTemplateID), force);
	}


	/**
	 * Delete the EmailTemplates whose IDs are given. If force is <code>false</code>, this happens
	 * only in case they all have no email dispatch order. If force is  <code>true</code>, the
	 * related email dispatch orders and dispatches are deleted as well.
	 * <p>
	 * Since the deletion involves the additional force attribute, we cannot just delegate
	 * to the super.delete() method.
	 */
	public void delete(List<Long> emailTemplateIDs, boolean force) throws Exception {
		if (serverModel.isLoggedIn()) {
			if (emailTemplateIDs != null) {
				getEmailTemplateMgr().delete(emailTemplateIDs, force);
				handleDeleteByKeyList(
					emailTemplateIDs,
					true	// fireCoModelEvent
				);
			}
		}
	}


	public void refreshForeignKey(Long eventPK) throws Exception {
		if (eventPK == null) {
			eventPK = NULL_FOREIGN_KEY;
		}
		super.refreshForeignKey(eventPK);
	}




	public void refreshEntitiesOfForeignKey(Long eventPK) throws Exception {
		if (eventPK == null) {
			eventPK = NULL_FOREIGN_KEY;
		}
		super.refreshEntitiesOfForeignKey(eventPK);
	}


	@Override
	protected EmailTemplate createEntityOnServer(EmailTemplate emailTemplate) throws Exception {
		emailTemplate.validate();
		EmailTemplate newEmailTemplate = getEmailTemplateMgr().create(emailTemplate);
		return newEmailTemplate;
	}


	@Override
	public EmailTemplate create(EmailTemplate emailTemplate) throws Exception {
		return super.create(emailTemplate);
	}


	@Override
	protected EmailTemplate updateEntityOnServer(EmailTemplate emailTemplate) throws Exception {
		emailTemplate.validate();
		emailTemplate = getEmailTemplateMgr().update(emailTemplate);

		return emailTemplate;
	}


	@Override
	public EmailTemplate update(EmailTemplate emailTemplate) throws Exception {
		/* The entity that will be updated has to be cloned, because CacheModel does not accept the same instance as in
		 * the cache if the Model supports foreign keys.
		 */
		emailTemplate = emailTemplate.clone();
		return super.update(emailTemplate);
	}


	@Override
	public void dataChange(CacheModelEvent<Long> event) {
		if (!serverModel.isLoggedIn()) {
			return;
		}

		try {
			if (event.getSource() == eventModel) {
				if (event.getOperation() == CacheModelOperation.DELETE) {
					for (Long eventID : event.getKeyList()) {
						refreshEntitiesOfForeignKey(eventID);
					}
				}
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	public void createStandardEmailTemplates(RegistrationFormConfig config) throws ErrorMessageException {
		String webId = config.getWebId();
		String url = RegistrationFormConfigModel.getInstance().getOnlineWebappUrl(webId);

		List<EmailTemplate> emailTemplateList = getRegistrationFormConfigMgr().createStandardEmailTemplates(config, url);

		// List can be empty when all required standard templates already exist
		if (! emailTemplateList.isEmpty()) {
			put(emailTemplateList);
			List<Long> idList = AbstractEntity.getPrimaryKeyList(emailTemplateList);
			try {
				fireCreate(idList);
			}
			catch (Exception e) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			}
		}
	}


	public void copyEmailTemplate(Long emailTemplatePK, Long targtEventPK) throws ErrorMessageException {
		EmailTemplate newEmailTemplate = getEmailTemplateMgr().copyEmailTemplate(emailTemplatePK, targtEventPK);

		put(newEmailTemplate);

		try {
    		fireDataChange(CacheModelOperation.CREATE, newEmailTemplate.getID());
    		fireDataChange(CoModelEvent.createInstance(this, newEmailTemplate));
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}

}
