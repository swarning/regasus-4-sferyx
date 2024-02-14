package de.regasus.event.combo;

import static com.lambdalogic.util.StringHelper.avoidNull;

import java.util.Collection;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.lambdalogic.i18n.LanguageString;
import com.lambdalogic.messeinfo.participant.data.EventVO;
import com.lambdalogic.util.EqualsHelper;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.model.EntityNotFoundException;
import com.lambdalogic.util.rcp.UtilI18N;
import com.lambdalogic.util.rcp.widget.AbstractComboComposite;

import de.regasus.I18N;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.Activator;
import de.regasus.event.EventFilter;
import de.regasus.event.EventModel;


@SuppressWarnings("rawtypes")
public class EventCombo extends AbstractComboComposite<EventVO> implements CacheModelListener {

	// Model
	private EventModel model;

	private EventFilter eventFilter;

	private Text filterText;


	public EventCombo(Composite parent, int style) throws Exception {
		super(parent, style);
	}


	@Override
	protected void createWidgets(Composite parent, int style) {
		setLayout(new FillLayout());

		combo = new Combo(parent, style | SWT.READ_ONLY);



		final int NUM_COLUMNS = 3;
		parent.setLayout(new GridLayout(NUM_COLUMNS, false));

		GridDataFactory labelGridDataFactory = GridDataFactory
			.swtDefaults()
			.align(SWT.RIGHT, SWT.CENTER);

		GridDataFactory widgetGridDataFactory = GridDataFactory
			.swtDefaults()
			.align(SWT.FILL, SWT.CENTER)
			.grab(true, false);

		widgetGridDataFactory.applyTo(combo);


		Label filterLabel = new Label(parent, SWT.NONE);
		labelGridDataFactory.applyTo(filterLabel);
		filterLabel.setText(UtilI18N.Filter);
		filterLabel.setToolTipText(I18N.EventFilter_Desc);

		filterText = new Text(parent, SWT.BORDER);

		widgetGridDataFactory.applyTo(filterText);

		filterText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent modifyEvent) {
				eventFilter.setFilterText( filterText.getText() );

				try {
					syncComboToModel();
				}
				catch (Exception e) {
					RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
				}
			}
		});
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

				EventVO eventVO = (EventVO) element;
				return LanguageString.toStringAvoidNull(eventVO.getLabel());
			}
		};
	}


	@Override
	protected Collection<EventVO> getModelData() throws Exception {
		Collection<EventVO> modelData = model.getAllEventVOs(true /*onlyUnclosed*/);

		// apply filter
		if (modelData != null) {
			modelData = eventFilter.filter(modelData);
		}

		return modelData;
	}


	@Override
	protected void initModel() {
		// init EventFilter before observing the Model
		eventFilter = new EventFilter();

		model = EventModel.getInstance();
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


	public Long getEventPK() {
		Long eventPK = null;
		if (entity != null) {
			eventPK = entity.getPK();
		}
		return eventPK;
	}


	public void setEventPK(Long eventPK) {
		try {
			if ( !EqualsHelper.isEqual(getEventPK(), eventPK) ) {
    			EventVO eventVO = null;
    			if (eventPK != null) {
    				eventVO = model.getEventVO(eventPK);
    				if (eventVO == null) {
    					throw new EntityNotFoundException("Event", eventPK);
    				}
    			}
    			setEntity(eventVO);
			}
		}
		catch (EntityNotFoundException e) {
			RegasusErrorHandler.handleSilentError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
		catch (Throwable t) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t);
		}
	}


	public String getFilter() {
		return filterText.getText();
	}


	public void setFilter(String filter) {
		filterText.setText( avoidNull(filter) );
		eventFilter.setFilterText(filter);

		try {
			syncComboToModel();
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


//	public Long getEventGroupFilter() {
//		return eventFilter.getFilterEventGroupId();
//	}
//
//
//	public void setEventGroupFilter(Long eventGroupFilter) {
//		eventFilter.setFilterEventGroupId(eventGroupFilter);
//
//		try {
//			syncComboToModel();
//		}
//		catch (Exception e) {
//			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
//		}
//	}

}
