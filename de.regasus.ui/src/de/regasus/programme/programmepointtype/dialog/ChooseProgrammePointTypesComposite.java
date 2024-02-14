package de.regasus.programme.programmepointtype.dialog;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;

import com.lambdalogic.messeinfo.participant.data.ProgrammePointTypeVO;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.rcp.widget.ChooseAndOrderComposite;
import com.lambdalogic.util.rcp.widget.EntityProvider;

import de.regasus.I18N;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.programme.ProgrammePointTypeModel;
import de.regasus.programme.programmepointtype.ProgrammePointTypeLabelProvider;
import de.regasus.ui.Activator;

public class ChooseProgrammePointTypesComposite extends ChooseAndOrderComposite<ProgrammePointTypeVO> {

	private ProgrammePointTypeModel pptModel;


	private CacheModelListener<Long> pptModelListener = new CacheModelListener<Long>() {
		@Override
		public void dataChange(CacheModelEvent<Long> event) throws Exception {
			try {
				initAvailableEntities();
			}
			catch (Exception e) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			}
		}
	};


	/**
	 * Create the composite.
	 * @param parent
	 * @param programmePointTypeProvider
	 *  Source Programme Point Types that are generally available (on the left side).
	 *  This includes such Programme Point Types that have been selected (moved to the right side).
	 * @param style
	 * @throws Exception
	 */
	public ChooseProgrammePointTypesComposite(
		Composite parent,
		EntityProvider<ProgrammePointTypeVO> programmePointTypeProvider,
		int style
	)
	throws Exception {
		super(parent, programmePointTypeProvider, style);

		pptModel = ProgrammePointTypeModel.getInstance();
		pptModel.addListener(pptModelListener);

		addDisposeListener(disposeListener);
	}


	private DisposeListener disposeListener = new DisposeListener() {
		@Override
		public void widgetDisposed(DisposeEvent e) {
			try {
				pptModel.removeListener(pptModelListener);
			}
			catch (Throwable t) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t);
			}
		}
	};


	@Override
	protected String getAvailableEntitiesLabel() {
		return I18N.ChooseProgrammePointTypesComposite_AvailableProgrammePointTypes;
	}


	@Override
	protected String getChosenEntitiesLabel() {
		return I18N.ChooseProgrammePointTypesComposite_ChosenProgrammePointTypes;
	}


	@Override
	protected ILabelProvider getLabelProvider() {
		return new ProgrammePointTypeLabelProvider();
	}


	@Override
	protected String buildCopyInfo(ProgrammePointTypeVO entity) {
		return entity.getCopyInfo();
	}


	@Override
	protected Object getId(ProgrammePointTypeVO entity) {
		return entity.getPK();
	}

}
