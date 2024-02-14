package de.regasus.event;

import static com.lambdalogic.util.CollectionsHelper.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;

import com.lambdalogic.messeinfo.kernel.KernelLabel;
import com.lambdalogic.messeinfo.participant.ParticipantLabel;
import com.lambdalogic.messeinfo.participant.data.EventVO;
import com.lambdalogic.util.rcp.ModifySupport;
import com.lambdalogic.util.rcp.SelectionHelper;
import com.lambdalogic.util.rcp.UtilI18N;

import de.regasus.I18N;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.ui.Activator;

public class EventTableComposite extends Composite {

	private static final int COL_COUNT = 2;

	// result
	private List<EventVO> selectedEvents;

	private Collection<Long> hiddenEventPKs = null; // TODO

	private ModifySupport modifySupport = new ModifySupport(this);

	private EventModel eventModel = EventModel.getInstance();
	private Collection<EventVO> eventVOs;

	private boolean multiSelection;

	private EventFilter eventFilter = new EventFilter();

	// widgets
	private Text filterText;
	private TableViewer tableViewer;
	private EventTable eventTable;


	public EventTableComposite(
		Composite parent,
		Collection<Long> hiddenEventPKs,
		Collection<Long> initSelectedEventPKs,
		boolean multiSelection,
		int style
	) {
		super(parent, style);

		try {
    		this.hiddenEventPKs = hiddenEventPKs;
    		this.multiSelection = multiSelection;

    		setLayout( new GridLayout(COL_COUNT, false) );

    		buildFilter(this);
    		buildTable(this);
    		initData();

    		// set initial selection
    		List<EventVO> initSelectedEvents = eventModel.getEventVOs(initSelectedEventPKs);
    		setSelectedEvents(initSelectedEvents);
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	private void buildFilter(Composite parent) throws Exception {
		Label label = new Label(this, SWT.NONE);
		GridDataFactory.swtDefaults().align(SWT.RIGHT, SWT.CENTER).applyTo(label);
		label.setText(UtilI18N.Filter);
		label.setToolTipText(I18N.EventFilter_Desc);

		filterText = new Text(this, SWT.BORDER);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(filterText);
		filterText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				initData();
			}
		});
	}


	private void buildTable(Composite parent) {
		int style = SWT.FULL_SELECTION | SWT.BORDER;
		if (multiSelection) {
			style |= SWT.MULTI;
		}

		final Table table = new Table(parent, style);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		GridDataFactory.fillDefaults().span(COL_COUNT, 1).grab(true, true).applyTo(table);

		final TableColumn labelTableColumn = new TableColumn(table, SWT.NONE);
		labelTableColumn.setWidth(400);
		labelTableColumn.setText( ParticipantLabel.Event_Label.getString() );

		final TableColumn mnemonicTableColumn = new TableColumn(table, SWT.NONE);
		mnemonicTableColumn.setWidth(100);
		mnemonicTableColumn.setText(ParticipantLabel.Mnemonic.getString());

		final TableColumn startTableColumn = new TableColumn(table, SWT.NONE);
		startTableColumn.setWidth(80);
		startTableColumn.setText(KernelLabel.StartTime.getString());

		final TableColumn endTableColumn = new TableColumn(table, SWT.NONE);
		endTableColumn.setWidth(80);
		endTableColumn.setText(KernelLabel.EndTime.getString());

		eventTable = new EventTable(table);
		tableViewer = eventTable.getViewer();
		tableViewer.addSelectionChangedListener(tableListener);
		tableViewer.addSelectionChangedListener(modifySupport);
	}


	private ISelectionChangedListener tableListener = new ISelectionChangedListener() {
		@Override
		public void selectionChanged(SelectionChangedEvent event) {
			selectedEvents = SelectionHelper.getSelection(tableViewer, EventVO.class);
		}
	};


	private void initData() {
		try {
			if (eventTable != null) {
        		eventVOs = eventModel.getAllEventVOs(!eventModel.isShowClosedEvents());

        		// apply filter
        		eventFilter.setFilterText( filterText.getText() );
        		eventVOs = eventFilter.filter(eventVOs);

        		// remove hidden Events
        		if ( notEmpty(hiddenEventPKs) ) {
        			eventVOs = eventVOs.stream()
        				.filter(eventVO -> !hiddenEventPKs.contains(eventVO.getId()))
        				.collect(Collectors.toList());
        		}

        		eventTable.setInput(eventVOs);

        		if ( empty(selectedEvents) ) {
        			tableViewer.setSelection( new StructuredSelection() );
        		}
        		else {
        			tableViewer.setSelection( new StructuredSelection(selectedEvents) );
        		}
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	/**
	 * PKs of Events that must not presented.
	 * @param eventPKs
	 */
	public void setHiddenEventPKs(Collection<Long> eventPKs) {
		this.hiddenEventPKs = eventPKs;
		initData();
	}


	public List<EventVO> getSelectedEvents() {
		return selectedEvents;
	}


	public void setSelectedEvents(List<EventVO> selectedEvents) {
		this.selectedEvents = selectedEvents;

		if ( !isDisposed() ) {
			tableViewer.setSelection(new StructuredSelection(selectedEvents), true);
		}
	}


	public List<Long> getSelectedEventIds() {
		return EventVO.getPKs(selectedEvents);
	}


	public void setSelectedEventIds(List<Long> selectedEventIds) {
		try {
    		List<EventVO> selectedEvents = eventModel.getEventVOs(selectedEventIds);
    		setSelectedEvents(selectedEvents);
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	public EventVO getSelectedEvent() {
		if (multiSelection) {
			throw new RuntimeException("The method getSelectedEvent() is not supported, because multiselection is true.");
		}

		if ( notEmpty(selectedEvents) ) {
			return selectedEvents.get(0);
		}
		return null;
	}


	public void setSelectedEvent(EventVO selectedEvent) {
		List<EventVO> eventList = new ArrayList<>();
		if (selectedEvent != null) {
			eventList.add(selectedEvent);
		}

		setSelectedEvents(eventList);
	}


	public Long getSelectedEventId() {
		if (multiSelection) {
			throw new RuntimeException("The method getSelectedEvent() is not supported, because multiselection is true.");
		}

		if ( notEmpty(selectedEvents) ) {
			return selectedEvents.get(0).getId();
		}
		return null;
	}


	public void setSelectedEventId(Long selectedEventId) {
		List<Long> eventIds = new ArrayList<>();
		if (selectedEventId != null) {
			eventIds.add(selectedEventId);
		}

		setSelectedEventIds(eventIds);
	}


	public void selectAll() {
		if (tableViewer != null) {
			ISelection selection = new StructuredSelection( tableViewer.getInput() );
			tableViewer.setSelection(selection);
		}
	}


	public void addModifyListener(ModifyListener modifyListener) {
		modifySupport.addListener(modifyListener);
	}


	public void removeModifyListener(ModifyListener modifyListener) {
		modifySupport.removeListener(modifyListener);
	}


	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

}
