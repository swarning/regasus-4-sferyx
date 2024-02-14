package de.regasus.hotel;

import static de.regasus.LookupService.*;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import de.regasus.core.model.MICacheModel;

public class HotelChainModel extends MICacheModel<Long, HotelChain> {

	private static HotelChainModel singleton;


	private HotelChainModel() {
		super();
	}


	public static HotelChainModel getInstance() {
		if (singleton == null) {
			singleton = new HotelChainModel();
		}
		return singleton;
	}


	@Override
	protected boolean isForeignKeySupported() {
		return false;
	}


	@Override
	protected Long getKey(HotelChain entity) {
		return entity.getId();
	}


	@Override
	protected HotelChain getEntityFromServer(Long hotelChainPK) throws Exception {
		HotelChain hotelChain = getHotelChainMgr().read(hotelChainPK);
		return hotelChain;
	}


	public HotelChain getHotelChain(Long hotelChainPK) throws Exception {
		return super.getEntity(hotelChainPK);
	}


	@Override
	protected List<HotelChain> getEntitiesFromServer(Collection<Long> hotelChainIDs) throws Exception {
		List<HotelChain> hotelChains = null;

		if (hotelChainIDs != null) {
    		hotelChains = getHotelChainMgr().read(hotelChainIDs);
		}

		return hotelChains;
	}


	public List<HotelChain> getHotelChains(List<Long> hotelChainPKs) throws Exception {
		return super.getEntities(hotelChainPKs);
	}


	@Override
	protected List<HotelChain> getAllEntitiesFromServer() throws Exception {
		List<HotelChain> hotelChains = null;
		if (serverModel.isLoggedIn()) {
			hotelChains = getHotelChainMgr().readAll();
		}
		else {
			hotelChains = Collections.emptyList();
		}

		return hotelChains;
	}


	public Collection<HotelChain> getAllHotelChains() throws Exception {
		Collection<HotelChain> hotelChains = getAllEntities();
		return hotelChains;
	}


	@Override
	protected HotelChain createEntityOnServer(HotelChain hotelChain) throws Exception {
		hotelChain.validate();
		hotelChain = getHotelChainMgr().create(hotelChain);
		return hotelChain;
	}


	@Override
	public HotelChain create(HotelChain hotelChain) throws Exception {
		return super.create(hotelChain);
	}


	@Override
	protected HotelChain updateEntityOnServer(HotelChain hotelChain) throws Exception {
		hotelChain.validate();
		hotelChain = getHotelChainMgr().update(hotelChain);
		return hotelChain;
	}


	@Override
	public HotelChain update(HotelChain hotelChain) throws Exception {
		return super.update(hotelChain);
	}


	@Override
	protected void deleteEntityOnServer(HotelChain hotelChain) throws Exception {
		if (hotelChain != null) {
			Long hotelChainId = hotelChain.getId();
			getHotelChainMgr().delete(hotelChainId);
		}
	}


	@Override
	public void delete(HotelChain hotelChain) throws Exception {
		super.delete(hotelChain);
	}

}

