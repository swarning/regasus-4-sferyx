package de.regasus.finance.impersonalaccount.combo;

import java.util.Collection;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import com.lambdalogic.messeinfo.invoice.data.ImpersonalAccountVO;
import com.lambdalogic.util.CollectionsHelper;
import com.lambdalogic.util.StringHelper;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.model.EntityNotFoundException;
import com.lambdalogic.util.rcp.widget.AbstractComboComposite;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.Activator;
import de.regasus.finance.ImpersonalAccountModel;


@SuppressWarnings("rawtypes")
public class ImpersonalAccountCombo 
extends AbstractComboComposite<ImpersonalAccountVO> 
implements CacheModelListener {
	private static final ImpersonalAccountVO EMPTY_IMPERSONAL_ACCOUNT = new ImpersonalAccountVO();

	// Model
	private ImpersonalAccountModel model;
	
	private boolean onlyFinanceAcounts = false;


	public ImpersonalAccountCombo(Composite parent, int style) throws Exception {
		super(parent, SWT.NONE);
	}


	@Override
	protected ImpersonalAccountVO getEmptyEntity() {
		return EMPTY_IMPERSONAL_ACCOUNT;
	}


	@Override
	protected LabelProvider getLabelProvider() {
		return new LabelProvider() {

			@Override
			public String getText(Object element) {
				String label = null;
				
				ImpersonalAccountVO impersonalAccountVO = (ImpersonalAccountVO) element;
				Integer no = impersonalAccountVO.getNo();
				if (no != null) {
					label= ImpersonalAccountVO.NUMBER_FORMAT.format(no);
					
					String name = impersonalAccountVO.getName();
					if (StringHelper.isNotEmpty(name)) {
						label += " - " + name;
					}
					else if (impersonalAccountVO.getNewTime() == null) {
						// add * if the entity does not exist
						label += "*";
					}
				}
				else {
					label = "";
				}
				
				return label;
			}
		};
	}


	@Override
	protected Collection<ImpersonalAccountVO> getModelData() throws Exception {
		Collection<ImpersonalAccountVO> modelData = model.getAllImpersonalAccountVOs();
		
		if (onlyFinanceAcounts) {
			// create list that contains only such impersonal accounts that are finance accounts
			Collection<ImpersonalAccountVO> financeAccounts = CollectionsHelper.createArrayList(modelData.size());
			for (ImpersonalAccountVO impersonalAccountVO : modelData) {
				if (impersonalAccountVO.isFinanceAccount()) {
					financeAccounts.add(impersonalAccountVO);
				}
			}
			
			// replace model data with finance accounts
			modelData = financeAccounts;
		}
		
		return modelData;
	}


	@Override
	protected void initModel() {
		model = ImpersonalAccountModel.getInstance();
		model.addListener(this);
	}


	@Override
	protected void disposeModel() {
		if (model != null) {
			model.removeListener(this);
		}
	}


	@Override
	public void dataChange(CacheModelEvent event) {
		try {
			handleModelChange();
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}
	
	
	public Integer getImpersonalAccountNo() {
		Integer impersonalAccountNo = null;
		if (entity != null) {
			impersonalAccountNo = entity.getPK();
		}
		return impersonalAccountNo;
	}

	
	public void setImpersonalAccountPK(Integer impersonalAccountNo) {
		ImpersonalAccountVO impersonalAccountVO = null;
		if (impersonalAccountNo != null) {
			try {
				impersonalAccountVO = model.getImpersonalAccountVO(impersonalAccountNo);
			}
			catch (EntityNotFoundException e) {
				// build entity to show value that does not exist in the DB
				impersonalAccountVO = new ImpersonalAccountVO();
				impersonalAccountVO.setNo(impersonalAccountNo);
			}
			catch (Throwable e) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			}
		}
		setEntity(impersonalAccountVO);
	}
	

	public boolean isOnlyFinanceAcounts() {
		return onlyFinanceAcounts;
	}


	public void setOnlyFinanceAcounts(boolean onlyFinanceAcounts) throws Exception {
		this.onlyFinanceAcounts = onlyFinanceAcounts;
		syncComboToModel();
	}

}
