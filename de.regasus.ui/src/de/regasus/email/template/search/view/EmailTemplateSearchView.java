package de.regasus.email.template.search.view;

import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.part.ViewPart;

import com.lambdalogic.messeinfo.contact.data.SimplePersonSearchData;
import com.lambdalogic.util.model.ModelEvent;
import com.lambdalogic.util.model.ModelListener;
import com.lambdalogic.util.rcp.CopyAction;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.core.ServerModel;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.model.ServerModelEvent;
import de.regasus.core.model.ServerModelEventType;
import de.regasus.email.template.SampleRecipientModel;
import de.regasus.email.template.search.view.pref.EmailTemplateSearchViewPreference;
import de.regasus.ui.Activator;


public class EmailTemplateSearchView extends ViewPart {

	public static final String ID = "EmailTemplateSearchView";

	private EmailTemplateSearchViewPreference preference;


	// Actions
	private CreateEmailTemplateAction createAction;
	private ActionFactory.IWorkbenchAction editAction;
	private DeleteEmailTemplateAction deleteAction;
	private ActionFactory.IWorkbenchAction refreshAction;
	private DuplicateEmailTemplateAction duplicateEmailTemplateAction;

	// Widgets
	protected EmailTemplateSearchComposite emailTemplateSearchComposite;


	public EmailTemplateSearchView() {
		preference = EmailTemplateSearchViewPreference.getInstance();

		preference.getPreferenceStore().addPropertyChangeListener( new IPropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent event) {
				initFromPreferences();
			}
		});
	}


	@Override
	public void createPartControl(Composite parent) {
		try {
			emailTemplateSearchComposite = new EmailTemplateSearchComposite(parent, SWT.NONE);

			createActions();

			initializeContextMenu();
			initializeDoubleClickAction();
			initializeToolBar();
			initializeMenu();

			TableViewer tableViewer = emailTemplateSearchComposite.getTableViewer();
			getSite().setSelectionProvider(tableViewer);

			getViewSite().getActionBars().setGlobalActionHandler(ActionFactory.COPY.getId(), new CopyAction());

			initFromPreferences();

			// observer ServerModel to init from preferences on login and save t preferences on logout
			ServerModel.getInstance().addListener(serverModelListener);
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
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


	@Override
	public void setFocus() {
		try {
			if (   emailTemplateSearchComposite != null
				&& !emailTemplateSearchComposite.isDisposed()
				&& emailTemplateSearchComposite.isEnabled()
			) {
				emailTemplateSearchComposite.setFocus();
			}
		}
		catch (Exception e) {
			com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
		}
	}


	/**
	 * Create the actions
	 */
	private void createActions() {
		IWorkbenchWindow window = getSite().getWorkbenchWindow();
		createAction = new CreateEmailTemplateAction(window, this);

		editAction = new EditEmailTemplateAction(window);
		duplicateEmailTemplateAction = new DuplicateEmailTemplateAction(window);
		deleteAction = new DeleteEmailTemplateAction(window);
		refreshAction = new RefreshEmailTemplateTableAction();
	}


	private void initializeContextMenu() {
		final MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			@Override
			public void menuAboutToShow(IMenuManager manager) {
				EmailTemplateSearchView.this.fillContextMenu(manager);
			}
		});

		final TableViewer tableViewer = emailTemplateSearchComposite.getTableViewer();
		final Table table = tableViewer.getTable();
		final Menu menu = menuMgr.createContextMenu(table);
		table.setMenu(menu);
		getSite().registerContextMenu(menuMgr, tableViewer);
	}


	private void initializeDoubleClickAction() {
		final TableViewer tableViewer = emailTemplateSearchComposite.getTableViewer();
		tableViewer.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				editAction.run();
			}
		});
	}


	private void fillContextMenu(IMenuManager manager) {
		manager.add(createAction);
		manager.add(duplicateEmailTemplateAction);
		manager.add(editAction);
		manager.add(deleteAction);
		manager.add(refreshAction);
		manager.add(new Separator());
		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}


	/**
	 * Initialize the toolbar
	 */
	private void initializeToolBar() {
		IToolBarManager toolbarManager = getViewSite().getActionBars().getToolBarManager();

		toolbarManager.add(createAction);
		toolbarManager.add(duplicateEmailTemplateAction);
		toolbarManager.add(editAction);
		toolbarManager.add(deleteAction);
		toolbarManager.add(refreshAction);
		toolbarManager.add(new Separator());
	}


	/**
	 * Initialize the menu
	 */
	private void initializeMenu() {
		IMenuManager menuManager = getViewSite().getActionBars().getMenuManager();

		menuManager.add(createAction);
		menuManager.add(duplicateEmailTemplateAction);
		menuManager.add(editAction);
		menuManager.add(deleteAction);
		menuManager.add(refreshAction);
		menuManager.add(new Separator());
	}


	public Long getEventPK() {
		return emailTemplateSearchComposite.getEventPK();
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
		Long eventPK = emailTemplateSearchComposite.getEventPK();
		preference.setEventId(eventPK);
		preference.setEventFilter( emailTemplateSearchComposite.getEventFilter() );

		SimplePersonSearchData personData = SampleRecipientModel.INSTANCE.getSimplePersonSearchData(eventPK);
		if (personData != null) {
    		preference.setSamplePersonId( personData.getId() );
    		preference.setSamplePersonName( personData.getName() );
		}
		else {
    		preference.setSamplePersonId(null);
    		preference.setSamplePersonName(null);
		}

		preference.save();
	}


	private void initFromPreferences() {
		try {
    		// eventPK
    		Long eventPK = preference.getEventId();
    		// set eventPK after sample person has been restored, because it will trigger loading the sample person

    		// eventFilter

    		// sample person
    		Long samplePersonId = preference.getSamplePersonId();
    		String samplePersonName = preference.getSamplePersonName();

			SimplePersonSearchData personData = new SimplePersonSearchData();
			personData.setId(samplePersonId);
			personData.setName(samplePersonName);
			personData.setEventID(eventPK);

			SampleRecipientModel.INSTANCE.put(eventPK, personData);

			// set eventPK after sample person has been restored, because it will trigger loading the sample person
			emailTemplateSearchComposite.setEventPK(eventPK);
			emailTemplateSearchComposite.setEventFilter( preference.getEventFilter() );
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}

	// *
	// * Preferences
	// *****************************************************************************************************************

}
