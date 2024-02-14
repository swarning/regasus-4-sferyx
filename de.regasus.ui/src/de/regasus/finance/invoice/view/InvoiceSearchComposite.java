package de.regasus.finance.invoice.view;

import static com.lambdalogic.util.rcp.KeyEventHelper.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnWeightData;
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

import com.lambdalogic.messeinfo.exception.InvalidValuesException;
import com.lambdalogic.messeinfo.invoice.InvoiceLabel;
import com.lambdalogic.messeinfo.invoice.data.InvoiceCVO;
import com.lambdalogic.messeinfo.invoice.data.InvoiceVO;
import com.lambdalogic.messeinfo.invoice.sql.InvoiceSearch;
import com.lambdalogic.messeinfo.kernel.data.AbstractCVO;
import com.lambdalogic.messeinfo.kernel.data.SearchFieldsCVO;
import com.lambdalogic.messeinfo.kernel.interfaces.SQLField;
import com.lambdalogic.messeinfo.kernel.interfaces.SQLOperator;
import com.lambdalogic.messeinfo.kernel.sql.SQLParameter;
import com.lambdalogic.messeinfo.participant.report.parameter.WhereField;
import com.lambdalogic.util.CollectionsHelper;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.model.ModelEvent;
import com.lambdalogic.util.model.ModelListener;
import com.lambdalogic.util.rcp.BusyCursorHelper;
import com.lambdalogic.util.rcp.ClipboardHelper;
import com.lambdalogic.util.rcp.SelectionHelper;
import com.lambdalogic.util.rcp.UtilI18N;
import com.lambdalogic.util.rcp.widget.NumberText;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.I18N;
import de.regasus.core.ServerModel;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.search.ISearcher;
import de.regasus.core.ui.search.SearchInterceptor;
import de.regasus.core.ui.search.WhereComposite;
import de.regasus.finance.InvoiceSQLFieldsModel;
import de.regasus.finance.InvoiceSearchModel;
import de.regasus.ui.Activator;

