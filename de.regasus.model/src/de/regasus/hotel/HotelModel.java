package de.regasus.hotel;

import static de.regasus.LookupService.getHotelMgr;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.lambdalogic.messeinfo.contact.Address;
import com.lambdalogic.messeinfo.hotel.Hotel;
import com.lambdalogic.messeinfo.hotel.data.HotelCVO;
import com.lambdalogic.messeinfo.hotel.data.HotelCVOSettings;
import com.lambdalogic.messeinfo.hotel.data.HotelVO;
import com.lambdalogic.messeinfo.salutation.AddressLabelGenerator;

import de.regasus.common.CountryCity;
import de.regasus.core.ClientCountryNameProvider;
import de.regasus.core.model.MICacheModel;
import de.regasus.person.AddressLabelGeneratorModel;

public class HotelModel extends MICacheModel<Long, Hotel> {

	private static HotelModel singleton;

	private static HotelCVOSettings hotelCVOSettings;


	static {
		// set ClientCountryNameProvider to take advantage of local caches
		AddressLabelGenerator.setCountryNameProvider(ClientCountryNameProvider.getInstance());

		hotelCVOSettings = new HotelCVOSettings();
	}


	public static Hotel getInitialHotel() throws Exception {
		Hotel hotel = new Hotel();

		prepareHotel(hotel);

    	return hotel;
	}


	private HotelModel() {
		super();
	}


	public static HotelModel getInstance() {
		if (singleton == null) {
			singleton = new HotelModel();
		}
		return singleton;
	}


	@Override
	protected Long getKey(Hotel entity) {
		return entity.getID();
	}


	private static void prepareHotel(Hotel hotel) throws Exception {
		if (hotel != null) {
			// set AddressLabelGenerator
			hotel.setAddressLabelGenerator(AddressLabelGeneratorModel.getInstance().getAddressLabelGenerator());
		}
	}


	@Override
	protected Hotel getEntityFromServer(Long hotelID) throws Exception {
		Hotel hotel = null;

		if (hotelID != null) {
    		HotelCVO hotelCVO = getHotelMgr().getHotelCVO(hotelID, hotelCVOSettings);
    		hotel = getHotel(hotelCVO);
		}

		return hotel;
	}


	public Hotel getHotel(Long hotelID) throws Exception {
		return super.getEntity(hotelID);
	}


	@Override
	protected List<Hotel> getEntitiesFromServer(Collection<Long> hotelIDs) throws Exception {
		List<Hotel> hotelList = null;

		if (hotelIDs != null) {
    		List<HotelCVO> hotelCVOs = getHotelMgr().getHotelCVOs(hotelIDs, hotelCVOSettings);
    		hotelList = getHotelList(hotelCVOs);
		}

		return hotelList;
	}


	public List<Hotel> getHotels(Collection<Long> hotelIDs) throws Exception {
		return super.getEntities(hotelIDs);
	}


	@Override
	protected Hotel createEntityOnServer(Hotel hotel) throws Exception {
		if (hotel != null) {
			HotelVO hotelVO = hotel.getHotelVO();
			hotelVO.validate();
			hotelVO = getHotelMgr().createHotel(hotelVO);
			hotel = new Hotel(hotelVO);
			prepareHotel(hotel);

			put(hotel);
		}

		return hotel;
	}


	@Override
	public Hotel create(Hotel hotel) throws Exception {
		return super.create(hotel);
	}


	@Override
	protected Hotel updateEntityOnServer(Hotel hotel) throws Exception {
		HotelVO hotelVO = hotel.getHotelVO();
		hotelVO.validate();
		hotelVO = getHotelMgr().updateHotel(hotelVO);
		hotel = new Hotel(hotelVO);
		prepareHotel(hotel);

		put(hotel);

		return hotel;
	}


	@Override
	public Hotel update(Hotel hotel) throws Exception {
		return super.update(hotel);
	}


	@Override
	protected void deleteEntityOnServer(Hotel hotel) throws Exception {
		if (hotel != null) {
			Long hotelID = hotel.getID();
			if (hotelID != null) {
				getHotelMgr().deleteHotel(hotelID);
			}
		}
	}


	@Override
	public void delete(Hotel hotel) throws Exception {
		super.delete(hotel);
	}


	@Override
	protected boolean isForeignKeySupported() {
		return true;
	}


	@Override
	protected Object getForeignKey(Hotel hotel) {
		CountryCity fk = null;
		if (hotel != null) {
			Address mainAddress = hotel.getMainAddress();
			String city = mainAddress.getCity();
			String countryCode = mainAddress.getCountryPK();
			if (city != null && countryCode != null) {
				fk = new CountryCity(city, countryCode);
			}
		}
		return fk;
	}


	@Override
	protected List<Hotel> getEntitiesByForeignKeyFromServer(Object foreignKey) throws Exception {
		CountryCity countryCity = (CountryCity) foreignKey;

		// HotelForCountrySelectionDialogHelper wants to know all hotels in a country.
		// Up to know, this was asked for directly via the LoadHelper from HotelManagerBean.
		// When asking this model, we get a CountryCity-key without a city, thus
		// we must prepare an empty set, rather then a singleton set containing null.
		Set<String> cityNames;
		Set<String> countryPKs;

		if (countryCity.getCity() != null) {
			cityNames = Collections.singleton(countryCity.getCity());
		}
		else {
			cityNames = Collections.emptySet();
		}

		if (countryCity.getCountryCode() != null) {
			countryPKs = Collections.singleton(countryCity.getCountryCode());
		}
		else {
			countryPKs = Collections.emptySet();
		}

		List<HotelCVO> hotelCVOs = getHotelMgr().getHotelCVOs(
			hotelCVOSettings,
			null,	// eventPK
			countryPKs,
			cityNames,
			false	// withDeleted
		);

		List<Hotel> hotelList = getHotelList(hotelCVOs);
		return hotelList;
	}


	public List<Hotel> getHotelsByCityKey(CountryCity countryCity) throws Exception {
		return getEntityListByForeignKey(countryCity);
	}


	private static Hotel getHotel(HotelCVO hotelCVO) throws Exception {
		Hotel hotel = null;
		if (hotelCVO != null) {
			hotel = hotelCVO.getHotelVO().getHotel();
			prepareHotel(hotel);
		}
		return hotel;
	}


	private static List<Hotel> getHotelList(List<HotelCVO> hotelCVOs) throws Exception {
		List<Hotel> hotelList = null;
		if (hotelCVOs != null) {
			hotelList = new ArrayList<Hotel>(hotelCVOs.size());
			for (HotelCVO hotelCVO : hotelCVOs) {
				Hotel hotel = getHotel(hotelCVO);
				hotelList.add(hotel);
			}
		}
		return hotelList;
	}

}
