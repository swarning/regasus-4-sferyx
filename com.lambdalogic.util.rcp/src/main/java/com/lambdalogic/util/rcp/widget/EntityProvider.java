package com.lambdalogic.util.rcp.widget;

import java.util.Collection;
import java.util.List;

public interface EntityProvider<EntityType> {

	List<EntityType> getEntityList();

	EntityType findEntity(Object entityId);

	Collection<EntityType> findEntities(Collection<?> entityIds);

}
