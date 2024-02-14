package de.regasus.profile.search;

import static com.lambdalogic.util.CollectionsHelper.notEmpty;
import static com.lambdalogic.util.StringHelper.isNotEmpty;

import java.util.List;

import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchWindow;

import com.lambdalogic.messeinfo.kernel.sql.SQLParameter;
import com.lambdalogic.messeinfo.participant.report.parameter.WhereField;
import com.lambdalogic.messeinfo.profile.Profile;
import com.lambdalogic.messeinfo.report.WhereClauseReportParameter;
import com.lambdalogic.util.model.ModelEvent;
import com.lambdalogic.util.model.ModelListener;
import com.lambdalogic.util.rcp.SelectionMode;
import com.lambdalogic.util.rcp.widget.SWTHelper;
import com.lambdalogic.xml.XMLContainer;

import de.regasus.core.ServerModel;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.model.ServerModelEvent;
import de.regasus.core.model.ServerModelEventType;
import de.regasus.core.ui.view.AbstractView;
import de.regasus.profile.command.EditProfileCommandHandler;
import de.regasus.profile.search.pref.ProfileSearchViewPreference;
import de.regasus.ui.Activator;


public class ProfileSearchView extends AbstractView {
	public static final String ID = "ProfileSearchView";

	private ProfileSearchViewPreference preference;

	// Widgets
	private ProfileSearchComposite searchComposite;


	/**
	 * The last value of visible as get from the ConfigParameterSet in isVisible().
	 * Has to be stored because the result of isVisible() should not change in the case that the
	 * getConfigParameterSet() returns null.
	 */
	private boolean visible = false;


	public ProfileSearchView() {
		preference = ProfileSearchViewPreference.getInstance();
	}


	@Override
	protected boolean isVisible() {
		/* Determine the visibility from the ConfigParameterSet.
		 * If getConfigParameterSet() returns null, its last result (the last value of visible)
		 * is returned.
		 */
		if (getConfigParameterSet() != null) {
			visible = 	getConfigParameterSet().getProfile().isVisible();
		}
		return visible;
	}


	@Override
	public void createWidgets(Composite parent) throws Exception {
		if (isVisible()) {
			Composite container = new Composite(parent, SWT.NONE);
			GridLayout viewLayout = new GridLayout();
			container.setLayout(viewLayout);
			searchComposite = new ProfileSearchComposite(
				container,
				SelectionMode.MULTI_SELECTION,
				SWT.NONE,
				false // useDetachedSearchModelInstance
			);
			searchComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

			// make the Table the SelectionProvider
			getSite().setSelectionProvider(searchComposite.getTableViewer());

			// make that Ctrl+C copies table contents to clipboard
			searchComposite.registerCopyAction(getViewSite().getActionBars());

			setContributionItemsVisible(true);

			initializeContextMenu();
			initializeDoubleClickAction();

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


	private void initializeContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			@Override
			public void menuAboutToShow(IMenuManager manager) {
				ProfileSearchView.this.fillContextMenu(manager);
			}
		});
		TableViewer viewer = searchComposite.getTableViewer();
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, viewer);
	}


	private void initializeDoubleClickAction() {
		final TableViewer viewer = searchComposite.getTableViewer();
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				TableItem[] selectedItems = viewer.getTable().getSelection();
				IWorkbenchWindow window = getSite().getWorkbenchWindow();
				for (TableItem tableItem : selectedItems) {
					Profile profile = (Profile) tableItem.getData();
					Long profileID = profile.getID();
					EditProfileCommandHandler.openProfileEditor(window.getActivePage(), profileID);
				}
			}
		});
	}


	private void fillContextMenu(IMenuManager menuManager) {
		// update the menu, necessary when it changes after after its first initialization
		menuManager.update(true);

		// Other plug-ins can contribute there actions here
		menuManager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
		menuManager.add(new Separator("emailAdditions"));
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


	public void doSearch() {
		searchComposite.doSearch();
	}

	public void doQuickSearch(String text) throws Exception {
		if (searchComposite != null) {
			searchComposite.doQuickSearch(text);
		}
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
