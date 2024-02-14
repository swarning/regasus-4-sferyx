package de.regasus.core.model;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.lambdalogic.util.model.CacheModel;
import com.lambdalogic.util.model.CacheModelOperation;
import com.lambdalogic.util.model.ModelEvent;
import com.lambdalogic.util.model.ModelListener;

import de.regasus.core.ServerModel;
import de.regasus.core.error.RegasusErrorHandler;

public abstract class MICacheModel<KeyType, EntityType>
extends CacheModel<KeyType, EntityType> {

	protected ServerModel serverModel;


	protected MICacheModel() {
		this(DEFAULT_ENTITY_CACHE_SIZE, DEFAULT_FOREIGN_KEY_CACHE_SIZE);
	}


	protected MICacheModel(int entityCacheSize, int foreignKeyCacheSize) {
		super(entityCacheSize, foreignKeyCacheSize);
		serverModel = ServerModel.getInstance();
		serverModel.addListener(serverModelListener);
	}


	private ModelListener serverModelListener = new ModelListener() {
		@Override
		public void dataChange(ModelEvent event) {
			try {
				ServerModelEvent serverModelEvent = (ServerModelEvent) event;

				if (!serverModel.isShutdown()
					&&
					(
						serverModelEvent.getType() == ServerModelEventType.REFRESH ||
						serverModelEvent.getType() == ServerModelEventType.LOGIN ||
						serverModelEvent.getType() == ServerModelEventType.LOGOUT
						)
					) {
					refresh();
				}
			}
			catch (Exception e) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			}
		}
	};


	@Override
	protected EntityType getEntity(KeyType key) throws Exception {
		EntityType entity = null;

		if (serverModel.isLoggedIn()) {
			entity = super.getEntity(key);
		}

		return entity;
	}


	@Override
	protected List<EntityType> getEntities(Collection<KeyType> keyList) throws Exception {
		List<EntityType> entities = null;

		if (serverModel.isLoggedIn()) {
			entities = super.getEntities(keyList);
		}
		else {
			entities = Collections.emptyList();
		}

		return entities;
	}


	@Override
	protected List<EntityType> getEntityListByForeignKey(Object foreignKey) throws Exception {
		List<EntityType> entities = null;

		if (foreignKey != null && serverModel.isLoggedIn()) {
			entities = super.getEntityListByForeignKey(foreignKey);
		}
		else {
			entities = Collections.emptyList();
		}

		return entities;
	}


	@Override
	protected List<EntityType> getAllEntities() throws Exception {
		List<EntityType> entityList = null;

		if (serverModel.isLoggedIn()) {
			entityList = super.getAllEntities();
		}
		else {
			entityList = Collections.emptyList();
		}

		return entityList;
	}


	@Override
	protected EntityType create(EntityType entity) throws Exception {
		EntityType newEntity = null;

		if (serverModel.isLoggedIn()) {
			newEntity = super.create(entity);
		}

		return newEntity;
	}


	@Override
	protected EntityType update(EntityType entity) throws Exception {
		EntityType newEntity = null;

		if (serverModel.isLoggedIn()) {
			newEntity = super.update(entity);
		}

		return newEntity;
	}


	@Override
	protected void delete(EntityType entity) throws Exception {
		if (serverModel.isLoggedIn()) {
			super.delete(entity);
		}
	}


	@Override
	protected void delete(Collection<EntityType> entities) throws Exception {
		if (serverModel.isLoggedIn()) {
			super.delete(entities);
		}
	}


	@Override
	protected EntityType getExtendedEntity(KeyType key) throws Exception {
		EntityType entity = null;

		if (serverModel.isLoggedIn()) {
			entity = super.getExtendedEntity(key);
		}

		return entity;
	}


	@Override
	protected List<EntityType> getExtendedEntities(List<KeyType> keyList) throws Exception {
		List<EntityType> entities = null;

		if (serverModel.isLoggedIn()) {
			entities = super.getExtendedEntities(keyList);
		}
		else {
			entities = Collections.emptyList();
		}

		return entities;
	}


	@Override
	public void refresh() throws Exception {
		if (serverModel.isLoggedIn()) {
			super.refresh();
		}
		else {
			clearAll();
			fireDataChange(CacheModelOperation.REFRESH);
		}
	}


	@Override
	public void refresh(KeyType key) throws Exception {
		if (serverModel.isLoggedIn()) {
			super.refresh(key);
		}
		else {
			clearAll();
			fireDataChange(CacheModelOperation.REFRESH);
		}
	}

}
