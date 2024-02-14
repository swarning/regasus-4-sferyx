package de.regasus.finance;

import static de.regasus.LookupService.*;

import java.util.Collection;
import java.util.List;

import com.lambdalogic.messeinfo.invoice.data.ImpersonalAccountVO;

import de.regasus.core.model.MICacheModel;

public class ImpersonalAccountModel 
extends MICacheModel<Integer, ImpersonalAccountVO> {
	private static ImpersonalAccountModel singleton = null;


	private ImpersonalAccountModel() {
		super();
	}


	public static ImpersonalAccountModel getInstance() {
		if (singleton == null) {
			singleton = new ImpersonalAccountModel();
		}
		return singleton;
	}


	@Override
	protected boolean isForeignKeySupported() {
		return false;
	}


	@Override
	protected Integer getKey(ImpersonalAccountVO entity) {
		return entity.getPK();
	}

	
	@Override
	protected ImpersonalAccountVO getEntityFromServer(Integer impersonalAccountNo) throws Exception {
		ImpersonalAccountVO impersonalAccountVO = getInvoiceMgr().getImpersonalAccountVO(impersonalAccountNo);
		return impersonalAccountVO;
	}

	public ImpersonalAccountVO getImpersonalAccountVO(Integer impersonalAccountNo) throws Exception {
		return super.getEntity(impersonalAccountNo);
	}
	
	@Override
	protected List<ImpersonalAccountVO> getEntitiesFromServer(Collection<Integer> impersonalAccountNos) throws Exception {
		List<ImpersonalAccountVO> impersonalAccountVOs = getInvoiceMgr().getImpersonalAccountVOs(impersonalAccountNos);
		return impersonalAccountVOs;
	}


	public List<ImpersonalAccountVO> getImpersonalAccountVOs(Collection<Integer> impersonalAccountNos) throws Exception {
		return super.getEntities(impersonalAccountNos);
	}

	@Override
	protected List<ImpersonalAccountVO> getAllEntitiesFromServer() throws Exception {
		List<ImpersonalAccountVO> impersonalAccountVOs = getInvoiceMgr().getImpersonalAccountVOs();
		return impersonalAccountVOs;
	}

	
	public Collection<ImpersonalAccountVO> getAllImpersonalAccountVOs() throws Exception {
		return getAllEntities();
	}

	@Override
	protected ImpersonalAccountVO createEntityOnServer(ImpersonalAccountVO impersonalAccountVO) throws Exception {
		impersonalAccountVO.validate();
		impersonalAccountVO = getInvoiceMgr().createImpersonalAccount(impersonalAccountVO);
		return impersonalAccountVO;
	}

	
	public ImpersonalAccountVO create(ImpersonalAccountVO impersonalAccountVO) throws Exception {
		return super.create(impersonalAccountVO);
	}

	
	@Override
	protected ImpersonalAccountVO updateEntityOnServer(ImpersonalAccountVO impersonalAccountVO) throws Exception {
		impersonalAccountVO.validate();
		impersonalAccountVO = getInvoiceMgr().updateImpersonalAccount(impersonalAccountVO);
		return impersonalAccountVO;
	}

	
	public ImpersonalAccountVO update(ImpersonalAccountVO impersonalAccountVO) throws Exception {
		return super.update(impersonalAccountVO);
	}

	
	@Override
	protected void deleteEntityOnServer(ImpersonalAccountVO impersonalAccountVO) throws Exception {
		if (impersonalAccountVO != null) {
			Integer impersonalAccountNo = impersonalAccountVO.getPK();
			getInvoiceMgr().deleteImpersonalAccount(impersonalAccountNo);
		}
	}

	public void delete(ImpersonalAccountVO impersonalAccountVO) throws Exception {
		super.delete(impersonalAccountVO);
	}

}
