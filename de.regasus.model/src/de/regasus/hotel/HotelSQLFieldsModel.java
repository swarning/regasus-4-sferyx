package de.regasus.hotel;

import java.util.Collection;

import com.lambdalogic.messeinfo.config.parameterset.ConfigParameterSet;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;

import de.regasus.core.ConfigParameterSetModel;
import de.regasus.core.CountryModel;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.model.Activator;
import de.regasus.core.model.MICacheModel;


@SuppressWarnings("rawtypes")
public class HotelSQLFieldsModel
extends MICacheModel<Long, ClientHotelSearch> {
	
	private static HotelSQLFieldsModel singleton = null;
	
	public static final Long NO_EVENT_KEY = 0L;
	
	
	private CountryModel countryModel;
	private HotelChainModel hotelChainModel;
	private ConfigParameterSetModel configParameterSetModel;
	
	
	private HotelSQLFieldsModel() {
		super();
	}
	

	public static HotelSQLFieldsModel getInstance() {
		if (singleton == null) {
			singleton = new HotelSQLFieldsModel();
			singleton.init();
		}
		return singleton;
	}

	
	private void init() {
		countryModel = CountryModel.getInstance();
		countryModel.addListener(cacheModelListener);

		hotelChainModel = HotelChainModel.getInstance();
		hotelChainModel.addListener(cacheModelListener);

		configParameterSetModel = ConfigParameterSetModel.getInstance();
		// observing the ConfigParameterSetModel is not necessary
	}


	private CacheModelListener cacheModelListener = new CacheModelListener() {
		@Override
		public void dataChange(CacheModelEvent event) {
			if (!serverModel.isLoggedIn()) {
				return;
			}
			
			try {
				if (event.getSource() == countryModel) {
					Collection<ClientHotelSearch> loadedData = getLoadedAndCachedEntities();
					for (ClientHotelSearch clientHotelSearch : loadedData) {
						clientHotelSearch.initCountryValues();
					}
				}
				else if (event.getSource() == hotelChainModel) {
					Collection<ClientHotelSearch> loadedData = getLoadedAndCachedEntities();
					for (ClientHotelSearch clientHotelSearch : loadedData) {
						clientHotelSearch.initHotelChainValues();
					}
				}
				
				fireDataChange();
			}
			catch (Exception e) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			}
		}
	};

		
	@Override
	protected boolean isForeignKeySupported() {
		return false;
	}
	
	
	@Override
	protected Long getKey(ClientHotelSearch entity) {
		Long key = entity.getEventPK();
		if (key == null) {
			key = NO_EVENT_KEY;
		}
		return key;
	}

	
	@Override
	protected ClientHotelSearch getEntityFromServer(Long eventPK) throws Exception {
		ClientHotelSearch hotelSearch = null;
		try {
			if (eventPK == NO_EVENT_KEY || eventPK == null) {
				ConfigParameterSet configParameterSet = configParameterSetModel.getConfigParameterSet();
				hotelSearch = new ClientHotelSearch(null, configParameterSet);
			}
			else {
				ConfigParameterSet configParameterSet = configParameterSetModel.getConfigParameterSet(eventPK);
				hotelSearch = new ClientHotelSearch(eventPK, configParameterSet);
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
		return hotelSearch;
	}

	
	public ClientHotelSearch getHotelSearch(Long eventPK) throws Exception {
		ClientHotelSearch hotelSearch = null;
		if (eventPK != null) {
			hotelSearch = getEntity(eventPK);
		}
		return hotelSearch;
	}
	
}
