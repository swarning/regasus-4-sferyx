package de.regasus.hotel;

import java.util.Collection;

import com.lambdalogic.messeinfo.config.parameterset.ConfigParameterSet;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;

import de.regasus.core.ConfigParameterSetModel;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.model.Activator;
import de.regasus.core.model.MICacheModel;


@SuppressWarnings("rawtypes")
public class HotelContingentSQLFieldsModel extends MICacheModel<Long, ClientHotelContingentSearch> {
	
	private static HotelContingentSQLFieldsModel singleton = null;
	
	public static final Long NO_EVENT_KEY = 0L;
	
	
	private HotelChainModel hotelChainModel;
	private ConfigParameterSetModel configParameterSetModel;
	
	
	private HotelContingentSQLFieldsModel() {
		super();
	}
	

	public static HotelContingentSQLFieldsModel getInstance() {
		if (singleton == null) {
			singleton = new HotelContingentSQLFieldsModel();
			singleton.init();
		}
		return singleton;
	}

	
	private void init() {
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
				if (event.getSource() == hotelChainModel) {
					Collection<ClientHotelContingentSearch> loadedData = getLoadedAndCachedEntities();
					for (ClientHotelContingentSearch clientHotelContingentSearch : loadedData) {
						clientHotelContingentSearch.initHotelChainValues();
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
	protected Long getKey(ClientHotelContingentSearch entity) {
		Long key = entity.getEventPK();
		if (key == null) {
			key = NO_EVENT_KEY;
		}
		return key;
	}

	
	@Override
	protected ClientHotelContingentSearch getEntityFromServer(Long eventPK) throws Exception {
		ClientHotelContingentSearch hotelContingentSearch = null;
		try {
			if (eventPK == NO_EVENT_KEY || eventPK == null) {
				ConfigParameterSet configParameterSet = configParameterSetModel.getConfigParameterSet();
				hotelContingentSearch = new ClientHotelContingentSearch(null, configParameterSet);
			}
			else {
				ConfigParameterSet configParameterSet = configParameterSetModel.getConfigParameterSet(eventPK);
				hotelContingentSearch = new ClientHotelContingentSearch(eventPK, configParameterSet);
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
		return hotelContingentSearch;
	}

	
	public ClientHotelContingentSearch getHotelContingentSearch(Long eventPK) throws Exception {
		ClientHotelContingentSearch hotelContingentSearch = null;
		if (eventPK != null) {
			hotelContingentSearch = getEntity(eventPK);
		}
		return hotelContingentSearch;
	}

}
