package de.regasus.profile;

import java.math.BigDecimal;
import java.util.Collection;

import com.lambdalogic.messeinfo.config.parameterset.ConfigParameterSet;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;

import de.regasus.core.ConfigParameterSetModel;
import de.regasus.core.CountryModel;
import de.regasus.core.CreditCardTypeModel;
import de.regasus.core.LanguageModel;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.model.Activator;
import de.regasus.core.model.MICacheModel;
import de.regasus.email.EmailTemplateModel;

/**
 * Model that manages {@link ClientProfileSearch}.
 * Client can get the instance of {@link ClientProfileSearch}.
 */
@SuppressWarnings("rawtypes")
public class ProfileSQLFieldsModel
extends MICacheModel<Object, ClientProfileSearch> {
	
	private static ProfileSQLFieldsModel singleton = null;
	
	private static final Object KEY = BigDecimal.ZERO;
	
	
	// Models with global data
	private LanguageModel languageModel;
	private CountryModel countryModel;
	private CreditCardTypeModel creditCardTypeModel;
	/* It is not necessary to observe ProfileCustomFieldGroupModel, because if the Group of a ProfileCustomField
	 * changes, ProfileCustomFieldModel will fire a CacheModelEvent, too.
	 */
	private ProfileCustomFieldModel profileCustomFieldModel;
	private ProfileRoleModel profileRoleModel;
	private EmailTemplateModel emailTemplateModel;
	private ConfigParameterSetModel configParameterSetModel;

	
	private ProfileSQLFieldsModel() {
		super();
	}
	
	
	public static ProfileSQLFieldsModel getInstance() {
		if (singleton == null) {
			singleton = new ProfileSQLFieldsModel();
			singleton.init();
		}
		return singleton;
	}

	
	private void init() {
		// init Models with global data
		
		languageModel = LanguageModel.getInstance();
		languageModel.addListener(cacheModelListener);
		
		countryModel = CountryModel.getInstance();
		countryModel.addListener(cacheModelListener);
		
		creditCardTypeModel = CreditCardTypeModel.getInstance();
		creditCardTypeModel.addListener(cacheModelListener);
		
		profileCustomFieldModel = ProfileCustomFieldModel.getInstance();
		profileCustomFieldModel.addListener(cacheModelListener);
		
		profileRoleModel = ProfileRoleModel.getInstance();
		profileRoleModel.addListener(cacheModelListener);
		
		emailTemplateModel = EmailTemplateModel.getInstance();
		emailTemplateModel.addListener(cacheModelListener);
		
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
				// Models with global data (independent from Event)
				if (event.getSource() == countryModel) {
					// init Country values in all ClientProfileSearch
					Collection<ClientProfileSearch> loadedData = getLoadedAndCachedEntities();
					for (ClientProfileSearch clientProfileSearch : loadedData) {
						clientProfileSearch.initCountryValues();
					}
				}
				else if (event.getSource() == languageModel) {
					// init Language values in all ClientProfileSearch
					Collection<ClientProfileSearch> loadedData = getLoadedAndCachedEntities();
					for (ClientProfileSearch clientProfileSearch : loadedData) {
						clientProfileSearch.initLanguageValues();
					}
				}
				else if (event.getSource() == creditCardTypeModel) {
					// init Credit Card Type values in all ClientProfileSearch
					Collection<ClientProfileSearch> loadedData = getLoadedAndCachedEntities();
					for (ClientProfileSearch clientProfileSearch : loadedData) {
						clientProfileSearch.initCreditCardTypeValues();
					}
				}
				else if (event.getSource() == profileCustomFieldModel) {
					// init Profile Custom Fields in all ClientProfileSearch
					for (ClientProfileSearch clientProfileSearch : getLoadedAndCachedEntities()) {
						clientProfileSearch.initCustomFieldSQLFields();
					}
				}
				else if (event.getSource() == emailTemplateModel) {
					// init Email fields in all ClientProfileSearch
					for (ClientProfileSearch clientProfileSearch : getLoadedAndCachedEntities()) {
						clientProfileSearch.initEmailFields();
					}
				}
				else if(event.getSource() == profileRoleModel) {
					//init profile role fields in all ClientProfileSearch
					for (ClientProfileSearch clientProfileSearch : getLoadedAndCachedEntities()) {
						clientProfileSearch.initProfileRoles();
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
	protected Object getKey(ClientProfileSearch entity) {
		return KEY;
	}
	

	@Override
	protected ClientProfileSearch getEntityFromServer(Object primaryKey) throws Exception {
		ClientProfileSearch profileSearch = null;
		try {
			ConfigParameterSet configParameterSet = configParameterSetModel.getConfigParameterSet();
			profileSearch = new ClientProfileSearch(true, configParameterSet);
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
		return profileSearch;
	}

	
	public ClientProfileSearch getProfileSearch() throws Exception {
		ClientProfileSearch profileSearch = getEntity(KEY);
		return profileSearch;
	}

}
