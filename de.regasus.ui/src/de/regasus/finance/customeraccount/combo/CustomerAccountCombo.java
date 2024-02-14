package de.regasus.finance.customeraccount.combo;

import java.util.Collection;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import com.lambdalogic.messeinfo.invoice.data.CustomerAccountVO;
import com.lambdalogic.util.StringHelper;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.rcp.widget.AbstractComboComposite;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.finance.CustomerAccountModel;
import de.regasus.ui.Activator;

@SuppressWarnings("rawtypes")
public class CustomerAccountCombo
extends AbstractComboComposite<CustomerAccountVO> 
implements CacheModelListener {
	
	private static final CustomerAccountVO EMPTY_CUSTOMER_ACCOUNT = new CustomerAccountVO();

	// Model
	private CustomerAccountModel model;


	public CustomerAccountCombo(Composite parent, int style) throws Exception {
		super(parent, SWT.NONE);
	}


	protected CustomerAccountVO getEmptyEntity() {
		return EMPTY_CUSTOMER_ACCOUNT;
	}


	protected LabelProvider getLabelProvider() {
		return new LabelProvider() {

			@Override
			public String getText(Object element) {
				CustomerAccountVO customerAccountVO = (CustomerAccountVO) element;
				
				StringBuilder label = new StringBuilder(50);
				
				String no = customerAccountVO.getNo();
				if (StringHelper.isNotEmpty(no)) {
					label.append(no);
				}
				
				String name = customerAccountVO.getName();
				if (StringHelper.isNotEmpty(name)) {
					label.append(" (");
					label.append(customerAccountVO.getName());
					label.append(")");
				}
				
				return label.toString();
			}
		};
	}


	protected Collection<CustomerAccountVO> getModelData() throws Exception {
		Collection<CustomerAccountVO> modelData = model.getAllCustomerAccountVOs();
		return modelData;
	}


	protected void initModel() {
		model = CustomerAccountModel.getInstance();
		model.addListener(this);
	}


	protected void disposeModel() {
		if (model != null) {
			model.removeListener(this);
		}
	}


	public void dataChange(CacheModelEvent event) {
		try {
			handleModelChange();
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}
	
	
	public String getCustomerAccountNo() {
		String customerAccountNo = null;
		if (entity != null) {
			customerAccountNo = entity.getPK();
		}
		return customerAccountNo;
	}

	
	public void setCustomerAccountPK(String pk) {
		CustomerAccountVO customerAccountVO = null;
		if (pk != null) {
			try {
				customerAccountVO = model.getCustomerAccountVO(pk);
			}
			catch (Throwable e) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			}
		}
		setEntity(customerAccountVO);
	}

}
