/**
 * GateModel.java
 * created on 24.09.2013 10:33:52
 */
package de.regasus.event;

import static de.regasus.LookupService.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.lambdalogic.messeinfo.participant.data.GateVO;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.model.CacheModelOperation;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.model.MICacheModel;
import de.regasus.model.Activator;

public class GateModel 
extends MICacheModel<Long, GateVO>
implements CacheModelListener<Long> {
	
	private static GateModel singleton = null;
	
	private LocationModel locationModel;
	
	
	private GateModel() {
		super();
		
		locationModel = LocationModel.getInstance();
		locationModel.addListener(this);
	}
	
	
	public static GateModel getInstance() {
		if (singleton == null) {
			singleton = new GateModel();
		}
		return singleton;
	}
	

	@Override
	public void dataChange(CacheModelEvent<Long> event) {
		if (!serverModel.isLoggedIn()) {
			return;
		}
		
		try {
			if (event.getSource() == locationModel && event.getOperation() == CacheModelOperation.DELETE) {
				Collection<Long> deletedGatePKs = new ArrayList<Long>();
				
				for (Long locationPK : event.getKeyList()) {
					for (GateVO gateVO : getLoadedAndCachedEntities()) {
						if (locationPK.equals(gateVO.getLocationPK())) {
							deletedGatePKs.add(gateVO.getPK());
						}
					}
				}
				
				if (!deletedGatePKs.isEmpty()) {
					fireDataChange(CacheModelOperation.DELETE, deletedGatePKs);
				}
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}

	
	@Override
	protected Long getKey(GateVO gateVO) {
		return gateVO.getID();
	}

	
	@Override
	protected GateVO getEntityFromServer(Long gatePK) throws Exception {
		GateVO gateVO = getGateMgr().getGateVO(gatePK);
		
		return gateVO;
	}

	
	public GateVO getGateVO(Long gatePK) throws Exception {
		return super.getEntity(gatePK);
	}
	
	
	@Override
	protected GateVO createEntityOnServer(GateVO gateVO) throws Exception {
		gateVO.validate();
		gateVO = getGateMgr().createGate(gateVO);
		return gateVO;
	}
	
	
	public GateVO create(GateVO gateVO) throws Exception {
		return super.create(gateVO);
	}
	
	
	@Override
	protected GateVO updateEntityOnServer(GateVO gateVO) throws Exception {
		gateVO.validate();
		gateVO = getGateMgr().updateGate(gateVO);
		return gateVO;
	}
	
	
	public GateVO update(GateVO gateVO) throws Exception {
		return super.update(gateVO);
	}
	
	
	@Override
	protected void deleteEntityOnServer(GateVO gateVO) throws Exception {
		if (gateVO != null) {
			Long gatePK = gateVO.getID();
			getGateMgr().deleteGate(gatePK);
		}
	}
	
	
	public void delete(GateVO gateVO) throws Exception {
		super.delete(gateVO);
	}


	@Override
	protected boolean isForeignKeySupported() {
		return true;
	}

	
	@Override
	protected Object getForeignKey(GateVO gateVO) {
		Long fk = null;
		if (gateVO != null) {
			fk = gateVO.getLocationPK();
		}
		return fk;
	}
	
	
	@Override
	protected List<GateVO> getEntitiesByForeignKeyFromServer(Object foreignKey) throws Exception {
		Long locationPK = (Long) foreignKey;
		List<GateVO> gateVOList = getGateMgr().getGateVOsByLocationPK(locationPK);
		
		return gateVOList;
	}
	
	
	public List<GateVO> getGateVOsByLocationPK(Long locationPK) throws Exception {
		return getEntityListByForeignKey(locationPK);
	}
	
	
	@Override
	protected List<GateVO> getEntitiesFromServer(Collection<Long> gatePKs) throws Exception {
		List<GateVO> gateVOList = getGateMgr().getGateVOs(gatePKs);
		
		return gateVOList;
	}
	
	
	public List<GateVO> getGateVOs(List<Long> gatePKs) throws Exception {
		List<GateVO> gateVOList = super.getEntities(gatePKs);
		return gateVOList;
	}
	
}
