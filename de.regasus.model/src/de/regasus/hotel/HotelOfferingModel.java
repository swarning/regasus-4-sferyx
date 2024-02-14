package de.regasus.hotel;

import static de.regasus.LookupService.getHotelOfferingMgr;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.lambdalogic.messeinfo.hotel.HotelOfferingParameter;
import com.lambdalogic.messeinfo.hotel.data.HotelContingentCVO;
import com.lambdalogic.messeinfo.hotel.data.HotelOfferingVO;
import com.lambdalogic.util.StringHelper;
import com.lambdalogic.util.exception.ErrorMessageException;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.model.CacheModelOperation;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.model.MICacheModel;
import de.regasus.model.Activator;

public class HotelOfferingModel
extends MICacheModel<Long, HotelOfferingVO>
implements CacheModelListener<Object> {

	private static HotelOfferingModel singleton;

	private HotelContingentModel hotelContingentModel;


	private HotelOfferingModel() {
		hotelContingentModel = HotelContingentModel.getInstance();
		hotelContingentModel.addListener(this);
	}


	public static HotelOfferingModel getInstance() {
		if (singleton == null) {
			singleton = new HotelOfferingModel();
		}
		return singleton;
	}


	@Override
	protected Long getKey(HotelOfferingVO entity) {
		return entity.getID();
	}


	@Override
	protected HotelOfferingVO getEntityFromServer(Long hotelOfferingPK) throws Exception {
		HotelOfferingVO hotelOfferingVO = getHotelOfferingMgr().getHotelOfferingVO(hotelOfferingPK);
		return hotelOfferingVO;
	}


	public HotelOfferingVO getHotelOfferingVO(Long hotelOfferingPK) throws Exception {
		return super.getEntity(hotelOfferingPK);
	}


	@Override
	protected List<HotelOfferingVO> getEntitiesFromServer(Collection<Long> hotelOfferingPKs) throws Exception {
		List<HotelOfferingVO> hotelOfferingVOs = getHotelOfferingMgr().getHotelOfferingVOs(hotelOfferingPKs);
		return hotelOfferingVOs;
	}


	public List<HotelOfferingVO> getHotelOfferingVOs(List<Long> hotelOfferingPKs) throws Exception {
		return super.getEntities(hotelOfferingPKs);
	}


	@Override
	protected HotelOfferingVO createEntityOnServer(HotelOfferingVO hotelOfferingVO) throws Exception {
		hotelOfferingVO.validate();
		hotelOfferingVO = getHotelOfferingMgr().createHotelOffering(hotelOfferingVO);
		return hotelOfferingVO;
	}


	@Override
	public HotelOfferingVO create(HotelOfferingVO hotelOfferingVO) throws Exception {
		return super.create(hotelOfferingVO);
	}


	@Override
	protected HotelOfferingVO updateEntityOnServer(HotelOfferingVO hotelOfferingVO) throws Exception {
		hotelOfferingVO.validate();
		hotelOfferingVO = getHotelOfferingMgr().updateHotelOffering(hotelOfferingVO);
		return hotelOfferingVO;
	}


	@Override
	public HotelOfferingVO update(HotelOfferingVO hotelOfferingVO) throws Exception {
		return super.update(hotelOfferingVO);
	}


	@Override
	protected void deleteEntityOnServer(HotelOfferingVO hotelOfferingVO) throws Exception {
		if (hotelOfferingVO != null) {
			Long id = hotelOfferingVO.getID();
			getHotelOfferingMgr().deleteHotelOffering(id);
		}
	}


	@Override
	public void delete(HotelOfferingVO hotelOfferingVO) throws Exception {
		super.delete(hotelOfferingVO);
	}


	@Override
	protected boolean isForeignKeySupported() {
		return true;
	}


	@Override
	protected Object getForeignKey(HotelOfferingVO hotelOfferingVO) {
		Long fk = null;
		if (hotelOfferingVO != null) {
			fk= hotelOfferingVO.getHotelContingentPK();
		}
		return fk;
	}


	@Override
	protected List<HotelOfferingVO> getEntitiesByForeignKeyFromServer(Object foreignKey) throws Exception {
		Long hotelPointPK = (Long) foreignKey;
		List<HotelOfferingVO> hotelOfferingVOs = getHotelOfferingMgr().getHotelOfferingVOsByContingentPK(
			hotelPointPK
		);

		return hotelOfferingVOs;
	}


	public List<HotelOfferingVO> getHotelOfferingVOsByHotelContingentPK(Long hotelContingentPK) throws Exception {
		return getEntityListByForeignKey(hotelContingentPK);
	}


	public HotelOfferingVO copyHotelOffering(
		Long sourceHotelOfferingPK,
		Long destHotelContingentPK
	)
	throws ErrorMessageException {
		HotelOfferingVO hoVO = getHotelOfferingMgr().copyHotelOffering(
			sourceHotelOfferingPK,
			destHotelContingentPK,
			null,	// dayShift
			true	// withCancelationTerms
		);

		put(hoVO);

		List<Long> primaryKeyList = Collections.singletonList(hoVO.getID());
		try {
			fireCreate(primaryKeyList);
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}

		return hoVO;
	}


	@Override
	public void dataChange(CacheModelEvent<Object> event) {
		if (!serverModel.isLoggedIn()) {
			return;
		}

		try {
			if (event.getSource() == hotelContingentModel && event.getOperation() == CacheModelOperation.DELETE) {

				Collection<Long> deletedHotelOfferingPKs = new ArrayList<Long>();

				for (Object hotelContingentPK : event.getKeyList()) {
					for (HotelOfferingVO hotelOfferingVO : getLoadedAndCachedEntities()) {
						if (hotelContingentPK.equals(hotelOfferingVO.getHotelContingentPK())) {
							deletedHotelOfferingPKs.add(hotelOfferingVO.getPK());
						}
					}
				}

				if (!deletedHotelOfferingPKs.isEmpty()) {
					fireDelete(deletedHotelOfferingPKs);
				}
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	/**
	 * Determine the set of all Hotel Offering Categories of an Event.
	 * @param eventPK
	 * @return
	 * @throws Exception
	 */
	public Collection<String> getHotelOfferingCategoriesByEvent(Long eventPK) throws Exception {
		Set<String> categorySet = new HashSet<>();

		// get PKs of Hotel Contingents of Event from HotelContingentModel
		List<HotelContingentCVO> hotelContingentCVOs = hotelContingentModel.getHotelContingentCVOsByEventPK(eventPK);
		List<Long> hotelContingentPKs = HotelContingentCVO.getPKs(hotelContingentCVOs);

		// check if the data of all Hotel Contingents is loaded
		if ( ! getLoadedForeignKeys().containsAll(hotelContingentPKs) ) {
			loadOfferingsOfEvent(eventPK, true /*force*/);
		}

		for (Long hotelContingentPK : hotelContingentPKs) {
			List<HotelOfferingVO> hotelOfferingVOs = getHotelOfferingVOsByHotelContingentPK(hotelContingentPK);
			for (HotelOfferingVO hotelOfferingVO : hotelOfferingVOs) {
				String category = hotelOfferingVO.getCategory();
				if ( StringHelper.isNotEmpty(category) ) {
					categorySet.add(category);
				}
			}
		}

		return categorySet;
	}


	public void loadOfferingsOfEvent(Long eventPK) throws Exception {
		loadOfferingsOfEvent(eventPK, false /*force*/);
	}


	private void loadOfferingsOfEvent(Long eventPK, boolean force) throws Exception {
		if (!force) {
			// get PKs of Hotel Contingents of Event from HotelContingentModel
			List<HotelContingentCVO> hotelContingentCVOs = hotelContingentModel.getHotelContingentCVOsByEventPK(eventPK);
			List<Long> hotelContingentPKs = HotelContingentCVO.getPKs(hotelContingentCVOs);

			// check if the data of all Hotel Contingents is loaded
			if ( getLoadedForeignKeys().containsAll(hotelContingentPKs) ) {
				return;
			}
		}


		// load data of all Hotel Offerings of the Event
		HotelOfferingParameter hotelOfferingParameter = new HotelOfferingParameter(eventPK);
		List<HotelOfferingVO> hotelOfferingVOs = getHotelOfferingMgr().getHotelOfferingVOs(hotelOfferingParameter);

		// build Map from Contingent to related Offerings
		Map<Long, List<HotelOfferingVO>> contingent2OfferingMap = new HashMap<>();
		for (HotelOfferingVO hotelOfferingVO : hotelOfferingVOs) {
			List<HotelOfferingVO> hoList = contingent2OfferingMap.get( hotelOfferingVO.getHotelContingentPK() );
			if (hoList == null) {
				hoList = new ArrayList<>();
				contingent2OfferingMap.put(hotelOfferingVO.getHotelContingentPK(), hoList);
			}
			hoList.add(hotelOfferingVO);
		}

		// put data into Model
		for (Map.Entry<Long, List<HotelOfferingVO>> entry : contingent2OfferingMap.entrySet()) {
			Long hotelContingentPK = entry.getKey();
			List<HotelOfferingVO> hotelOfferingList = entry.getValue();
			putForeignKeyData(hotelContingentPK, hotelOfferingList);
		}
	}

}
