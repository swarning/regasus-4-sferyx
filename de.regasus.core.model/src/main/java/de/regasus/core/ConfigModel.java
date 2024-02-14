package de.regasus.core;

import static de.regasus.LookupService.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import com.lambdalogic.messeinfo.config.Config;
import com.lambdalogic.messeinfo.config.ConfigScope;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.model.EntityNotFoundException;

import de.regasus.core.model.MICacheModel;

public class ConfigModel extends MICacheModel<Long, Config> {

	private static ConfigModel singleton;

	private CacheModelListener<Long> dummyForeignKeyListener = new CacheModelListener<Long>() {
		@Override
		public void dataChange(CacheModelEvent<Long> event) {
		}
	};


	private ConfigModel() {
		super();
	}


	public static ConfigModel getInstance() {
		if (singleton == null) {
			singleton = new ConfigModel();
		}
		return singleton;
	}


	@Override
	protected Long getKey(Config entity) {
		return entity.getId();
	}


	@Override
	protected boolean isForeignKeySupported() {
		return true;
	}


	@Override
	protected Object getForeignKey(Config entity) {
		ConfigIdentifier fk = null;
		if (entity != null) {
			fk= new ConfigIdentifier(entity.getScope(), entity.getKey());
		}
		return fk;
	}


	@Override
	protected Config getEntityFromServer(Long id) throws Exception {
		Config config = getConfigMgr().read(id);
		return config;
	}


	public Config getConfig(Long id) throws Exception {
		Config config = null;
		try {
			config = super.getEntity(id);
		}
		catch (EntityNotFoundException e) {
			// ignore, because it's not an error if a Config for a user does not exist.
		}
		return config;
	}


	@Override
	protected List<Config> getEntitiesByForeignKeyFromServer(Object foreignKey) throws Exception {
		List<Config> configList = new ArrayList<>(1);

		ConfigIdentifier configIdentifier = (ConfigIdentifier) foreignKey;

		Config config = getConfigMgr().read(
			configIdentifier.getScope(),
			configIdentifier.getKey()
		);
		if (config != null) {
			configList.add(config);
		}

		return configList;
	}


	public Config getConfig(ConfigScope scope, String key) throws Exception {
		Config config = null;

		if (key == null) {
			key = Config.EMPTY_KEY;
		}

		ConfigIdentifier configIdentifier = new ConfigIdentifier(scope, key);

		// add a foreignKeyListener to assure that Configs of this ConfigIdentifier are cached.
		addForeignKeyListener(dummyForeignKeyListener, configIdentifier);

		List<Config> configList = getEntityListByForeignKey(configIdentifier);
		if (configList != null && !configList.isEmpty()) {
			config = configList.get(0);
		}

		return config;
	}


	public Config getGlobalAdminConfig() throws Exception {
		Config config = getConfig(ConfigScope.GLOBAL_ADMIN, null);
		return config;
	}


	public Config getGlobalCustomerConfig() throws Exception {
		Config config = getConfig(ConfigScope.GLOBAL_CUSTOMER, null);
		return config;
	}


	public Config getEventAdminConfig(Long eventPK) throws Exception {
		return getConfig(ConfigScope.EVENT_ADMIN, eventPK.toString());
	}


	public Config getEventCustomerConfig(Long eventPK) throws Exception {
		return getConfig(ConfigScope.EVENT_CUSTOMER, eventPK.toString());
	}


	public Config getGroupConfig(String groupID) throws Exception {
		return getConfig(ConfigScope.GROUP, groupID);
	}


	public Config getUserConfig(Long userID) throws Exception {
		return getConfig(ConfigScope.USER, userID.toString());
	}


	@Override
	protected List<Config> getEntitiesFromServer(Collection<Long> ids) throws Exception {
		List<Config> configs = getConfigMgr().read(ids);
		return configs;
	}


	public List<Config> getConfigs(List<Long> ids) throws Exception {
		return super.getEntities(ids);
	}


	@Override
	protected Config createEntityOnServer(Config config) throws Exception {
		config.validate();
		config = getConfigMgr().create(config);
		return config;
	}


	@Override
	public Config create(Config config) throws Exception {
		// add a foreignKeyListener to assure that Configs of this ConfigIdentifier are cached.
		ConfigIdentifier configIdentifier = new ConfigIdentifier(config.getScope(), config.getKey());
		addForeignKeyListener(dummyForeignKeyListener, configIdentifier);

		return super.create(config);
	}


	@Override
	public Config update(Config config) throws Exception {
		if (serverModel.isLoggedIn()) {
			// there are no foreign keys and no extensions and no CoModels

			Objects.requireNonNull(config.getId(), "The key of entities to be updated must not be null.");

			config.validate();
			List<Config> updatedConfigs = getConfigMgr().update(config);

			handleUpdate( Config.getPKs(updatedConfigs) );

			return updatedConfigs.get(0);
		}

		return null;
	}


	public void completeConfigs() throws Exception {
		if (serverModel.isLoggedIn()) {
			getConfigMgr().completeConfigs();
			refresh();
		}
	}


	@Override
	protected void deleteEntityOnServer(Config config) throws Exception {
		if (config != null) {
			getConfigMgr().delete( config.getId() );
		}
	}

	@Override
	public void delete(Config config) throws Exception {
		super.delete(config);
	}

}
