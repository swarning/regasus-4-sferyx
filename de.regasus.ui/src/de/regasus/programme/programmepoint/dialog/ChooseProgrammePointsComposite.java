package de.regasus.programme.programmepoint.dialog;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;

import com.lambdalogic.messeinfo.participant.data.ProgrammePointCVO;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.rcp.widget.EntityProvider;
import com.lambdalogic.util.rcp.widget.ChooseAndOrderComposite;

import de.regasus.I18N;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.programme.ProgrammePointModel;
import de.regasus.programme.programmepoint.ProgrammePointLabelProvider;
import de.regasus.ui.Activator;

public class ChooseProgrammePointsComposite extends ChooseAndOrderComposite<ProgrammePointCVO> {

	private ProgrammePointModel ppModel;


	private CacheModelListener<Long> ppModelListener = new CacheModelListener<Long>() {
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
	 * @param programmePointProvider
	 *  Source Programme Points that are generally available (on the left side).
	 *  This includes such Programme Points that have been selected (moved to the right side).
	 * @param style
	 * @throws Exception
	 */
	public ChooseProgrammePointsComposite(
		Composite parent,
		EntityProvider<ProgrammePointCVO> programmePointProvider,
		int style
	)
	throws Exception {
		super(parent, programmePointProvider, style);

		ppModel = ProgrammePointModel.getInstance();
		ppModel.addListener(ppModelListener);

		addDisposeListener(disposeListener);
	}


	private DisposeListener disposeListener = new DisposeListener() {
		@Override
		public void widgetDisposed(DisposeEvent e) {
			try {
				ppModel.removeListener(ppModelListener);
			}
			catch (Throwable t) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t);
			}
		}
	};


	@Override
	protected String getAvailableEntitiesLabel() {
		return I18N.ChooseProgrammePointsComposite_AvailableProgrammePoints;
	}


	@Override
	protected String getChosenEntitiesLabel() {
		return I18N.ChooseProgrammePointsComposite_ChosenProgrammePoints;
	}


	@Override
	protected ILabelProvider getLabelProvider() {
		return new ProgrammePointLabelProvider();
	}


	@Override
	protected String buildCopyInfo(ProgrammePointCVO entity) {
		return entity.getCopyInfo();
	}


	@Override
	protected Object getId(ProgrammePointCVO entity) {
		return entity.getPK();
	}

}
