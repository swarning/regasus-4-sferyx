package de.regasus.programme.programmepointtype.combo;

import java.util.Collection;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import com.lambdalogic.i18n.LanguageString;
import com.lambdalogic.messeinfo.participant.data.ProgrammePointTypeVO;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.rcp.widget.AbstractComboComposite;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.programme.ProgrammePointTypeModel;
import de.regasus.ui.Activator;


@SuppressWarnings("rawtypes")
public class ProgrammePointTypeCombo
extends AbstractComboComposite<ProgrammePointTypeVO>
implements CacheModelListener {

	// Model
	private ProgrammePointTypeModel model;
	
	
	public ProgrammePointTypeCombo(Composite parent, int style) throws Exception {
		super(parent, SWT.NONE);
	}

	
	@Override
	protected Object getEmptyEntity() {
		return EMPTY_ELEMENT;
	}

	
	protected LabelProvider getLabelProvider() {
		return new LabelProvider() {
			
			@Override
			public String getText(Object element) {
				if (element == EMPTY_ELEMENT) {
					return EMPTY_ELEMENT;
				}

				ProgrammePointTypeVO programmePointTypeVO = (ProgrammePointTypeVO) element;
				return LanguageString.toStringAvoidNull(programmePointTypeVO.getName());
			}
		};
	}
	
	
	protected Collection<ProgrammePointTypeVO> getModelData() throws Exception {
		Collection<ProgrammePointTypeVO> modelData = null;
		if (model != null) {
			modelData = model.getAllUndeletedProgrammePointTypeVOs();
		}
		return modelData;
	}
	
	
	protected void initModel() {
		model = ProgrammePointTypeModel.getInstance();
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
	
	
	public Long getProgrammePointTypePK() {
		Long programmePointTypePK = null;
		if (entity != null) {
			Long pk = entity.getPK();
			if (pk != null) {
				programmePointTypePK = pk;
			}
		}
		return programmePointTypePK;
	}

	
	public void setProgrammePointTypePK(Long programmePointTypePK) {
		try {
			ProgrammePointTypeVO programmePointTypeVO = null;
			if (model != null && programmePointTypePK != null) {
				programmePointTypeVO = model.getProgrammePointTypeVO(programmePointTypePK);
			}
			setEntity(programmePointTypeVO);
		}
		catch (Throwable t) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t);
		}
	}
	
}
