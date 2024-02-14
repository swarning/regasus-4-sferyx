package de.regasus.hotel;

import static de.regasus.LookupService.getHotelContingentMgr;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import com.lambdalogic.messeinfo.hotel.data.HotelContingentCVO;
import com.lambdalogic.messeinfo.hotel.data.HotelContingentCVOSettings;
import com.lambdalogic.messeinfo.hotel.data.HotelContingentVO;
import com.lambdalogic.messeinfo.hotel.data.OptionalHotelBookingVO;
import com.lambdalogic.messeinfo.hotel.data.VolumeVO;
import com.lambdalogic.util.CollectionsHelper;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.model.CacheModelOperation;

import de.regasus.core.OrderPosition;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.model.MICacheModel;
import de.regasus.event.EventModel;
import de.regasus.model.Activator;

public class HotelContingentModel  extends MICacheModel<Long, HotelContingentCVO> implements CacheModelListener<Long> {
	private static HotelContingentModel singleton = null;


	private static final HotelContingentCVOSettings standardHotelContingentCVOSettings;
	private static final HotelContingentCVOSettings extendedHotelContingentCVOSettings;

	static {
		/* If the settings change, the method update() must be adapted!
		 *
		 * At this time the standard settings and the extended settings are the same.
		 */
		standardHotelContingentCVOSettings = new HotelContingentCVOSettings();
		standardHotelContingentCVOSettings.withVolumeVOs = true;
		standardHotelContingentCVOSettings.withOptionalHotelBookingVOs = true;
		standardHotelContingentCVOSettings.withRoomDefinitionPKs = true;

		extendedHotelContingentCVOSettings = new HotelContingentCVOSettings();
		extendedHotelContingentCVOSettings.withVolumeVOs = true;
		extendedHotelContingentCVOSettings.withOptionalHotelBookingVOs = true;
		extendedHotelContingentCVOSettings.withRoomDefinitionPKs = true;
	}


	private EventModel eventModel;


	private HotelContingentModel() {
		super();

		eventModel = EventModel.getInstance();
		eventModel.addListener(this);
	}


	public static HotelContingentModel getInstance() {
		if (singleton == null) {
			singleton = new HotelContingentModel();
		}
		return singleton;
	}


	@Override
	protected Long getKey(HotelContingentCVO entity) {
		return entity.getPK();
	}


	@Override
	protected HotelContingentCVO getEntityFromServer(Long pk) throws Exception {
		HotelContingentCVO hotelContingentCVO = getHotelContingentMgr().getHotelContingentCVO(
			pk,
			standardHotelContingentCVOSettings
		);
		return hotelContingentCVO;
	}


	public HotelContingentCVO getHotelContingentCVO(Long pk) throws Exception {
		return super.getEntity(pk);
	}


	public HotelContingentVO getHotelContingentVO(Long hotelContingentPK) throws Exception {
		HotelContingentVO hotelContingentVO = null;
		HotelContingentCVO hotelContingentCVO = super.getEntity(hotelContingentPK);
		if (hotelContingentCVO != null) {
			hotelContingentVO = hotelContingentCVO.getVO();
		}
		return hotelContingentVO;
	}


	@Override
	protected List<HotelContingentCVO> getEntitiesFromServer(Collection<Long> hotelContingentPKs) throws Exception {
		List<HotelContingentCVO> hotelContingentCVOs = getHotelContingentMgr().getHotelContingentCVOs(
			hotelContingentPKs,
			standardHotelContingentCVOSettings
		);
		return hotelContingentCVOs;
	}


	public List<HotelContingentCVO> getHotelContingentCVOs(Collection<Long> hotelContingentPKs) throws Exception {
		return super.getEntities(hotelContingentPKs);
	}


	@Override
	protected HotelContingentCVO createEntityOnServer(HotelContingentCVO hotelContingentCVO) throws Exception {
		HotelContingentVO hotelContingentVO = hotelContingentCVO.getVO();
		List<VolumeVO> volumesVOs = hotelContingentCVO.getVolumes();
		List<OptionalHotelBookingVO> optionalHotelBookingVOs = hotelContingentCVO.getOptionalHotelBookingVOs();
		Collection<Long> roomDefinitionPKs = hotelContingentCVO.getRoomDefinitionPKs();

		// temporarily set the position to pass validation
		hotelContingentVO.setPosition(0);
		hotelContingentVO.validate();
		// remove position because it should be calculated automatically by the server
		hotelContingentVO.setPosition(null);

		HotelContingentVO createdHotelContingentVO = getHotelContingentMgr().createHotelContingent(
			hotelContingentVO,
			volumesVOs,
			optionalHotelBookingVOs,
			roomDefinitionPKs
		);

		HotelContingentCVO createdHotelContingentCVO = getHotelContingentMgr().getHotelContingentCVO(
			createdHotelContingentVO.getPK(),
			extendedHotelContingentCVOSettings
		);
		return createdHotelContingentCVO;
	}

