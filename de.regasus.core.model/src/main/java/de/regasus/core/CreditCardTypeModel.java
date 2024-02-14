package de.regasus.core;

import static de.regasus.LookupService.*;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.lambdalogic.messeinfo.contact.data.CreditCardTypeVO;

import de.regasus.core.model.MICacheModel;

public class CreditCardTypeModel
extends MICacheModel<Long, CreditCardTypeVO> {
	private static CreditCardTypeModel singleton = null;
	
	
	private CreditCardTypeModel() {
		super();
	}
	
	
	public static CreditCardTypeModel getInstance() {
		if (singleton == null) {
			singleton = new CreditCardTypeModel();
		}
		return singleton;
	}


	@Override
	protected boolean isForeignKeySupported() {
		return false;
	}

	
	@Override
	protected Long getKey(CreditCardTypeVO entity) {
		return entity.getID();
	}

	@Override
	protected CreditCardTypeVO getEntityFromServer(Long creditCardTypePK) throws Exception {
		CreditCardTypeVO creditCardTypeVO = getContactMgr().getCreditCardTypeVO(creditCardTypePK);
		return creditCardTypeVO;
	}

	public CreditCardTypeVO getCreditCardTypeVO(Long creditCardTypePK) throws Exception {
		return super.getEntity(creditCardTypePK);
	}

	@Override
	protected List<CreditCardTypeVO> getEntitiesFromServer(Collection<Long> creditCardTypePKs) throws Exception {
		List<CreditCardTypeVO> creditCardTypeVOs = getContactMgr().getCreditCardTypeVOs(creditCardTypePKs);
		return creditCardTypeVOs;
	}
	
	
	public List<CreditCardTypeVO> getCreditCardTypeVOs(Collection<Long> creditCardTypePKs) throws Exception {
		return super.getEntities(creditCardTypePKs);
	}


	protected List<CreditCardTypeVO> getAllEntitiesFromServer() throws Exception {
		List<CreditCardTypeVO> creditCardTypeVOs = null;
		
		if (serverModel.isLoggedIn()) {
			creditCardTypeVOs = getContactMgr().getCreditCardTypeVOs();
		}
		else {
			creditCardTypeVOs = Collections.emptyList();
		}
		
		return creditCardTypeVOs;
	}

	
	public Collection<CreditCardTypeVO> getAllCreditCardTypeVOs() throws Exception {
		return getAllEntities();
	}

	
	@Override
	protected CreditCardTypeVO createEntityOnServer(CreditCardTypeVO creditCardTypeVO) throws Exception {
		creditCardTypeVO = getContactMgr().createCreditCardType(creditCardTypeVO);
		return creditCardTypeVO;
	}

	
	public CreditCardTypeVO create(CreditCardTypeVO creditCardTypeVO) throws Exception {
		return super.create(creditCardTypeVO);
	}

	
	@Override
	protected CreditCardTypeVO updateEntityOnServer(CreditCardTypeVO creditCardTypeVO) throws Exception {
		creditCardTypeVO.validate();
		creditCardTypeVO = getContactMgr().updateCreditCardType(creditCardTypeVO);
		return creditCardTypeVO;
	}

	
	public CreditCardTypeVO update(CreditCardTypeVO creditCardTypeVO) throws Exception {
		return super.update(creditCardTypeVO);
	}

	
	@Override
	protected void deleteEntityOnServer(CreditCardTypeVO creditCardTypeVO) throws Exception {
		if (creditCardTypeVO != null) {
			Long pk = creditCardTypeVO.getPK();
			getContactMgr().deleteCreditCardType(pk);
		}
	}

	public void delete(CreditCardTypeVO creditCardTypeVO) throws Exception {
		super.delete(creditCardTypeVO);
	}

}
