package de.regasus.profile.search;

import static com.lambdalogic.util.CollectionsHelper.*;
import static com.lambdalogic.util.rcp.KeyEventHelper.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;

import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
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
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import com.lambdalogic.messeinfo.contact.Address;
import com.lambdalogic.messeinfo.contact.Person;
import com.lambdalogic.messeinfo.contact.sql.PersonSearch;
import com.lambdalogic.messeinfo.exception.InvalidValuesException;
import com.lambdalogic.messeinfo.kernel.data.SearchFieldsCVO;
import com.lambdalogic.messeinfo.kernel.interfaces.SQLField;
import com.lambdalogic.messeinfo.kernel.interfaces.SQLOperator;
import com.lambdalogic.messeinfo.kernel.sql.SQLParameter;
import com.lambdalogic.messeinfo.participant.report.parameter.WhereField;
import com.lambdalogic.messeinfo.profile.Profile;
import com.lambdalogic.messeinfo.profile.sql.ProfileSearch;
import com.lambdalogic.util.CollectionsHelper;
import com.lambdalogic.util.EqualsHelper;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.model.CacheModelOperation;
import com.lambdalogic.util.model.ModelEvent;
import com.lambdalogic.util.model.ModelListener;
import com.lambdalogic.util.rcp.ClipboardHelper;
import com.lambdalogic.util.rcp.SelectionHelper;
import com.lambdalogic.util.rcp.SelectionMode;
import com.lambdalogic.util.rcp.widget.NumberText;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.I18N;
import de.regasus.core.ServerModel;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.search.ISearcher;
import de.regasus.core.ui.search.SearchInterceptor;
import de.regasus.core.ui.search.WhereComposite;
import de.regasus.profile.ProfileModel;
import de.regasus.profile.ProfileSQLFieldsModel;
import de.regasus.profile.ProfileSearchModel;
import de.regasus.profile.editor.ProfileEditor;
import de.regasus.profile.editor.ProfileEditorInput;
import de.regasus.ui.Activator;

public class ProfileSearchComposite extends Composite implements DisposeListener, ISearcher {

	public static final int DEFAULT_RESULT_COUNT = 500;
	public static final boolean DEFAULT_RESULT_COUNT_SELECTION = true;

	// Models
	private ProfileSearchModel searchModel;
	private ProfileSQLFieldsModel profileSQLFieldsModel = ProfileSQLFieldsModel.getInstance();
	private ProfileModel profileModel = ProfileModel.getInstance();


	private ProfileSearch profileSearch;
	private SearchInterceptor searchInterceptor;

	// Widgets
	private WhereComposite whereComposite;
	private TableViewer tableViewer;
	private ProfileSearchTable profileSearchTable;
	private Label resultCount;
	private Button searchButton;
	private Button resultCountLimitCheckbox;
	private NumberText resultCountLimitNumberText;

	/**
	 * Stores parameters to be used when this search composite is supposed to show initial
	 * search criteria.
	 */
	private List<SQLParameter> initialSQLParameters;

	/**
	 * If present, is used for initial search with this last name
	 */
	private String initialLastName;

	/**
	 * If present, is used for initial search with this first name
	 */
	private String initialFirstName;

	protected List<Profile> selectedProfiles = new ArrayList<>();


