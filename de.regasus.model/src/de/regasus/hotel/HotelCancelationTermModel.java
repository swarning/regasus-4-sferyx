package de.regasus.hotel;

import static de.regasus.LookupService.*;
import static com.lambdalogic.util.CollectionsHelper.*;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.lambdalogic.messeinfo.hotel.data.CreateHotelCancelationTermResult;
import com.lambdalogic.messeinfo.hotel.data.HotelCancelationTermVO;
import com.lambdalogic.messeinfo.hotel.data.HotelOfferingVO;
import com.lambdalogic.messeinfo.invoice.data.PriceVO;
import com.lambdalogic.messeinfo.kernel.data.AbstractVO;
import com.lambdalogic.util.exception.ErrorMessageException;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.model.CacheModelOperation;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.model.MICacheModel;
import de.regasus.model.Activator;


public class HotelCancelationTermModel
extends MICacheModel<Long, HotelCancelationTermVO>
implements CacheModelListener<Object> {

	private static HotelCancelationTermModel instance;
	
	private HotelOfferingModel hotelOfferingModel;
	
	
	private HotelCancelationTermModel() {
		super();
		
		hotelOfferingModel = HotelOfferingModel.getInstance();
		hotelOfferingModel.addListener(this);
	}

	
	public static HotelCancelationTermModel getInstance() {
		if (instance == null) {
			instance = new HotelCancelationTermModel();
		}
		return instance;
	}

	@Override
	protected Long getKey(HotelCancelationTermVO data) {
		return data.getID();
	}

	@Override
	protected HotelCancelationTermVO getEntityFromServer(Long pk) throws Exception {
		HotelCancelationTermVO vo = getHotelCancelationTermMgr().getHotelCancelationTermVO(pk);
		return vo;
	}

	public HotelCancelationTermVO getHotelCancelationTermVO(Long pk) throws Exception {
		return super.getEntity(pk);
	}
	

	@Override
	protected List<HotelCancelationTermVO> getEntitiesFromServer(Collection<Long> keyList) throws Exception {
		List<HotelCancelationTermVO> entities = getHotelCancelationTermMgr().getHotelCancelationTermVOs(keyList);
		return entities;
	}

	public List<HotelCancelationTermVO> getHotelCancelationTermVOs(List<Long> pkList) throws Exception {
		return super.getEntities(pkList);
	}


	@Override
	protected HotelCancelationTermVO createEntityOnServer(HotelCancelationTermVO vo) throws Exception {
		vo.validate();
		vo = getHotelCancelationTermMgr().createHotelCancelationTerm(vo);
		return vo;
	}

	
	public HotelCancelationTermVO create(HotelCancelationTermVO vo) throws Exception {
		return super.create(vo);
	}

	
	@Override
	protected HotelCancelationTermVO updateEntityOnServer(HotelCancelationTermVO vo) throws Exception {
		vo.validate();
		vo = getHotelCancelationTermMgr().updateHotelCancelationTerm(vo);
		return vo;
	}

	
	public HotelCancelationTermVO update(HotelCancelationTermVO vo) throws Exception {
		return super.update(vo);
	}

	
	@Override
	protected void deleteEntityOnServer(HotelCancelationTermVO vo) throws Exception {
		if (vo != null) {
			Long id = vo.getID();
			getHotelCancelationTermMgr().deleteHotelCancelationTerm(id);
		}
	}
	

	public void delete(HotelCancelationTermVO hotelCancelationTermVO) throws Exception {
		super.delete(hotelCancelationTermVO);
	}


	@Override
	protected boolean isForeignKeySupported() {
		return true;
	}

	
	protected Object getForeignKey(HotelCancelationTermVO hotelCancelationTermVO) {
		Long fk = null;
		if (hotelCancelationTermVO != null) {
			fk= hotelCancelationTermVO.getOfferingPK();
		}
		return fk;
	}

	
	protected List<HotelCancelationTermVO> getEntitiesByForeignKeyFromServer(Object foreignKey)
	throws Exception {
		Long hotelOfferingPK = (Long) foreignKey;
		
		List<HotelCancelationTermVO> voList = getHotelCancelationTermMgr().getHotelCancelationTermVOsByHotelOfferingPK(
			hotelOfferingPK,
			null	// referendeDate
		);
		
		return voList;
	}
	
	
	public List<HotelCancelationTermVO> getHotelCancelationTermVOsByHotelOfferingPK(Long hotelOfferingPK) 
	throws Exception {
		return getEntityListByForeignKey(hotelOfferingPK);
	}


	public CreateHotelCancelationTermResult createHotelCancelationTermsByContingent(
		Long hotelContingentPK,
		Date startDate,
		Date endDate,
		boolean pricePerNight,
		BigDecimal percentValue,
		PriceVO priceVO,
		boolean forceInterval
	)
	throws Exception {
		CreateHotelCancelationTermResult result = getHotelCancelationTermMgr().createHotelCancelationTermsByContingent(
			hotelContingentPK,
			startDate,
			endDate,
			pricePerNight,
			percentValue,
			priceVO,
			forceInterval
		);

		// assure that the cache is big enough
		int formerCacheSize = assureCacheSize(result.getSuccessCount());

		List<Long> hotelCancelationTermPKs = AbstractVO.getPKs(result.getHotelCancelationTermVOs());

		List<HotelCancelationTermVO> hotelCancelationTermVOs = getHotelCancelationTermMgr().getHotelCancelationTermVOs(hotelCancelationTermPKs);
		put(hotelCancelationTermVOs);
		
		fireCreate(hotelCancelationTermPKs);

		// reset the initial cache size
		setCacheSize(formerCacheSize);

		return result;
	}


	public CreateHotelCancelationTermResult createHotelCancelationTermsByEvent(
		Long eventPK,
		Date startDate,
		Date endDate,
		boolean pricePerNight,
		BigDecimal percentValue,
		PriceVO priceVO,
		boolean forceInterval
	)
	throws Exception {
		CreateHotelCancelationTermResult result = getHotelCancelationTermMgr().createHotelCancelationTermsByEvent(
			eventPK,
			startDate,
			endDate,
			pricePerNight,
			percentValue,
			priceVO,
			forceInterval
		);

		// assure that the cache is big enough
		int formerCacheSize = assureCacheSize(result.getSuccessCount());

		List<Long> hotelCancelationTermPKs = AbstractVO.getPKs(result.getHotelCancelationTermVOs());

		List<HotelCancelationTermVO> hotelCancelationTermVOs = getHotelCancelationTermMgr().getHotelCancelationTermVOs(hotelCancelationTermPKs);
		put(hotelCancelationTermVOs);

		fireCreate(hotelCancelationTermPKs);

		// reset the initial cache size
		setCacheSize(formerCacheSize);

		return result;
	}

	
	public CreateHotelCancelationTermResult createHotelCancelationTermsByHotelAndEvent(
		Long hotelPK, 
		Long eventPK,
		Date startDate,
		Date endDate,
		boolean pricePerNight,
		BigDecimal percentValue,
		PriceVO priceVO,
		boolean forceInterval
	)
	throws Exception {
		CreateHotelCancelationTermResult result = getHotelCancelationTermMgr().createHotelCancelationTermsByHotelAndEvent(
			hotelPK,
			eventPK,
			startDate,
			endDate,
			pricePerNight,
			percentValue,
			priceVO,
			forceInterval
		);

		// assure that the cache is big enough
		int formerCacheSize = assureCacheSize(result.getSuccessCount());
		
		List<HotelCancelationTermVO> cancelationTermVOs = result.getHotelCancelationTermVOs();
		put(cancelationTermVOs);
		
		List<Long> cancelationTermPKs = AbstractVO.getPKs(cancelationTermVOs);
		fireCreate(cancelationTermPKs);

		// reset the initial cache size
		setCacheSize(formerCacheSize);

		return result;
	}

	
	public HotelCancelationTermVO copyHotelCancelationTerm(
		Long sourceHotelCancelTermPK, 
		Long destHotelOfferingPK
	)
	throws ErrorMessageException {
		HotelCancelationTermVO hctVO = getHotelCancelationTermMgr().copyHotelCancelationTerm(
			sourceHotelCancelTermPK, 
			destHotelOfferingPK, 
			null	// dayShift
		);
		
		put(hctVO);

		List<Long> primaryKeyList = Collections.singletonList(hctVO.getID());
		try {
			fireCreate(primaryKeyList);
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}

		return hctVO;
	}

	
	@Override
	public void dataChange(CacheModelEvent<Object> event) {
		if (!serverModel.isLoggedIn()) {
			return;
		}
		
		try {
			if (event.getSource() == hotelOfferingModel){
				
				if (event.getOperation() == CacheModelOperation.UPDATE) {
    				for (Object hoPK : event.getKeyList()) {
    					// load updated Hotel Offering
    					HotelOfferingVO poVO = hotelOfferingModel.getHotelOfferingVO((Long) hoPK);
    					
    					// find Hotel Cancelation Terms that belong to the updated Hotel Offering
    					for (HotelCancelationTermVO hctVO : getLoadedAndCachedEntities()) {
    						if (hoPK.equals(hctVO.getOfferingPK())) {
    							/* Update the values of currency and brutto silently without
    							 * refreshing the entities and without firing an update event.
    							 * 
    							 * This reflects the fact, that both fields are not persisted in
    							 * the table HOTEL_CANCELATION_TERM but taken from
    							 * HOTEL_OFFERING.
    							 * 
    							 * Though the data of these Hotel Cancelation Terms have changed,
    							 * no event is fired, because there might be editors with unsaved
    							 * data. If those editors receive a refresh or update event,
    							 * the changes would get lost. So we had to check if there are editors 
    							 * with unsaved data and ask (annoy) the user to save them before 
    							 * updating a Hotel Offering.
    							 * The consequence that arises from this is that GUI components that
    							 * show the values of currency and brutto are responsible on their 
    							 * own to keep them up to date. So they should observe 
    							 * HotelOfferingModel. 
    							 */
    							hctVO.setCurrency(poVO.getCurrency());
    							hctVO.setBrutto(poVO.isGross());
    						}
    					}
    				}
				}
				else if (event.getOperation() == CacheModelOperation.DELETE) {
					// determine deleted entities
					Collection<Long> deletedHotelCancelationTermsPKs = createArrayList();;
					for (Object hotelOfferingPK : event.getKeyList()) {
						for (HotelCancelationTermVO hctVO : getLoadedAndCachedEntities()) {
							if (hotelOfferingPK.equals(hctVO.getOfferingPK())) {
								deletedHotelCancelationTermsPKs.add(hctVO.getPK());
							}
						}
					}
					
					// inform listeners about deleted entities
					if (!deletedHotelCancelationTermsPKs.isEmpty()) {
						fireDelete(deletedHotelCancelationTermsPKs);
					}
    				
    				// remove deleted entities from model
    				removeEntities(deletedHotelCancelationTermsPKs);
				}
				
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}

	}

}
