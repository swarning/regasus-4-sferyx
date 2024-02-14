package de.regasus.programme.programmepoint.combo;

import java.util.Collection;
import java.util.List;
import java.util.Locale;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import com.lambdalogic.i18n.LanguageString;
import com.lambdalogic.messeinfo.participant.data.ProgrammePointVO;
import com.lambdalogic.util.EqualsHelper;
import com.lambdalogic.util.StringHelper;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.rcp.Activator;
import de.regasus.core.error.RegasusErrorHandler;
import com.lambdalogic.util.rcp.widget.AbstractComboComposite;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.programme.ProgrammePointModel;

@SuppressWarnings("rawtypes")
public class ProgrammePointCombo extends AbstractComboComposite<ProgrammePointVO> implements CacheModelListener {

	// Model
	private ProgrammePointModel model;


	/**
	 * ID of the Event whose Programme Points are included.
	 */
	private Long eventPK;


	public ProgrammePointCombo(Composite parent, int style) throws Exception {
		super(parent, SWT.NONE);

		/* Programme Points cannot be deleted softly (marked as deleted).
		 * Therefore deleted Programme Points must not be available.
		 */
		setKeepEntityInList(false);
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

				ProgrammePointVO programmePointVO = (ProgrammePointVO) element;
				return StringHelper.avoidNull( programmePointVO.getName() );
			}
		};
	}


	@Override
	protected Collection<ProgrammePointVO> getModelData() throws Exception {
		if (model != null) {
			List<ProgrammePointVO> programmePointVOs = model.getProgrammePointVOsByEventPK(eventPK, false);
			return programmePointVOs;
		}
		else {
			return null;
		}
	}


	@Override
	protected void initModel() {
		// do nothing because we don't know the Event yet
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


	public Long getProgrammePointPK() {
		if (entity != null) {
			return entity.getPK();
		}
		else {
			return null;
		}
	}


	public void setProgrammePointPK(Long programmePointPK) {
		ProgrammePointVO programmePointVO = null;

		try {
			if (model != null && programmePointPK != null) {
				programmePointVO = model.getProgrammePointVO(programmePointPK);
			}
		}
		catch (Throwable t) {
			RegasusErrorHandler.handleSilentError(Activator.PLUGIN_ID, getClass().getName(), t);

			// build and add dummy ProgrammePointVO
			programmePointVO = new ProgrammePointVO();
			programmePointVO.setID(programmePointPK);
			programmePointVO.setName( new LanguageString(Locale.getDefault().getLanguage(), String.valueOf(programmePointPK)) );
		}

		setEntity(programmePointVO);
	}


	public Long getEventPK() {
		return eventPK;
	}


	public void setEventPK(Long newEventPK) throws Exception {
		if ( ! EqualsHelper.isEqual(this.eventPK, newEventPK)) {

			Long oldEventPK = this.eventPK;
			this.eventPK = newEventPK;

			// remember old selection
			final ISelection selection = comboViewer.getSelection();
			entity = null;

			if (model == null) {
				// get and register new model
				try {
					model = ProgrammePointModel.getInstance();
				}
				catch (Throwable t) {
					RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t);
				}
			}
			else {
				model.removeForeignKeyListener(this, oldEventPK);
			}

			// register at the model before getting its data, so the data will be put to the models cache
			model.addForeignKeyListener(this, newEventPK);

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


	@Override
	protected ViewerSorter getViewerSorter() {
		/* Do not re-order to keep the original order from the model
		 * Program Points remain sorted according to their position.
		 */
		return null;
	}

}
