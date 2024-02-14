package de.regasus.finance;

import static de.regasus.LookupService.*;

import java.util.Collection;
import java.util.List;

import com.lambdalogic.messeinfo.invoice.data.CustomerAccountVO;

import de.regasus.core.model.MICacheModel;

public class CustomerAccountModel 
extends MICacheModel<String, CustomerAccountVO> {
	private static CustomerAccountModel singleton = null;


	private CustomerAccountModel() {
		super();
	}


	public static CustomerAccountModel getInstance() {
		if (singleton == null) {
			singleton = new CustomerAccountModel();
		}
		return singleton;
	}


	@Override
	protected boolean isForeignKeySupported() {
		return false;
	}


	@Override
	protected String getKey(CustomerAccountVO entity) {
		return entity.getPK();
	}

	
	@Override
	protected CustomerAccountVO getEntityFromServer(String customerAccountNo) throws Exception {
		CustomerAccountVO customerAccountVO = getInvoiceMgr().getCustomerAccountVO(customerAccountNo);
		return customerAccountVO;
	}

	
	public CustomerAccountVO getCustomerAccountVO(String customerAccountNo) throws Exception {
		return super.getEntity(customerAccountNo);
	}
	
	
	@Override
	protected List<CustomerAccountVO> getEntitiesFromServer(Collection<String> customerAccountNos) throws Exception {
		List<CustomerAccountVO> customerAccountVOs = getInvoiceMgr().getCustomerAccountVOs(customerAccountNos);
		return customerAccountVOs;
	}


	public List<CustomerAccountVO> getCustomerAccountVOs(Collection<String> customerAccountNos) throws Exception {
		return super.getEntities(customerAccountNos);
	}

	
	@Override
	protected List<CustomerAccountVO> getAllEntitiesFromServer() throws Exception {
		List<CustomerAccountVO> customerAccountVOs = getInvoiceMgr().getCustomerAccountVOs();
		return customerAccountVOs;
	}

	
	public Collection<CustomerAccountVO> getAllCustomerAccountVOs() throws Exception {
		return getAllEntities();
	}

	
	@Override
	protected CustomerAccountVO createEntityOnServer(CustomerAccountVO customerAccountVO) throws Exception {
		customerAccountVO.validate();
		customerAccountVO = getInvoiceMgr().createCustomerAccount(customerAccountVO);
		return customerAccountVO;
	}

	
	public CustomerAccountVO create(CustomerAccountVO customerAccountVO) throws Exception {
		return super.create(customerAccountVO);
	}

	
	@Override
	protected CustomerAccountVO updateEntityOnServer(CustomerAccountVO customerAccountVO) throws Exception {
		customerAccountVO.validate();
		customerAccountVO = getInvoiceMgr().updateCustomerAccount(customerAccountVO);
		return customerAccountVO;
	}

	
	public CustomerAccountVO update(CustomerAccountVO customerAccountVO) throws Exception {
		return super.update(customerAccountVO);
	}

	
	@Override
	protected void deleteEntityOnServer(CustomerAccountVO customerAccountVO) throws Exception {
		if (customerAccountVO != null) {
			String customerAccountNo = customerAccountVO.getPK();
			getInvoiceMgr().deleteCustomerAccount(customerAccountNo);
		}
	}

	public void delete(CustomerAccountVO customerAccountVO) throws Exception {
		super.delete(customerAccountVO);
	}

}
