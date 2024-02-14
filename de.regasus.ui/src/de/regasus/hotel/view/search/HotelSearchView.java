package de.regasus.hotel.view.search;

import static com.lambdalogic.util.CollectionsHelper.notEmpty;
import static com.lambdalogic.util.StringHelper.isNotEmpty;

import java.util.List;

import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.PartInitException;

import com.lambdalogic.messeinfo.hotel.Hotel;
import com.lambdalogic.messeinfo.hotel.HotelLabel;
import com.lambdalogic.messeinfo.kernel.sql.SQLParameter;
import com.lambdalogic.messeinfo.participant.ParticipantLabel;
import com.lambdalogic.messeinfo.participant.data.EventVO;
import com.lambdalogic.messeinfo.participant.report.parameter.WhereField;
import com.lambdalogic.messeinfo.report.WhereClauseReportParameter;
import com.lambdalogic.time.I18NDate;
import com.lambdalogic.util.model.ModelEvent;
import com.lambdalogic.util.model.ModelListener;
import com.lambdalogic.util.rcp.SelectionHelper;
import com.lambdalogic.util.rcp.SelectionMode;
import com.lambdalogic.util.rcp.datetime.DateComposite;
import com.lambdalogic.util.rcp.widget.NullableSpinner;
import com.lambdalogic.util.rcp.widget.SWTHelper;
import com.lambdalogic.util.rcp.widget.WidgetSizer;
import com.lambdalogic.xml.XMLContainer;

import de.regasus.core.ServerModel;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.model.ServerModelEvent;
import de.regasus.core.model.ServerModelEventType;
import de.regasus.core.ui.view.AbstractView;
import de.regasus.event.EventIdProvider;
import de.regasus.event.EventModel;
import de.regasus.event.combo.EventCombo;
import de.regasus.hotel.editor.HotelEditor;
import de.regasus.hotel.editor.HotelEditorInput;
import de.regasus.hotel.view.search.pref.HotelSearchViewPreference;
import de.regasus.ui.Activator;

/**
 * The view for searching hotels according to MIRCP-1527
 */
public class HotelSearchView extends AbstractView implements EventIdProvider {
	public static final String ID = "HotelSearchView";

	private HotelSearchViewPreference preference;

	// Widgets
	private EventCombo eventCombo;

	private HotelSearchComposite searchComposite;


	/**
	 * The last value of visible as get from the ConfigParameterSet in isVisible().
	 * Has to be stored because the result of isVisible() should not change in the case that the
	 * getConfigParameterSet() returns null.
	 */
	private boolean visible = false;

	private DateComposite arrivalDateComposite;
	private DateComposite departureDateComposite;
	private NullableSpinner roomsCountSpinner;


	public HotelSearchView() {
		preference = HotelSearchViewPreference.getInstance();
	}


	@Override
	protected boolean isVisible() {
		/* Determine the visibility from the ConfigParameterSet.
		 * If getConfigParameterSet() returns null, its last result (the last value of visible)
		 * is returned.
		 */
		if (getConfigParameterSet() != null) {
			visible = getConfigParameterSet().getHotel().isVisible();
		}
		return visible;
	}


	public Long getSelectedEventPK() {
		Long eventPK = null;

		if (eventCombo != null) {
			eventPK = eventCombo.getEventPK();
		}

		return eventPK;
	}


	@Override
	public Long getEventId() {
		Long eventPK = getSelectedEventPK();
		if (eventPK != null) {
			return eventPK;
		}
		else {
			return null;
		}
	}


	@Override
	public void createWidgets(Composite parent) throws Exception {
		if (isVisible()) {
			final int columnCount = 6;

			parent.setLayout(new GridLayout(columnCount, false));

			// 1st standard search field for event
			SWTHelper.createLabel(parent, ParticipantLabel.Event);
			eventCombo = new EventCombo(parent, SWT.READ_ONLY);
			eventCombo.setKeepEntityInList(false);
			eventCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, columnCount - 1, 1));
			eventCombo.addModifyListener(new ModifyListener() {
				@Override
				public void modifyText(ModifyEvent event) {
					initEventDependentWidgets();
				}
			});


			// 2nd standard search fields for arrival and departure

			// arrival
			{
				Label label = new Label(parent, SWT.RIGHT);
				GridData layoutData = new GridData(SWT.RIGHT, SWT.CENTER, false, false);
				layoutData.horizontalIndent = 10;
				label.setLayoutData(layoutData);
				label.setText(HotelLabel.HotelBooking_Arrival.getString());

    			arrivalDateComposite = new DateComposite(parent, SWT.NONE);
    			arrivalDateComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
    			arrivalDateComposite.addModifyListener(new ModifyListener() {
    				@Override
    				public void modifyText(ModifyEvent e) {
    					I18NDate arrivalDate = arrivalDateComposite.getI18NDate();
    					searchComposite.setArrivalDate(arrivalDate);
    				}
    			});
			}


