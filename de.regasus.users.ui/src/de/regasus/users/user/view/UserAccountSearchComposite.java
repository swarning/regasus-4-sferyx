package de.regasus.users.user.view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
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

import com.lambdalogic.messeinfo.account.AccountLabel;
import com.lambdalogic.messeinfo.account.data.UserAccountVO;
import com.lambdalogic.messeinfo.account.sql.UserAccountSearch;
import com.lambdalogic.messeinfo.contact.ContactLabel;
import com.lambdalogic.messeinfo.contact.Person;
import com.lambdalogic.messeinfo.kernel.data.SearchFieldsCVO;
import com.lambdalogic.messeinfo.kernel.interfaces.SQLField;
import com.lambdalogic.messeinfo.kernel.interfaces.SQLOperator;
import com.lambdalogic.messeinfo.kernel.sql.SQLParameter;
import com.lambdalogic.messeinfo.participant.report.parameter.WhereField;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.model.CacheModelOperation;
import com.lambdalogic.util.model.ModelEvent;
import com.lambdalogic.util.model.ModelListener;
import com.lambdalogic.util.rcp.BusyCursorHelper;
import com.lambdalogic.util.rcp.SelectionHelper;
import com.lambdalogic.util.rcp.UtilI18N;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.search.ISearcher;
import de.regasus.core.ui.search.SearchInterceptor;
import de.regasus.core.ui.search.WhereComposite;
import de.regasus.users.ClientUserAccountSearch;
import de.regasus.users.UserAccountModel;
import de.regasus.users.UserAccountSQLFieldsModel;
import de.regasus.users.UserAccountSearchModel;
import de.regasus.users.UsersI18N;
import de.regasus.users.ui.Activator;

