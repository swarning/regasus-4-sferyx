package de.regasus.portal;

import static de.regasus.LookupService.getPortalTypeMgr;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import de.regasus.core.model.MICacheModel;

public class PortalTypeModel extends MICacheModel<String, PortalType> {

	private static PortalTypeModel singleton;


	private PortalTypeModel() {
	}


	public static PortalTypeModel getInstance() {
		if (singleton == null) {
			singleton = new PortalTypeModel();
		}
		return singleton;
	}


	@Override
	protected String getKey(PortalType entity) {
		return entity.getId();
	}


	@Override
	protected PortalType getEntityFromServer(String id) throws Exception {
		PortalType portalType = getPortalTypeMgr().read(id);
		return portalType;
	}


	public PortalType getPortalType(String id) throws Exception {
		return super.getEntity(id);
	}


	@Override
	protected List<PortalType> getEntitiesFromServer(Collection<String> portalTypePKs) throws Exception {
		List<PortalType> portalTypeList = getPortalTypeMgr().read(portalTypePKs);
		return portalTypeList;
	}


	public List<PortalType> getPortalTypes(List<String> portalTypePKs) throws Exception {
		return super.getEntities(portalTypePKs);
	}


	@Override
	protected List<PortalType> getAllEntitiesFromServer() throws Exception {
		List<PortalType> portalTypeList = null;

		if (serverModel.isLoggedIn()) {
			portalTypeList = getPortalTypeMgr().readAll();
		}
		else {
			portalTypeList = Collections.emptyList();
		}

		return portalTypeList;
	}


	public List<PortalType> getAllPortalTypes() throws Exception {
		List<PortalType> portalTypeList = getAllEntities();
		Collections.sort(portalTypeList, PortalTypeComparator.getInstance());
		return portalTypeList;
	}


	public List<PortalType> getPortalTypes(boolean eventDependent) throws Exception {
		List<PortalType> allPortalTypeList = getAllEntities();

		List<PortalType> portalTypeList = new ArrayList<PortalType>( allPortalTypeList.size() );
		for (PortalType portalType : allPortalTypeList) {
			if (portalType.isEventDependent() == eventDependent) {
				portalTypeList.add(portalType);
			}
		}

		Collections.sort(portalTypeList, PortalTypeComparator.getInstance());

		return portalTypeList;
	}


	@Override
	protected boolean isForeignKeySupported() {
		return false;
	}

}
