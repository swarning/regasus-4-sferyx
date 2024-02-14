package de.regasus.finance.currency.combo;

import java.util.Collection;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;

import com.lambdalogic.messeinfo.invoice.data.CurrencyVO;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.model.EntityNotFoundException;
import com.lambdalogic.util.rcp.widget.AbstractComboComposite;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.Activator;
import de.regasus.finance.CurrencyModel;

@SuppressWarnings("rawtypes")
public class CurrencyCombo
extends AbstractComboComposite<CurrencyVO> 
implements CacheModelListener {
	
	private static final CurrencyVO EMPTY_CURRENCY = new CurrencyVO();
	
	// Model
	private CurrencyModel model;

	
	public CurrencyCombo(Composite parent, int style) throws Exception {
		super(parent, SWT.NONE);
	}

	
	protected CurrencyVO getEmptyEntity() {
		return EMPTY_CURRENCY;
	}

	
	protected LabelProvider getLabelProvider() {
		return new LabelProvider() {
			
			@Override
			public String getText(Object element) {
				String label = null;
				
				CurrencyVO currencyVO = (CurrencyVO) element;
				label = currencyVO.getPK();
				if (label != null) {
					// add * if the entity does not exist
					if (currencyVO.getNewTime() == null) {
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
	
	
	protected Collection<CurrencyVO> getModelData() throws Exception {
		Collection<CurrencyVO> modelData = model.getAllCurrencyVOs();
		return modelData;
	}
	
	
	protected void initModel() {
		model = CurrencyModel.getInstance();
		
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
	
	
	public String getCurrencyCode() {
		String currencyCode = null;
		if (entity != null) {
			currencyCode = entity.getPK();
		}
		return currencyCode;
	}

	
	public void setCurrencyCode(String currencyCode) {
		CurrencyVO currencyVO = null;
		if (currencyCode != null) {
			try {
				currencyVO = model.getCurrencyVO(currencyCode);
			}
			catch (EntityNotFoundException e) {
				// build entity to show value that does not exist in the DB
				currencyVO = new CurrencyVO();
				currencyVO.setID(currencyCode);
			}
			catch (Throwable e) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			}
		}
		setEntity(currencyVO);
	}


	public Combo getCombo() {
		return combo;
	}

}
