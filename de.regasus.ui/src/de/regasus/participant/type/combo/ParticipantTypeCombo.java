package de.regasus.participant.type.combo;

import java.util.Collection;
import java.util.List;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import com.lambdalogic.i18n.LanguageString;
import com.lambdalogic.util.EqualsHelper;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.rcp.Activator;
import de.regasus.core.error.RegasusErrorHandler;
import com.lambdalogic.util.rcp.widget.AbstractComboComposite;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.event.ParticipantType;
import de.regasus.participant.ParticipantTypeModel;


@SuppressWarnings("rawtypes")
public class ParticipantTypeCombo extends AbstractComboComposite<ParticipantType> {

	// Model
	private ParticipantTypeModel model;

	/**
	 * PK of the Event whose ParticipantTypes shall be presented.
	 */
	private Long eventID;


	public ParticipantTypeCombo(Composite parent, int style) throws Exception {
		super(parent, SWT.NONE);
	}


	private CacheModelListener<Long> participantTypeModelListener = new CacheModelListener<Long>() {
		@Override
		public void dataChange(CacheModelEvent event) {
			try {
				handleModelChange();
			}
			catch (Exception e) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			}
		}
	};


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

				ParticipantType participantType = (ParticipantType) element;
				return LanguageString.toStringAvoidNull(participantType.getName());
			}
		};
	}


	@Override
	protected Collection<ParticipantType> getModelData() throws Exception {
		List<ParticipantType> modelData = null;
		if (eventID != null) {
			modelData = model.getParticipantTypesByEvent(eventID);
		}
		return modelData;
	}


	public boolean containsParticipantTypePK(Long participantTypePK) {
		boolean result = false;

		List list = (List) comboViewer.getInput();
		if (list != null) {
			for (Object element : list) {
				// list contains EMPTY_ELEMENT that is no ParticipantTypeVO
				if (element instanceof ParticipantType) {
					ParticipantType participantType = (ParticipantType) element;
					if (participantTypePK.equals(participantType.getId())) {
						result = true;
						break;
					}
				}
			}
		}

		return result;
	}


	@Override
	protected void initModel() {
		model = ParticipantTypeModel.getInstance();
		// do not observe the model now, because we don't know the Event yet
	}


	@Override
	protected void disposeModel() {
		model.removeListener(participantTypeModelListener);
	}


	public Long getParticipantTypePK() {
		Long id = null;
		if (entity != null) {
			id = entity.getId();
		}
		return id;
	}


	public void setParticipantTypePK(Long participantTypePK) {
		try {
			ParticipantType participantType = null;
			if (participantTypePK != null) {
				participantType = model.getParticipantType(participantTypePK);
			}
			setEntity(participantType);
		}
		catch (Throwable t) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t);
		}
	}


	public Long getEventID() {
		return eventID;
	}


	public void setEventID(Long eventID) throws Exception {
		if ( ! EqualsHelper.isEqual(this.eventID, eventID)) {
			Long oldEventID = this.eventID;
			this.eventID = eventID;

			// save old selection
			final ISelection selection = comboViewer.getSelection();
			entity = null;

			model.removeForeignKeyListener(participantTypeModelListener, oldEventID);

			// register at the model before getting its data, so the data will be put to the models cache
			model.addForeignKeyListener(participantTypeModelListener, eventID);

			// update combo
			handleModelChange();


			SWTHelper.syncExecDisplayThread(new Runnable() {
				@Override
				public void run() {
					try {
						modifySupport.setEnabled(false);
						comboViewer.setSelection(selection);
						entity = getEntityFromComboViewer();
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
