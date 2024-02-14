package de.regasus.event;

import static de.regasus.LookupService.getEventGroupMgr;

import java.util.Collection;
import java.util.List;

import de.regasus.core.model.MICacheModel;


public class EventGroupModel extends MICacheModel<Long, EventGroup> {
	private static EventGroupModel singleton = null;


	private EventGroupModel() {
	}


	public static EventGroupModel getInstance() {
		if (singleton == null) {
			singleton = new EventGroupModel();
		}
		return singleton;
	}


	@Override
	protected Long getKey(EventGroup entity) {
		return entity.getId();
	}


	@Override
	protected boolean isForeignKeySupported() {
		return false;
	}


	@Override
	protected EventGroup getEntityFromServer(Long id) throws Exception {
		EventGroup eventGroup = getEventGroupMgr().read(id);
		return eventGroup;
	}


	public EventGroup getEventGroup(Long id) throws Exception {
		return super.getEntity(id);
	}


	@Override
	protected List<EventGroup> getEntitiesFromServer(Collection<Long> idCol) throws Exception {
		List<EventGroup> eventGroups = getEventGroupMgr().read(idCol);
		return eventGroups;
	}


	public List<EventGroup> getEventGroups(Collection<Long> idCol) throws Exception {
		return super.getEntities(idCol);
	}


	@Override
	protected List<EventGroup> getAllEntitiesFromServer() throws Exception {
		List<EventGroup> eventGroups = getEventGroupMgr().readAll();
		return eventGroups;
	}


	@Override
	public void loadAll() throws Exception {
		super.loadAll();
	}


	public List<EventGroup> getAllEventGroups() throws Exception {
		return getAllEntities();
	}


	@Override
	protected EventGroup createEntityOnServer(EventGroup eventGroup) throws Exception {
		eventGroup.validate();
		eventGroup = getEventGroupMgr().create(eventGroup);
		return eventGroup;
	}


	@Override
	public EventGroup create(EventGroup eventGroup) throws Exception {
		return super.create(eventGroup);
	}


	@Override
	protected EventGroup updateEntityOnServer(EventGroup eventGroup) throws Exception {
		eventGroup.validate();
		eventGroup = getEventGroupMgr().update(eventGroup);
		return eventGroup;
	}


	@Override
	public EventGroup update(EventGroup entity) throws Exception {
		return super.update(entity);
	}


	@Override
	protected void deleteEntityOnServer(EventGroup eventGroup) throws Exception {
		if (eventGroup != null) {
			Long id = eventGroup.getId();
			getEventGroupMgr().delete(id);
		}
	}


	@Override
	public void delete(EventGroup eventGroup) throws Exception {
		super.delete(eventGroup);
	}

}
