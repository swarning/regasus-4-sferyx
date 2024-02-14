package de.regasus.eventgroup.combo;

import java.util.Collection;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.widgets.Composite;

import com.lambdalogic.i18n.LanguageString;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.model.EntityNotFoundException;
import com.lambdalogic.util.rcp.widget.AbstractComboComposite;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.Activator;
import de.regasus.event.EventGroup;
import de.regasus.event.EventGroupModel;


@SuppressWarnings("rawtypes")
public class EventGroupCombo extends AbstractComboComposite<EventGroup> implements CacheModelListener {

	// Model
	private EventGroupModel model;


	public EventGroupCombo(Composite parent, int style) throws Exception {
		super(parent, style);
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

				EventGroup eventGroup = (EventGroup) element;
				return LanguageString.toStringAvoidNull( eventGroup.getName() );
			}
		};
	}


	@Override
	protected Collection<EventGroup> getModelData() throws Exception {
		Collection<EventGroup> modelData = model.getAllEventGroups();
		return modelData;
	}


	@Override
	protected void initModel() {
		model = EventGroupModel.getInstance();
		model.addListener(this);
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


	public Long getEventGroupId() {
		Long eventGroupId = null;
		if (entity != null) {
			eventGroupId = entity.getId();
		}
		return eventGroupId;
	}


	public void setEventGroupId(Long eventGroupId) {
		try {
			EventGroup eventGroup = null;
			if (eventGroupId != null) {
				eventGroup = model.getEventGroup(eventGroupId);
				if (eventGroup == null) {
					throw new EntityNotFoundException("EventGroup", eventGroupId);
				}
			}
			setEntity(eventGroup);
		}
		catch (EntityNotFoundException e) {
			RegasusErrorHandler.handleSilentError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
		catch (Throwable t) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t);
		}
	}

}
