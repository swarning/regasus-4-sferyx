package de.regasus.hotel.view.search;

import static com.lambdalogic.util.rcp.KeyEventHelper.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IActionBars;

import com.lambdalogic.messeinfo.contact.Address;
import com.lambdalogic.messeinfo.contact.ContactLabel;
import com.lambdalogic.messeinfo.exception.InvalidValuesException;
import com.lambdalogic.messeinfo.hotel.Hotel;
import com.lambdalogic.messeinfo.hotel.sql.EventArrivalDepartureSQLField;
import com.lambdalogic.messeinfo.hotel.sql.HotelSearch;
import com.lambdalogic.messeinfo.kernel.data.SearchFieldsCVO;
import com.lambdalogic.messeinfo.kernel.interfaces.SQLField;
import com.lambdalogic.messeinfo.kernel.interfaces.SQLOperator;
import com.lambdalogic.messeinfo.kernel.sql.SQLParameter;
import com.lambdalogic.messeinfo.participant.report.parameter.WhereField;
import com.lambdalogic.messeinfo.participant.sql.ParticipantSearch;
import com.lambdalogic.time.I18NDate;
import com.lambdalogic.util.CollectionsHelper;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.model.CacheModelOperation;
import com.lambdalogic.util.model.ModelEvent;
import com.lambdalogic.util.model.ModelListener;
import com.lambdalogic.util.rcp.BusyCursorHelper;
import com.lambdalogic.util.rcp.ClipboardHelper;
import com.lambdalogic.util.rcp.SelectionHelper;
import com.lambdalogic.util.rcp.SelectionMode;
import com.lambdalogic.util.rcp.UtilI18N;
import com.lambdalogic.util.rcp.widget.NumberText;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.I18N;
import de.regasus.core.CountryModel;
import de.regasus.core.ServerModel;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.search.ISearcher;
import de.regasus.core.ui.search.SearchInterceptor;
import de.regasus.core.ui.search.WhereComposite;
import de.regasus.hotel.HotelModel;
import de.regasus.hotel.HotelSQLFieldsModel;
import de.regasus.hotel.HotelSearchModel;
import de.regasus.ui.Activator;