	@Override
	public HotelContingentCVO create(HotelContingentCVO hotelContingentCVO) throws Exception {
		return super.create(hotelContingentCVO);
	}


	@Override
	protected HotelContingentCVO updateEntityOnServer(HotelContingentCVO hotelContingentCVO) throws Exception {
		HotelContingentVO hotelContingentVO = hotelContingentCVO.getVO();
		hotelContingentVO.validate();

		List<VolumeVO> volumeVOs = hotelContingentCVO.getVolumes();
		List<OptionalHotelBookingVO> optionalHotelBookingVOs = hotelContingentCVO.getOptionalHotelBookingVOs();
		Collection<Long> roomDefinitionPKs = hotelContingentCVO.getRoomDefinitionPKs();

		hotelContingentVO = getHotelContingentMgr().updateHotelContingent(
			hotelContingentVO,
			volumeVOs,
			optionalHotelBookingVOs,
			roomDefinitionPKs
		);

		hotelContingentCVO = getEntityFromServer(hotelContingentVO.getPK());
		return hotelContingentCVO;
	}

	@Override
	public HotelContingentCVO update(HotelContingentCVO hotelContingentCVO) throws Exception {
		return super.update(hotelContingentCVO);
	}


	/**
	 * Move a {@link HotelContingentVO} before or after another one.
	 * The target Hotel Contingent must belong to the same Hotel and Event.
	 *
	 * @param movedHotelContingentId
	 * @param orderPosition
	 * @param targetHotelContingentId
	 * @throws Exception
	 */
	public void move(Long movedHotelContingentId, OrderPosition orderPosition, Long targetHotelContingentId)
	throws Exception {
		List<HotelContingentVO> hcVOs = getHotelContingentMgr().move(
			movedHotelContingentId,
			orderPosition,
			targetHotelContingentId
		);

		// copy extensions of possibly existing data from the cache
		List<Long> hcIds = HotelContingentVO.getPKs(hcVOs);
		HashMap<Long, HotelContingentVO> hcMap = HotelContingentVO.abstractVOs2Map(hcVOs);

		List<HotelContingentCVO> hcCVOs = getHotelContingentCVOs(hcIds);

		for (HotelContingentCVO hcCVO : hcCVOs) {
			HotelContingentVO hcVO = hcMap.get( hcCVO.getId() );
			hcCVO.setVO(hcVO);
		}

		put(hcCVOs);

		fireDataChange(CacheModelOperation.UPDATE, hcIds);
	}


	@Override
	protected void deleteEntityOnServer(HotelContingentCVO hotelContingentCVO) throws Exception {
		if (hotelContingentCVO != null) {
			Long hotelContingentPK = hotelContingentCVO.getPK();
			getHotelContingentMgr().deleteHotelContingent(hotelContingentPK);
		}
	}


	public void delete(HotelContingentVO hotelContingentVO) throws Exception {
		HotelContingentCVO hotelContingentCVO = new HotelContingentCVO();
		hotelContingentCVO.setVO(hotelContingentVO);
		super.delete(hotelContingentCVO);
	}


	@Override
	public void delete(HotelContingentCVO entity) throws Exception {
		super.delete(entity);
	}


	// **************************************************************************
	// * Extensions
	// *

	@Override
	protected boolean isExtended(HotelContingentCVO hotelContingentCVO) {
		// Do not check the data, but always the settings, because the data may initialized lazily.

		return true;
	}


	@Override
	protected HotelContingentCVO getExtendedEntityFromServer(Long pk) throws Exception {
		return getHotelContingentMgr().getHotelContingentCVO(pk, extendedHotelContingentCVOSettings);
	}


	@Override
	protected List<HotelContingentCVO> getExtendedEntitiesFromServer(List<Long> hotelContingentPKs)
	throws Exception {

		List<HotelContingentCVO> hotelContingentCVOs = getHotelContingentMgr().getHotelContingentCVOs(
			hotelContingentPKs,
			extendedHotelContingentCVOSettings
		);
		return hotelContingentCVOs;
	}


