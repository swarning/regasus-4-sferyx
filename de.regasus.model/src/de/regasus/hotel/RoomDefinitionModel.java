package de.regasus.hotel;

import static de.regasus.LookupService.getRoomDefinitionMgr;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.lambdalogic.messeinfo.hotel.data.RoomDefinitionVO;
import com.lambdalogic.util.exception.ErrorMessageException;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.model.CacheModelOperation;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.model.MICacheModel;
import de.regasus.model.Activator;

public class RoomDefinitionModel
extends MICacheModel<Long, RoomDefinitionVO>
implements CacheModelListener<Long>{

	private static RoomDefinitionModel singleton;
	
	private HotelModel hotelModel = HotelModel.getInstance();
	
	
	private RoomDefinitionModel() {
		hotelModel.addListener(this);
	}
	

	public static RoomDefinitionModel getInstance() {
		if (singleton == null) {
			singleton = new RoomDefinitionModel();
		}
		return singleton;
	}
	
	@Override
	protected Long getKey(RoomDefinitionVO entity) {
		return entity.getID();
	}

	@Override
	protected RoomDefinitionVO getEntityFromServer(Long roomDefinitionPK) throws Exception {
		RoomDefinitionVO roomDefinitionVO = getRoomDefinitionMgr().getRoomDefinitionVO(roomDefinitionPK);
		return roomDefinitionVO;
	}

	
	public RoomDefinitionVO getRoomDefinitionVO(Long roomDefinitionPK) throws Exception {
		return super.getEntity(roomDefinitionPK);
	}
	
	
	@Override
	protected List<RoomDefinitionVO> getEntitiesFromServer(Collection<Long> roomDefinitionPKs) throws Exception {
		List<RoomDefinitionVO> roomDefinitionVOs = getRoomDefinitionMgr().getRoomDefinitionVOs(roomDefinitionPKs);
		
		return roomDefinitionVOs;
	}

	
	public List<RoomDefinitionVO> getRoomDefinitionVOs(Collection<Long> roomDefinitionPKs) throws Exception {
		return super.getEntities(roomDefinitionPKs);
	}

	
	@Override
	protected RoomDefinitionVO createEntityOnServer(RoomDefinitionVO roomDefinitionVO) throws Exception {
		roomDefinitionVO.validate();
		roomDefinitionVO = getRoomDefinitionMgr().createRoomDefinition(roomDefinitionVO);
		return roomDefinitionVO;
	}

	
	public RoomDefinitionVO create(RoomDefinitionVO roomDefinitionVO) throws Exception {
		return super.create(roomDefinitionVO);
	}

	
	@Override
	protected RoomDefinitionVO updateEntityOnServer(RoomDefinitionVO roomDefinitionVO) throws Exception {
		roomDefinitionVO.validate();
		roomDefinitionVO = getRoomDefinitionMgr().updateRoomDefinition(roomDefinitionVO);
		return roomDefinitionVO;
	}

	
	public RoomDefinitionVO update(RoomDefinitionVO roomDefinitionVO) throws Exception {
		return super.update(roomDefinitionVO);
	}

	
	@Override
	protected void deleteEntityOnServer(RoomDefinitionVO roomDefinitionVO) throws Exception {
		if (roomDefinitionVO != null) {
			Long id = roomDefinitionVO.getID();
			getRoomDefinitionMgr().deleteRoomDefinition(id);
		}
	}
	

	public void delete(RoomDefinitionVO roomDefinitionVO) throws Exception {
		RoomDefinitionVO roomDefinitionVO2 = roomDefinitionVO.clone();
		roomDefinitionVO2.setDeleted(true);
		super.update(roomDefinitionVO2);
	}


	@Override
	protected boolean isForeignKeySupported() {
		return true;
	}

	
	protected Object getForeignKey(RoomDefinitionVO roomDefinitionVO) {
		Long fk = null;
		if (roomDefinitionVO != null) {
			fk = roomDefinitionVO.getHotelPK();
		}
		return fk;
	}

	
	protected List<RoomDefinitionVO> getEntitiesByForeignKeyFromServer(Object foreignKey) throws Exception {
		Long hotelID = (Long) foreignKey;
		
		List<RoomDefinitionVO> roomDefinitionVOs = getRoomDefinitionMgr().getRoomDefinitionVOsByHotelPK(
			hotelID,
			true	// withDeleted
		);
		
		return roomDefinitionVOs;
	}
	
	
	public List<RoomDefinitionVO> getRoomDefinitionVOsByHotelPK(Long hotelPK) throws Exception {
		return getEntityListByForeignKey(hotelPK);
	}


	public List<RoomDefinitionVO> getUndeletedRoomDefinitionVOsByHotelPK(Long hotelPK) throws Exception {
		List<RoomDefinitionVO> rdVOs = getEntityListByForeignKey(hotelPK);

		// make a copy of the list to avoid removing deleted entities from the original list
		rdVOs = new ArrayList<RoomDefinitionVO>(rdVOs);
		
		// remove deleted RoomDefinitions
		for (Iterator<RoomDefinitionVO> it = rdVOs.iterator(); it.hasNext();) {
			RoomDefinitionVO roomDefinitionVO = it.next();
			if (roomDefinitionVO.isDeleted()) {
				it.remove();
			}
		}
		
		return rdVOs;
	}


	public void dataChange(CacheModelEvent<Long> event) {
		if (!serverModel.isLoggedIn()) {
			return;
		}
		
		try {
			if (event.getOperation() == CacheModelOperation.DELETE) {
				List<Long> hotelPKs = event.getKeyList();
				Collection<RoomDefinitionVO> allRoomDefinitionVOs = getLoadedAndCachedEntities();
				Collection<RoomDefinitionVO> deletedRoomDefinitionVOs = new ArrayList<RoomDefinitionVO>();
				for (RoomDefinitionVO roomDefinitionVO : allRoomDefinitionVOs) {
					if (hotelPKs.contains(roomDefinitionVO.getHotelPK())) {
						deletedRoomDefinitionVOs.add(roomDefinitionVO);
					}
				}
				
				handleDelete(
					deletedRoomDefinitionVOs,
					true	// fireCoModelEvent
				);
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}
	
	
	public RoomDefinitionVO copyRoomDefinition(
		Long sourceRoomDefinitionID,
		Long destHotelPK
	) 
	throws ErrorMessageException {
		RoomDefinitionVO roomDefinitionVO = getRoomDefinitionMgr().copyRoomDefinition(sourceRoomDefinitionID, destHotelPK);
		put(roomDefinitionVO);
		List<Long> primaryKeyList = Collections.singletonList(roomDefinitionVO.getID());
		
		try {
			fireCreate(primaryKeyList);
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
		
		return roomDefinitionVO;
	}
	
}
