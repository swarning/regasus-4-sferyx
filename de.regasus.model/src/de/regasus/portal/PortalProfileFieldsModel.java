package de.regasus.portal;

import static de.regasus.LookupService.*;

import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;

import de.regasus.core.model.MICacheModel;
import de.regasus.portal.profile.PortalProfileFields;
import de.regasus.profile.ProfileCustomFieldGroupModel;
import de.regasus.profile.ProfileCustomFieldModel;

public class PortalProfileFieldsModel extends MICacheModel<Long, PortalProfileFields> {

	/**
	 * There is only one instance of PortalProfileFields which does not have a key.
	 * To avoid null values we use KEY instead.
	 */
	private static final Long KEY = Long.valueOf(0);

	private static PortalProfileFieldsModel singleton;

	private ProfileCustomFieldGroupModel prCFGrpModel;

	private ProfileCustomFieldModel prCFModel;


	public static PortalProfileFieldsModel getInstance() {
		if (singleton == null) {
			singleton = new PortalProfileFieldsModel();
		}
		return singleton;
	}


	private PortalProfileFieldsModel() {
		super();

		prCFGrpModel = ProfileCustomFieldGroupModel.getInstance();
		prCFGrpModel.addListener(groupModelListener);

		prCFModel = ProfileCustomFieldModel.getInstance();
		prCFModel.addListener(customFieldModelListener);
	}


	private CacheModelListener<Long> groupModelListener = new CacheModelListener<Long>() {
		@Override
		public void dataChange(CacheModelEvent<Long> event) throws Exception {
			refresh();
		}
	};


	private CacheModelListener<Long> customFieldModelListener = new CacheModelListener<Long>() {
		@Override
		public void dataChange(CacheModelEvent<Long> event) throws Exception {
			refresh();
		}
	};


	@Override
	protected Long getKey(PortalProfileFields fields) {
		return KEY;
	}


	@Override
	protected PortalProfileFields getEntityFromServer(Long obsoletID) throws Exception {
		PortalProfileFields fields = getPortalProfileFieldsMgr().getPortalProfileFields();
		return fields;
	}


	public PortalProfileFields getPortalProfileFields() throws Exception {
		return super.getEntity(KEY);
	}


	@Override
	protected boolean isForeignKeySupported() {
		return false;
	}

}
