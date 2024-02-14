/**
 * GateDeviceModel.java
 * created on 24.09.2013 16:23:20
 */
package de.regasus.common;

import static de.regasus.LookupService.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.lambdalogic.messeinfo.participant.data.GateDeviceVO;

import de.regasus.core.model.MICacheModel;

public class GateDeviceModel 
extends MICacheModel<Long, GateDeviceVO> {
	
	private static GateDeviceModel singleton = null;
	
	
	private GateDeviceModel() {
		super();
	}
	
	
	public static GateDeviceModel getInstance() {
		if (singleton == null) {
			singleton = new GateDeviceModel();
		}
		return singleton;
	}


	@Override
	protected boolean isForeignKeySupported() {
		return true;
	}

	
	@Override
	protected Object getForeignKey(GateDeviceVO gateDeviceVO) {
		Long fk = null;
		if (gateDeviceVO != null) {
			fk = gateDeviceVO.getCurrentGatePK();
		}
		return fk;
	}
	
	
	public GateDeviceVO create(GateDeviceVO gateDeviceVO) throws Exception {
		return super.create(gateDeviceVO);
	}
	
	
	@Override
	protected GateDeviceVO createEntityOnServer(GateDeviceVO gateDeviceVO) throws Exception {
		gateDeviceVO.validate();
		gateDeviceVO = getGateDeviceMgr().createGateDevice(gateDeviceVO);
		return gateDeviceVO;
	}
	
	
	public GateDeviceVO update(GateDeviceVO gateDeviceVO) throws Exception {
		return super.update(gateDeviceVO);
	}
	
	
	@Override
	protected GateDeviceVO updateEntityOnServer(GateDeviceVO gateDeviceVO) throws Exception {
		gateDeviceVO.validate();
		gateDeviceVO = getGateDeviceMgr().updateGateDevice(gateDeviceVO);
		return gateDeviceVO;
	}
	
	
	@Override
	protected void deleteEntityOnServer(GateDeviceVO gateDeviceVO) throws Exception {
		if (gateDeviceVO != null) {
			Long gateDevicePK = gateDeviceVO.getPK();
			getGateDeviceMgr().deleteGateDevice(gateDevicePK);
		}
	}
	
	
	public void delete(GateDeviceVO gateDeviceVO) throws Exception {
		super.delete(gateDeviceVO);
	}

	
	@Override
	protected Long getKey(GateDeviceVO gateDeviceVO) {
		return gateDeviceVO.getID();
	}

	
	@Override
	protected GateDeviceVO getEntityFromServer(Long gateDevicePK) throws Exception {
		GateDeviceVO gateDeviceVO = getGateDeviceMgr().getGateDeviceVO(gateDevicePK);
		return gateDeviceVO;
	}
	
	
	@Override
	protected List<GateDeviceVO> getAllEntitiesFromServer() throws Exception {
		List<GateDeviceVO> gateDeviceVOList = null;
		
		if (serverModel.isLoggedIn()) {
			gateDeviceVOList = getGateDeviceMgr().getGateDeviceVOs(false);
		}
		else {
			gateDeviceVOList = Collections.emptyList();
		}
		
		return gateDeviceVOList;
	}
	
	
	public Collection<GateDeviceVO> getAllUndeletedGateDeviceVOs() throws Exception {
		Collection<GateDeviceVO> allGateDeviceVOs = getAllEntities();
		List<GateDeviceVO> undeletedGateDeviceVOs = new ArrayList<GateDeviceVO>(allGateDeviceVOs.size());
		for (GateDeviceVO gateDeviceVO : allGateDeviceVOs) {
			if (!gateDeviceVO.isDeleted()) {
				undeletedGateDeviceVOs.add(gateDeviceVO);
			}
		}
		return undeletedGateDeviceVOs;
	}
	
	
	public GateDeviceVO getGateDeviceVO(Long gateDevicePK) throws Exception {
		return super.getEntity(gateDevicePK);
	}
	
}
