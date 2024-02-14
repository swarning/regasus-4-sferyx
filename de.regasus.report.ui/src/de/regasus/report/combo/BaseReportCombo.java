package de.regasus.report.combo;

import java.util.Collection;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import com.lambdalogic.i18n.LanguageString;
import com.lambdalogic.messeinfo.report.data.BaseReportCVO;
import com.lambdalogic.messeinfo.report.data.BaseReportVO;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.rcp.widget.AbstractComboComposite;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.Activator;
import de.regasus.report.model.BaseReportListModel;

@SuppressWarnings("rawtypes")
public class BaseReportCombo
extends AbstractComboComposite<BaseReportCVO> 
implements CacheModelListener {
	private static final BaseReportCVO EMPTY_BASE_REPORT;
	
	static {
		EMPTY_BASE_REPORT = new BaseReportCVO();
		EMPTY_BASE_REPORT.setBaseReportVO(new BaseReportVO());
	}
	
	
	// Model
	private BaseReportListModel model;

	
	public BaseReportCombo(Composite parent, int style) throws Exception {
		super(parent, SWT.NONE);
	}

	
	protected BaseReportCVO getEmptyEntity() {
		return EMPTY_BASE_REPORT;
	}

	
	protected LabelProvider getLabelProvider() {
		return new LabelProvider() {
			
			@Override
			public String getText(Object element) {
				BaseReportCVO baseReportCVO = (BaseReportCVO) element;
				LanguageString name = baseReportCVO.getBaseReportVO().getName();
				return LanguageString.toStringAvoidNull(name);
			}
		};
	}
	
	
	protected Collection<BaseReportCVO> getModelData() throws Exception {
		Collection<BaseReportCVO> modelData = model.getAllBaseReportCVOs();
		return modelData;
	}
	
	
	protected void initModel() {
		model = BaseReportListModel.getInstance();
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
		BaseReportCVO baseReportCVO = null;
		if (pk != null) {
			try {
				baseReportCVO = model.getBaseReportCVO(pk);
			}
			catch (Throwable e) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			}
		}
		setEntity(baseReportCVO);
	}

}