	public HotelContingentCVO getExtendedHotelContingentCVO(Long hotelContingentPK)
	throws Exception {
		return super.getExtendedEntity(hotelContingentPK);
	}


	public List<HotelContingentCVO> getExtendedHotelContingentCVOs(List<Long> hotelContingentPKs)
	throws Exception {
		return super.getExtendedEntities(hotelContingentPKs);
	}

	// *
	// * Extensions
	// **************************************************************************

	@Override
	protected boolean isForeignKeySupported() {
		return true;
	}


	@Override
	protected Object getForeignKey(HotelContingentCVO hotelContingentCVO) {
		Long fk = null;
		if (hotelContingentCVO != null) {
			fk = hotelContingentCVO.getEventPK();
		}
		return fk;
	}


	@Override
	protected List<HotelContingentCVO> getEntitiesByForeignKeyFromServer(Object foreignKey) throws Exception {
		Long eventPK = (Long) foreignKey;
		List<Long> eventPKs = new ArrayList<>(1);
		eventPKs.add(eventPK);

		List<HotelContingentCVO> hotelContingentCVOs = getHotelContingentMgr().getHotelContingentCVOsByEventPKs(
			eventPKs,
			standardHotelContingentCVOSettings
		);

		return hotelContingentCVOs;
	}


	public List<HotelContingentCVO> getHotelContingentCVOsByEventPK(Long eventPK) throws Exception {
		return getEntityListByForeignKey(eventPK);
	}


	public List<HotelContingentCVO> getHotelContingentCVOsByEventAndHotel(Long eventPK, Long hotelPK)
	throws Exception {
		List<HotelContingentCVO> resultHotelContingentCVOs = null;

		if (hotelPK == null) {
			resultHotelContingentCVOs = getHotelContingentCVOsByEventPK(eventPK);
		}
		else {
    		List<HotelContingentCVO> allHotelContingentCVOs = getHotelContingentCVOsByEventPK(eventPK);

    		resultHotelContingentCVOs = CollectionsHelper.createArrayList(allHotelContingentCVOs.size());

    		for (HotelContingentCVO hotelContingentCVO : allHotelContingentCVOs) {
    			Long currentHotelPK = hotelContingentCVO.getVO().getHotelPK();
    			if (currentHotelPK.equals(hotelPK)) {
    				resultHotelContingentCVOs.add(hotelContingentCVO);
    			}
    		}
		}

		return resultHotelContingentCVOs;
	}


	public Collection<Long> getHotelPKsByEventPK(Long eventPK) throws Exception {
		List<HotelContingentCVO> hotelContingentCVOs = getHotelContingentCVOsByEventPK(eventPK);
		Collection<Long> hotelPKs = new HashSet<>(hotelContingentCVOs.size());
		for (HotelContingentCVO hotelContingentCVO : hotelContingentCVOs) {
			Long hotelPK = hotelContingentCVO.getVO().getHotelPK();
			hotelPKs.add(hotelPK);
		}
		return hotelPKs;
	}


	@Override
	public void dataChange(CacheModelEvent<Long> event) {
		if (!serverModel.isLoggedIn()) {
			return;
		}

		try {
			if (event.getSource() == eventModel && event.getOperation() == CacheModelOperation.DELETE) {

				Collection<Long> deletedPKs = new ArrayList<>(event.getKeyList().size());

				for (Object eventPK : event.getKeyList()) {
					for (HotelContingentCVO hotelContingentCVO : getLoadedAndCachedEntities()) {
						if (eventPK.equals(hotelContingentCVO.getEventPK())) {
							deletedPKs.add(hotelContingentCVO.getPK());
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


	public Long copyHotelContingent(
		Long sourceHotelContingentPK,
		Long destEventPK
	)
	throws Exception {
		// let the server copy the entity
		Long hotelContingentPK = getHotelContingentMgr().copyHotelContingent(
			sourceHotelContingentPK,
			destEventPK,
			null,	// dayShift
			true,	// withOfferings
			true	// withCancelationTerms
		);

		// load new entity and put it to the model
		HotelContingentCVO hotelContingentCVO = getEntityFromServer(hotelContingentPK);
		put(hotelContingentCVO);

		// inform listeners about the new entity
		List<Long> primaryKeyList = Collections.singletonList(hotelContingentPK);
		fireCreate(primaryKeyList);

		return hotelContingentPK;
	}

}
