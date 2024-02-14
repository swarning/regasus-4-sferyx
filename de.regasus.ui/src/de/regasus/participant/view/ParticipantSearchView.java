package de.regasus.participant.view;

import static com.lambdalogic.util.CollectionsHelper.notEmpty;
import static com.lambdalogic.util.StringHelper.isNotEmpty;

import java.util.List;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchWindow;

import com.lambdalogic.messeinfo.kernel.sql.SQLParameter;
import com.lambdalogic.messeinfo.participant.Participant;
import com.lambdalogic.messeinfo.participant.data.EventVO;
import com.lambdalogic.messeinfo.participant.data.ParticipantSearchData;
import com.lambdalogic.messeinfo.participant.report.parameter.WhereField;
import com.lambdalogic.messeinfo.report.WhereClauseReportParameter;
import com.lambdalogic.util.model.ModelEvent;
import com.lambdalogic.util.model.ModelListener;
import com.lambdalogic.util.rcp.SelectionHelper;
import com.lambdalogic.util.rcp.SelectionMode;
import com.lambdalogic.util.rcp.widget.SWTHelper;
import com.lambdalogic.xml.XMLContainer;

import de.regasus.core.ServerModel;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.model.ServerModelEvent;
import de.regasus.core.model.ServerModelEventType;
import de.regasus.core.ui.view.AbstractView;
import de.regasus.event.EventIdProvider;
import de.regasus.event.EventModel;
import de.regasus.event.combo.EventCombo;
import de.regasus.participant.command.EditParticipantCommandHandler;
import de.regasus.participant.search.ParticipantSearchComposite;
import de.regasus.participant.view.pref.ParticipantSearchViewPreference;
import de.regasus.ui.Activator;


public class ParticipantSearchView extends AbstractView implements EventIdProvider {
	public static final String ID = "ParticipantSearchView";

	private ParticipantSearchViewPreference preference;

	// Widgets
	private EventCombo eventCombo;

	private ParticipantSearchComposite searchComposite;


	/**
	 * The last value of visible as get from the ConfigParameterSet in isVisible().
	 * Has to be stored because the result of isVisible() should not change in the case that the
	 * getConfigParameterSet() returns null.
	 */
	private boolean visible = false;


	public ParticipantSearchView() {
		preference = ParticipantSearchViewPreference.getInstance();
	}


	@Override
	protected boolean isVisible() {
		/* Determine the visibility from the ConfigParameterSet.
		 * If getConfigParameterSet() returns null, its last result (the last value of visible)
		 * is returned.
		 */
		if (getConfigParameterSet() != null) {
			visible = getConfigParameterSet().getEvent().isVisible();
		}
		return visible;
	}


	@Override
	public Long getEventId() {
		Long eventPK = null;
		if (eventCombo != null) {
			eventPK = eventCombo.getEventPK();
		}
		return eventPK;
	}


	@Override
	public void createWidgets(Composite parent) throws Exception {
		if ( isVisible() ) {
			final int NUM_COLUMNS = 2;
			parent.setLayout(new GridLayout(NUM_COLUMNS, false));

			GridDataFactory labelGridDataFactory = GridDataFactory
				.swtDefaults()
				.align(SWT.RIGHT, SWT.CENTER);
			GridDataFactory widgetGridDataFactory = GridDataFactory
    			.swtDefaults()
    			.align(SWT.FILL, SWT.CENTER)
    			.grab(true, false);

			// row 1
			{
				Label eventLabel = new Label(parent, SWT.NONE);
				labelGridDataFactory.applyTo(eventLabel);
				eventLabel.setText( Participant.EVENT.getString() );

				eventCombo = new EventCombo(parent, SWT.READ_ONLY);
				eventCombo.setKeepEntityInList(false);

				widgetGridDataFactory.applyTo(eventCombo);

				eventCombo.addModifyListener(new ModifyListener() {
					@Override
					public void modifyText(ModifyEvent e) {
						Long eventPK = eventCombo.getEventPK();
						searchComposite.setEventPK(eventPK);
					}
				});
			}

			// row 2
			{
    			searchComposite = new ParticipantSearchComposite(
    				parent,
    				SelectionMode.MULTI_SELECTION,
    				SWT.NONE,
    				false,	// useDetachedSearchModelInstance
    				null	// eventPK
    			);

    			GridDataFactory
        			.fillDefaults()
        			.span(NUM_COLUMNS, 1)
        			.grab(true,  true)
        			.applyTo(searchComposite);

    			// make the Table the SelectionProvider
    			getSite().setSelectionProvider(searchComposite.getTableViewer());
    			// make that Ctrl+C copies table contents to clipboard
    			searchComposite.registerCopyAction(getViewSite().getActionBars());
			}


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

		menuMgr.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
		menuMgr.add(new Separator("emailAdditions"));

		TableViewer tableViewer = searchComposite.getTableViewer();
		Table table = tableViewer.getTable();
		Menu menu = menuMgr.createContextMenu(table);
		table.setMenu(menu);
		getSite().registerContextMenu(menuMgr, tableViewer);
	}


	private void initializeDoubleClickAction() {
		final TableViewer tableViewer = searchComposite.getTableViewer();
		tableViewer.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				List<ParticipantSearchData> selectedParticipants = SelectionHelper.getSelection(tableViewer, ParticipantSearchData.class);
				for (ParticipantSearchData participantSearchData : selectedParticipants) {
					Long participantID = participantSearchData.getPK();
					IWorkbenchWindow window = getSite().getWorkbenchWindow();
					EditParticipantCommandHandler.openParticipantEditor(window.getActivePage(), participantID);
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


	public void doSearch() {
		searchComposite.doSearch();

	}


	public void doQuickSearch(String text) {
		searchComposite.doQuickSearch(text);
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
    		EventVO eventVO = null;
    		try {
    			eventVO = EventModel.getInstance().getEventVO(eventPK);
    		}
    		catch (Exception e) {
    			// don't show error dialog, just log the error
    			System.err.println(e);
    		}

    		if (eventVO != null) {
    			eventCombo.setEventPK(eventPK);
    		}

    		// set eventPK in SearchComposite, even if it is null
    		searchComposite.setEventPK(eventPK);


    		// eventFilter
    		eventCombo.setFilter( preference.getEventFilter() );


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
