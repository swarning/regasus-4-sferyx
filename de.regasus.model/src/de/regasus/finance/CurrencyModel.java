package de.regasus.finance;

import static de.regasus.LookupService.getCurrencyMgr;

import java.util.Collection;
import java.util.List;

import com.lambdalogic.messeinfo.invoice.data.CurrencyVO;

import de.regasus.core.model.MICacheModel;

public class CurrencyModel extends MICacheModel<String, CurrencyVO> {

	private static CurrencyModel singleton = null;


	private CurrencyModel() {
		super();
	}


	public static CurrencyModel getInstance() {
		if (singleton == null) {
			singleton = new CurrencyModel();
		}
		return singleton;
	}


	@Override
	protected boolean isForeignKeySupported() {
		return false;
	}


	@Override
	protected String getKey(CurrencyVO entity) {
		return entity.getPK();
	}


	@Override
	protected CurrencyVO getEntityFromServer(String symbol) throws Exception {
		CurrencyVO currencyVO = getCurrencyMgr().getCurrencyVO(symbol);
		return currencyVO;
	}


	public CurrencyVO getCurrencyVO(String symbol) throws Exception {
		return super.getEntity(symbol);
	}


	@Override
	protected List<CurrencyVO> getEntitiesFromServer(Collection<String> symbols) throws Exception {
		List<CurrencyVO> currencyVOs = getCurrencyMgr().getCurrencyVOs(symbols);
		return currencyVOs;
	}


	public List<CurrencyVO> getCurrencyVOs(Collection<String> symbols) throws Exception {
		return super.getEntities(symbols);
	}


	@Override
	protected List<CurrencyVO> getAllEntitiesFromServer() throws Exception {
		List<CurrencyVO> currencyVOs = getCurrencyMgr().getCurrencyVOs();
		return currencyVOs;
	}


	public Collection<CurrencyVO> getAllCurrencyVOs() throws Exception {
		return getAllEntities();
	}


	@Override
	protected CurrencyVO createEntityOnServer(CurrencyVO currencyVO) throws Exception {
		currencyVO.validate();
		currencyVO = getCurrencyMgr().createCurrency(currencyVO);
		return currencyVO;
	}


	@Override
	public CurrencyVO create(CurrencyVO currencyVO) throws Exception {
		return super.create(currencyVO);
	}


	@Override
	protected CurrencyVO updateEntityOnServer(CurrencyVO currencyVO) throws Exception {
		currencyVO.validate();
		currencyVO = getCurrencyMgr().updateCurrency(currencyVO);
		return currencyVO;
	}


	@Override
	public CurrencyVO update(CurrencyVO currencyVO) throws Exception {
		return super.update(currencyVO);
	}


	@Override
	protected void deleteEntityOnServer(CurrencyVO currencyVO) throws Exception {
		if (currencyVO != null) {
			String symbol = currencyVO.getPK();
			getCurrencyMgr().deleteCurrency(symbol);
		}
	}


	@Override
	public void delete(CurrencyVO currencyVO) throws Exception {
		super.delete(currencyVO);
	}

}