public class HotelSearchComposite
extends Composite
implements DisposeListener, CacheModelListener<Long>, ModelListener, ISearcher {

	public static final int DEFAULT_RESULT_COUNT = 500;
	public static final boolean DEFAULT_RESULT_COUNT_SELECTION = true;

	// Models
	private HotelSearchModel hotelSearchModel;
	private HotelSQLFieldsModel hotelSQLFieldsModel;
	private HotelModel hotelModel;
	private CountryModel countryModel;

	// Values for search parameters set in the surrounding view
	private Long eventPK;
	private I18NDate arrival;
	private I18NDate departure;
	private Integer roomsCount;

	private HotelSearch hotelSearch;
	private SearchInterceptor searchInterceptor;

	// Widgets
	private WhereComposite whereComposite;
	private TableViewer tableViewer;
	private HotelSearchTable hotelSearchTable;
	private Label resultCount;
	private Button searchButton;
	private Button resultCountLimitCheckbox;
	private NumberText resultCountLimitNumberText;



	/**
	 * Stores parameters to be used when this search composite is supposed to show initially a search
	 * criterium (like for group membership)
	 */
	private List<SQLParameter> initialSQLParameters;

	/**
	 * A flag whether the initial parameters have been presented, because they are set in {@link #setEventPK(Long)}, but
	 * are to be set of course only once, and not again in case the event gets changed
	 */
	private boolean initialSearchParametersSet;


	/**
	 * Create the composite for searching with the common singleton {@link HotelModel} instance.
	 *
	 * @param parent
	 * @param style
	 */
	public HotelSearchComposite(
		Composite parent,
		SelectionMode selectionMode,
		int style,
		boolean useDetachedSearchModelInstance
	) {
		super(parent, style);

		addDisposeListener(this);

		if (selectionMode == null) {
			selectionMode = SelectionMode.NO_SELECTION;
		}


		// get model instances
		hotelModel = HotelModel.getInstance();
		countryModel = CountryModel.getInstance();
		hotelSQLFieldsModel = HotelSQLFieldsModel.getInstance();
		if (useDetachedSearchModelInstance) {
			hotelSearchModel = HotelSearchModel.getDetachedInstance();
		}
		else {
			hotelSearchModel = HotelSearchModel.getInstance();
		}


		setLayout(new FillLayout());

		SashForm sashForm = new SashForm(this, SWT.VERTICAL);

		ScrolledComposite scrolledComposite = new ScrolledComposite(sashForm, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		scrolledComposite.setExpandHorizontal(true);
		scrolledComposite.setExpandVertical(true);
		scrolledComposite.setMinWidth(400);
		scrolledComposite.setMinHeight(100);

		whereComposite = new WhereComposite(scrolledComposite, SWT.NONE);
		scrolledComposite.setContent(whereComposite);
		whereComposite.setSearcher(this);

		Composite searchComposite = new Composite(sashForm, SWT.NONE);
		searchComposite.setLayout(new GridLayout());

		Composite searchButtonComposite = new Composite(searchComposite, SWT.NONE);
		searchButtonComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		searchButtonComposite.setLayout(new GridLayout(4, false));

		searchButton = new Button(searchButtonComposite, SWT.NONE);
		searchButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				doSearch();
			}
		});
		searchButton.setText(UtilI18N.Search);
		searchButton.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false));

		// result count limit
		resultCountLimitCheckbox = new Button(searchButtonComposite, SWT.CHECK);
		resultCountLimitCheckbox.setSelection(DEFAULT_RESULT_COUNT_SELECTION);
		resultCountLimitCheckbox.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				resultCountLimitNumberText.setEnabled(resultCountLimitCheckbox.getSelection());
			}
		});

		final Label limitCountLabel = new Label(searchButtonComposite, SWT.NONE);
		limitCountLabel.setText(I18N.HotelSearchComposite_ResultCountLimit);
		limitCountLabel.setToolTipText(I18N.HotelSearchComposite_ResultCountLimit_description);

		resultCountLimitNumberText = new NumberText(searchButtonComposite, SWT.BORDER);
		resultCountLimitNumberText.setValue(DEFAULT_RESULT_COUNT);
		{
    		GridData gd = new GridData(SWT.FILL, SWT.CENTER, false, false);
    		gd.widthHint = SWTHelper.computeTextWidgetWidthForCharCount(resultCountLimitNumberText, 6);
    		resultCountLimitNumberText.setLayoutData(gd);
		}

		// **************************************************************************
		// * SearchTable
		// *

		// search result table
		final Composite searchTableComposite = new Composite(searchComposite, SWT.BORDER);
		searchTableComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		searchTableComposite.setLayout(new FillLayout());

		// create SWT Table
		Table table = new Table(searchTableComposite, selectionMode.getSwtStyle());
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		table.addKeyListener(tableKeyListener);

		// create TableColumns (and set their width and header text)
		final TableColumn numberTableColumn = new TableColumn(table, SWT.NONE);
		numberTableColumn.setWidth(160);
		numberTableColumn.setText(ContactLabel.Organisation_name1.getString());

		final TableColumn cityTableColumn = new TableColumn(table, SWT.NONE);
		cityTableColumn.setWidth(110);
		cityTableColumn.setText( Address.CITY.getString() );

		final TableColumn countryTableColumn = new TableColumn(table, SWT.NONE);
		countryTableColumn.setWidth(110);
		countryTableColumn.setText( Address.COUNTRY.getString() );


		// create HotelSearchTable
		hotelSearchTable = new HotelSearchTable(table);

		tableViewer = hotelSearchTable.getViewer();

		// make all columns moveable
		for (TableColumn tableColumn : table.getColumns()) {
			tableColumn.setMoveable(true);
		}

		// *
		// * SearchTable
		// **************************************************************************


		final Composite searchResultComposite = new Composite(searchComposite, SWT.NONE);

		searchResultComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		searchResultComposite.setLayout(new GridLayout(2, false));

		final Label foundHotelsLabel = new Label(searchResultComposite, SWT.NONE);
		foundHotelsLabel.setText(I18N.HotelSearchComposite_FoundHotelsLabel);
		foundHotelsLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));

		resultCount = new Label(searchResultComposite, SWT.NONE);
		resultCount.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));


		sashForm.setWeights(new int[] { 2, 3 });

		// observe models
		hotelSearchModel.addListener(this);
		hotelModel.addListener(this);
	}


	private KeyListener tableKeyListener = new KeyAdapter() {
		@Override
		public void keyPressed(org.eclipse.swt.events.KeyEvent event) {
			try {
    			if (
    				   isCopy(event)
    				|| isCopyPK(event)
    				|| isCopyVigenere1(event)
    				|| isCopyVigenere1Hex(event)
    				|| isCopyVigenere2(event)
    				|| isCopyVigenere2Hex(event)
    			) {
    				List<Hotel> hotelList = SelectionHelper.getSelection(tableViewer, Hotel.class);
    				StringBuilder text = new StringBuilder(hotelList.size() * 34);
    				for (Hotel hotel : hotelList) {
    					// determine clipboard content depending on keys

    					if ( isCopy(event) ) {
    						if (text.length() > 0) text.append("\n");
    						text.append( hotel.getCopyInfo() );
    					}
    					else if ( isCopyPK(event) ) {
    						if (text.length() > 0) text.append(", ");
    						text.append( hotel.getID() );
    					}
    					else if ( isCopyVigenere1(event) ) {
    						if (text.length() > 0) text.append(", ");
    						text.append( hotel.getVigenereCode() );
    					}
    					else if ( isCopyVigenere1Hex(event) ) {
    						if (text.length() > 0) text.append(", ");
    						text.append( hotel.getVigenereCodeHex() );
    					}
    					else if ( isCopyVigenere2(event) ) {
    						if (text.length() > 0) text.append(", ");
    						text.append( hotel.getVigenere2Code() );
    					}
    					else if ( isCopyVigenere2Hex(event) ) {
    						if (text.length() > 0) text.append(", ");
    						text.append( hotel.getVigenere2CodeHex() );
    					}
					}

    				ClipboardHelper.copyToClipboard( text.toString() );
    			}
			}
			catch (Exception e) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			}
    	};
	};


	@Override
	public void widgetDisposed(DisposeEvent e) {
		if (hotelSQLFieldsModel != null) {
			hotelSQLFieldsModel.removeListener(
				this,
				eventPK != null ? eventPK : HotelSQLFieldsModel.NO_EVENT_KEY
			);
		}

		if (hotelModel != null) {
			hotelModel.removeListener(this);
		}

		if (hotelSearchModel != null) {
			hotelSearchModel.removeListener(this);
		}
	}


	public void setInitialSQLParameters(List<SQLParameter> initialSQLParameters) {
		this.initialSQLParameters = initialSQLParameters;
	}


	public void setSearchInterceptor(SearchInterceptor searchInterceptor) {
		this.searchInterceptor = searchInterceptor;
	}


	public Long getEventPK() {
		return eventPK;
	}


	public void setEventPK(final Long newEventPK) {
		if (this.eventPK != null && this.eventPK.equals(newEventPK)) {
			return;
		}


		final Long oldEventPK = this.eventPK;
		this.eventPK = newEventPK;

		hotelSearchModel.setEventPK(newEventPK);

		BusyCursorHelper.busyCursorWhile(new Runnable() {
			@Override
			public void run() {
				try {
					hotelSQLFieldsModel.removeListener(
						HotelSearchComposite.this,
						oldEventPK != null ? oldEventPK : HotelSQLFieldsModel.NO_EVENT_KEY
					);

					hotelSearch = hotelSQLFieldsModel.getHotelSearch(
						newEventPK != null ? newEventPK : HotelSQLFieldsModel.NO_EVENT_KEY
					);

					hotelSQLFieldsModel.addListener(
						HotelSearchComposite.this,
						newEventPK != null ? newEventPK : HotelSQLFieldsModel.NO_EVENT_KEY
					);


					/* When this method is called during logout, SearchFieldsCVO cannot get data
					 * from the server!
					 */

					List<SQLField> sqlFields = null;
					if (ServerModel.getInstance().isLoggedIn()) {
    					SearchFieldsCVO searchFieldsCVO = hotelSearch.getSearchFieldsCVO();

    					sqlFields = searchFieldsCVO.getWhereFields();

    					/* Remove SQLFields which are set separately from event combo
    					 * and arrival and departure date composites
    					 */
    					sqlFields.remove(HotelSearch.EVENT_ARRIVAL_DEPARTURE);
					}
					else {
						sqlFields = Collections.emptyList();
					}

					// Sets the possible Search-Fields.
					whereComposite.setSqlFields(sqlFields);
				}
				catch (Exception e) {
					RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
				}
			}
		});

		try {
			if (!initialSearchParametersSet) {
				/* If nobody wanted particular search fields in the beginning, choose the default
				 * ones, but inactive.
				 */

				if (CollectionsHelper.empty(initialSQLParameters)) {
					// Take the default criteria (which AINT'GET
					initialSQLParameters = new ArrayList<>();

					initialSQLParameters.add(
						HotelSearch.NAME1.getSQLParameter("", SQLOperator.FUZZY_LOWER_ASCII)
					);

					whereComposite.setSQLParameterList(initialSQLParameters);
					whereComposite.setActive(false);
				}
				else {
					whereComposite.setSQLParameterList(initialSQLParameters);
				}

				initialSearchParametersSet = true;
			}
		}
		catch (InvalidValuesException e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	@Override
	public void dataChange(ModelEvent event) {
		if (event.getSource() == hotelSearchModel) {
			List<Hotel> hotelList = null;
			try {
				hotelList = hotelSearchModel.getModelData();
			}
			catch (Exception e) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			}

			if (hotelList == null) {
				hotelList = Collections.emptyList();
			}


			/*
			 * Preload country data to avoid multiple server calls by getting them one by one.
			 * Background info: HotelSearchTable.getColumnText() is getting country data
			 * from CountryModel for each table row.
			 */

			// collect PKs of all countries
			Set<String> countryPKs = CollectionsHelper.createHashSet(hotelList.size());
			for (Hotel hotel : hotelList) {
				String countryPK = hotel.getMainAddress().getCountryPK();
				countryPKs.add(countryPK);
			}

			// get country data from CountryModel to get them into its cache
			try {
				countryModel.assureCacheSize(countryPKs.size());
				countryModel.getCountrys(countryPKs);
			}
			catch (Exception e) {
				com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			}


			final List<Hotel> finalHotelList = hotelList;

			SWTHelper.asyncExecDisplayThread(new Runnable() {
				@Override
				public void run() {
					try {
						tableViewer.setInput(finalHotelList);

						int size = finalHotelList.size();

						if (size > 0) {
							Table table = tableViewer.getTable();
							table.select(0);
							table.forceFocus();
							table.notifyListeners(SWT.Selection, null);
						}

						resultCount.setText(String.valueOf(size));
					}
					catch (Throwable t) {
						RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t);
					}
				}
			});
		}
	}


	@SuppressWarnings("unchecked")
	@Override
	public void dataChange(CacheModelEvent<Long> event) {
		// if application is getting closed, the event must be ignored
		if (isDisposed()) {
			return;
		}


		// auf Änderungen an Hotels reagieren
		try {
			if (event.getSource() == hotelSQLFieldsModel) {
				// data in HotelSQLFieldsModel changed

				hotelSearch = hotelSQLFieldsModel.getHotelSearch(
					eventPK != null ? eventPK : HotelSQLFieldsModel.NO_EVENT_KEY
				);

				List<SQLField> sqlFields = null;
				if (hotelSearch != null) {
					SearchFieldsCVO searchFieldsCVO = hotelSearch.getSearchFieldsCVO();
					sqlFields = searchFieldsCVO.getWhereFields();

					/* Remove EVENT. It must not appear in the list of search criteria, because
					 * there is a special widget for it.
					 */
					sqlFields.remove(ParticipantSearch.EVENT);
				}

				// Sets the possible Search-Fields.
				whereComposite.setSqlFields(sqlFields);
			}
			else if (event.getSource() == hotelModel) {
				boolean refresh = false;

				if (event.getOperation() == CacheModelOperation.UPDATE ||
					event.getOperation() == CacheModelOperation.REFRESH
				) {
					// Daten der geänderten Hotels vom Model holen und aktualisieren
					final List<Hotel> hotelList = (List<Hotel>) tableViewer.getInput();
					final List<Long> editedHotelKeys = event.getKeyList();
					if (hotelList != null && editedHotelKeys != null) {
						for (Long editedHotelKey : editedHotelKeys) {
							for (ListIterator<Hotel> it = hotelList.listIterator(); it.hasNext();) {
								Hotel hotel = it.next();
								if (editedHotelKey.equals(hotel.getID())) {
									hotel = hotelModel.getHotel(editedHotelKey);
									it.set(hotel);
									refresh = true;
									break;
								}
							}
						}
					}
				}
				else if (event.getOperation() == CacheModelOperation.DELETE) {
					// remove deleted Hotels
					final List<Hotel> hotelList = (List<Hotel>) tableViewer.getInput();
					final List<?> deletedHotelKeys = event.getKeyList();
					if (hotelList != null && deletedHotelKeys != null) {
						for (Object deletedHotelKey : deletedHotelKeys) {
							for (Iterator<Hotel> it = hotelList.iterator(); it.hasNext();) {
								Hotel hotel = it.next();
								if (deletedHotelKey.equals(hotel.getPrimaryKey())) {
									it.remove();
									refresh = true;
									break;
								}
							}
						}
					}
					refresh = true;
				}

				// if data has changed, update table and number of rows
				if (refresh) {
					SWTHelper.syncExecDisplayThread(new Runnable() {
						@Override
						public void run() {
							tableViewer.refresh();

							final List<?> list = (List<?>) tableViewer.getInput();
							resultCount.setText(String.valueOf(list.size()));
						}
					});
				}

			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}

	}


	@Override
	public void doSearch() {
		try {
			List<SQLParameter> sqlParameters = whereComposite.getSQLParameterList();


			// add SQLParameter for fields that are set in surrounding View
			EventArrivalDepartureSQLField.Value value = new EventArrivalDepartureSQLField.Value();
			value.eventPK = eventPK;
			value.arrival = arrival;
			value.departure = departure;
			value.roomCount = roomsCount;

			if (value.isValid()) {
				EventArrivalDepartureSQLField sqlField = new EventArrivalDepartureSQLField(null);
				SQLParameter sqlParameter = sqlField.getSQLParameter(value, SQLOperator.EQUAL);
				sqlParameters.add(sqlParameter);
			}


			// set ResultCountLimit
			if (resultCountLimitCheckbox.getSelection()) {
				hotelSearchModel.setResultCountLimit(resultCountLimitNumberText.getValue());
			}
			else {
				hotelSearchModel.setResultCountLimit(null);
			}

			// give the SearchInterceptor a chance to modify the SQL-Parameters
			if (searchInterceptor != null) {
				searchInterceptor.changeSearchParameter(sqlParameters);
			}

			hotelSearchModel.setSqlParameterList(sqlParameters);
		}
		catch (Throwable t) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t);
		}
	}


	public TableViewer getTableViewer() {
		return tableViewer;
	}


	public List<SQLParameter> getSQLParameter() {
		return whereComposite.getSQLParameterList();
	}


	public List<SQLParameter> getSQLParameterListForPreferences() {
		return whereComposite.getSQLParameterListForPreferences();
	}


	public String getDescription() {
		return whereComposite.getDescription();
	}


	public HotelSearch getHotelSearch() {
		return hotelSearch;
	}


	public void setSQLParameters(List<SQLParameter> sqlParameters) {
		whereComposite.setSQLParameterList(sqlParameters);
	}


	public void setWhereFields(List<WhereField> whereFields) {
		try {
			List<SQLParameter> sqlParameterList = null;

			// hotelSearch is null if the event has not been set yet
			if (whereFields != null) {
				sqlParameterList = new ArrayList<>(whereFields.size());
				for (WhereField whereField : whereFields) {
					if (whereField.getKey() != null) {
						SQLField sqlField = hotelSearch.getSQLField(whereField.getKey());

						if (sqlField != null) {
							SQLParameter sqlParameter = sqlField.getSQLParameter(
								whereField.getValue(),
								whereField.getSqlOperator()
							);
							sqlParameter.setActive(whereField.isActive());

							sqlParameterList.add(sqlParameter);
						}
					}
				}
			}

			setSQLParameters(sqlParameterList);
		}
		catch (Throwable t) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t);
		}
	}


	public void setWhereFieldsFromMemento(List<WhereField> whereFields) {
		List<SQLParameter> sqlParameterList = null;

		// hotelSearch is null if the event has not been set yet
		if (whereFields != null && hotelSearch != null) {
			sqlParameterList = new ArrayList<>(whereFields.size());
			for (WhereField whereField : whereFields) {
				if (whereField.getKey() != null) {
					SQLField sqlField = hotelSearch.getSQLField(whereField.getKey());

					if (sqlField != null) {
						SQLParameter sqlParameter = null;
						try {
    						sqlParameter = sqlField.getSQLParameter(
    							whereField.getValue(),
    							whereField.getSqlOperator()
    						);
						}
						catch (InvalidValuesException e) {
							/* InvalidValuesExceptions may happen if the user types values that are
							 * not valid. Actually this can only happen if the widget is a Text.
							 * Therefore we create a SQLParameter with a value od type String.
							 */
    						sqlParameter = new SQLParameter(
    							sqlField,
    							whereField.getValue(),
    							whereField.getSqlOperator()
    						);
						}
						sqlParameter.setActive(whereField.isActive());
						sqlParameterList.add(sqlParameter);
					}
				}
			}
		}

		setSQLParameters(sqlParameterList);
	}


	/**
	 * Make that ctrl+c copies table contents to clipboard
	 */
	public void registerCopyAction(IActionBars actionBars) {
		hotelSearchTable.registerCopyAction(actionBars);

	}


	public int[] getColumnOrder() {
		return tableViewer.getTable().getColumnOrder();
	}


	public void setColumnOrder(int[] columnOrder) {
		try {
			tableViewer.getTable().setColumnOrder(columnOrder);
		}
		catch (Exception e) {
			// ignore, because the number of column may change
			com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
		}
	}


	public int[] getColumnWidths() {
		TableColumn[] tableColumns = tableViewer.getTable().getColumns();
		int[] columnWidths = new int[tableColumns.length];
		for (int i = 0; i < tableColumns.length; i++) {
			int width = tableColumns[i].getWidth();
			columnWidths[i] = width;
		}
		return columnWidths;
	}


	public void setColumnWidths(int[] columnWidths) {
		if (columnWidths != null) {
			TableColumn[] tableColumns = tableViewer.getTable().getColumns();
			for (int i = 0; i < tableColumns.length && i < columnWidths.length; i++) {
				tableColumns[i].setWidth(columnWidths[i]);
			}
		}
	}


	/* Set the focus to searchButton.
	 */
	@Override
	public boolean setFocus() {
		try {
			if (isEnabled() && searchButton != null) {
				return searchButton.setFocus();
			}
		}
		catch (Exception e) {
			com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
		}
		return super.setFocus();
	}


	public boolean isResultCountSelected() {
		return resultCountLimitCheckbox.getSelection();
	}


	public void setResultCountSelection(boolean selected) {
		resultCountLimitCheckbox.setSelection(selected);
		resultCountLimitNumberText.setEnabled(selected);
	}


	public Integer getResultCount() {
		return resultCountLimitNumberText.getValue();
	}


	public void setResultCount(Integer value) {
		resultCountLimitNumberText.setValue(value);
	}


	public void setArrivalDate(I18NDate arrival) {
		this.arrival = arrival;
	}


	public void setDepartureDate(I18NDate departure) {
		this.departure = departure;
	}


	public void setRoomsCount(Integer count) {
		this.roomsCount = count;
	}


	public void removeSelectionChangedListener(ISelectionChangedListener listener) {
		tableViewer.removeSelectionChangedListener(listener);
	}


	public void addPostSelectionChangedListener(ISelectionChangedListener listener) {
		tableViewer.addPostSelectionChangedListener(listener);
	}

}
