package de.regasus.participant.state.combo;

import java.util.Collection;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import com.lambdalogic.i18n.LanguageString;
import com.lambdalogic.messeinfo.participant.ParticipantState;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.rcp.Activator;
import de.regasus.core.error.RegasusErrorHandler;
import com.lambdalogic.util.rcp.widget.AbstractComboComposite;

import de.regasus.participant.ParticipantStateModel;


@SuppressWarnings("rawtypes")
public class ParticipantStateCombo
extends AbstractComboComposite<ParticipantState>
implements CacheModelListener {
	
	// Model
	private ParticipantStateModel model = ParticipantStateModel.getInstance();

	public ParticipantStateCombo(Composite parent, int style) throws Exception {
		super(parent, SWT.NONE);
	}

	
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

				ParticipantState participantState = (ParticipantState) element;
				return LanguageString.toStringAvoidNull(participantState.getName());
			}
		};
	}
	
	
	protected Collection<ParticipantState> getModelData() throws Exception {
		Collection<ParticipantState> modelData = model.getParticipantStates();
		return modelData;
	}
	
	
	protected void initModel() {
		model = ParticipantStateModel.getInstance();
		model.addListener(this);
	}
	
	
	protected void disposeModel() {
		if (model != null) {
			model.removeListener(this);
		}
	}


	public Long getParticipantStateID() {
		if (entity != null) {
			return entity.getPrimaryKey();
		}
		else {
			return null;
		}
	}

	
	public void setParticipantStateID(Long participantStateID) {
		try {
			ParticipantState participantState = null;
			if (participantStateID != null) {
				participantState = model.getParticipantState(participantStateID);
			}
			setEntity(participantState);
		}
		catch (Throwable t) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t);
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
	
}