public class InvoiceSearchComposite
extends Composite
implements DisposeListener, CacheModelListener<Long>, ModelListener, ISearcher {

	public static final int DEFAULT_RESULT_COUNT = 500;
	public static final boolean DEFAULT_RESULT_COUNT_SELECTION = true;

	// Models
	private InvoiceSearchModel invoiceSearchModel = InvoiceSearchModel.getInstance();

	private InvoiceSQLFieldsModel invoiceSQLFieldsModel = InvoiceSQLFieldsModel.getInstance();

	// Widgets
	private WhereComposite whereComposite;
	private TableViewer tableViewer;
	private Label resultCount;
	private Button searchButton;
	private InvoiceSearchTable invoiceSearchTable;
	private Button resultCountLimitCheckbox;
	private NumberText resultCountLimitNumberText;

	// Other Attributes
	private Long eventPK;

	private InvoiceSearch invoiceSearch;

	private SearchInterceptor searchInterceptor;


	/**
	 * Stores parameters to be used when this search composite is supposed to show initially a seach criterium (like for
	 * group membership)
	 */
	private List<SQLParameter> initialSQLParameters;

	/**
	 * A flag whether the initial parameters have been presented, because they are set in {@link #setEventPK(Long)}, but
	 * are to be set of course only once, and not again in case the event gets changed
	 */
	private boolean initialSearchParametersSet;

	// *************************************************************************
	// * Constructors
	// *

	public InvoiceSearchComposite(Composite parent, int style) {
		this(parent, style, false);
	}


	public InvoiceSearchComposite(Composite parent, int style, boolean useDetachedSearchModel) {
		super(parent, style);

		if (useDetachedSearchModel) {
			invoiceSearchModel = InvoiceSearchModel.getDetachedInstance();
		}

		this.addDisposeListener(this);

		setLayout(new FillLayout());

		final SashForm sashForm = new SashForm(this, SWT.VERTICAL);

		ScrolledComposite scrolledComposite = new ScrolledComposite(sashForm, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		scrolledComposite.setExpandHorizontal(true);
		scrolledComposite.setExpandVertical(true);
		scrolledComposite.setMinWidth(400);
		scrolledComposite.setMinHeight(100);

		whereComposite = new WhereComposite(scrolledComposite, SWT.NONE);
		whereComposite.setSearcher(this);
		scrolledComposite.setContent(whereComposite);

		final Composite searchComposite = new Composite(sashForm, SWT.NONE);
		searchComposite.setLayout(new GridLayout());

		final Composite searchButtonComposite = new Composite(searchComposite, SWT.NONE);
		searchButtonComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		searchButtonComposite.setLayout(new GridLayout(4, false));


		searchButton = new Button(searchButtonComposite, SWT.NONE);
		searchButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				doSearch();
			}
		});
		searchButton.setText(I18N.ParticipantSearchComposite_SearchButtonText);
		searchButton.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false));

		resultCountLimitCheckbox = new Button(searchButtonComposite, SWT.CHECK);
		resultCountLimitCheckbox.setSelection(DEFAULT_RESULT_COUNT_SELECTION);
		resultCountLimitCheckbox.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				resultCountLimitNumberText.setEnabled(resultCountLimitCheckbox.getSelection());
			}
		});

		final Label limitCountLabel = new Label(searchButtonComposite, SWT.NONE);
		limitCountLabel.setText(I18N.ParticipantSearchComposite_ResultCountLimit);
		limitCountLabel.setToolTipText(I18N.ParticipantSearchComposite_ResultCountLimit_description);

		resultCountLimitNumberText = new NumberText(searchButtonComposite, SWT.BORDER);
		resultCountLimitNumberText.setValue(DEFAULT_RESULT_COUNT);

		GridData gd = new GridData(SWT.FILL, SWT.CENTER, false, false);
		gd.widthHint = SWTHelper.computeTextWidgetWidthForCharCount(resultCountLimitNumberText, 6);
		resultCountLimitNumberText.setLayoutData(gd);

		final Composite searchTableComposite = new Composite(searchComposite, SWT.BORDER);
		searchTableComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		TableColumnLayout layout = new TableColumnLayout();
		searchTableComposite.setLayout(layout);

		// **************************************************************************
		// * SearchTable
		// *

		Table table = new Table(searchTableComposite, SWT.MULTI | SWT.FULL_SELECTION);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		table.addKeyListener(tableKeyListener);

		TableColumn numberTableColumn = new TableColumn(table, SWT.RIGHT);
		numberTableColumn.setText(UtilI18N.NumberAbreviation);

		TableColumn invoiceNumberRangeTableColumn = new TableColumn(table, SWT.NONE);
		invoiceNumberRangeTableColumn.setText(InvoiceLabel.InvoiceNoRange.getString());

		TableColumn nameTableColumn = new TableColumn(table, SWT.NONE);
		nameTableColumn.setText(UtilI18N.Name);

		TableColumn invoiceDateTableColumn = new TableColumn(table, SWT.NONE);
		invoiceDateTableColumn.setText(InvoiceLabel.InvoiceDate.getString());

		TableColumn amountTableColumn = new TableColumn(table, SWT.RIGHT);
		amountTableColumn.setText(InvoiceLabel.Amount.getString());

		TableColumn openAmountTableColumn = new TableColumn(table, SWT.RIGHT);
		openAmountTableColumn.setText(InvoiceLabel.Open.getString());

		TableColumn reminderStateTableColumn = new TableColumn(table, SWT.LEFT);
		reminderStateTableColumn.setText(InvoiceLabel.ReminderState.getString());

		TableColumn nextReminderTableColumn = new TableColumn(table, SWT.NONE);
		nextReminderTableColumn.setText(InvoiceLabel.NextReminder.getString());

		TableColumn finalPayTimeDateTableColumn = new TableColumn(table, SWT.NONE);
		finalPayTimeDateTableColumn.setText(InvoiceLabel.Invoice_FinalPayTimeDate.getString());


		layout.setColumnData(numberTableColumn,				new ColumnWeightData(20));
		layout.setColumnData(invoiceNumberRangeTableColumn,	new ColumnWeightData(60));
		layout.setColumnData(nameTableColumn,				new ColumnWeightData(80));
		layout.setColumnData(invoiceDateTableColumn,		new ColumnWeightData(40));
		layout.setColumnData(amountTableColumn,				new ColumnWeightData(40));
		layout.setColumnData(openAmountTableColumn,			new ColumnWeightData(40));
		layout.setColumnData(reminderStateTableColumn,		new ColumnWeightData(40));
		layout.setColumnData(nextReminderTableColumn,		new ColumnWeightData(40));
		layout.setColumnData(finalPayTimeDateTableColumn,	new ColumnWeightData(40));


		invoiceSearchTable = new InvoiceSearchTable(table);

		Composite searchResultComposite = new Composite(searchComposite, SWT.NONE);

		searchResultComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		searchResultComposite.setLayout(new GridLayout(2, false));

		Label foundInvoicesLabel = new Label(searchResultComposite, SWT.NONE);
		foundInvoicesLabel.setText(I18N.InvoiceSearchComposite_FoundInvoicesLabel);
		foundInvoicesLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));

		resultCount = new Label(searchResultComposite, SWT.NONE);
		resultCount.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		tableViewer = invoiceSearchTable.getViewer();

		// *
		// * SearchTable
		// **************************************************************************

		sashForm.setWeights(new int[] { 2, 3 });

		invoiceSearchModel.addListener(this);
	}


	private KeyListener tableKeyListener = new KeyAdapter() {
		@Override
		public void keyPressed(org.eclipse.swt.events.KeyEvent event) {
			try {
    			if (
    				   isCopy(event)
    				|| isCopyPK(event)
    			) {
    				List<InvoiceCVO> invoiceList = SelectionHelper.getSelection(tableViewer, InvoiceCVO.class);
    				StringBuilder text = new StringBuilder(invoiceList.size() * 34);
    				for (InvoiceCVO invoice : invoiceList) {
    					// determine clipboard content depending on keys

    					if ( isCopy(event) ) {
    						if (text.length() > 0) text.append("\n");
    						text.append( invoice.getCopyInfo() );
    					}
    					else if ( isCopyPK(event) ) {
    						if (text.length() > 0) text.append(", ");
    						text.append( invoice.getPK() );
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


	/**
	 * The InvoiceSQLFieldsModel might be null if this composite is part of a wizard page which has never been made
	 * visible because the wizard was cancelled.
	 */
	@Override
	public void widgetDisposed(DisposeEvent e) {
		if (invoiceSearchModel != null) {
			invoiceSearchModel.removeListener(this);
		}

		if (invoiceSQLFieldsModel != null) {
			invoiceSQLFieldsModel.removeListener(
				this,
				eventPK != null ? eventPK : InvoiceSQLFieldsModel.NO_EVENT_KEY
			);
		}
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

		invoiceSearchModel.setEventPK(newEventPK);

		BusyCursorHelper.busyCursorWhile(new Runnable() {
			@Override
			public void run() {
				try {
					invoiceSQLFieldsModel.removeListener(
						InvoiceSearchComposite.this,
						oldEventPK != null ? oldEventPK : InvoiceSQLFieldsModel.NO_EVENT_KEY
					);

					invoiceSearch = invoiceSQLFieldsModel.getInvoiceSearch(
						newEventPK != null ? newEventPK : InvoiceSQLFieldsModel.NO_EVENT_KEY
					);

					invoiceSQLFieldsModel.addListener(
						InvoiceSearchComposite.this,
						newEventPK != null ? newEventPK : InvoiceSQLFieldsModel.NO_EVENT_KEY
					);


					/* When this method is called during logout, SearchFieldsCVO cannot get data
					 * from the server!
					 */
					List<SQLField> sqlFields = null;
					if (ServerModel.getInstance().isLoggedIn()) {
						SearchFieldsCVO searchFieldsCVO = invoiceSearch.getSearchFieldsCVO();

						sqlFields = searchFieldsCVO.getWhereFields();
    					/* Remove SQLField EVENT, so that it's not in the list of search criteria
    					 * the user can see. The event is specified by selection in a combo box.
    					 */
						sqlFields.remove(InvoiceSearch.EVENT);
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
		if (!initialSearchParametersSet) {

			if (!CollectionsHelper.empty(initialSQLParameters)) {
				whereComposite.setSQLParameterList(initialSQLParameters);
				whereComposite.setActive(true);
			}

			initialSearchParametersSet = true;
		}
	}



	@Override
	public void dataChange(ModelEvent event) {
		if (event.getSource() == invoiceSearchModel) {
			List<InvoiceCVO> invoiceList = null;
			try {
				invoiceList = invoiceSearchModel.getModelData();
			}
			catch (Exception e) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			}

			if (invoiceList == null) {
				invoiceList = Collections.emptyList();
			}

			final List<InvoiceCVO> finalInvoiceList = invoiceList;

			SWTHelper.asyncExecDisplayThread(new Runnable() {
				@Override
				public void run() {
					try {
						tableViewer.setInput(finalInvoiceList);

						int size = finalInvoiceList.size();

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


	@Override
	public void dataChange(CacheModelEvent<Long> event) {
		// auf Änderungen an Teilnehmern reagieren
		try {
			if (event.getSource() == invoiceSQLFieldsModel) {
				// die Daten im InvoiceSQLFieldsModel haben sich geändert

				invoiceSearch = invoiceSQLFieldsModel.getInvoiceSearch(
					eventPK != null ? eventPK : InvoiceSQLFieldsModel.NO_EVENT_KEY
				);

				List<SQLField> sqlFields = null;
				if (invoiceSearch != null) {
					SearchFieldsCVO searchFieldsCVO = invoiceSearch.getSearchFieldsCVO();
					sqlFields = searchFieldsCVO.getWhereFields();

					/*
					 * EVENT entfernen, damit es nicht in der Liste der Suchkriterien erscheint.
					 */
					sqlFields.remove(InvoiceSearch.EVENT);
				}

				// Sets the possible Search-Fields.
				whereComposite.setSqlFields(sqlFields);
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

			if (resultCountLimitCheckbox.getSelection()) {
				invoiceSearchModel.setResultCountLimit(resultCountLimitNumberText.getValue());
			}
			else {
				invoiceSearchModel.setResultCountLimit(null);
			}

			// add SQLParameter for the event
			if (eventPK != null) {
				SQLParameter eventSqlParameter = InvoiceSearch.EVENT.getSQLParameter(
					eventPK,
					SQLOperator.EQUAL
				);
				sqlParameters.add(eventSqlParameter);
			}

			// give the SearchInterceptor a chance to modify the SQL-Parameters
			if (searchInterceptor != null) {
				searchInterceptor.changeSearchParameter(sqlParameters);
			}

			invoiceSearchModel.setSqlParameterList(sqlParameters);
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


	public InvoiceSearch getInvoiceSearch() {
		return invoiceSearch;
	}


	public void setSQLParameters(List<SQLParameter> sqlParameters) {
		whereComposite.setSQLParameterList(sqlParameters);
	}


	public void setWhereFields(List<WhereField> whereFields) {
		List<SQLParameter> sqlParameterList = null;

		// invoiceSearch is null if the event has not been set yet
		if (whereFields != null && invoiceSearch != null) {
			sqlParameterList = new ArrayList<>(whereFields.size());
			for (WhereField whereField : whereFields) {
				if (whereField.getKey() != null) {
					SQLField sqlField = invoiceSearch.getSQLField(whereField.getKey());

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
	 * Make that Ctl+C copies table contents to clipboard
	 */
	public void registerCopyAction(IActionBars actionBars) {
		invoiceSearchTable.registerCopyAction(actionBars);
	}


	public void setInitialSQLParameters(List<SQLParameter> initialSQLParameters) {
		this.initialSQLParameters = initialSQLParameters;
	}


	public void setSearchInterceptor(SearchInterceptor searchInterceptor) {
		this.searchInterceptor = searchInterceptor;
	}


	public List<InvoiceVO> getSelectedInvoiceVOs() {
		List<InvoiceCVO> invoiceCVOs = SelectionHelper.toList(tableViewer.getSelection());
		List<InvoiceVO> invoiceVOs = AbstractCVO.getVOs(invoiceCVOs);
		return invoiceVOs;
	}


	public void addSelectionListener(ISelectionChangedListener selectionListener) {
		tableViewer.addSelectionChangedListener(selectionListener);

	}


	public void removeSelectionListener(ISelectionChangedListener selectionListener) {
		tableViewer.removeSelectionChangedListener(selectionListener);
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
}
