package de.regasus.programme.workgroup.combo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import com.lambdalogic.messeinfo.participant.ParticipantLabel;
import com.lambdalogic.messeinfo.participant.data.ProgrammeBookingParameter;
import com.lambdalogic.messeinfo.participant.data.WorkGroupVO;
import com.lambdalogic.util.EqualsHelper;
import com.lambdalogic.util.StringHelper;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.rcp.Activator;
import de.regasus.core.error.RegasusErrorHandler;
import com.lambdalogic.util.rcp.widget.AbstractComboComposite;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.programme.WorkGroupModel;


@SuppressWarnings("rawtypes")
public class WorkGroupCombo extends AbstractComboComposite<WorkGroupVO> implements CacheModelListener {

	private static final WorkGroupVO AUTO_ELEMENT = new WorkGroupVO();

	// Model
	private WorkGroupModel model;


	/**
	 * ID of the Programme Point whose Work Groups are included.
	 */
	private Long programmePointPK;


	public WorkGroupCombo(Composite parent, int style) throws Exception {
		super(parent, SWT.NONE);

		AUTO_ELEMENT.setID(ProgrammeBookingParameter.AUTO_WORK_GROUP);
		AUTO_ELEMENT.setName(ParticipantLabel.AUTO.getString());
	}


	@Override
	protected Object getEmptyEntity() {
		return EMPTY_ELEMENT;
	}


	@Override
	protected LabelProvider getLabelProvider() {
		return new LabelProvider() {

			@Override
			public String getText(Object element) {
				if (element == EMPTY_ELEMENT) {
					return EMPTY_ELEMENT;
				}

				WorkGroupVO workGroupVO = (WorkGroupVO) element;
				return StringHelper.avoidNull( workGroupVO.getName() );
			}
		};
	}


	@Override
	protected Collection<WorkGroupVO> getModelData() throws Exception {
		if (model != null) {
			List<WorkGroupVO> workGroupVOs = model.getWorkGroupVOsByProgrammePointPK(programmePointPK);

			List<WorkGroupVO> workGroupVOsWithAuto = new ArrayList<>(workGroupVOs);
			workGroupVOsWithAuto.add(0, AUTO_ELEMENT);

			return workGroupVOsWithAuto;
		}
		else {
			return null;
		}
	}


	@Override
	protected void initModel() {
		// do nothing because we don't know the Programme Point yet
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


	public Long getWorkGroupPK() {
		if (entity != null) {
			return entity.getPK();
		}
		else {
			return null;
		}
	}


	public void setWorkGroupPK(Long workGroupPK) {
		try {
			WorkGroupVO workGroupVO = null;
			if (model != null && workGroupPK != null) {
				workGroupVO = model.getWorkGroupVO(workGroupPK);
			}
			setEntity(workGroupVO);
		}
		catch (Throwable t) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t);
		}
	}


	public Long getProgrammePointPK() {
		return programmePointPK;
	}


	public void setProgrammePointPK(Long newProgrammePointPK) throws Exception {
		if ( ! EqualsHelper.isEqual(this.programmePointPK, newProgrammePointPK)) {

			Long oldProgrammePointPK = this.programmePointPK;
			this.programmePointPK = newProgrammePointPK;

			// remember old selection
			final ISelection selection = comboViewer.getSelection();
			entity = null;

			if (model == null) {
				// get and register new model
				try {
					model = WorkGroupModel.getInstance();
				}
				catch (Throwable t) {
					RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t);
				}
			}
			else {
				model.removeForeignKeyListener(this, oldProgrammePointPK);
			}

			// register at the model before getting its data, so the data will be put to the models cache
			model.addForeignKeyListener(this, newProgrammePointPK);

			// update combo
			handleModelChange();


			SWTHelper.syncExecDisplayThread(new Runnable() {
				@Override
				public void run() {
					try {
						modifySupport.setEnabled(false);
						comboViewer.setSelection(selection);
					}
					catch (Throwable t) {
						RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t);
					}
					finally {
						modifySupport.setEnabled(true);
					}
				}
			});
		}
	}

}
