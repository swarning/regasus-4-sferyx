package de.regasus.hotel;

import static de.regasus.LookupService.getEventHotelInfoMgr;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.lambdalogic.messeinfo.hotel.data.EventHotelInfoVO;
import com.lambdalogic.messeinfo.hotel.data.EventHotelKey;
import com.lambdalogic.messeinfo.hotel.data.HotelContingentCVO;
import com.lambdalogic.messeinfo.hotel.data.HotelContingentVO;
import com.lambdalogic.util.CollectionsHelper;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.model.CacheModelOperation;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.model.MICacheModel;
import de.regasus.event.EventModel;
import de.regasus.model.Activator;

@SuppressWarnings("rawtypes")
public class EventHotelInfoModel
extends MICacheModel<Long, EventHotelInfoVO>
implements CacheModelListener {
	
	private static EventHotelInfoModel singleton = null;

	private EventModel eventModel;
	private HotelContingentModel hotelContingentModel;


	private EventHotelInfoModel() {
		super();

		eventModel = EventModel.getInstance();
		eventModel.addListener(this);

		hotelContingentModel = HotelContingentModel.getInstance();
		hotelContingentModel.addListener(this);
	}


	public static EventHotelInfoModel getInstance() {
		if (singleton == null) {
			singleton = new EventHotelInfoModel();
		}
		return singleton;
	}


	@Override
	protected Long getKey(EventHotelInfoVO entity) {
		return entity.getPK();
	}


	@Override
	protected EventHotelInfoVO getEntityFromServer(Long pk) throws Exception {
		EventHotelInfoVO eventHotelInfoVO = getEventHotelInfoMgr().getEventHotelInfoVO(pk);
		return eventHotelInfoVO;
	}


	public EventHotelInfoVO getEventHotelInfoVO(Long pk) throws Exception {
		return super.getEntity(pk);
	}


	@Override
	protected List<EventHotelInfoVO> getEntitiesFromServer(Collection<Long> hotelContingentPKs)
		throws Exception {
		List<EventHotelInfoVO> eventHotelInfoVOs = getEventHotelInfoMgr().getEventHotelInfoVOs(
			hotelContingentPKs
			);
		return eventHotelInfoVOs;
	}


	public List<EventHotelInfoVO> getEventHotelInfoVOs(Collection<Long> hotelContingentPKs) throws Exception {
		return super.getEntities(hotelContingentPKs);
	}


	@Override
	protected EventHotelInfoVO createEntityOnServer(EventHotelInfoVO eventHotelInfoVO) throws Exception {
		eventHotelInfoVO.validate();
		EventHotelInfoVO createdEventHotelInfoVO = getEventHotelInfoMgr().createEventHotelInfoVO(eventHotelInfoVO);
		return createdEventHotelInfoVO;
	}


	@Override
	public EventHotelInfoVO create(EventHotelInfoVO eventHotelInfoVO) throws Exception {
		return super.create(eventHotelInfoVO);
	}


	@Override
	protected EventHotelInfoVO updateEntityOnServer(EventHotelInfoVO eventHotelInfoVO) throws Exception {
		eventHotelInfoVO.validate();
		eventHotelInfoVO = getEventHotelInfoMgr().updateEventHotelInfoVO(eventHotelInfoVO);
		return eventHotelInfoVO;
	}


	@Override
	public EventHotelInfoVO update(EventHotelInfoVO eventHotelInfoVO) throws Exception {
		return super.update(eventHotelInfoVO);
	}


	@Override
	protected void deleteEntityOnServer(EventHotelInfoVO eventHotelInfoVO) throws Exception {
		if (eventHotelInfoVO != null) {
			Long pk = eventHotelInfoVO.getPK();
			getEventHotelInfoMgr().deleteEventHotelInfoVO(pk);
		}
	}


	public void delete(EventHotelInfoVO eventHotelInfoVO) throws Exception {
		super.delete(eventHotelInfoVO);
	}


	@Override
	protected boolean isForeignKeySupported() {
		return true;
	}


	@Override
	protected Object getForeignKey(EventHotelInfoVO eventHotelInfoVO) {
		EventHotelKey fk = null;
		if (eventHotelInfoVO != null) {
			Long eventPK = eventHotelInfoVO.getEventPK();
			Long hotelPK = eventHotelInfoVO.getHotelPK();
			fk = new EventHotelKey(eventPK, hotelPK);
		}
		return fk;
	}


	protected List<EventHotelInfoVO> getEntitiesByForeignKeyFromServer(Object foreignKey) throws Exception {
		EventHotelKey eventHotelKey = (EventHotelKey) foreignKey;

		Long eventPK = eventHotelKey.getEventPK();
		Long hotelPK = eventHotelKey.getHotelPK();

		EventHotelInfoVO eventHotelInfoVO = getEventHotelInfoMgr().getEventHotelInfoVOByEventPKAndHotelPK(
			eventPK,
			hotelPK
			);

		List<EventHotelInfoVO> eventHotelInfoVOs = null;
		if (eventHotelInfoVO != null) {
			eventHotelInfoVOs = CollectionsHelper.createArrayList(eventHotelInfoVO);
		}
		else {
			/*
			 * Do not use Collections.emptyList(), because it is unmodifiable what will lead to an error if ths
			 * EventHotelInfo is created later. Instead create an ArrayList of size 1, because there will be at most one
			 * EventHotelInfo for each Event-Hotel combination.
			 */
			eventHotelInfoVOs = CollectionsHelper.createArrayList(1);
		}

		return eventHotelInfoVOs;
	}


	public EventHotelInfoVO getEventHotelInfoByEventPKAndHotelPK(Long eventPK, Long hotelPK) throws Exception {
		EventHotelInfoVO eventHotelInfoVO = null;
		EventHotelKey eventHotelKey = new EventHotelKey(eventPK, hotelPK);
		List<EventHotelInfoVO> eventHotelInfoVOs = getEntityListByForeignKey(eventHotelKey);
		if (!eventHotelInfoVOs.isEmpty()) {
			eventHotelInfoVO = eventHotelInfoVOs.get(0);
		}
		return eventHotelInfoVO;
	}


	public void dataChange(CacheModelEvent event) {
		if (!serverModel.isLoggedIn()) {
			return;
		}
		
		try {
			// We don't have to cover the case that a Hotel is deleted, because the HotelContingents and
			// also the EventHotelInfos remain in the DB; the Hotels are only marked as deleted
			if (event.getSource() == eventModel && event.getOperation() == CacheModelOperation.DELETE) {

				Collection<Long> deletedPKs = new ArrayList<>(event.getKeyList().size());

				for (Object eventPK : event.getKeyList()) {
					
					// EventHotelKeys of the eventPK that exist in the model
					Set<EventHotelKey> eventHotelKeys = CollectionsHelper.createHashSet();

					// collect PKs and foreign keys (EventHotelKey) of the deleted eventPK from all loaded or cached data
					for (EventHotelInfoVO eventHotelInfoVO : getLoadedAndCachedEntities()) {
						if (eventPK.equals(eventHotelInfoVO.getEventPK())) {
							deletedPKs.add(eventHotelInfoVO.getPK());
							
							/* Because of the combined foreign key EventHotelKey the affected foreign keys have to be determined 
							 * first.
							 */
							EventHotelKey eventHotelKey = new EventHotelKey(
								eventHotelInfoVO.getEventPK(), 
								eventHotelInfoVO.getHotelPK()
							);
							eventHotelKeys.add(eventHotelKey);
						}
					}
					
					
					/* Remove the foreign key whose entity has been deleted from the model before firing the 
					 * corresponding CacheModelEvent. The entities shall exist in the model when firing the 
					 * CacheModelEvent, but not the structural information about the foreign keys. If a listener gets 
					 * the CacheModelEvent and consequently requests the list of all entities of the foreign key, it 
					 * shall get an empty list.
					 */
					
					// remove foreign key information from the model
					for (EventHotelKey eventHotelKey : eventHotelKeys) {
						removeForeignKeyData(eventHotelKey);
					}
				}

				if (!deletedPKs.isEmpty()) {
					fireDelete(deletedPKs);
					removeEntities(deletedPKs);
				}
			}
			else if (event.getSource() == hotelContingentModel && event.getOperation() == CacheModelOperation.DELETE) {
				// In case the last (known) HotelContingent for one event and hotel was deleted, we
				// forget the Info. If it was really the last, it gets deleted by and on the database.
				// And if the user creates a new contingent, and we didn't forget the info, the user
				// would see it in the editor and falsely think it is still there, no need to create again.
				for (Object key : event.getKeyList()) {
					Long hotelContingentPK = (Long) key;
					HotelContingentVO hotelContingentVO = hotelContingentModel.getHotelContingentVO(hotelContingentPK);
					Long eventPK = hotelContingentVO.getEventPK();
					Long hotelPK = hotelContingentVO.getHotelPK();
					List<HotelContingentCVO> remainingHotelContingentCVOsForSameEventAndHotel =
						hotelContingentModel.getHotelContingentCVOsByEventAndHotel(
							eventPK,
							hotelPK
						);

					if (remainingHotelContingentCVOsForSameEventAndHotel.isEmpty()) {
						EventHotelInfoVO eventHotelInfoVO = getEventHotelInfoByEventPKAndHotelPK(eventPK, hotelPK);
						if (eventHotelInfoVO != null && eventHotelInfoVO.getPK() != null) {
							Long eventHotelInfoPK = eventHotelInfoVO.getPK();
							fireDelete(eventHotelInfoPK);
							removeEntity(eventHotelInfoPK);
						}
					}
				}
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


}
