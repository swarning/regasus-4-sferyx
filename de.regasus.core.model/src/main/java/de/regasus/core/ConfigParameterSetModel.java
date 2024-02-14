package de.regasus.core;

import com.lambdalogic.messeinfo.config.Config;
import com.lambdalogic.messeinfo.config.parameterset.ConfigParameterSet;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.model.Activator;
import de.regasus.core.model.MICacheModel;

public class ConfigParameterSetModel
extends MICacheModel<Long, ConfigParameterSet>
implements CacheModelListener<Long> {

	private static ConfigParameterSetModel singleton;

	/**
	 * Dummy value for ConfigurationSets without an event context.
	 */
	private static final Long NULL_EVENT_PK = 0L;

	private ConfigModel configModel;


	private ConfigParameterSetModel() {
		configModel = ConfigModel.getInstance();
		configModel.addListener(this);
	}


	public static ConfigParameterSetModel getInstance() {
		if (singleton == null) {
			singleton = new ConfigParameterSetModel();
		}
		return singleton;
	}


	@Override
	protected boolean isForeignKeySupported() {
		return false;
	}


	@Override
	protected Long getKey(ConfigParameterSet entity) {
		Long key = entity.getEventPK();
		if (key == null) {
			key = NULL_EVENT_PK;
		}
		return key;
	}


	@Override
	protected ConfigParameterSet getEntityFromServer(Long eventPK) throws Exception {
		Config globalCustomerConfig = configModel.getGlobalCustomerConfig();

		Config eventCustomerConfig = null;
 		if (eventPK != NULL_EVENT_PK) {
			eventCustomerConfig = configModel.getEventCustomerConfig(eventPK);
		}

//		List<Config> groupConfigList = null;
//		Config userConfig = null;
//		{
//			UserAccountCVO userAccountCVO = getUserMgr().getCurrentUserAccountCVO();
//
//			/* TODO: Load UserAccountCVO from a model, unfortunately the UserAccountModel is not
//			 * visible, because it belongs to the com.lambdalogic.users.ui
//			 *
//			 * String user = ServerModel.getInstance().getUser();;
//			 * UserAccountModel.getInstance().get...
//			 */
//
//			// get Config of Groups
//			List<String> groupIDs = UserGroupVO.getPKs(userAccountCVO.getUserGroupVOs());
//			groupConfigList = new ArrayList<Config>(groupIDs.size());
//			for (String groupID : groupIDs) {
//				Config groupConfig = configModel.getGroupConfig(groupID);
//				if (groupConfig != null) {
//					groupConfigList.add(groupConfig);
//				}
//			}
//
//			// get Config of User
//			userConfig = configModel.getUserConfig(userAccountCVO.getPK().getLongValue());
//		}


		ConfigParameterSet configParameterSet = new ConfigParameterSet(
			globalCustomerConfig,	// globalCustomerConfig
			eventCustomerConfig		// eventCustomerConfig
//			groupConfigList,		// groupConfigList
//			userConfig				// userConfig
		);

		configParameterSet.setEventPK(eventPK);

		return configParameterSet;
	}


	public ConfigParameterSet getConfigParameterSet() throws Exception {
		return getConfigParameterSet(NULL_EVENT_PK);
	}


	public ConfigParameterSet getConfigParameterSet(Long eventPK) throws Exception {
		if (eventPK == null) {
			eventPK = NULL_EVENT_PK;
		}
		return super.getEntity(eventPK);
	}


	@Override
	public void dataChange(CacheModelEvent<Long> event) {
		if (!serverModel.isLoggedIn()) {
			return;
		}

		try {
    		if (event.getSource() == configModel) {
    			refresh();
    		}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}

}