			// departure
			{
				Label label = new Label(parent, SWT.RIGHT);
				GridData layoutData = new GridData(SWT.RIGHT, SWT.CENTER, false, false);
				layoutData.horizontalIndent = 10;
				label.setLayoutData(layoutData);
				label.setText(HotelLabel.HotelBooking_Departure.getString());

    			departureDateComposite = new DateComposite(parent, SWT.NONE);
    			departureDateComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
    			departureDateComposite.addModifyListener(new ModifyListener() {
    				@Override
    				public void modifyText(ModifyEvent e) {
    					I18NDate departureDate = departureDateComposite.getI18NDate();
    					searchComposite.setDepartureDate(departureDate);
    				}
    			});
			}


			// roomCount
			{
				Label label = new Label(parent, SWT.RIGHT);
				GridData layoutData = new GridData(SWT.RIGHT, SWT.CENTER, false, false);
				layoutData.horizontalIndent = 10;
				label.setLayoutData(layoutData);
				label.setText(HotelLabel.HotelContingent_Rooms.getString());

    			roomsCountSpinner = new NullableSpinner(parent, SWT.BORDER);
    			roomsCountSpinner.setMinimum(1);
    			roomsCountSpinner.setMaximum(100);
    			WidgetSizer.setWidth(roomsCountSpinner);
    			roomsCountSpinner.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
    			roomsCountSpinner.addModifyListener(new ModifyListener() {
    				@Override
    				public void modifyText(ModifyEvent e) {
    					Integer roomsCount = roomsCountSpinner.getValueAsInteger();
    					searchComposite.setRoomsCount(roomsCount);
    				}
    			});
			}