	/**
	 * Create the composite
	 *
	 * @param parent
	 * @param style
	 */
	public ProfileSearchComposite(
		Composite parent,
		SelectionMode selectionMode,
		int style,
		boolean useDetachedSearchModelInstance
	) {
		super(parent, style);

		Objects.requireNonNull(selectionMode);

		addDisposeListener(this);


		// init ProfileSearchModel
		if (useDetachedSearchModelInstance) {
			searchModel = ProfileSearchModel.getDetachedInstance();
		}
		else {
			searchModel = ProfileSearchModel.getInstance();
		}

		setLayout(new FillLayout());

		SashForm sashForm = new SashForm(this, SWT.VERTICAL);

		ScrolledComposite scrolledComposite = new ScrolledComposite(sashForm, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		scrolledComposite.setExpandHorizontal(true);
		scrolledComposite.setExpandVertical(true);
		scrolledComposite.setMinWidth(400);
		scrolledComposite.setMinHeight(200);

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
		searchButton.setText(I18N.ProfileSearchComposite_SearchButtonText);
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

		Label limitCountLabel = new Label(searchButtonComposite, SWT.NONE);
		limitCountLabel.setText(I18N.ParticipantSearchComposite_ResultCountLimit);
		limitCountLabel.setToolTipText(I18N.ParticipantSearchComposite_ResultCountLimit_description);

		resultCountLimitNumberText = new NumberText(searchButtonComposite, SWT.BORDER);
		resultCountLimitNumberText.setValue(DEFAULT_RESULT_COUNT);
		{
			GridData gd = new GridData(SWT.FILL, SWT.CENTER, false, false);
			gd.widthHint = SWTHelper.computeTextWidgetWidthForCharCount(resultCountLimitNumberText, 6);
			resultCountLimitNumberText.setLayoutData(gd);
		}


		profileSearchTable = createTable(searchComposite, selectionMode.getSwtStyle());
		tableViewer = profileSearchTable.getViewer();
		tableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				selectedProfiles = SelectionHelper.getSelection(tableViewer, Profile.class);
			}
		});


		Composite searchResultComposite = new Composite(searchComposite, SWT.NONE);
		searchResultComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		searchResultComposite.setLayout(new GridLayout(2, false));

		Label foundProfilesLabel = new Label(searchResultComposite, SWT.NONE);
		foundProfilesLabel.setText(I18N.ProfileSearchComposite_FoundProfilesLabel);
		foundProfilesLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));

		resultCount = new Label(searchResultComposite, SWT.NONE);
		resultCount.setText("                                                        ");
		resultCount.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		sashForm.setWeights(new int[] { 1, 1 });

		// observe Models
		profileModel.addListener(profileModelListener);
		searchModel.addListener(searchModelListener);
		profileSQLFieldsModel.addListener(sqlFieldsModelListener);

		// init SQLFields
		refreshSqlFields();

		initSqlParametersOfWhereComposite();
	}


	private ProfileSearchTable createTable(Composite parent, int style) {
		Composite searchTableComposite = new Composite(parent, SWT.BORDER);
		searchTableComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		TableColumnLayout layout = new TableColumnLayout();
		searchTableComposite.setLayout(layout);

		// create SWT Table
		Table table = new Table(searchTableComposite, style);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		table.addKeyListener(tableKeyListener);

		// create TableColumns (and set their width and header text)

		TableColumn firstNameTableColumn = new TableColumn(table, SWT.NONE);
		layout.setColumnData(firstNameTableColumn, new ColumnWeightData(140));
		firstNameTableColumn.setText(Person.FIRST_NAME.getString());

		TableColumn lastNameTableColumn = new TableColumn(table, SWT.NONE);
		layout.setColumnData(lastNameTableColumn, new ColumnWeightData(140));
		lastNameTableColumn.setText(Person.LAST_NAME.getString());

		TableColumn organisationTableColumn = new TableColumn(table, SWT.NONE);
		layout.setColumnData(organisationTableColumn, new ColumnWeightData(100));
		organisationTableColumn.setText( Address.ORGANISATION.getString() );

		TableColumn cityTableColumn = new TableColumn(table, SWT.NONE);
		layout.setColumnData(cityTableColumn, new ColumnWeightData(100));
		cityTableColumn.setText( Address.CITY.getString() );

		// make all columns moveable
		for (TableColumn tableColumn : table.getColumns()) {
			tableColumn.setMoveable(true);
		}

		// create ProfileSearchTable
		return new ProfileSearchTable(table);
	}


	private KeyListener tableKeyListener = new KeyAdapter() {
		@Override
		public void keyPressed(org.eclipse.swt.events.KeyEvent event) {
			try {
    			if ( isInsert(event) ) {
   					createProfile();
    			}
    			else if (
    				   isCopy(event)
    				|| isCopyPK(event)
    				|| isCopyVigenere1(event)
    				|| isCopyVigenere1Hex(event)
    				|| isCopyVigenere2(event)
    				|| isCopyVigenere2Hex(event)
    			) {
    				List<Profile> profileList = SelectionHelper.getSelection(tableViewer, Profile.class);
    				StringBuilder text = new StringBuilder(profileList.size() * 34);
    				for (Profile profile : profileList) {
    					// determine clipboard content depending on keys

    					if ( isCopy(event) ) {
    						if (text.length() > 0) text.append("\n");
    						text.append( profile.getCopyInfo() );
    					}
    					else if ( isCopyPK(event) ) {
    						if (text.length() > 0) text.append(", ");
    						text.append( profile.getID() );
    					}
    					else if ( isCopyVigenere1(event) ) {
    						if (text.length() > 0) text.append(", ");
    						text.append( profile.getVigenereCode() );
    					}
    					else if ( isCopyVigenere1Hex(event) ) {
    						if (text.length() > 0) text.append(", ");
    						text.append( profile.getVigenereCodeHex() );
    					}
    					else if ( isCopyVigenere2(event) ) {
    						if (text.length() > 0) text.append(", ");
    						text.append( profile.getVigenere2Code() );
    					}
    					else if ( isCopyVigenere2Hex(event) ) {
    						if (text.length() > 0) text.append(", ");
    						text.append( profile.getVigenere2CodeHex() );
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
		if (profileModel != null) {
			profileModel.removeListener(profileModelListener);
		}

		if (profileSQLFieldsModel != null) {
			profileSQLFieldsModel.removeListener(sqlFieldsModelListener);
		}

		if (searchModel != null) {
			searchModel.removeListener(searchModelListener);
		}
	}


	/**
	 * Refresh SQLFields of WhereComposite.
	 */
	private void refreshSqlFields() {
		try {
			// init SQLFields
			final List<SQLField> sqlFieldList = new ArrayList<>();
			// When this method is called during logout, SearchFieldsCVO cannot get data from the server!
			if (ServerModel.getInstance().isLoggedIn()) {
				profileSearch = profileSQLFieldsModel.getProfileSearch();

				if (profileSearch != null) {
					SearchFieldsCVO searchFieldsCVO = profileSearch.getSearchFieldsCVO();
					sqlFieldList.addAll(searchFieldsCVO.getWhereFields());
				}
			}


			SWTHelper.syncExecDisplayThread(new Runnable() {
				@Override
				public void run() {
					// set fresh SQLFields
					whereComposite.setSqlFields(sqlFieldList);
				}
			});

		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	private void initSqlParametersOfWhereComposite() {
		try {
			// init SQLParameters
			final List<SQLParameter> sqlParameterList = new ArrayList<>();

			if (notEmpty(initialSQLParameters)) {
				sqlParameterList.addAll(initialSQLParameters);
			}

			if (initialLastName != null) {
				sqlParameterList.add(new SQLParameter(
					PersonSearch.LAST_NAME,
					initialLastName,
					SQLOperator.FUZZY_LOWER_ASCII,
					true // active
				));
			}

			if (initialFirstName != null) {
				sqlParameterList.add(new SQLParameter(
					PersonSearch.FIRST_NAME,
					initialFirstName,
					SQLOperator.FUZZY_LOWER_ASCII,
					true // active
				));
			}

			if (empty(sqlParameterList)) {
				// add default SQLParameters
				sqlParameterList.add(new SQLParameter(
					PersonSearch.LAST_NAME,
					"", // value
					SQLOperator.FUZZY_LOWER_ASCII,
					false // active
				));

				sqlParameterList.add(new SQLParameter(
					PersonSearch.FIRST_NAME,
					"", // value
					SQLOperator.FUZZY_LOWER_ASCII,
					false // active
				));
			}


			SWTHelper.syncExecDisplayThread(new Runnable() {
				@Override
				public void run() {
					// set fresh SQLParameters
					if (notEmpty(sqlParameterList)) {
						whereComposite.setSQLParameterList(sqlParameterList);
					}
				}
			});

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
				searchModel.setResultCountLimit(resultCountLimitNumberText.getValue());
			}
			else {
				searchModel.setResultCountLimit(null);
			}

			// give the SearchInterceptor a chance to modify the SQL-Parameters
			if (searchInterceptor != null) {
				searchInterceptor.changeSearchParameter(sqlParameters);
			}

			searchModel.setSqlParameterList(sqlParameters);
		}
		catch (Throwable t) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t);
		}
	}


	public void doQuickSearch(String quickSearchInput) {
		try {
			// Search with given input
			searchModel.setQuickSearchInput(quickSearchInput);
			List<Profile> profileList = searchModel.getModelData();

			// If one profile found, open its editor
			if (profileList.size() == 1) {
				Long profileID = profileList.get(0).getID();
				ProfileEditorInput editorInput = new ProfileEditorInput(profileID);

				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().openEditor(
					editorInput,
					ProfileEditor.ID
				);
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleApplicationError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	private ModelListener searchModelListener = new ModelListener() {
		@Override
		public void dataChange(ModelEvent event) {
			List<Profile> profileList = null;
			try {
				profileList = searchModel.getModelData();
			}
			catch (Exception e) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			}

			if (profileList == null) {
				profileList = Collections.emptyList();
			}

			final List<Profile> finalProfileList = profileList;

			SWTHelper.asyncExecDisplayThread(new Runnable() {
				@Override
				public void run() {
					try {
						tableViewer.setInput(finalProfileList);

						int size = finalProfileList.size();

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
	};


	private CacheModelListener<Long> sqlFieldsModelListener = new CacheModelListener<Long>() {
		@Override
		public void dataChange(CacheModelEvent<Long> event) {
			try {
				refreshSqlFields();
			}
			catch (Throwable e) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			}
		}
	};


	private CacheModelListener<Long> profileModelListener = new CacheModelListener<Long>() {
		@Override
		public void dataChange(CacheModelEvent<Long> event) {
			// react to changes on Profiles
			try {
				// a tag that signals that the table has to be refreshed
				boolean refresh = false;

				if (event.getOperation() == CacheModelOperation.UPDATE ||
					event.getOperation() == CacheModelOperation.REFRESH
				) {
					// get and update changed entities from the model
					List<Profile> currentEntities = (List<Profile>) tableViewer.getInput();

					// PKs of changed entities
					List<Long> changedIDs = event.getKeyList();

					if (currentEntities != null && changedIDs != null) {
						// for each changed entity in the table
						for (Long changedID : changedIDs) {
							for (ListIterator<Profile> it = currentEntities.listIterator(); it.hasNext();) {
								Profile entity = it.next();
								if (changedID.equals(entity.getID())) {
									// replace out.dated entity with fresh one
									Profile freshEntity = ProfileModel.getInstance().getExtendedProfile(changedID);
									it.set(freshEntity);
									refresh = true;
									break;
								}
							}
						}
					}
				}
				else if (event.getOperation() == CacheModelOperation.DELETE) {
					// remove deleted entities
					remove(event.getKeyList());
				}

				// if data has changed, update table and number of rows
				if (refresh) {
					refreshSearchResult();
				}
			}
			catch (Throwable e) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			}
		}
	};


	/**
	 * Remove a Profiles from the search result table.
	 * @param profilePKs
	 */
	public void remove(Collection<Long> profilePKs) {
		List<Profile> entityList = (List) tableViewer.getInput();

		if (entityList != null && profilePKs != null) {
			boolean refresh = false;

			for (Long pk : profilePKs) {
				for (Iterator<Profile> it = entityList.iterator(); it.hasNext();) {
					Profile entity = it.next();
					if (pk.equals(entity.getPK())) {
						it.remove();
						refresh = true;
						break;
					}
				}
			}

			// if data has changed, update table and number of rows
			if (refresh) {
				refreshSearchResult();
			}
		}
	}


	/**
	 * Refresh search result table and number of rows.
	 */
	private void refreshSearchResult() {
		SWTHelper.syncExecDisplayThread(new Runnable() {
			@Override
			public void run() {
				tableViewer.refresh();

				List<?> list = (List<?>) tableViewer.getInput();
				resultCount.setText(String.valueOf(list.size()));
			}
		});
	}


	public void setSelection(Collection<Long> profilePKs) {
		List<Profile> currentEntities = (List) tableViewer.getInput();

		List<Profile> selectedEntities = new ArrayList<>();

		if (notEmpty(profilePKs)) {
			if (currentEntities != null) {
				for (Profile entity : currentEntities) {
					if (profilePKs.contains(entity.getPK())) {
						selectedEntities.add(entity);
					}
				}
			}
		}

		// create StructuredSelection not before selectedEntities is completely filled
		ISelection selection = new StructuredSelection(selectedEntities);

		tableViewer.setSelection(selection, true /*reveal*/);
	}


	public void setSelection(Long participantPK) {
		setSelection(Collections.singletonList(participantPK));
	}


	public TableViewer getTableViewer() {
		return tableViewer;
	}


	public List<SQLParameter> getSQLParameters() {
		return whereComposite.getSQLParameterList();
	}


	public List<SQLParameter> getSQLParameterListForPreferences() {
		return whereComposite.getSQLParameterListForPreferences();
	}


	public String getDescription() {
		return whereComposite.getDescription();
	}


	public ProfileSearch getProfileSearch() {
		return profileSearch;
	}


	public void setSQLParameters(List<SQLParameter> sqlParameters) {
		whereComposite.setSQLParameterList(sqlParameters);
	}


	public void setWhereFields(List<WhereField> whereFields) {
		List<SQLParameter> sqlParameterList = null;

		if (whereFields != null && profileSearch != null) {
			sqlParameterList = new ArrayList<>(whereFields.size());
			for (WhereField whereField : whereFields) {
				if (whereField.getKey() != null) {
					SQLField sqlField = profileSearch.getSQLField(whereField.getKey());

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
							 * Therefore we create an SQLParameter with a value of type String.
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
		profileSearchTable.registerCopyAction(actionBars);
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


	public void setSearchInterceptor(SearchInterceptor searchInterceptor) {
		this.searchInterceptor = searchInterceptor;
	}


	protected void createProfile() {
		try {
			ProfileEditorInput editorInput = new ProfileEditorInput();
			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().openEditor(
				editorInput,
				ProfileEditor.ID
			);
		}
		catch (PartInitException e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	public void setInitialSQLParameters(List<SQLParameter> initialSQLParameters) {
		if (!CollectionsHelper.isEqual(this.initialSQLParameters, initialSQLParameters)) {
			this.initialSQLParameters = initialSQLParameters;
			initSqlParametersOfWhereComposite();
		}
	}


	public void setInitialLastName(String initialLastName) {
		if (!EqualsHelper.isEqual(this.initialLastName, initialLastName)) {
			this.initialLastName = initialLastName;
			initSqlParametersOfWhereComposite();
		}
	}


	public void setInitialFirstName(String initialFirstName) {
		if (!EqualsHelper.isEqual(this.initialFirstName, initialFirstName)) {
			this.initialFirstName = initialFirstName;
			initSqlParametersOfWhereComposite();
		}
	}


	public List<Profile> getSelectedProfiles() {
		return Collections.unmodifiableList(selectedProfiles);
	}


	public List<Long> getSelectedPKs() {
		return Profile.getPrimaryKeyList(selectedProfiles);
	}


	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

}
