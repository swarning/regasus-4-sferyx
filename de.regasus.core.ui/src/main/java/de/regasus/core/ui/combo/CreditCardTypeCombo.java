package de.regasus.core.ui.combo;

import java.util.Collection;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import com.lambdalogic.messeinfo.contact.data.CreditCardTypeVO;
import com.lambdalogic.util.StringHelper;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.rcp.widget.AbstractComboComposite;

import de.regasus.core.CreditCardTypeModel;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.Activator;

@SuppressWarnings("rawtypes")
public class CreditCardTypeCombo
extends AbstractComboComposite<CreditCardTypeVO> 
implements CacheModelListener {

	private static final CreditCardTypeVO EMPTY_CREDIT_CARD_TYPE = new CreditCardTypeVO();
	
	// Model
	private CreditCardTypeModel model;

	
	public CreditCardTypeCombo(Composite parent, int style) throws Exception {
		super(parent, SWT.NONE);
	}

	
	protected CreditCardTypeVO getEmptyEntity() {
		return EMPTY_CREDIT_CARD_TYPE;
	}

	
	protected LabelProvider getLabelProvider() {
		return new LabelProvider() {
			
			@Override
			public String getText(Object element) {
				CreditCardTypeVO creditCardTypeVO = (CreditCardTypeVO) element;
				return StringHelper.avoidNull(creditCardTypeVO.getName());
			}
		};
	}
	
	
	protected Collection<CreditCardTypeVO> getModelData() throws Exception {
		Collection<CreditCardTypeVO> modelData = model.getAllCreditCardTypeVOs();
		return modelData;
	}
	
	
	protected void initModel() {
		model = CreditCardTypeModel.getInstance();
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
	
	
	public Long getPK() {
		Long pk = null;
		if (entity != null) {
			pk = entity.getPK();
		}
		return pk;
	}

	
	public void setPK(Long pk) {
		try {
			CreditCardTypeVO creditCardTypeVO = null;
			if (pk != null) {
				creditCardTypeVO = model.getCreditCardTypeVO(pk);
			}
			setEntity(creditCardTypeVO);
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}

}