			// Further search fields may be added dynamically by the user
			// within the HotelSearchComposite
			searchComposite = new HotelSearchComposite(
				parent,
				SelectionMode.SINGLE_SELECTION,
				SWT.NONE,	// style
				false		// useDetachedSearchModelInstance
			);
			searchComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, columnCount, 1));

			// make the Table the SelectionProvider - for possible commands that might work on selected hotels
			getSite().setSelectionProvider(searchComposite.getTableViewer());

			// make that Ctl+C copies table contents to clipboard
			searchComposite.registerCopyAction(getViewSite().getActionBars());

			// make that double click opens a hotel editor
			initializeDoubleClickAction();

			// init widgets
			initFromPreferences();

			// observer ServerModel to init from preferences on login and save t preferences on logout
			ServerModel.getInstance().addListener(serverModelListener);
		}
		else {
			Label label = new Label(parent, SWT.NONE);
			label.setText(de.regasus.core.ui.CoreI18N.ViewNotAvailable);
		}
	}


	@Override
	public void dispose() {
		try {
			ServerModel.getInstance().removeListener(serverModelListener);
		}
		catch (Exception e) {
			com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
		}

		super.dispose();
	}


	private void initEventDependentWidgets() {
		Long eventPK = getSelectedEventPK();

		// set arrival and departure according to startTime and endTime of Event
		I18NDate arrival = null;
		I18NDate departure = null;

		if (eventPK != null) {
			try {
				// get EventVO
				EventVO eventVO = EventModel.getInstance().getEventVO(eventPK);

				if (eventVO != null) {
    				// get arrival
    				arrival = eventVO.getBeginDate();

    				// calc departure
    				departure = eventVO.getEndDate();
    				if (departure != null) {
    					departure = departure.plusDays(1);
    				}
				}
			}
			catch (Exception e) {
				com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
			}
		}

		arrivalDateComposite.setI18NDate(arrival);
		departureDateComposite.setI18NDate(departure);

		if (eventPK == null) {
			roomsCountSpinner.setValue((Long) null);
		}
		else if (roomsCountSpinner.getValue() == null) {
			roomsCountSpinner.setValue(1L);
		}


		arrivalDateComposite.setEnabled(eventPK != null);
		departureDateComposite.setEnabled(eventPK != null);
		roomsCountSpinner.setEnabled(eventPK != null);


		searchComposite.setEventPK(eventPK);
	}


	private void initializeDoubleClickAction() {
		final TableViewer tableViewer = searchComposite.getTableViewer();
		tableViewer.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				List<Hotel> selectedHotels = SelectionHelper.getSelection(tableViewer, Hotel.class);
				for (Hotel hotel : selectedHotels) {
					Long hotelID = hotel.getPrimaryKey();
					try {
						getSite().getPage().openEditor(new HotelEditorInput(hotelID), HotelEditor.ID);
					}
					catch (PartInitException e) {
						RegasusErrorHandler.handleError(Activator.PLUGIN_ID.getClass().getName(), HotelSearchView.class.getName(), e);
					}
				}
			}
		});
	}


	/* Set focus to searchComposite and therewith to the search button.
	 * Otherwise the focus would be on eventCombo which is not wanted, because the user could change its value by
	 * accident easily.
	 */
	@Override
	public void setFocus() {
		try {
			if (searchComposite != null &&
				!searchComposite.isDisposed() &&
				searchComposite.isEnabled()
			) {
				searchComposite.setFocus();
			}
		}
		catch (Exception e) {
			com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
		}
	}


	public void refreshSelection() {
		ISelection selection = searchComposite.getTableViewer().getSelection();
		searchComposite.getTableViewer().setSelection(selection);

	}


	// *****************************************************************************************************************
	// * Preferences
	// *

	private ModelListener serverModelListener = new ModelListener() {
		@Override
		public void dataChange(ModelEvent event) {
			ServerModelEvent serverModelEvent = (ServerModelEvent) event;
			if (serverModelEvent.getType() == ServerModelEventType.BEFORE_LOGOUT) {
				// save values to preferences before the logout will remove them
				savePreferences();
			}
			else if (serverModelEvent.getType() == ServerModelEventType.LOGIN) {
				SWTHelper.asyncExecDisplayThread(new Runnable() {
					@Override
					public void run() {
						initFromPreferences();
					}
				});
			}
		}
	};


	private void savePreferences() {
		preference.setEventId( eventCombo.getEventPK() );
		preference.setEventFilter( eventCombo.getFilter() );
		preference.setArrival( arrivalDateComposite.getI18NDate() );
		preference.setDeparture( departureDateComposite.getI18NDate() );
		preference.setRoomCount( roomsCountSpinner.getValueAsInteger() );

		// save where clause
		List<SQLParameter> sqlParameterList = searchComposite.getSQLParameterListForPreferences();
		if ( notEmpty(sqlParameterList) ) {
			XMLContainer xmlContainer = new XMLContainer("<searchViewMemento/>");

			WhereClauseReportParameter parameter = new WhereClauseReportParameter(xmlContainer);
			parameter.setSQLParameters(sqlParameterList);

			String xmlSource = xmlContainer.getRawSource();
			preference.setSearchFields(xmlSource);
		}

		preference.setColumnOrder( searchComposite.getColumnOrder() );
		preference.setColumnWidths( searchComposite.getColumnWidths() );
		preference.setResultCountCheckboxSelected( searchComposite.isResultCountSelected() );
		preference.setResultCount( searchComposite.getResultCount() );

		preference.save();
	}


	private void initFromPreferences() {
		try {
    		// eventPK
    		Long eventPK = preference.getEventId();
    		// try to get the event data to assure that the event does exist
    		try {
    			EventModel.getInstance().getEventVO(eventPK);
    		}
    		catch (Exception e) {
    			// don't show error dialog, just log the error
    			System.err.println(e);
    			eventPK = null;
    		}
   			eventCombo.setEventPK(eventPK);


    		// eventFilter
    		eventCombo.setFilter( preference.getEventFilter() );


    		initEventDependentWidgets();


    		arrivalDateComposite.setI18NDate( preference.getArrival() );
    		departureDateComposite.setI18NDate( preference.getDeparture() );
    		roomsCountSpinner.setValue( preference.getRoomCount() );

    		// set eventPK in SearchComposite, even if it is null
    		searchComposite.setEventPK(eventPK);


    		// search fields
    		String whereClauseXMLSource = preference.getSearchFields();
    		if ( isNotEmpty(whereClauseXMLSource) ) {
    			XMLContainer xmlContainer = new XMLContainer(whereClauseXMLSource);
    			WhereClauseReportParameter parameters = new WhereClauseReportParameter(xmlContainer);
    			List<WhereField> whereFields = parameters.getWhereFields();
    			searchComposite.setWhereFields(whereFields);
    		}


    		// InvalidThreadAccess can happen, because this method runs
    		// in non-UI-Thread via BusyCursorHelper.busyCursorWhile(Runnable)
    		// after "System > Alles aktualisieren"
    		Runnable tableUpdater = new Runnable() {
    			@Override
    			public void run() {
    				int[] columnOrder = preference.getColumnOrder();
    				if (columnOrder != null) {
    					searchComposite.setColumnOrder(columnOrder);
    				}

    				int[] columnWidths = preference.getColumnWidths();
    				if (columnWidths != null) {
    					searchComposite.setColumnWidths(columnWidths);
    				}
    			}
    		};
    		SWTHelper.asyncExecDisplayThread(tableUpdater);

			searchComposite.setResultCountSelection( preference.isResultCountCheckboxSelected() );
			searchComposite.setResultCount( preference.getResultCount() );
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}

	// *
	// * Preferences
	// *****************************************************************************************************************

}
