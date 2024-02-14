package de.regasus.event;

import static de.regasus.LookupService.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.lambdalogic.messeinfo.participant.data.LocationVO;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.model.CacheModelOperation;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.model.MICacheModel;
import de.regasus.model.Activator;

public class LocationModel
extends MICacheModel<Long, LocationVO>
implements CacheModelListener<Long> {

	private static LocationModel singleton = null;

	private EventModel eventModel;

	private LocationModel() {
		super();

		eventModel = EventModel.getInstance();
		eventModel.addListener(this);
	}


	public static LocationModel getInstance() {
		if (singleton == null) {
			singleton = new LocationModel();
		}
		return singleton;
	}


	@Override
	protected boolean isForeignKeySupported() {
		return true;
	}


	@Override
	protected Object getForeignKey(LocationVO locationVO) {
		Long fk = null;
		if (locationVO != null) {
			fk = locationVO.getEventPK();
		}
		return fk;
	}


	@Override
	public LocationVO create(LocationVO locationVO) throws Exception {
		return super.create(locationVO);
	}


	@Override
	protected LocationVO createEntityOnServer(LocationVO locationVO) throws Exception {
		locationVO.validate();
		locationVO = getLocationMgr().createLocation(locationVO);
		return locationVO;
	}


	@Override
	public LocationVO update(LocationVO locationVO) throws Exception {
		return super.update(locationVO);
	}


	@Override
	protected LocationVO updateEntityOnServer(LocationVO locationVO) throws Exception {
		locationVO.validate();
		locationVO = getLocationMgr().updateLocation(locationVO);
		return locationVO;
	}


	@Override
	protected Long getKey(LocationVO entity) {
		return entity.getPK();
	}


	@Override
	protected LocationVO getEntityFromServer(Long pk) throws Exception {
		LocationVO locationVO = getLocationMgr().getLocationVO(pk);
		return locationVO;
	}


	@Override
	protected List<LocationVO> getEntitiesFromServer(Collection<Long> pkList) throws Exception {
		List<LocationVO> locationVOs = getLocationMgr().getLocationVOs(pkList);
		return locationVOs;
	}


	@Override
	protected List<LocationVO> getEntitiesByForeignKeyFromServer(Object foreignKey) throws Exception {
		Long eventPK = (Long) foreignKey;
		List<LocationVO> locationVOs = getLocationMgr().getLocationVOsByEventPK(eventPK);
		return locationVOs;
	}


	public LocationVO getLocationVO(Long pk) throws Exception {
		return super.getEntity(pk);
	}


	public List<LocationVO> getLocationVOsByEventPK(Long eventPK) throws Exception {
		return super.getEntityListByForeignKey(eventPK);
	}


	public List<LocationVO> getLocationVOs(List<Long> locationPKs) throws Exception {
		return super.getEntities(locationPKs);
	}


	@Override
	public void delete(LocationVO locationVO) throws Exception {
		super.delete(locationVO);
	}


	@Override
	protected void deleteEntityOnServer(LocationVO locationVO) throws Exception {
		if (locationVO != null) {
			Long locationPK = locationVO.getID();
			getLocationMgr().deleteLocation(locationPK);
		}
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
					for (LocationVO locationVO : getLoadedAndCachedEntities()) {
						if (eventPK.equals(locationVO.getEventPK())) {
							deletedPKs.add(locationVO.getID());
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
					fireDataChange(CacheModelOperation.DELETE, deletedPKs);
					removeEntities(deletedPKs);
				}
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}

}
