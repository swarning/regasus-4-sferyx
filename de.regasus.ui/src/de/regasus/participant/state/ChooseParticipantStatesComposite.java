package de.regasus.participant.state;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;

import com.lambdalogic.messeinfo.participant.ParticipantState;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.rcp.widget.ChooseAndOrderComposite;
import com.lambdalogic.util.rcp.widget.EntityProvider;

import de.regasus.I18N;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.participant.ParticipantStateModel;
import de.regasus.ui.Activator;

public class ChooseParticipantStatesComposite extends ChooseAndOrderComposite<ParticipantState> {

	private ParticipantStateModel participantStateModel;


	private CacheModelListener<Long> participantStateModelListener = new CacheModelListener<Long>() {
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
	 * @param participantStateProvider
	 *  Source Participant Types that are generally available (on the left side).
	 *  This includes such Participant Types that have been selected (moved to the right side).
	 * @param style
	 * @throws Exception
	 */
	public ChooseParticipantStatesComposite(
		Composite parent,
		EntityProvider<ParticipantState> participantStateProvider,
		int style
	)
	throws Exception {
		super(parent, participantStateProvider, style);

		participantStateModel = ParticipantStateModel.getInstance();
		participantStateModel.addListener(participantStateModelListener);

		addDisposeListener(disposeListener);
	}


	private DisposeListener disposeListener = new DisposeListener() {
		@Override
		public void widgetDisposed(DisposeEvent e) {
			try {
				participantStateModel.removeListener(participantStateModelListener);
			}
			catch (Throwable t) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t);
			}
		}
	};


	@Override
	protected String getAvailableEntitiesLabel() {
		return I18N.AvailableParticipantStates;
	}


	@Override
	protected String getChosenEntitiesLabel() {
		return I18N.ChosenParticipantStates;
	}


	@Override
	protected ILabelProvider getLabelProvider() {
		return new ParticipantStateLabelProvider();
	}


	@Override
	protected String buildCopyInfo(ParticipantState entity) {
		return entity.getCopyInfo();
	}


	@Override
	protected Object getId(ParticipantState entity) {
		return entity.getID();
	}

}
