package de.regasus.core;

import static com.lambdalogic.util.StringHelper.isEmpty;
import static de.regasus.LookupService.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import com.lambdalogic.messeinfo.kernel.interfaces.IKernelManager;
import com.lambdalogic.messeinfo.kernel.interfaces.ServerRole;
import com.lambdalogic.util.EqualsHelper;
import com.lambdalogic.util.StringHelper;
import com.lambdalogic.util.TypeHelper;

import de.regasus.common.Property;
import de.regasus.core.model.MICacheModel;

/**
 * Model for the data stored in Table PROPERTY.
 */
public class PropertyModel extends MICacheModel<String, Property> {

	private static PropertyModel singleton = null;

	private PropertyModel() {
	}


	public static PropertyModel getInstance() {
		if (singleton == null) {
			singleton = new PropertyModel();
		}
		return singleton;
	}


	@Override
	protected boolean isForeignKeySupported() {
		return false;
	}


	@Override
	protected String getKey(Property entity) {
		return entity.getKey();
	}


	@Override
	protected Property getEntityFromServer(String key) throws Exception {
		Property property = getPropertyMgr().read(key);
		if (property != null) {
			return property;
		}
		else {
			return new Property(key, "");
		}

	}


	public Property getProperty(String key) throws Exception {
		return super.getEntity(key);
	}


	public String getPropertyValue(String key) throws Exception {
		String propertyValue = null;
		if (key != null) {
			Property property = getProperty(key);
			if (property != null) {
				propertyValue = property.getValue();

		    	// do not cache empty value
		    	if ( isEmpty(propertyValue) ) {
		    		removeEntity(key);
		    	}
			}
		}

		return propertyValue;
	}


	@Override
	protected List<Property> getEntitiesFromServer(Collection<String> keyCol) throws Exception {
		List<Property> propertyList = getPropertyMgr().read(keyCol);
		return propertyList;
	}


	public List<Property> getPropertyList(Collection<String> keyCol) throws Exception {
		return super.getEntities(keyCol);
	}


	@Override
	protected List<Property> getAllEntitiesFromServer() throws Exception {
		Collection<Property> propertyCol = getPropertyMgr().readAllPublic();
		return new ArrayList<>(propertyCol);
	}


	public List<Property> getPublicPropertyList() throws Exception {
		Collection<Property> allPropertyList = getAllEntities();
		List<Property> publicPropertyList = new ArrayList<>(allPropertyList.size());

		for (Property property : allPropertyList) {
			if (property.isPublicValue()) {
				publicPropertyList.add(property);
			}
		}

		return publicPropertyList;
	}


	@Override
	protected Property updateEntityOnServer(Property entity) throws Exception {
		getPropertyMgr().update(entity);
		return entity;
	}

	// There are still places that want to update without caring about server IDs
	public void update(Collection<Property> propertyList) throws Exception {
		updatePropertyList_internal(propertyList);
		refresh();
	}


	public void update(Collection<Property> propertyList, int serverID) throws Exception {
		updatePropertyList_internal(propertyList);

		if (ServerModel.getInstance().isLoggedIn()) {
			getKernelMgr().setServerID(serverID);
		}
		refresh();
	}

	private void updatePropertyList_internal(Collection<Property> propertyList) throws Exception {
		if (propertyList != null && ServerModel.getInstance().isLoggedIn()) {
			/* Compare values in the parameter propertyList with those in propertyCache to determine which ones
			 * actually have changed. To avoid side effects with changes made by other users only those property
			 * values are stored on the server that have been changed by this user.
			 */
			List<Property> editedPropertyList = new ArrayList<>( propertyList.size() );

			// assure that all properties are loaded
			getAllEntities();

			for (Property property : propertyList) {
				String key = property.getKey();

				String newValue = property.getValue();
				newValue = StringHelper.trim(newValue);

				String oldValue = getPropertyValue(key);
				oldValue = StringHelper.trim(oldValue);

				if ( ! EqualsHelper.isEqual(oldValue, newValue) ) {
					editedPropertyList.add(property);
				}
			}

			if (!editedPropertyList.isEmpty()) {
				getPropertyMgr().update(editedPropertyList);
			}
		}
	}

	// **************************************************************************
	// * Getter for special properties
	// *

	public Integer getServerID() throws Exception {
		Integer serverID = getKernelMgr().getServerID();
        return serverID;
	}


    /**
     * The ServerRole defines if the server is a main or remote system.
     * The setting is important when importing data. Remote systems will by default overwrite secondary data (like
     * country data when importing an event) while main systems will not. However, this setting is only setting the
     * default value in the corresponding import dialog and users can change the setting.
     *
     * The ServerRole is set in the table PROPERTY with the key {@link IKernelManager#PROPERTY_KEY_SERVER_ROLE}.
     * Accepted values are "main" and "remote" (case insensitive).
     * The default value if no value exists or any other value is set is MAIN, because it means less damage if the
     * user doesn't check the setting in the import dialog.
     */
	public ServerRole getServerRole() throws Exception {
		/* This is almost a copy of KernelManagerBean.getServerRole().
		 * We don't call that method to get advantage of the model cache.
		 */
		ServerRole serverRole = ServerRole.MAIN;

		String strServerRole = getPropertyValue(CorePropertyKey.SERVER_ROLE);
		if (strServerRole != null) {
			strServerRole = strServerRole.toUpperCase();

			try {
				serverRole = ServerRole.valueOf(strServerRole);
			}
			catch (Exception e) {
				com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
			}
		}

		return serverRole;
    }


    public String getDefaultLanguages() throws Exception {
        String defaultLanguages = getPropertyValue(CorePropertyKey.DEFAULT_LANGUAGES);
        return defaultLanguages;
    }


	public List<String> getDefaultLanguageList() throws Exception {
		List<String> languageList = new ArrayList<>();

		// get default languages from table PROPERTY as comma separated String
		String langCodes = getDefaultLanguages();

		// alternatively get system default language
		if (StringHelper.isEmpty(langCodes)) {
			langCodes = Locale.getDefault().getLanguage();
		}


		// convert comma separated String into List<String>
		List<String> defaultLanguageCodeList = StringHelper.getSegments(langCodes);
		for (String languageCode : defaultLanguageCodeList) {
			if (languageCode != null && languageCode.length() == 2) {
				languageList.add(languageCode);
			}
		}

		// if the language codes from table PROPERTY are not valid
		if (languageList.isEmpty()) {
			String languageCode = Locale.getDefault().getLanguage();
			languageList.add(languageCode);
		}

		return languageList;
	}


    public String getDefaultCountry() throws Exception {
    	return getPropertyValue(CorePropertyKey.DEFAULT_COUNTRY);
    }


    public String getDefaultCurrency() throws Exception {
    	return getPropertyValue(CorePropertyKey.DEFAULT_CURRENCY);
    }


    /**
     * Returns true if a property with the given key exists and has a value that can be evaluated to TRUE.
     */
    public boolean isAvailable(String key) {
    	try {
			Property property = getProperty(key);

			if (property != null) {
				String propertyValue = StringHelper.trim(property.getValue());
				return TypeHelper.toBoolean(propertyValue, false);
			}
		}
		catch (Exception e) {
			com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
		}
    	return false;
    }

	// *
	// * Getter for special properties
	// **************************************************************************

}
