package de.regasus.finance.costcenter.combo;

import java.text.NumberFormat;
import java.util.Collection;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import com.lambdalogic.messeinfo.invoice.data.CostCenterVO;
import com.lambdalogic.util.StringHelper;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.model.EntityNotFoundException;
import com.lambdalogic.util.rcp.widget.AbstractComboComposite;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.Activator;
import de.regasus.finance.CostCenter1Model;

@SuppressWarnings("rawtypes")
public class CostCenterCombo
extends AbstractComboComposite<CostCenterVO> 
implements CacheModelListener {
	private static final CostCenterVO EMPTY_COST_CENTER = new CostCenterVO();
	
	public static final NumberFormat NUMBER_FORMAT;
	static {
		NUMBER_FORMAT = NumberFormat.getInstance();
		NUMBER_FORMAT.setMinimumIntegerDigits(5);
		NUMBER_FORMAT.setGroupingUsed(false);
	}
	
	// Model
	private CostCenter1Model model;

	
	public CostCenterCombo(Composite parent, int style) throws Exception {
		super(parent, SWT.NONE);
	}

	
	protected CostCenterVO getEmptyEntity() {
		return EMPTY_COST_CENTER;
	}

	
	protected LabelProvider getLabelProvider() {
		return new LabelProvider() {
			
			@Override
			public String getText(Object element) {
				String label = null;
				
				CostCenterVO costCenterVO = (CostCenterVO) element;
				Integer no = costCenterVO.getNo();
				if (no != null) {
					label= NUMBER_FORMAT.format(no);
					
					String name = costCenterVO.getName();
					if (StringHelper.isNotEmpty(name)) {
						label += " - " + name;
					}
					else if (costCenterVO.getNewTime() == null) {
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
	
	
	protected Collection<CostCenterVO> getModelData() throws Exception {
		Collection<CostCenterVO> modelData = model.getAllCostCenterVOs();
		return modelData;
	}
	
	
	protected void initModel() {
		model = CostCenter1Model.getInstance();
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
	
	
	public Integer getCostCenter1() {
		Integer costCenterNo = null;
		if (entity != null) {
			costCenterNo = entity.getPK();
		}
		return costCenterNo;
	}

	
	public void setCostCenter1(Integer costCenter1No) {
		CostCenterVO costCenterVO = null;
		if (costCenter1No != null) {
			try {
				costCenterVO = model.getCostCenterVO(costCenter1No);
			}
			catch (EntityNotFoundException e) {
				// build entity to show value that does not exist in the DB
				costCenterVO = new CostCenterVO();
				costCenterVO.setNo(costCenter1No);
			}
			catch (Throwable e) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			}
		}
		setEntity(costCenterVO);
	}
	
}