public class UserAccountSearchComposite
extends Composite
implements DisposeListener, ISearcher {

	// Models
	private UserAccountSearchModel userAccountSearchModel = UserAccountSearchModel.getInstance();

	private UserAccountModel userAccountModel = UserAccountModel.getInstance();

	private UserAccountSQLFieldsModel userAccountSQLFieldsModel = UserAccountSQLFieldsModel.getInstance();

	// Widgets
	private WhereComposite whereComposite;

	private TableViewer tableViewer;

	private Label resultCount;

	private Button searchButton;

	private UserAccountSearchTable userAccountSearchTable;


	private SearchInterceptor searchInterceptor;


	private ClientUserAccountSearch userAccountSearch;

	private boolean firstInitialization;



	// *************************************************************************
	// * Constructors
	// *

	public UserAccountSearchComposite(Composite parent, int style) {
		this(parent, style, false);
	}


	public UserAccountSearchComposite(Composite parent, int style, boolean useDetachedSearchModel) {
		super(parent, style);

		if (useDetachedSearchModel) {
			userAccountSearchModel = UserAccountSearchModel.getDetachedInstance();
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
		searchButtonComposite.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false));
		searchButtonComposite.setLayout(new GridLayout());

		searchButton = new Button(searchButtonComposite, SWT.NONE);
		searchButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				doSearch();
			}
		});
		searchButton.setText(UtilI18N.Search);

		final Composite searchTableComposite = new Composite(searchComposite, SWT.BORDER);
		searchTableComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		TableColumnLayout layout = new TableColumnLayout();
		searchTableComposite.setLayout(layout);

		// **************************************************************************
		// * SearchTable
		// *

		final Table table = new Table(searchTableComposite, SWT.MULTI | SWT.FULL_SELECTION);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		final TableColumn userIDTableColumn = new TableColumn(table, SWT.RIGHT);
		layout.setColumnData(userIDTableColumn, new ColumnWeightData(80));
		userIDTableColumn.setText( AccountLabel.UserID.getString() );


		final TableColumn firstNameTableColumn = new TableColumn(table, SWT.NONE);
		layout.setColumnData(firstNameTableColumn, new ColumnWeightData(80));
		firstNameTableColumn.setText( Person.FIRST_NAME.getString() );

		final TableColumn lastNameTableColumn = new TableColumn(table, SWT.NONE);
		layout.setColumnData(lastNameTableColumn, new ColumnWeightData(80));
		lastNameTableColumn.setText( Person.LAST_NAME.getString() );

		final TableColumn emailTableColumn = new TableColumn(table, SWT.NONE);
		layout.setColumnData(emailTableColumn, new ColumnWeightData(80));
		emailTableColumn.setText( ContactLabel.email.getString() );


		userAccountSearchTable = new UserAccountSearchTable(table);

		final Composite searchResultComposite = new Composite(searchComposite, SWT.NONE);

		searchResultComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		searchResultComposite.setLayout(new GridLayout(2, false));

		final Label foundUserAccountsLabel = new Label(searchResultComposite, SWT.NONE);
		foundUserAccountsLabel.setText(UsersI18N.FoundUserAccountsLabel);
		foundUserAccountsLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));

		resultCount = new Label(searchResultComposite, SWT.NONE);
		resultCount.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		tableViewer = userAccountSearchTable.getViewer();

		// *
		// * SearchTable
		// **************************************************************************

		sashForm.setWeights(new int[] { 4, 7 });

		userAccountSearchModel.addListener(userAccountSearchModelListener);
		userAccountModel.addListener(userAccountModelListener);
		userAccountSQLFieldsModel.addListener(userAccountSQLFieldsModelListener);
		initSearchFields();
	}


	/**
	 * The UserAccountSQLFieldsModel might be null if this composite is part of a wizard page which has never been made
	 * visible because the wizard was cancelled.
	 */
	@Override
	public void widgetDisposed(DisposeEvent e) {
		if (userAccountSearchModel != null) {
			userAccountSearchModel.removeListener(userAccountSearchModelListener);
		}
		if (userAccountModel != null) {
			userAccountModel.removeListener(userAccountModelListener);
		}
		if (userAccountSQLFieldsModel != null) {
			userAccountSQLFieldsModel.removeListener(userAccountSQLFieldsModelListener);
		}
	}


	private ModelListener userAccountSearchModelListener = new ModelListener() {
		@Override
		public void dataChange(ModelEvent event) {
			List<UserAccountVO> userAccountVOs = null;
			try {
				userAccountVOs = userAccountSearchModel.getModelData();
			}
			catch (Exception e) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			}

			if (userAccountVOs == null) {
				userAccountVOs = Collections.emptyList();
			}

			final List<UserAccountVO> finalUserAccountList = userAccountVOs;

			SWTHelper.asyncExecDisplayThread(new Runnable() {
				@Override
				public void run() {
					try {
						tableViewer.setInput(finalUserAccountList);

						int size = finalUserAccountList.size();

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


	private CacheModelListener<Long> userAccountModelListener = new CacheModelListener<Long>() {
		@Override
		public void dataChange(CacheModelEvent<Long> event) {
			try {
				boolean refresh = false;

				if (event.getOperation() == CacheModelOperation.UPDATE ||
					event.getOperation() == CacheModelOperation.REFRESH
				) {
					// Daten der geänderten Teilnehmer vom Model holen und aktualisieren
					final List<UserAccountVO> userAccountVOList = (List<UserAccountVO>) tableViewer.getInput();
					final List<Long> editedUserAccountPKs = event.getKeyList();
					if (userAccountVOList != null && editedUserAccountPKs != null) {
						for (Long editedUserAccountPK : editedUserAccountPKs) {

							for(int i=0; i<userAccountVOList.size(); i++) {
								UserAccountVO userAccountVO = userAccountVOList.get(i);

								if (editedUserAccountPK.equals(userAccountVO.getPK())) {
									UserAccountVO editedUserAccount = userAccountModel.getUserAccountCVO(editedUserAccountPK).getVO();
									userAccountVOList.set(i, editedUserAccount);
									refresh = true;
									break;
								}
							}
						}
					}
				}
				else if (event.getOperation() == CacheModelOperation.DELETE) {
					// gelöschte Teilnehmer entfernen
					final List<UserAccountVO> userAccountVOList = (List<UserAccountVO>) tableViewer.getInput();
					final List<?> deletedParticipantKeys = event.getKeyList();
					if (userAccountVOList != null && deletedParticipantKeys != null) {
						for (Object deletedParticipantKey : deletedParticipantKeys) {
							for (Iterator<UserAccountVO> it = userAccountVOList.iterator(); it.hasNext();) {
								UserAccountVO participantSearchData = it.next();
								if (deletedParticipantKey.equals(participantSearchData.getPrimaryKey())) {
									it.remove();
									refresh = true;
									break;
								}
							}
						}
					}
					refresh = true;
				}

				// Wenn eine Aktualisierung stattgefunden hat: Tabelle und Anzahl der Zeilen aktualisieren
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
			catch (Exception e) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			}
		}
	};


	private ModelListener userAccountSQLFieldsModelListener = new ModelListener() {
		@Override
		public void dataChange(ModelEvent event) {
			try {
				// data in UserAccountSQLFieldsModel changed

				userAccountSearch = userAccountSQLFieldsModel.getModelData();

				if (userAccountSearch != null) {
					SearchFieldsCVO searchFieldsCVO = userAccountSearch.getSearchFieldsCVO();
					List<SQLField> sqlFields = searchFieldsCVO.getWhereFields();

					// Sets the possible Search-Fields.
					whereComposite.setSqlFields(sqlFields);
				}
			}
			catch (Exception e) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			}
		}
	};


	private void initSearchFields() {
		try {
			userAccountSearch = userAccountSQLFieldsModel.getModelData();

			final List<SQLField> sqlFields = new ArrayList<>();
			List<SQLParameter> sqlParameterList = null;

			if (userAccountSearch != null) {
				SearchFieldsCVO searchFieldsCVO = userAccountSearch.getSearchFieldsCVO();
				sqlFields.addAll(searchFieldsCVO.getWhereFields());

				if (!firstInitialization) {
					sqlParameterList = new ArrayList<>();
					sqlParameterList.add(UserAccountSearch.USER_ID.getSQLParameter("", SQLOperator.FUZZY_LOWER_ASCII));
					sqlParameterList.add(UserAccountSearch.LAST_NAME.getSQLParameter("", SQLOperator.FUZZY_LOWER_ASCII));
				}
			}

			final List<SQLParameter> finalSqlParameterList = sqlParameterList;
			SWTHelper.syncExecDisplayThread(new Runnable() {
				@Override
				public void run() {
					// Sets the possible Search-Fields.
					whereComposite.setSqlFields(sqlFields);

					if (!firstInitialization) {
						whereComposite.setSQLParameterList(finalSqlParameterList);
						firstInitialization = true;
					}

					whereComposite.setActive(false);
				}
			});

		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	@Override
	public void doSearch() {
		final List<SQLParameter> sqlParameters = whereComposite.getSQLParameterList();

		BusyCursorHelper.busyCursorWhile(new Runnable() {
			@Override
			public void run() {
				try {
					// give the SearchInterceptor a chance to modify the SQL-Parameters
					if (searchInterceptor != null) {
						searchInterceptor.changeSearchParameter(sqlParameters);
					}

					userAccountSearchModel.setSqlParameterList(sqlParameters);
				}
				catch (Throwable t) {
					RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t);
				}
			}
		});
	}


	public TableViewer getTableViewer() {
		return tableViewer;
	}


	public List<SQLParameter> getSQLParameter() {
		return whereComposite.getSQLParameterList();
	}


	public String getDescription() {
		return whereComposite.getDescription();
	}


	public UserAccountSearch getUserAccountSearch() {
		return userAccountSearch;
	}


	public void setSQLParameters(List<SQLParameter> sqlParameters) {
		whereComposite.setSQLParameterList(sqlParameters);
	}


	public void setWhereFields(List<WhereField> whereFields) {
		try {
			List<SQLParameter> sqlParameterList = null;

			if (whereFields != null) {
				sqlParameterList = new ArrayList<>(whereFields.size());
				for (WhereField whereField : whereFields) {
					if (whereField.getKey() != null) {
						SQLField sqlField = userAccountSearch.getSQLField(whereField.getKey());

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


	/**
	 * Make that Ctl+C copies table contents to clipboard
	 */
	public void registerCopyAction(IActionBars actionBars) {
		userAccountSearchTable.registerCopyAction(actionBars);
	}



	public void setSearchInterceptor(SearchInterceptor searchInterceptor) {
		this.searchInterceptor = searchInterceptor;
	}


	public List<UserAccountVO> getSelectedUserAccountVOs() {
		List<UserAccountVO> invoiceVOs = SelectionHelper.toList(tableViewer.getSelection());
		return invoiceVOs;
	}


	public void addSelectionListener(ISelectionChangedListener selectionListener) {
		tableViewer.addSelectionChangedListener(selectionListener);

	}


	public void removeSelectionListener(ISelectionChangedListener selectionListener) {
		tableViewer.removeSelectionChangedListener(selectionListener);
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

}
