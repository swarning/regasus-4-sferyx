package de.regasus.participant.type;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;

import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.rcp.widget.EntityProvider;
import com.lambdalogic.util.rcp.widget.ChooseAndOrderComposite;

import de.regasus.I18N;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.event.ParticipantType;
import de.regasus.participant.ParticipantTypeModel;
import de.regasus.ui.Activator;

public class ChooseParticipantTypesComposite extends ChooseAndOrderComposite<ParticipantType> {

	private ParticipantTypeModel participantTypeModel;


	private CacheModelListener<Long> participantTypeModelListener = new CacheModelListener<Long>() {
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
	 * @param participantTypeProvider
	 *  Source Participant Types that are generally available (on the left side).
	 *  This includes such Participant Types that have been selected (moved to the right side).
	 * @param style
	 * @throws Exception
	 */
	public ChooseParticipantTypesComposite(
		Composite parent,
		EntityProvider<ParticipantType> participantTypeProvider,
		int style
	)
	throws Exception {
		super(parent, participantTypeProvider, style);

		participantTypeModel = ParticipantTypeModel.getInstance();
		participantTypeModel.addListener(participantTypeModelListener);

		addDisposeListener(disposeListener);
	}


	private DisposeListener disposeListener = new DisposeListener() {
		@Override
		public void widgetDisposed(DisposeEvent e) {
			try {
				participantTypeModel.removeListener(participantTypeModelListener);
			}
			catch (Throwable t) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t);
			}
		}
	};


	@Override
	protected String getAvailableEntitiesLabel() {
		return I18N.AvailableParticipantTypes;
	}


	@Override
	protected String getChosenEntitiesLabel() {
		return I18N.ChosenParticipantTypes;
	}


	@Override
	protected ILabelProvider getLabelProvider() {
		return new ParticipantTypeLabelProvider();
	}


	@Override
	protected String buildCopyInfo(ParticipantType entity) {
		return entity.getCopyInfo();
	}


	@Override
	protected Object getId(ParticipantType entity) {
		return entity.getId();
	}

}
