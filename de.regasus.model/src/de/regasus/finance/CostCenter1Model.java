package de.regasus.finance;

import static de.regasus.LookupService.*;

import java.util.Collection;
import java.util.List;

import com.lambdalogic.messeinfo.invoice.data.CostCenterVO;

import de.regasus.core.model.MICacheModel;

public class CostCenter1Model extends MICacheModel<Integer, CostCenterVO> {

	private static CostCenter1Model singleton = null;


	private CostCenter1Model() {
		super();
	}


	public static CostCenter1Model getInstance() {
		if (singleton == null) {
			singleton = new CostCenter1Model();
		}
		return singleton;
	}


	@Override
	protected boolean isForeignKeySupported() {
		return false;
	}


	@Override
	protected Integer getKey(CostCenterVO entity) {
		return entity.getPK();
	}


	@Override
	protected CostCenterVO getEntityFromServer(Integer costCenterNo) throws Exception {
		CostCenterVO costCenterVO = getInvoiceMgr().getCostCenter1VO(costCenterNo);
		return costCenterVO;
	}

	public CostCenterVO getCostCenterVO(Integer costCenterNo) throws Exception {
		return super.getEntity(costCenterNo);
	}

	@Override
	protected List<CostCenterVO> getEntitiesFromServer(Collection<Integer> costCenterNos) throws Exception {
		List<CostCenterVO> costCenterVOs = getInvoiceMgr().getCostCenter1VOs(costCenterNos);
		return costCenterVOs;
	}


	public List<CostCenterVO> getCostCenterVOs(Collection<Integer> costCenterNos) throws Exception {
		return super.getEntities(costCenterNos);
	}


	@Override
	protected List<CostCenterVO> getAllEntitiesFromServer() throws Exception {
		List<CostCenterVO> costCenterVOs = getInvoiceMgr().getCostCenter1VOs();
		return costCenterVOs;
	}


	public Collection<CostCenterVO> getAllCostCenterVOs() throws Exception {
		return getAllEntities();
	}


	@Override
	protected CostCenterVO createEntityOnServer(CostCenterVO costCenterVO) throws Exception {
		costCenterVO.validate();
		costCenterVO = getInvoiceMgr().createCostCenter1(costCenterVO);
		return costCenterVO;
	}


	@Override
	public CostCenterVO create(CostCenterVO costCenterVO) throws Exception {
		return super.create(costCenterVO);
	}


	@Override
	protected CostCenterVO updateEntityOnServer(CostCenterVO costCenterVO) throws Exception {
		costCenterVO.validate();
		costCenterVO = getInvoiceMgr().updateCostCenter1(costCenterVO);
		return costCenterVO;
	}


	@Override
	public CostCenterVO update(CostCenterVO costCenterVO) throws Exception {
		return super.update(costCenterVO);
	}


	@Override
	protected void deleteEntityOnServer(CostCenterVO costCenterVO) throws Exception {
		if (costCenterVO != null) {
			Integer costCenterNo = costCenterVO.getPK();
			getInvoiceMgr().deleteCostCenter1(costCenterNo);
		}
	}

	@Override
	public void delete(CostCenterVO costCenterVO) throws Exception {
		super.delete(costCenterVO);
	}

}
