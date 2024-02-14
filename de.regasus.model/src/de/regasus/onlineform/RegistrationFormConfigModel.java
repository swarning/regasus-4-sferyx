package de.regasus.onlineform;

import static de.regasus.LookupService.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.lambdalogic.messeinfo.regasus.RegistrationFormConfig;
import com.lambdalogic.util.CollectionsHelper;
import com.lambdalogic.util.exception.ErrorMessageException;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.model.CacheModelOperation;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.model.MICacheModel;
import de.regasus.email.EmailTemplateModel;
import de.regasus.event.EventModel;
import de.regasus.model.Activator;


public class RegistrationFormConfigModel
extends MICacheModel<Long, RegistrationFormConfig>
implements CacheModelListener<Long> {

	private static RegistrationFormConfigModel singleton;

	private EventModel eventModel;


	private RegistrationFormConfigModel() {
		super();

		eventModel = EventModel.getInstance();
		eventModel.addListener(this);
	}


	public static RegistrationFormConfigModel getInstance() {
		if (singleton == null) {
			singleton = new RegistrationFormConfigModel();
		}
		return singleton;
	}


	@Override
	protected Long getKey(RegistrationFormConfig entity) {
		return entity.getId();
	}


	@Override
	protected RegistrationFormConfig getEntityFromServer(Long configId) throws Exception {
		RegistrationFormConfig registrationFormConfig = getRegistrationFormConfigMgr().find(configId);
		return registrationFormConfig;
	}


	public RegistrationFormConfig getRegistrationFormConfig(Long configId) throws Exception {
		return super.getEntity(configId);
	}


	@Override
	protected List<RegistrationFormConfig> getEntitiesFromServer(Collection<Long> registrationFormConfigIds)
		throws Exception {
		List<RegistrationFormConfig> registrationFormConfigs =
			getRegistrationFormConfigMgr().findByPKs(registrationFormConfigIds);
		return registrationFormConfigs;
	}


	public List<RegistrationFormConfig> getRegistrationFormConfigs(Collection<Long> registrationFormConfigIds)
		throws Exception {
		return super.getEntities(registrationFormConfigIds);
	}


	@Override
	protected RegistrationFormConfig createEntityOnServer(RegistrationFormConfig entity) throws Exception {

		entity.validate();

		String webId = entity.getWebId();
		String url = getOnlineWebappUrl(webId);

		RegistrationFormConfig createdEntity = getRegistrationFormConfigMgr().create(entity, true, url);

		return createdEntity;
	}


	@Override
	public RegistrationFormConfig create(
		RegistrationFormConfig registrationFormConfig) throws Exception {

		RegistrationFormConfig config = super.create(registrationFormConfig);

		EmailTemplateModel.getInstance().refreshForeignKey(config.getEventPK());

		return config;
	}


	@Override
	protected RegistrationFormConfig updateEntityOnServer(RegistrationFormConfig registrationFormConfig)
		throws Exception {
		registrationFormConfig.validate();
		RegistrationFormConfig updatedEntity =
			getRegistrationFormConfigMgr().update(registrationFormConfig);
		return updatedEntity;
	}


	@Override
	public RegistrationFormConfig update(RegistrationFormConfig registrationFormConfig) throws Exception {
		return super.update(registrationFormConfig);
	}


	@Override
	protected void deleteEntityOnServer(RegistrationFormConfig registrationFormConfig) throws Exception {
		// Check if this rfConfig was the last one of its event
		getRegistrationFormConfigMgr().delete(registrationFormConfig);
	}


	@Override
	public void delete(RegistrationFormConfig registrationFormConfig) throws Exception {
		super.delete(registrationFormConfig);

		getFormProgrammePointTypeConfigMgr().deleteFormProgrammePointTypeConfigByFormId(registrationFormConfig.getId());
		getDataStoreMgr().deleteWithPathLike("/regasus/" + registrationFormConfig);
	}


	@Override
	protected boolean isForeignKeySupported() {
		return true;
	}


	@Override
	protected Object getForeignKey(RegistrationFormConfig registrationFormConfig) {
		Long fk = null;
		if (registrationFormConfig != null) {
			fk = registrationFormConfig.getEventPK();
		}
		return fk;
	}


	@Override
	protected List<RegistrationFormConfig> getEntitiesByForeignKeyFromServer(Object foreignKey) throws Exception {

		Long eventId = null;
		if (foreignKey instanceof Long) {
			eventId = (Long) foreignKey;
		}
		if (foreignKey instanceof Long) {
			Long eventPK = (Long) foreignKey;
			eventId = eventPK;
		}

		List<RegistrationFormConfig> configs = getRegistrationFormConfigMgr().searchListByEventID(eventId);

		if (CollectionsHelper.notEmpty(configs)) {
			return configs;
		}
		else {
			// Collections.emptyList() is not a good idea, because
			// some entity might later to be added to this list
			// e.g. during copy and paste
			return new ArrayList<>();
		}
	}


	public List<RegistrationFormConfig> getRegistrationFormConfigsByEventPK(Long eventPK) throws Exception {
		List<RegistrationFormConfig> entityListByForeignKey = getEntityListByForeignKey(eventPK);
		return entityListByForeignKey;
	}
	
	
	public RegistrationFormConfig getRegistrationFormConfigByWebId(String webId) throws Exception {
		return getRegistrationFormConfigMgr().searchByWebId(webId);
	}


	public RegistrationFormConfig copyRegistrationFormConfig(
		Long configID,
		Long targetEventID,
		String webId,
		boolean copyUploadedFiles,
		boolean copyEmailTemplates,
		boolean copyBookingRules
	)
	throws Exception {

		String webappUrl = getOnlineWebappUrl(webId);

		RegistrationFormConfig copiedConfig = getRegistrationFormConfigMgr().copy(
			configID,
			targetEventID,
			webId,
			copyUploadedFiles,
			copyEmailTemplates,
			copyBookingRules,
			webappUrl
		);

		put(copiedConfig);
		fireCreate(copiedConfig.getId());

		EmailTemplateModel.getInstance().refreshForeignKey( copiedConfig.getEventPK() );

		return copiedConfig;
	}


	@Override
	public void dataChange(CacheModelEvent<Long> event) {
		if (!serverModel.isLoggedIn()) {
			return;
		}

		try {
			if (event.getSource() == eventModel && event.getOperation() == CacheModelOperation.DELETE) {

				Collection<Long> deletedPKs = new ArrayList<>(event.getKeyList().size());

				for (Long eventPK : event.getKeyList()) {
					for (RegistrationFormConfig registrationFormConfig : getLoadedAndCachedEntities()) {
						if (eventPK.equals(registrationFormConfig.getEventPK())) {
							deletedPKs.add(registrationFormConfig.getId());
						}
					}

					/* Remove the foreign key whose entity has been deleted from the model before firing the
					 * corresponding CacheModelEvent. The entities shall exist in the model when firing the
					 * CacheModelEvent, but not the structural information about the foreign keys. If a listener gets
					 * the CacheModelEvent and consequently requests the list of all entities of the foreign key, it
					 * shall get an empty list.
					 */
					removeForeignKeyData(eventPK);
				}

				if (!deletedPKs.isEmpty()) {
					fireDelete(deletedPKs);
					removeEntities(deletedPKs);
				}

			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	/**
	 * Returns the URL that leads to the start page of the form with the given webId or, if null, to its context root.
	 */
	public String getOnlineWebappUrl(String webId) {
		try {
			String onlineFormUrl = getKernelMgr().getOnlineFormURL();

			return new OnlineFormUrlBuilder()
				.withOnlineFormUrl(onlineFormUrl)
				.withWebId(webId)
				.build();
		}
		catch (ErrorMessageException e) {
			com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
			return null;
		}

	}

}
